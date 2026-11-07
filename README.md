# TreeCalc

Android calculator that shows **how** an expression is evaluated: infix typing, postfix (reverse Polish) notation, and an expression tree. The tree is for demonstration.

Package: `com.lkovari.mobile.apps.treecalc`

## How it works

### What you see

1. **Splash** — short branded screen, then the calculator.
2. **Calculator** — display, expression line, keypad, base chips (2 / 8 / 10 / 16), and Deg/Rad next to the base.
3. **Expression tree** — swipe left or right from the calculator. Same swipe from the tree returns to the calculator. After `=`, this screen shows **tappable postfix tokens**, a Play/Pause walk through those tokens, and a collapsible tree. Tap a token to jump to that evaluation step in the tree (see [Tree screen — navigate by postfix](#tree-screen--navigate-by-postfix)).
4. **Help** and **About** — overflow menu. Each screen is a list of cards, and one card is open at a time. Help covers bases, repeating decimals such as `0.(3)`, and the postfix walk. About keeps the GitHub link, the privacy-policy link, and the author line.

Theme: Light, Dark, or Auto (light 06:00–18:00). Language follows the device locale (English default, Hungarian in `values-hu`).

### Modules

| Module | Role |
| --- | --- |
| `:engine` | Keys, infix state, shunting-yard, expression tree, evaluation, bases, memory, angle mode |
| `:app` | Jetpack Compose UI, theme, navigation |

The UI (`CalculatorViewModel`) only forwards key presses and base changes. All math lives in `CalculatorEngine`.

### Evaluation pipeline

Pressing `=` runs three steps:

```
typed keys
    → infix tokens   (numbers, operators, parentheses)
        → postfix tokens   (no parentheses; operators after their operands)
            → expression tree   (value / unary / binary nodes)
                → exact fraction, or a Double when the operation is not rational
                    (formatted for the active base)
```

1. Flush the number being typed into the infix list.
2. Convert infix to postfix with Dijkstra’s **shunting-yard** (`InfixToPostfix`).
3. Walk postfix left to right and build a tree (`ExpressionTreeBuilder`). The tree evaluates from the leaves up (`Operations`).

Postfix and the tree are the same order. Example: `9 + 7 × 6` becomes postfix `9, 7, 6, ×, +` and result `51`.

### Tree screen — navigate by postfix

The tree screen (`ExpressionTreeScreen`) does not show postfix as one truncated line. After `=`, each postfix token is a **chip** in a horizontally scrolling row (`postfixTokens` on `EvaluationResult`).

**Tap a chip** to select that evaluation step. Tap the same chip again to clear the selection. A new `=` (new postfix) resets the selection.

Chip index *i* is the *i*-th node in a **post-order** (postfix) walk of the tree: children left-to-right, then the node. `ExpressionNode.pathForPostOrderIndex(i)` returns the UI path (`root`, `root/0`, `root/1/0`, …). That path is the selected node.

For `9 + 7 × 6` (tree: `+` with left `9` and right `7 × 6`):

| Chip | Token | Path | Node |
| --- | --- | --- | --- |
| 0 | `9` | `root/0` | left leaf |
| 1 | `7` | `root/1/0` | left child of `×` |
| 2 | `6` | `root/1/1` | right child of `×` |
| 3 | `×` | `root/1` | multiplication |
| 4 | `+` | `root` | root addition |

**Play** walks the chips from the first token. Each step waits 700 ms, highlights that chip, and highlights the matching branch. **Pause** freezes the current step. **Play** again continues with the next token. After the last token, **Play** starts over at the first. Tapping a chip stops the walk and selects that step; tapping the same chip again clears the selection. A new postfix resets the walk. If the system animator duration scale is 0, Play does not run a timer: each tap moves one step.

While a step is selected:

- The chip uses `badgeFill` and `operandRing`.
- The matching tree node is brought into view (the tree pane scrolls vertically and horizontally).
- Collapsed ancestors on the path **expand** so the node is visible.
- The selected node and its **descendants** get a highlight wash (`badgeFill` at reduced alpha). Selecting `×` highlights `×`, `7`, and `6`; selecting `+` highlights the whole tree.

You can still expand or collapse any operator with the `+` / `−` box on the row. Highlight is independent of that, except a selection forces ancestors open.

Empty tree (nothing evaluated yet) shows `—` for postfix and the empty-tree hint.

### Stack order (postfix evaluation)

Read postfix **left to right** on a LIFO stack:

- **Number** → push.
- **Unary** (`√`, `sin`, `!`, …) → pop the operand, push the result.
- **Binary** (`+`, `×`, `^`, …) → pop **right first**, then **left**, then push `left op right`.

After the last token the stack must hold exactly one value. For `9 7 6 × +`:

| Token | Stack | Operation |
| --- | --- | --- |
| `9` | `9` | push |
| `7` | `9` `7` | push |
| `6` | `9` `7` `6` | push |
| `×` | `9` `42` | pop 6 (right), pop 7 (left), `7 × 6` |
| `+` | `51` | pop 42 (right), pop 9 (left), `9 + 42` |

`^` and prefix unary minus (`neg`) are **right-associative** (`2^3^2` = `2^(3^2)` = 512; `-2^2` = `-(2^2)` = −4). Almost everything else is left-associative. Full precedence, errors, and worked traces: [`docs/CALC-ENGINE-en.md`](docs/CALC-ENGINE-en.md) (Hungarian: [`docs/CALC-ENGINE-hu.md`](docs/CALC-ENGINE-hu.md)).

### Bases, memory, angle mode, and after `=`

- **Every base** accepts a point. Switching base rewrites the number being typed, the result, and the numbers already in the expression.
- **Decimal** — `π`, trig, logs, factorial. `1 ÷ 3` displays `0.(3)`, `1 ÷ 6` displays `0.1(6)`, `1 ÷ 7` displays `0.(142857)`. A repeating block longer than 12 digits is rounded without parentheses.
- **Binary / octal / hex** — digits below the radix. Decimal `10` becomes hex `A` and binary `1010`. Decimal `0.5` becomes hex `0.8` and binary `0.1`. Decimal `1/3` becomes binary `0.(01)`, octal `0.(25)`, and hex `0.(5)`. Scientific keys stay disabled outside decimal.
- **Deg / Rad** — `sin` / `cos` / `tan` use **degrees** by default (`sin(90)` = 1). The Deg/Rad key (`CalculatorKey.ANGLE_MODE`) toggles `AngleMode`. `C` does not reset it. Changing the unit does not recompute a result already on screen; type the expression again and press `=`.
- **`MC` `MR` `M+` `M−`** — one register, independent of `C`. An exact fraction such as `1/3` is stored as that fraction, so a later recall stays exact. The `M` indicator is `memorySet` (stays on until `MC`, even if the value is 0). `M+` / `M−` add or subtract the current number or last result. `C` does **not** clear memory.
- After **`=`**, a digit starts a new expression; an operator continues from the result. `Tst` types a nested sample without evaluating.

## Color tokens — where to change page and control colors

Almost all UI color is Compose, not XML. Change tokens here:

| File | What it is |
| --- | --- |
| [`app/src/main/java/com/lkovari/mobile/apps/treecalc/ui/theme/Color.kt`](app/src/main/java/com/lkovari/mobile/apps/treecalc/ui/theme/Color.kt) | **Named paints** and **`TreeCalcPalette`** (`LightPalette` / `DarkPalette`) |
| [`app/src/main/java/com/lkovari/mobile/apps/treecalc/ui/theme/Theme.kt`](app/src/main/java/com/lkovari/mobile/apps/treecalc/ui/theme/Theme.kt) | Material 3 `colorScheme` + `LocalTreeCalcPalette` |
| [`app/src/main/java/com/lkovari/mobile/apps/treecalc/ui/theme/Type.kt`](app/src/main/java/com/lkovari/mobile/apps/treecalc/ui/theme/Type.kt) | Typography (not colors) |
| [`app/src/main/res/values/colors.xml`](app/src/main/res/values/colors.xml) | Window / status-bar colors before Compose draws |
| [`app/src/main/res/values/themes.xml`](app/src/main/res/values/themes.xml) | Light window theme |
| [`app/src/main/res/values-night/themes.xml`](app/src/main/res/values-night/themes.xml) | Night window theme |

Screens read `LocalTreeCalcPalette.current`. `TreeCalcTheme(darkTheme)` picks `LightPalette` or `DarkPalette` and the matching Material scheme. Page backgrounds use `pastelScreenBrush(palette)` (`screenWashTop` → `screenWashBottom`).

You do **not** recolor each screen by hunting composables. Edit `LightPalette` and `DarkPalette` in `Color.kt`. Named constants at the top of that file (`PetalPaper`, `NumberSage`, …) are the shared paints those palettes reference.

### Palette fields → what they paint

**Pages and chrome**

| Token | Used on |
| --- | --- |
| `screenWashTop` / `screenWashBottom` | Calculator, tree, Help, About — full-screen wash and app bars |
| `titleAccent` | “TreeCalc” brand lettering when emphasized |
| `link` | About-screen links |
| `displaySurface` / `displayBorder` | Calculator display card; tree postfix / tree cards |
| `chipIdle` / `chipIdleBorder` | Unselected postfix chips |

**Keypad** (`CalculatorKeypad.kt` maps `KeyVisual` → these)

| Token | Keys |
| --- | --- |
| `numberKey` (+ `keyLabel`, `numberKeyBorder`) | Digits, `.` |
| `functionKey` | `sin`, `√`, `π`, `!`, `xʸ`, Deg/Rad, parentheses, `MC` `MR` `M+` `M−` |
| `operatorKey` | `+ − × ÷` |
| `logicKey` | base chips `2` `8` `10` `16`, `and or xor not lsh mod` |
| `equalsKey` (+ `equalsLabel`, `equalsKeyBorder`) | `=` |
| `actionKey` | `C`, backspace, `+/−`, `Tst` |
| `disabledKey` / `disabledLabel` / `disabledKeyBorder` | Unused by the glossy keys; a disabled key is drawn at 38% opacity |

Keys are glossy: a vertical gradient (lighter at the top) and a 1.5 dp border. The selected base uses a 2.5 dp white border. In the light theme a gray drop shadow sits down and to the right. In the dark theme that shadow is omitted, because a gray offset reads as a light smear on the dark background; the fill is darker and a white highlight sits on the top half. Pressing a key deepens the gradient. The named gloss paints are `GlossNumber`, `GlossFunction`, `GlossLogic`, `GlossOperator`, and `GlossAction`. Function rows and number rows use the same column width, so a long expression cannot make one row wider than the other. The expression itself is one horizontally scrolling line.

**Expression tree nodes**

| Token | Used on |
| --- | --- |
| `rootBadgeFill` | Root operator circle |
| `badgeFill` / `badgeGlyph` | Other operator circles and icons |
| `operandFill` / `operandRing` / `operandLabel` | Number leaves |

**Splash** (`SplashScreen.kt`)

| Token | Used on |
| --- | --- |
| `splashTop` / `splashMid` / `splashBottom` | Splash gradient |
| `splashOn` | Title and node labels |
| `splashTagline` | Subtitle |
| `splashBadgeFill` | Title pill |
| `splashStem` | Connecting stems |
| `splashNodeRoot` / `splashNodeLeft` / `splashNodeRight` / `splashNodeOp` | Demo tree nodes |

**Material 3** (text on pages, errors, idle chip label)

`Theme.kt` `LightColors` / `DarkColors`: `background`, `onBackground`, `onSurface`, `onSurfaceVariant`, `primary` (memory `M` indicator), `error`. Body text on Help/About uses `onBackground`; errors on the display use `error`.

**XML (cold start only)**

`colors.xml`: `splash_background` / `splash_background_night` match `PetalPaper` / `DuskPlum` so the window is not a different color before Compose. Keep them in sync if you change those named paints.

### Typical edits

- **Whole app wash** — `screenWashTop` / `screenWashBottom` on both palettes.
- **Keypad gloss** — `GlossNumber`, `GlossFunction`, `GlossLogic`, `GlossOperator`, `GlossAction`, then the matching `*Key` fields on both palettes. `GlossKey` applies the gradient.
- **Tree nodes** — `badgeFill`, `rootBadgeFill`, `operandFill`, `operandRing`.
- **Splash** — `splash*` fields only; they do not affect the calculator pages.

Light/Dark/Auto is stored in DataStore (`ThemePreferences`). Auto is dark before 06:00 and from 18:00 (`ThemeResolver`).

## Website and privacy

Local copy: `docs/website/`

GitHub Pages (after KLHome deploy):

- https://lkovari.github.io/KLHome/assets/bigfiles/treecalc-index.html
- https://lkovari.github.io/KLHome/assets/bigfiles/treecalc-privacy-policy.html

Play listing copy and the launcher artwork: `docs/play-listing/`. Store screenshots are not kept in this repository; capture those from a device when you prepare the listing.

The launcher icon is a pale blue field with branches running downward. The root is `+`, the left branch is `×` over `2` and `−` (`8` and `4`), and the right branch is `÷` over `6` and `3`. The same artwork is the in-app mark.

## Tests

**178** `@Test` methods in **19** classes across **17** files (plus `EngineTestSupport.kt`, helpers only). JUnit 4. `ThemeModeTest` and `ThemeResolverTest` share `ThemeResolverTest.kt`. Counts below are current as of this writing.

### Categories and files

| Category | Tests | Files |
| --- | ---: | --- |
| **Engine — evaluation** | 80 | `CalculatorEngineTest.kt` (39), `CalculatorEngineBehaviorTest.kt` (29), `CalculatorEngineMathFixesTest.kt` (12) |
| **Engine — exact fractions and bases** | 10 | `ExactFractionTest.kt` |
| **Engine — postfix playback** | 4 | `PostfixPlaybackTest.kt` |
| **Engine — infix → postfix** | 10 | `InfixToPostfixTest.kt` |
| **Engine — expression tree** | 10 | `ExpressionTreeBuilderTest.kt` |
| **Engine — post-order paths** | 3 | `ExpressionNodePostOrderTest.kt` |
| **Engine — operations** | 10 | `OperationsTest.kt` |
| **Engine — number format / parse** | 10 | `NumberFormatterTest.kt` |
| **Engine — numeric bases** | 6 | `NumericBaseTest.kt` |
| **App — ViewModel** | 5 | `app/src/test/java/com/lkovari/mobile/apps/treecalc/viewmodel/CalculatorViewModelTest.kt` |
| **App — theme** | 5 | `settings/ThemeResolverTest.kt` (`ThemeModeTest` + `ThemeResolverTest`) |
| **App — tree UI helpers** | 12 | `ui/theme/TreeNodeCirclePaletteTest.kt` (6), `ui/components/TreeGuidesTest.kt` (6) |
| **Instrumented — Compose screens** | 12 | `app/src/androidTest/java/com/lkovari/mobile/apps/treecalc/ui/screens/CalculatorScreensTest.kt` |
| **Instrumented — app identity** | 1 | `AppIdentityTest.kt` |

| Totals | Tests |
| --- | ---: |
| Engine JVM (`:engine`) | 143 |
| App JVM (`:app` `src/test`) | 22 |
| App instrumented (`src/androidTest`, device/emulator) | 13 |
| **All** | **178** |

Engine evaluation covers mixed precedence, parentheses, bases, memory, unary/binary keys, and error kinds. Math-fixes cover unary minus vs power, trig in degrees, decimal formatting, and 0÷0. Instrumented tests compose the calculator / tree / help screens and check the application id.

### How to run

From the repo root. `./gradlew test` runs **JVM unit tests only** (`:engine` + `:app`). It does **not** run instrumented tests.

```bash
./gradlew test
```

**By Gradle module**

```bash
./gradlew :engine:test
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

`connectedDebugAndroidTest` needs a running emulator or device.

**By category** (JUnit `--tests` filter; quote the pattern)

```bash
./gradlew :engine:test --tests "com.lkovari.mobile.apps.treecalc.engine.CalculatorEngine*"
./gradlew :engine:test --tests "*.InfixToPostfixTest"
./gradlew :engine:test --tests "*.ExpressionTreeBuilderTest"
./gradlew :engine:test --tests "*.ExpressionNodePostOrderTest"
./gradlew :engine:test --tests "*.OperationsTest"
./gradlew :engine:test --tests "*.NumberFormatterTest"
./gradlew :engine:test --tests "*.NumericBaseTest"
./gradlew :engine:test --tests "*.ExactFractionTest"
./gradlew :engine:test --tests "*.PostfixPlaybackTest"
./gradlew :app:testDebugUnitTest --tests "*.CalculatorViewModelTest"
./gradlew :app:testDebugUnitTest --tests "*.ThemeResolverTest"
./gradlew :app:testDebugUnitTest --tests "*.ThemeModeTest"
./gradlew :app:testDebugUnitTest --tests "com.lkovari.mobile.apps.treecalc.ui.*"
./gradlew :app:connectedDebugAndroidTest --tests "*.CalculatorScreensTest"
./gradlew :app:connectedDebugAndroidTest --tests "*.AppIdentityTest"
```

Several `--tests` flags can be combined on one command. A single method: `--tests "*.InfixToPostfixTest.powerIsRightAssociative"`.

## Signing

Copy `keystore.properties.example` to `keystore.properties` and point it at the EKL release keystore (same as sensors-s). `keystore.properties` is gitignored.
