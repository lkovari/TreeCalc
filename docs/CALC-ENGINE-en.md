# TreeCalc calculator engine

This document describes how the current `:engine` module works: key handling, infix entry, conversion to postfix (reverse Polish notation), stack evaluation through an expression tree, numeric bases, memory (`MC`, `MR`, `M+`, `M−`), angle mode, and errors.

Source of truth: `engine/src/main/kotlin/com/lkovari/mobile/apps/treecalc/engine/`. The Android UI (`CalculatorViewModel`) only forwards key presses and base changes, then publishes `EvaluationResult`.

## Evaluation pipeline

Pressing `=` runs three steps in order:

1. Flush the number being typed into the infix token list.
2. Convert that infix list to postfix with Dijkstra’s shunting-yard algorithm (`InfixToPostfix`).
3. Walk the postfix tokens left to right and build an expression tree (`ExpressionTreeBuilder`). The tree is then evaluated from the leaves up (`ExpressionNode.evaluate(angleMode)` → `Operations.executeUnary` / `executeBinary`).

The postfix string and the tree are the same evaluation order. The tree screen shows postfix as **tappable chips** (one per token, for example `9` `7` `6` `×` `+`) and the tree as expandable nodes. See [Tree screen — navigate by postfix](#tree-screen--navigate-by-postfix).

```
typed keys
    → infix tokens  (numbers, operators, parentheses)
        → postfix tokens  (no parentheses; operators after their operands)
            → expression tree  (binary / unary / value nodes)
                → Double result  (formatted for the active numeric base)
```

## Engine state

`CalculatorEngine` keeps:

| Field | Role |
| --- | --- |
| `base` | Active numeric base: binary, octal, decimal, or hexadecimal. Default decimal. |
| `infix` | Committed infix tokens (numbers already finished, operators, parentheses). |
| `currentInput` | Digits of the number still being typed. Not yet an infix token. |
| `afterEquals` | `true` after a successful `=`. The next digit starts a new expression; the next operator continues from the result. |
| `lastResult` | Last successfully computed `Double`, or `null`. |
| `lastTree` | Expression tree of the last successful `=`, or `null`. |
| `lastPostfix` | Postfix tokens of the last successful `=`. |
| `lastExpressionText` | Infix text frozen at the last successful `=`. |
| `liveDisplay` | Main display string. |
| `errorKind` | Current error, or `null`. |
| `memory` | Independent memory register (`Double`, starts at `0.0`). |
| `memorySet` | `true` after `M+` or `M−` until `MC`. Drives the `M` indicator. `C` does not clear memory. |
| `angleMode` | `AngleMode.DEGREES` (default) or `RADIANS`. Used by `sin` / `cos` / `tan`. `C` does not reset it. |

`snapshot()` is the public view:

- `display` — current number or last result
- `expression` — live infix while typing; frozen infix after `=`
- `postfix` — last successful postfix, comma-separated, or empty
- `postfixTokens` — same tokens as a list (empty until a successful `=`); drives the tree-screen chips
- `tree` — last successful tree, or `null`
- `base`, `errorKind`, `memorySet`, `afterEquals`, `angleMode`

A failed `=` clears `tree` and `postfix` on the snapshot, but does not clear the typed infix or `lastResult`.

## Tokens

```
Token
  NumberLiteral(raw)   e.g. "9", "1.25", "π", "A"
  OperatorToken(kind)  any OperatorKind
  LeftParen
  RightParen
```

There is no implicit multiplication token. `2(3+4)` is not rewritten as `2 × (3+4)`.

Unary keypad functions are stored **after** their operand in the infix list (`9`, `√`), not as prefix functions. Prefix unary minus (a `-` at the start of the expression, after `(`, after a binary operator, or after another `neg`) is an `OperatorKind.NEG` token (precedence 99, right-associative). The `+/−` key is still not a `NEG` token while you type: it edits `currentInput` or `lastResult` (see [Sign change](#sign-change)). Flushing a negative literal (`-5`) emits `NEG` plus `NumberLiteral("5")`.

## Keys and when they are ignored

`press(key)` is a no-op when `isKeyEnabled(key)` is false, except `C` (`CLEAR`), which always works.

Enabled keys depend on the base (`NumericBase.allowsKey`):

- Digit keys: only digits strictly less than the radix (`0–1` binary, `0–7` octal, `0–9` decimal, `0–9` and `A–F` hexadecimal).
- Decimal only: `π`, `sin`, `cos`, `tan`, `ln`, `log`, `exp`, `!`. The point (`.`) is allowed in every base.
- Everything else (arithmetic, bitwise, `√`, `1/`, `²`, `³`, parentheses, memory, `=`, backspace, `Tst`, Deg/Rad) stays enabled in every base. Deg/Rad (`CalculatorKey.ANGLE_MODE`) is available in every base even though trig keys are decimal-only.

Hex letter keys in decimal are ignored; the display stays `"0"` if nothing else was typed.

## Building the infix expression

### Current input versus committed tokens

A number lives in `currentInput` until something **flushes** it into `infix` as a `NumberLiteral`:

- binary operator
- unary operator
- parenthesis
- `π`
- `=`

Flush skips incomplete fragments: empty, `"."`, `"-."`. Those are discarded, not turned into tokens. A lone `"-"` is flushed as `OperatorKind.NEG`, not discarded.

Live expression text is the committed tokens joined with spaces, plus `currentInput` if it is non-empty. Example while typing `9+7*6`:

```
display:     6
expression:  9 + 7 × 6
postfix:     (empty until =)
```

After `=`:

```
display:     51
expression:  9 + 7 × 6
postfix:     9, 7, 6, ×, +
afterEquals: true
```

### Digits

- Invalid digit for the base → `INVALID_DIGIT`; the digit is not appended.
- A lone `"0"` is replaced by the next non-dot digit (`0` then `5` becomes `5`).
- `"-0"` plus a non-dot digit becomes `"-"` plus that digit (`-05` is not kept).
- After `=`, a digit starts a **new** expression (`beginNewInputIfNeeded`).

### Decimal point

- Second `.` in the same number is ignored (`1.2.5` stays `1.25`).
- Leading `.` becomes `0.` (`.5` evaluates to `0.5`).
- Allowed in every base.

### Binary operators (`+ − × ÷ mod ^ and or xor lsh`)

1. If `afterEquals`, the last result is inserted as the first operand of a new infix list.
2. Flush `currentInput`.
3. If the operator is `−` and infix is empty, ends with `(`, a binary operator, or `neg`, append `OperatorKind.NEG` instead of binary minus (prefix unary minus). Leading `-5` is `neg` then `5`, not `0 − 5`. Leading `+5` still inserts `0` then `+`.
4. If infix is still empty, insert `0` (leading `+5` is `0 + 5`).
5. If the last token is already a binary operator, **replace** it (`8+` then `×` becomes `8 ×`).
6. Append the new operator.

### Unary operators (`not √ 1/ sin cos tan ln log exp ² ³ !`)

1. Same “continue from result” behavior as binary operators after `=`.
2. Flush `currentInput`.
3. If infix is empty → `MALFORMED_EXPRESSION` (`√` with nothing to apply to).
4. Otherwise append the unary operator **after** the operand: `5` then `²` then `1/` is tokens `5`, `²`, `1/`.

Chained unaries are postfix on the same value. `5 ² 1/` evaluates as `(5²)⁻¹ = 0.04`.

### Parentheses

Flush, then append `(` or `)`. Balance is **not** checked while typing. Unbalanced parentheses are reported only at `=`.

### π

Flush, then append a `NumberLiteral("π")`. The display shows π formatted in the current base. Decimal only.

### Sign change

`+/−` is not shunting-yard unary minus.

| Situation | Effect |
| --- | --- |
| Typing a number | Toggle a leading `-` on `currentInput`. |
| After `=` | Negate `lastResult` in place. `afterEquals` stays `true`. A following operator continues from the negated value. |
| Empty input (not after `=`) | Start `currentInput` with `-`. |

`5 +/− + 3 =` is `-2`. While typing, that leading `-` is still on `currentInput`. At flush it becomes `NEG` plus `NumberLiteral("5")`, so the tree shows a `neg` node. Typing `-` as a prefix operator (start, after `(`, or after `+ − × …`) inserts `NEG` immediately. `-2^2` is postfix `2, 2, ^, −` and evaluates to `-(2^2) = −4`.

### Backspace

- After `=` — ignored (result stays).
- If `currentInput` is non-empty — delete the last character.
- Else pop the last infix token. If it was a number longer than one character, the remaining characters return to `currentInput`.

### Clear (`C`)

Resets infix, current input, last result, tree, postfix, expression text, display (`0` in the current base), error, and `afterEquals`. **Does not** reset `memory`, `memorySet`, or `angleMode`.

### After `=`

| Next key | Behavior |
| --- | --- |
| Digit, `.`, `π`, `(` `)` | Start a new expression. Previous result is not reused. |
| Binary or unary operator | Continue: result becomes the first operand. |
| `=` again | Redisplay `lastResult`. Not an error. |
| Backspace | No effect. |
| `+/−` | Negate the result; still “after equals”. |
| Deg/Rad | Toggle `angleMode` only. Does not start a new expression and does not recompute the value on screen. Type the expression again and press `=` to apply the new unit. |
| Memory keys | See [Memory](#memory-mc-mr-m-m). |

`=` with an empty infix and no `lastResult` → `EMPTY_EXPRESSION`.

### Test key (`Tst`)

Clears the engine, forces decimal, and types this sample **without** evaluating:

```
888/(6*1.25+3*(7-4*1.5+(9/3-1.2))*2)-11*(4.5/2^3)/(5-1.25)
```

Press `=` afterwards. The exact decimal result is the repeating value `34.89(320987654)`. Changing base after `Tst`, before or after `=`, rewrites every number in the expression into the new base.

## Infix to postfix (shunting-yard)

`InfixToPostfix.convert(infix)` uses two deques:

- **Output** — postfix result (append only).
- **Operator stack** — pending operators and left parentheses. The top is the last element.

Parentheses never appear in the output. Numbers go straight to the output. Operators wait on the stack until their operands are complete.

### Algorithm

For each infix token, left to right:

1. **Number** → append to output.
2. **Operator** → while the stack top is an operator that should be popped (see below), pop it to output; then push the incoming operator.
3. **Left parenthesis** → push onto the operator stack.
4. **Right parenthesis** → pop operators to output until a left parenthesis is found. Discard the matching `(`. If none is found → `UNBALANCED_PARENTHESES` (“missing left parenthesis”).

After the last token, pop remaining operators to output. A leftover `(` → `UNBALANCED_PARENTHESES` (“missing right parenthesis”).

### When to pop the stack (`shouldPop`)

Let `incoming` be the new operator and `stackTop` the operator on top of the stack.

| Associativity of incoming | Pop while |
| --- | --- |
| Left (almost all operators) | `stackTop.precedence >= incoming.precedence` |
| Right (`^` and `neg`) | `stackTop.precedence > incoming.precedence` |

A left parenthesis on the stack is never popped by `shouldPop`; it only comes off for `)` or as a final imbalance error.

**Left-associative** means `a − b − c` becomes `a b - c -` and evaluates as `(a − b) − c`.

**Right-associative** means `2 ^ 3 ^ 2` becomes `2, 3, 2, ^, ^` and evaluates as `2^(3^2) = 512`, not `(2^3)^2 = 64`. Equal precedence powers do **not** pop the earlier `^`.

### Precedence table

Higher number binds tighter. These are the values in `OperatorKind`.

| Precedence | Operators | Arity | Associativity |
| ---: | --- | --- | --- |
| 99 | `^` | binary | right |
| 99 | `neg` | unary | right |
| 99 | `√` `1/` `²` `³` `!` | unary | left |
| 14 | `not` | unary | left |
| 12 | `×` `÷` `mod` `lsh` | binary | left |
| 12 | `sin` `cos` `tan` `ln` `log` `exp` | unary | left |
| 11 | `+` `−` | binary | left |
| 7 | `and` | binary | left |
| 6 | `xor` | binary | left |
| 5 | `or` | binary | left |

So `1 or 2 and 3` is `1, 2, 3, and, or` and evaluates as `1 or (2 and 3)`.

`√` `1/` `²` `³` `!` share precedence 99 with `^`. Because they are left-associative and sit after the operand, `9 √` is simply `9, √`. Chained `5 ² 1/` pops the earlier unary before pushing the next: postfix `5, ², 1/`.

`sin` `cos` `tan` `ln` `log` `exp` share precedence 12 with `×`, so they wrap a preceding multiply or divide: `π ÷ 2 sin` is postfix `π, 2, ÷, sin` = `sin(π/2)`. Use parentheses for `2 × (3 sin)`.

`neg` is right-associative at 99, same as `^`, so `-2^2` is `-(2^2) = −4`, not `(-2)^2 = 4`.

### Worked conversion: `9 + 7 × 6`

Infix tokens: `9`, `+`, `7`, `×`, `6`.

| Token | Output | Operator stack | Note |
| --- | --- | --- | --- |
| `9` | `9` | | number → output |
| `+` | `9` | `+` | stack empty |
| `7` | `9` `7` | `+` | |
| `×` | `9` `7` | `+` `×` | 11 ≥ 12 is false, so `+` stays |
| `6` | `9` `7` `6` | `+` `×` | |
| end | `9` `7` `6` `×` `+` | | drain stack, top first |

Postfix display: `9, 7, 6, ×, +`.

### Worked conversion: `(1 + 2) × 3`

| Token | Output | Operator stack |
| --- | --- | --- |
| `(` | | `(` |
| `1` | `1` | `(` |
| `+` | `1` | `(` `+` |
| `2` | `1` `2` | `(` `+` |
| `)` | `1` `2` `+` | | pop until `(` |
| `×` | `1` `2` `+` | `×` |
| `3` | `1` `2` `+` `3` | `×` |
| end | `1` `2` `+` `3` `×` | |

Parentheses force addition before multiplication.

### Worked conversion: `2 ^ 3 ^ 2`

| Token | Output | Operator stack | Note |
| --- | --- | --- | --- |
| `2` | `2` | | |
| `^` | `2` | `^` | |
| `3` | `2` `3` | `^` | |
| `^` | `2` `3` | `^` `^` | right-assoc: 99 > 99 is false, do not pop |
| `2` | `2` `3` `2` | `^` `^` | |
| end | `2` `3` `2` `^` `^` | | inner `^` (3²) is on top, so it comes out first |

### Nested sample used in tests

`777/(5*1.33+2*(5-3*1.3)*3)` converts to:

```
777, 5, 1.33, ×, 2, 5, 3, 1.3, ×, -, ×, 3, ×, +, ÷
```

## Stack order to evaluate postfix

Evaluation does **not** re-parse infix. It reads postfix **left to right** with a value stack (LIFO). `ExpressionTreeBuilder` builds nodes instead of raw numbers; the pop order is the same.

### Rules

Start with an empty stack. For each postfix token:

| Token | Stack action |
| --- | --- |
| Number | Parse in the **current base**, push a value node. |
| Unary operator | Stack must have ≥ 1 item. **Pop the operand** (stack top). Push `UnaryNode(op, operand)`. |
| Binary operator | Stack must have ≥ 2 items. **Pop right first, then left.** Push `BinaryNode(op, left, right)`. |
| `(` or `)` | `MALFORMED_EXPRESSION` (should never appear after a successful conversion). |

After the last token the stack must contain **exactly one** node: the root. Otherwise `MALFORMED_EXPRESSION` (empty expression, leftover values, or missing operands).

The pop order for binary operators is mandatory because the stack is LIFO:

```
right = stack.pop()   // most recently pushed operand
left  = stack.pop()   // earlier operand
result = left  op  right
stack.push(result)
```

For `9 7 6 × +` that means `×` sees `left=7`, `right=6`, then `+` sees `left=9`, `right=42`. Reversing left/right would compute `6×7` still, but `42+9` vs `9+42` would break subtraction, division, `mod`, `^`, and `lsh`.

Unary:

```
operand = stack.pop()
result = op(operand)
stack.push(result)
```

### Worked evaluation: `9, 7, 6, ×, +`

| Token | Value stack after the token | Operation |
| --- | --- | --- |
| `9` | `9` | push |
| `7` | `9` `7` | push |
| `6` | `9` `7` `6` | push |
| `×` | `9` `42` | pop `6` (right), pop `7` (left), `7×6` |
| `+` | `51` | pop `42` (right), pop `9` (left), `9+42` |

Tree:

```
        + = 51
       / \
      9   × = 42
         / \
        7   6
```

### Worked evaluation: `2, 3, 2, ^, ^`

| Token | Stack | Operation |
| --- | --- | --- |
| `2` | `2` | |
| `3` | `2` `3` | |
| `2` | `2` `3` `2` | |
| `^` | `2` `9` | `3^2` (inner, right-assoc) |
| `^` | `512` | `2^9` |

Tree: `^` at the root, left child `2`, right child `(3 ^ 2)`.

### Worked evaluation: `9, √`

| Token | Stack |
| --- | --- |
| `9` | `9` |
| `√` | `3` |

### Malformed stack cases

| Postfix | Why it fails |
| --- | --- |
| empty | stack size ≠ 1 |
| `1` `2` | two leftover values |
| `√` | unary with empty stack |
| `1` `+` | binary with only one operand |
| leftover `(` | parentheses are illegal in postfix |

## Operations

`UnaryNode` / `BinaryNode` call `Operations.executeUnary` / `executeBinary`. Values are IEEE `Double`. Bitwise ops convert to 32-bit signed integers first.

### Binary

| Kind | Meaning | Extra rules |
| --- | --- | --- |
| `+` `−` `×` | usual arithmetic | |
| `÷` | `left / right` | `right == 0` and `left == 0` → `UNDEFINED`; other `right == 0` → `DIVISION_BY_ZERO` |
| `mod` | `left % right` (IEEE remainder) | `right == 0` → `DIVISION_BY_ZERO` |
| `^` | `Math.pow(left, right)` | NaN or ∞ → `DOMAIN` (e.g. `(-1)^0.5`) |
| `and` `or` `xor` | 32-bit bitwise | both operands must be whole and in `[Int.MIN_VALUE, Int.MAX_VALUE]` |
| `lsh` | `left << right` as 32-bit ints | `right` must be in `0…31`; else `DOMAIN` |

Using a unary kind in `executeBinary` (or the reverse) → `MALFORMED_EXPRESSION`.

### Unary

| Kind | Meaning | Extra rules |
| --- | --- | --- |
| `neg` | `-x` | emitted for prefix minus and when flushing a negative literal |
| `not` | 32-bit bitwise complement (`0` → `-1`) | whole 32-bit integer required |
| `√` | `sqrt` | `x < 0` → `DOMAIN` |
| `1/` | `1 / x` | `x == 0` → `DIVISION_BY_ZERO` |
| `sin` `cos` `tan` | degrees by default; radians when `angleMode` is `RADIANS` (`executeUnary(..., angleMode)`) | `tan` NaN/∞ → `DOMAIN` |
| `ln` | natural log | `x ≤ 0` → `DOMAIN` |
| `log` | log₁₀ | `x ≤ 0` → `DOMAIN` |
| `exp` | `e^x` | overflow → `DOMAIN` |
| `²` `³` | `x*x`, `x*x*x` | |
| `!` | factorial | see below |

Factorial:

- `x` must be a non-negative whole number, else `INVALID_FACTORIAL`.
- `n > 170` → `DOMAIN` (overflow).
- `0! = 1! = 1`.

### Whole-number test

`NumberFormatter.isWhole`: `|x − rint(x)| < 1e-9`. Bitwise conversion then uses `Math.rint` and `toInt()`. Out of 32-bit range → `DOMAIN`. Non-whole → `BITWISE_NON_INTEGER`.

## Memory (`MC`, `MR`, `M+`, `M−`)

Memory is a single `Double` register, independent of infix, display, last result, and `C`.

The `M` indicator is `memorySet`, not “memory is non-zero”. `M+ 8` then `M− 8` leaves `memory == 0` but `memorySet == true` until `MC`.

### Which number `M+` / `M−` use

`currentNumericValue()` picks the first that exists:

1. **Current input**, if it is a complete number (not empty / `-` / `.` / `-.`). Parsed in the current base.
2. Else **`lastResult`**, if it exists and (`afterEquals` **or** infix is empty). This is how `5+5=` then `M+` stores `10`.
3. Else the **last infix token**, if it is a `NumberLiteral`.
4. Else `null` — `M+` / `M−` do nothing (no error, `memorySet` unchanged).

Parse failures return `null` as well.

### `M+` (`MEMORY_ADD`)

```
memory += value
memorySet = true
```

Display, infix, and `afterEquals` are unchanged.

### `M−` (`MEMORY_SUB`)

```
memory -= value
memorySet = true
```

Same non-destructive display behavior.

### `MR` (`MEMORY_RECALL`)

1. If `afterEquals`, start a new expression (result is not kept as an operand).
2. Replace `currentInput` with `memory` formatted in the **current** base.
3. Show that string on the display.
4. Clear `errorKind`.

`MR` does not commit the value into `infix` until the next flush (`=`, operator, parenthesis, …). `MR` of a cleared memory (`0`, `memorySet == false`) still types `0`.

### `MC` (`MEMORY_CLEAR`)

```
memory = 0.0
memorySet = false
```

Does not change the display or expression.

### Examples

**Store, clear entry, recall**

```
8  M+  C  MR  =     → display 8, memory still 8, M indicator on
MC C  MR  =         → display 0, M indicator off
```

**Subtract from memory**

```
10 M+  C  3 M−  C  MR  =     → 7
```

**Add the last result**

```
5 + 5 =  M+  C  MR  =        → 10
```

**Recall after equals starts a new expression**

```
1 + 2 =  MR
```

If memory is `8`, the next `=` evaluates `8`, not `3` combined with `8`.

Memory survives `C` and base changes. An exact value is stored as a fraction, so recalling `1/3` in hexadecimal shows `0.(5)`. A stored decimal `10` recalled in hex is `A`.

## Numeric bases

Addition, subtraction, multiplication, division, negation, reciprocal, square, cube, and integer powers stay on a reduced `BigInteger` fraction. Roots, trigonometry, logarithms, `exp`, factorial, `mod`, and bitwise operations produce a `Double` and drop the fraction. The base affects digit keys, literal parsing, and formatting.

| Base | Radix | Fractions | Notes |
| --- | ---: | --- | --- |
| Binary | 2 | yes | digits `0–1`; `.` allowed |
| Octal | 8 | yes | digits `0–7`; `.` allowed |
| Decimal | 10 | yes | default; scientific keys and `π` |
| Hexadecimal | 16 | yes | digits `0–9A–F`; letters stored uppercase; `.` allowed |

`setBase`:

- No-op if the base is unchanged.
- Rewrites the number being typed, every number already in the expression, and the result. Decimal `10` becomes hex `A` and binary `1010`. Decimal `0.5` becomes hex `0.8` and binary `0.1`. Decimal `1/3` becomes binary `0.(01)`, octal `0.(25)`, and hex `0.(5)`.
- A trailing point is kept (`12.` becomes `C.` in hexadecimal). The `π` token is not rewritten.
- Clears `errorKind`.

Literals parse as exact fractions in the active radix, including a parenthesized repeating block. `1.5` in hexadecimal is `1.8`, not a rounded integer.

## Number formatting and π

Parse:

- `"π"`, `"PI"`, `"pi"` → `Math.PI`.
- A fraction literal, including `WHOLE.FRAC(PERIOD)`, becomes that exact value as a `Double` for the approximate path.
- Decimal scientific text such as `1e20` uses `toDouble()`.
- Other finite literals are parsed digit by digit in the active radix.
- Empty / `-` / `.` / `-.` / garbage → `MALFORMED_EXPRESSION`.

Format:

- NaN or infinity → `DOMAIN`.
- Decimal whole numbers (within `Long` range) as integers without a decimal point.
- Other decimal values that are exact fractions use parentheses for the repeating block (`0.(3)`, `0.1(6)`, `1.(3)`). A repeating block longer than 12 digits falls back to a 12-digit rounding.
- Whole values in any base use that radix. Non-whole approximate values use up to 12 fractional digits. Very large magnitudes use scientific notation.
- `-0` becomes `0`.

## Errors

| `ErrorKind` | Typical cause | UI (English) |
| --- | --- | --- |
| `EMPTY_EXPRESSION` | `=` with nothing typed and no last result | Enter an expression first. |
| `UNBALANCED_PARENTHESES` | missing `(` or `)` at conversion | Unbalanced parentheses. |
| `DIVISION_BY_ZERO` | `÷ 0`, `mod 0`, `1/0` (when the numerator is not 0) | Division by zero. |
| `UNDEFINED` | `0 ÷ 0` | Undefined. |
| `DOMAIN` | `√` of negative, `ln`/`log` of `≤0`, factorial `>170`, bad `^`, bad `lsh`, overflow, NaN/∞ | The value is outside the domain of this operation. |
| `BITWISE_NON_INTEGER` | `and`/`or`/`xor`/`not`/`lsh` on a non-integer | Bitwise operations require whole numbers. |
| `INVALID_DIGIT` | digit not allowed in this base (if it somehow reaches `enterDigit`) | That digit is not valid in this numerical system. |
| `MALFORMED_EXPRESSION` | incomplete expression, unary with no operand, leftover stack values, bad parse | The expression is not complete. |
| `INVALID_FACTORIAL` | negative or non-whole factorial argument | Factorial needs a non-negative whole number. |

Disabled keys are ignored rather than raising `INVALID_DIGIT`. The English phrases in the table above are UI copy from `strings.xml`, not strings stored in the engine. See [Localization](#localization).

## Localization

The `:engine` module has **no translated text**. It emits `ErrorKind` values, operator symbols (`OperatorKind.displaySymbol` / `treeLabel`), and formatted numbers. All user-visible wording lives in the Android app resources. The UI loads them with `stringResource(R.string…)` (Compose).

Android picks the file from the **device language**. There is no in-app language switch.

| Locale | File |
| --- | --- |
| English (default / fallback) | [`app/src/main/res/values/strings.xml`](../app/src/main/res/values/strings.xml) |
| Hungarian (`hu`) | [`app/src/main/res/values-hu/strings.xml`](../app/src/main/res/values-hu/strings.xml) |

To add another language, copy `values/strings.xml` to `values-<bcp47>/strings.xml` (for example `values-de/`) and translate every `<string name="…">`. Keep the `name` attributes identical; only the text nodes change.

### What each file contains

Menus, theme names, screen titles, splash tagline, Help and About bodies (including angle-unit help: `help_angle_title` / `help_angle_body`), base chips (`Bin` / `Oct` / `Dec` / `Hex`), keypad word-keys (`And`, `Or`, `Xor`, `Not`, `Lsh`, `Mod`, `Tst`, Deg/Rad via `key_degrees` / `key_radians`), memory indicator `M`, content descriptions, and **error messages** (including `error_undefined`).

Error mapping is in `CalculatorScreen.kt` (`errorMessage`): `ErrorKind` → `R.string.error_*`. Help copy is in `HelpScreen.kt`; About copy in `AboutScreen.kt`; keypad labels in `CalculatorKeypad.kt`.

### What is not localized

- Operator glyphs (`+`, `×`, `√`, `sin`, `π`, …) come from `OperatorKind` / `CalculatorKey`, not `strings.xml`.
- Digit keys, `MC` / `MR` / `M+` / `M−`, `C`, `+/−`, and `=` use the same characters in every language.
- Postfix and tree labels use those same engine symbols plus formatted numbers.

Play Store listing copy (English and Hungarian) is separate: [`docs/play-listing/STORE_LISTING.md`](play-listing/STORE_LISTING.md).

## Public types (module map)

| Type | Responsibility |
| --- | --- |
| `CalculatorEngine` | Keystroke state machine; owns memory and last result. |
| `CalculatorKey` | Every keypad key, including `MEMORY_*`, `TEST`, and `ANGLE_MODE`. |
| `AngleMode` | `DEGREES` (default) or `RADIANS`. |
| `Token` | Infix/postfix token. |
| `OperatorKind` | Symbol, tree label, precedence, arity, associativity. |
| `InfixToPostfix` | Shunting-yard. |
| `ExpressionTreeBuilder` | Postfix → tree via the evaluation stack. |
| `ExpressionNode` | `ValueNode`, `UnaryNode`, `BinaryNode`; `evaluate(angleMode)` and `displayLabel(base, angleMode)`. |
| `Operations` | Numeric and bitwise implementation (`executeUnary` takes `angleMode`). |
| `NumberFormatter` | Parse/format/`isWhole`/`π`. Approximate values only. |
| `Fraction` | Reduced `BigInteger` fraction for rational operations. |
| `DecimalDisplay` | Terminating and repeating text, including `0.(3)`. |
| `PostfixPlayback` | Play, pause, resume, and chip selection for the tree screen. |
| `NumericBase` | Radix and which keys are legal. The point is legal in every base. |
| `EvaluationResult` | Snapshot for the UI (includes `angleMode`). |
| `CalculatorException` / `ErrorKind` | Typed failures (including `UNDEFINED`). |

The tree screen walks `childCount()` / `childAt()` and `displayLabel(base, angleMode)` (operators show `× = 42` style labels; leaves show the original raw literal).

## Tree screen — navigate by postfix

This is UI behavior in `:app` (`ExpressionTreeScreen`, `ExpressionTreeView`) on top of engine post-order paths.

After `=`, postfix is a row of chips from `EvaluationResult.postfixTokens`, not a single comma-separated `Text`. The row is a `LazyRow` (horizontal scroll). With no tree, the row shows `—`.

**Tap chip *i*** to select evaluation step *i*. Tap it again to deselect. Play above the row walks the chips; Pause holds the current step, and Play continues from the next one. After the last chip, Play starts again at the first. Changing postfix (a new successful `=`) clears the selection (`rememberSaveable(state.postfix)`).

Index *i* is the *i*-th node of a **post-order** walk: visit children left-to-right, then the node. That is the same order as postfix and as `ExpressionTreeBuilder`. Engine API:

- `ExpressionNode.postOrderNodes()` — nodes in that order
- `ExpressionNode.pathForPostOrderIndex(i)` — path string used by the Compose tree (`root`, `root/0`, `root/1/0`, …), or `null` if out of range

Example `9 + 7 × 6` (root `+`, left `9`, right `×` of `7` and `6`):

| *i* | Token | Path | Node |
| ---: | --- | --- | --- |
| 0 | `9` | `root/0` | left leaf |
| 1 | `7` | `root/1/0` | left of `×` |
| 2 | `6` | `root/1/1` | right of `×` |
| 3 | `×` | `root/1` | multiply |
| 4 | `+` | `root` | add |

While a step is selected:

1. The chip uses `badgeFill` / `operandRing`.
2. Ancestors of that path **expand** if they were collapsed.
3. The selected row is **scrolled into view** (`BringIntoViewRequester`) inside the tree pane (vertical and horizontal scroll).
4. The selected node and its **descendants** are highlighted (`path == highlightedPath` or `path` starts with `highlightedPath/`). Selecting `×` highlights `×`, `7`, and `6`.

Operator rows still expand/collapse with the `+` / `−` box. A postfix selection only forces ancestors open; it does not lock the rest of the tree.

Covered by `ExpressionNodePostOrderTest`.

## Display

The infix line is a single horizontally scrolling row of fixed height. A long expression, including the sample from `Tst`, does not wrap and does not change keypad column width. Function keys and number keys share one column width. Postfix chips on the tree screen still scroll in a `LazyRow`.

Play walks those chips one token every 700 ms and highlights the matching tree branch. Pause keeps the current step; Play continues from the next token. After the last token, Play starts at the first. Tapping a chip selects that step and stops the walk. Tapping it again clears the selection. If the system animator duration scale is 0, Play steps one token per tap and does not run a timer.
