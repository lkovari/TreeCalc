# TreeCalc számológép-motor

Ez a dokumentum a jelenlegi `:engine` modul működését írja le: billentyűkezelés, infix bevitel, postfixre (fordított lengyel jelölésre) alakítás, verem alapú kiértékelés kifejezésfán keresztül, számrendszerek, memória (`MC`, `MR`, `M+`, `M−`), szögegység és hibák.

Forrás: `engine/src/main/kotlin/com/lkovari/mobile/apps/treecalc/engine/`. Az Android felület (`CalculatorViewModel`) csak továbbítja a gombnyomásokat és a számrendszer-váltást, majd közzéteszi az `EvaluationResult` állapotot.

## Kiértékelési folyamat

Az `=` három lépést futtat sorban:

1. A gépelés alatt álló számot belerakja az infix tokenlistába.
2. Az infix listát postfixre alakítja Dijkstra shunting-yard (rendezőpályaudvar) algoritmusával (`InfixToPostfix`).
3. A postfix tokeneket balról jobbra bejárva felépíti a kifejezésfát (`ExpressionTreeBuilder`). A fa a levelektől a gyökérig értékelődik ki (`ExpressionNode.evaluate(angleMode)` → `Operations.executeUnary` / `executeBinary`).

A postfix szöveg és a fa ugyanazt a kiértékelési sorrendet mutatja. A fa képernyő a postfixet **koppinttható chipekként** jeleníti meg (tokenenként egyet, például `9` `7` `6` `×` `+`), a fát pedig kinyitható csomópontokként. Lásd [Fa képernyő — navigáció postfixszel](#fa-képernyő--navigáció-postfixszel).

```
begépelt billentyűk
    → infix tokenek  (számok, operátorok, zárójelek)
        → postfix tokenek  (nincs zárójel; az operátor az operandusai után áll)
            → kifejezésfa  (bináris / unáris / érték csomópontok)
                → Double eredmény  (az aktuális számrendszerben formázva)
```

## A motor állapota

A `CalculatorEngine` a következőket tartja:

| Mező | Szerep |
| --- | --- |
| `base` | Aktív számrendszer: kettes, nyolcas, tízes vagy tizenhatos. Alapértelmezés: tízes. |
| `infix` | Rögzített infix tokenek (befejezett számok, operátorok, zárójelek). |
| `currentInput` | Az éppen gépelt szám jegyei. Még nem infix token. |
| `afterEquals` | `true` sikeres `=` után. A következő jegy új kifejezést kezd; a következő operátor az eredményt folytatja. |
| `lastResult` | Az utolsó sikeresen számolt `Double`, vagy `null`. |
| `lastTree` | Az utolsó sikeres `=` kifejezésfája, vagy `null`. |
| `lastPostfix` | Az utolsó sikeres `=` postfix tokenjei. |
| `lastExpressionText` | Az utolsó sikeres `=`-nél befagyasztott infix szöveg. |
| `liveDisplay` | A fő kijelző szövege. |
| `errorKind` | Aktuális hiba, vagy `null`. |
| `memory` | Független memóriaregiszter (`Double`, kezdetben `0.0`). |
| `memorySet` | `true` `M+` vagy `M−` után, amíg `MC` nem jön. Az `M` jelzőt vezérli. A `C` nem törli a memóriát. |
| `angleMode` | `AngleMode.DEGREES` (alapértelmezés) vagy `RADIANS`. A `sin` / `cos` / `tan` ezt használja. A `C` nem állítja vissza. |

A `snapshot()` a nyilvános nézet:

- `display` — aktuális szám vagy utolsó eredmény
- `expression` — élő infix gépelés közben; befagyasztott infix `=` után
- `postfix` — az utolsó sikeres postfix, vesszővel tagolva, vagy üres
- `postfixTokens` — ugyanazok a tokenek listaként (üres, amíg nincs sikeres `=`); a fa képernyő chipeit ebből rajzolja
- `tree` — az utolsó sikeres fa, vagy `null`
- `base`, `errorKind`, `memorySet`, `afterEquals`, `angleMode`

Sikertelen `=` törli a snapshot `tree` és `postfix` mezőjét, de a begépelt infixtet és a `lastResult` értéket nem.

## Tokenek

```
Token
  NumberLiteral(raw)   pl. "9", "1.25", "π", "A"
  OperatorToken(kind)  bármely OperatorKind
  LeftParen
  RightParen
```

Nincs implicit szorzás token. A `2(3+4)` nem íródik át `2 × (3+4)` alakra.

A billentyűzet unáris függvényei az operandus **után** kerülnek az infix listába (`9`, `√`), nem prefix függvényként. A prefix unáris mínusz (kifejezés elején, `(` után, bináris operátor után, vagy egy másik `neg` után) `OperatorKind.NEG` token (precedencia 99, jobbasszociatív). A `+/−` gomb gépelés közben továbbra sem `NEG` token: a `currentInput` vagy a `lastResult` értékét módosítja (lásd [Előjelváltás](#előjelváltás)). Negatív literál kiürítésekor (`-5`) `NEG` plusz `NumberLiteral("5")` kerül a listába.

## Billentyűk és a tiltott gombok

A `press(key)` nem csinál semmit, ha `isKeyEnabled(key)` hamis, kivéve a `C` (`CLEAR`) gombot, amely mindig működik.

Az engedélyezett gombok a számrendszertől függenek (`NumericBase.allowsKey`):

- Számjegyek: csak a radiksznál kisebb jegyek (`0–1` kettes, `0–7` nyolcas, `0–9` tízes, `0–9` és `A–F` tizenhatos).
- Csak tízesben: `π`, `sin`, `cos`, `tan`, `ln`, `log`, `exp`, `!`. A pont (`.`) minden számrendszerben engedélyezett.
- Minden más (számtan, bitműveletek, `√`, `1/`, `²`, `³`, zárójelek, memória, `=`, backspace, `Tst`, Fok/Rad) minden számrendszerben engedélyezett. A Fok/Rad (`CalculatorKey.ANGLE_MODE`) minden számrendszerben elérhető, noha a trigonometrikus gombok csak tízesben vannak.

Tízesben a hexabetűk figyelmen kívül maradnak; a kijelző `"0"` marad, ha még semmi más nem volt begépelve.

## Az infix kifejezés felépítése

### Aktuális bevitel és rögzített tokenek

A szám a `currentInput`-ban él, amíg valami **ki nem üríti** az `infix` listába `NumberLiteral` tokenként:

- bináris operátor
- unáris operátor
- zárójel
- `π`
- `=`

A kiürítés átugorja a befejezetlen töredékeket: üres, `"."`, `"-."`. Ezek nem válnak tokenné. A magányos `"-"` `OperatorKind.NEG` tokenné ürül, nem dobódik el.

Az élő kifejezésszöveg a rögzített tokenek szóközzel összefűzve, plusz a `currentInput`, ha nem üres. Példa `9+7*6` gépelése közben:

```
kijelző:     6
kifejezés:   9 + 7 × 6
postfix:     (üres az = -ig)
```

`=` után:

```
kijelző:     51
kifejezés:   9 + 7 × 6
postfix:     9, 7, 6, ×, +
afterEquals: true
```

### Számjegyek

- Az adott számrendszerben érvénytelen jegy → `INVALID_DIGIT`; a jegy nem íródik be.
- Egy magányos `"0"`-t a következő, nem pont jegy lecseréli (`0`, majd `5` → `5`).
- `"-0"` plusz nem pont jegy → `"-"` plusz az a jegy (nem marad `-05`).
- `=` után a számjegy **új** kifejezést kezd (`beginNewInputIfNeeded`).

### Tizedespont

- Ugyanabban a számban a második `.` figyelmen kívül marad (`1.2.5` → `1.25`).
- Kezdő `.` → `0.` (`.5` értéke `0.5`).
- Minden számrendszerben engedélyezett.

### Bináris operátorok (`+ − × ÷ mod ^ and or xor lsh`)

1. Ha `afterEquals`, az utolsó eredmény lesz az új infix lista első operandusa.
2. `currentInput` kiürítése.
3. Ha az operátor `−`, és az infix üres, `(`-re, bináris operátorra vagy `neg`-re végződik, `OperatorKind.NEG` kerül be bináris mínusz helyett (prefix unáris mínusz). A kezdő `-5` `neg`, majd `5`, nem `0 − 5`. A kezdő `+5` továbbra is `0`, majd `+`.
4. Ha az infix még mindig üres, beszúródik egy `0` (a kezdő `+5` → `0 + 5`).
5. Ha az utolsó token már bináris operátor, **lecserélődik** (`8+`, majd `×` → `8 ×`).
6. Az új operátor hozzáadása.

### Unáris operátorok (`not √ 1/ sin cos tan ln log exp ² ³ !`)

1. `=` után ugyanaz a „folytatás az eredménytől” viselkedés, mint a bináris operátoroknál.
2. `currentInput` kiürítése.
3. Ha az infix üres → `MALFORMED_EXPRESSION` (`√` operandus nélkül).
4. Egyébként az unáris operátor az operandus **után** kerül: `5`, majd `²`, majd `1/` tokenjei: `5`, `²`, `1/`.

A láncolt unárisok ugyanarra az értékre postfixként hatnak. `5 ² 1/` = `(5²)⁻¹ = 0.04`.

### Zárójelek

Kiürítés, majd `(` vagy `)` hozzáadása. A párosítást gépelés közben **nem** ellenőrzi. A kiegyensúlyozatlan zárójelet csak az `=` jelzi.

### π

Kiürítés, majd `NumberLiteral("π")` hozzáadása. A kijelző a π-t az aktuális számrendszerben mutatja. Csak tízes.

### Előjelváltás

A `+/−` nem a shunting-yard unáris mínusza.

| Helyzet | Hatás |
| --- | --- |
| Szám gépelése | Vezető `-` ki/be a `currentInput`-on. |
| `=` után | A `lastResult` helyben előjelet vált. Az `afterEquals` `true` marad. A következő operátor a negált értéket folytatja. |
| Üres bevitel (nem `=` után) | A `currentInput` `-`-szal indul. |

`5 +/− + 3 =` eredménye `-2`. Gépelés közben a vezető `-` még a `currentInput`-on van. Kiürítéskor `NEG` plusz `NumberLiteral("5")` lesz belőle, ezért a fa `neg` csomópontot mutat. Prefix operátorként begépelt `-` (elején, `(` után, vagy `+ − × …` után) azonnal `NEG`-et szúr be. A `-2^2` postfixe `2, 2, ^, −`, értéke `-(2^2) = −4`.

### Backspace

- `=` után — nincs hatása (az eredmény marad).
- Ha a `currentInput` nem üres — törli az utolsó karaktert.
- Egyébként kiveszi az utolsó infix tokent. Ha az egynél hosszabb szám volt, a maradék jegyek visszaíródnak a `currentInput`-ba.

### Törlés (`C`)

Visszaállítja az infixtet, az aktuális bevitelt, az utolsó eredményt, a fát, a postfixet, a kifejezésszöveget, a kijelzőt (`0` az aktuális számrendszerben), a hibát és az `afterEquals` jelzőt. **Nem** törli a `memory`, `memorySet` és `angleMode` értékét.

### `=` után

| Következő gomb | Viselkedés |
| --- | --- |
| Számjegy, `.`, `π`, `(` `)` | Új kifejezés. Az előző eredmény nem használódik fel. |
| Bináris vagy unáris operátor | Folytatás: az eredmény lesz az első operandus. |
| Újabb `=` | Újra megjeleníti a `lastResult` értéket. Nem hiba. |
| Backspace | Nincs hatása. |
| `+/−` | Negálja az eredményt; továbbra is „egyenlő utáni” állapot. |
| Fok/Rad | Csak az `angleMode` vált. Nem kezd új kifejezést, és nem számolja újra a kijelzőn lévő értéket. Az új egységhez gépeld be újra a kifejezést, és nyomj `=`-t. |
| Memóriagombok | Lásd [Memória](#memória-mc-mr-m-m). |

`=` üres infixszel és `lastResult` nélkül → `EMPTY_EXPRESSION`.

### Tesztgomb (`Tst`)

Törli a motort, tízesre állítja, és begépeli ezt a mintát **kiértékelés nélkül**:

```
888/(6*1.25+3*(7-4*1.5+(9/3-1.2))*2)-11*(4.5/2^3)/(5-1.25)
```

Utána `=` kell. A pontos tízes eredmény a szakaszos `34.89(320987654)`. A `Tst` után, `=` előtt vagy után történt számrendszer-váltás a kifejezés minden számát átírja az új számrendszerbe.

## Infixből postfix (shunting-yard)

Az `InfixToPostfix.convert(infix)` két dequet használ:

- **Kimenet** — a postfix eredmény (csak hozzáfűzés).
- **Operátorverem** — várakozó operátorok és bal zárójelek. A tető az utolsó elem.

Zárójel soha nem kerül a kimenetre. A számok azonnal a kimenetre mennek. Az operátorok a vermen várnak, amíg az operandusaik készen nincsenek.

### Algoritmus

Minden infix tokenre, balról jobbra:

1. **Szám** → a kimenetre.
2. **Operátor** → amíg a verem tetején olyan operátor van, amelyet ki kell venni (lásd alább), az a kimenetre kerül; aztán a bejövő operátor a veremre.
3. **Bal zárójel** → az operátorveremre.
4. **Jobb zárójel** → operátorok a kimenetre, amíg bal zárójel nem kerül elő. A páros `(` eldobódik. Ha nincs ilyen → `UNBALANCED_PARENTHESES` („hiányzó bal zárójel”).

Az utolsó token után a maradék operátorok a kimenetre mennek. Maradék `(` → `UNBALANCED_PARENTHESES` („hiányzó jobb zárójel”).

### Mikor kell levenni a verem tetejét (`shouldPop`)

Legyen `incoming` az új operátor, `stackTop` a verem tetején lévő.

| A bejövő asszociativitása | Levesz, amíg |
| --- | --- |
| Bal (szinte minden operátor) | `stackTop.precedence >= incoming.precedence` |
| Jobb (`^` és `neg`) | `stackTop.precedence > incoming.precedence` |

A vermen lévő bal zárójelet a `shouldPop` soha nem veszi le; csak `)`-re vagy végső egyensúlyhibára kerül le.

**Balasszociatív:** `a − b − c` → `a b - c -`, kiértékelés: `(a − b) − c`.

**Jobbasszociatív:** `2 ^ 3 ^ 2` → `2, 3, 2, ^, ^`, kiértékelés: `2^(3^2) = 512`, nem `(2^3)^2 = 64`. Azonos precedenciájú hatványoknál a korábbi `^` **nem** kerül le.

### Precedenciatáblázat

A nagyobb szám szorosabban köt. Ezek az `OperatorKind` értékei.

| Precedencia | Operátorok | Arítás | Asszociativitás |
| ---: | --- | --- | --- |
| 99 | `^` | bináris | jobb |
| 99 | `neg` | unáris | jobb |
| 99 | `√` `1/` `²` `³` `!` | unáris | bal |
| 14 | `not` | unáris | bal |
| 12 | `×` `÷` `mod` `lsh` | bináris | bal |
| 12 | `sin` `cos` `tan` `ln` `log` `exp` | unáris | bal |
| 11 | `+` `−` | bináris | bal |
| 7 | `and` | bináris | bal |
| 6 | `xor` | bináris | bal |
| 5 | `or` | bináris | bal |

Tehát `1 or 2 and 3` postfixe `1, 2, 3, and, or`, értéke `1 or (2 and 3)`.

A `√` `1/` `²` `³` `!` a `^`-gel osztozik a 99-es precedencián. Mivel balasszociatívak és az operandus után állnak, a `9 √` egyszerűen `9, √`. A láncolt `5 ² 1/` a korábbi unárist leveszi, mielőtt a következőt feltenné: postfix `5, ², 1/`.

A `sin` `cos` `tan` `ln` `log` `exp` a `×`-szel osztozik a 12-es precedencián, ezért a megelőző szorzást vagy osztást becsomagolják: `π ÷ 2 sin` postfixe `π, 2, ÷, sin` = `sin(π/2)`. A `2 × (3 sin)` zárójelet igényel.

A `neg` 99-es precedencián jobbasszociatív, mint a `^`, ezért a `-2^2` értéke `-(2^2) = −4`, nem `(-2)^2 = 4`.

### Kidolgozott átalakítás: `9 + 7 × 6`

Infix tokenek: `9`, `+`, `7`, `×`, `6`.

| Token | Kimenet | Operátorverem | Megjegyzés |
| --- | --- | --- | --- |
| `9` | `9` | | szám → kimenet |
| `+` | `9` | `+` | üres verem |
| `7` | `9` `7` | `+` | |
| `×` | `9` `7` | `+` `×` | 11 ≥ 12 hamis, a `+` marad |
| `6` | `9` `7` `6` | `+` `×` | |
| vég | `9` `7` `6` `×` `+` | | verem ürítése, tető először |

Postfix a kijelzőn: `9, 7, 6, ×, +`.

### Kidolgozott átalakítás: `(1 + 2) × 3`

| Token | Kimenet | Operátorverem |
| --- | --- | --- |
| `(` | | `(` |
| `1` | `1` | `(` |
| `+` | `1` | `(` `+` |
| `2` | `1` `2` | `(` `+` |
| `)` | `1` `2` `+` | | levesz `(`-ig |
| `×` | `1` `2` `+` | `×` |
| `3` | `1` `2` `+` `3` | `×` |
| vég | `1` `2` `+` `3` `×` | |

A zárójel az összeadást a szorzás elé kényszeríti.

### Kidolgozott átalakítás: `2 ^ 3 ^ 2`

| Token | Kimenet | Operátorverem | Megjegyzés |
| --- | --- | --- | --- |
| `2` | `2` | | |
| `^` | `2` | `^` | |
| `3` | `2` `3` | `^` | |
| `^` | `2` `3` | `^` `^` | jobb asszoc.: 99 > 99 hamis, nem veszi le |
| `2` | `2` `3` `2` | `^` `^` | |
| vég | `2` `3` `2` `^` `^` | | a belső `^` (3²) van felül, ezért az jön ki először |

### A tesztekben használt beágyazott minta

`777/(5*1.33+2*(5-3*1.3)*3)` postfixe:

```
777, 5, 1.33, ×, 2, 5, 3, 1.3, ×, -, ×, 3, ×, +, ÷
```

## A postfix kiértékeléséhez szükséges vermes sorrend

A kiértékelés **nem** parse-olja újra az infixtet. A postfixet **balról jobbra** olvassa egy értékveremmel (LIFO). Az `ExpressionTreeBuilder` nyers számok helyett csomópontokat épít; a levételek sorrendje ugyanaz.

### Szabályok

Üres veremmel indul. Minden postfix tokenre:

| Token | Veremművelet |
| --- | --- |
| Szám | Feldolgozás az **aktuális számrendszerben**, értékcsomópont a veremre. |
| Unáris operátor | A vermen ≥ 1 elem kell. **Az operandus levétele** (verem teteje). `UnaryNode(op, operand)` feltevése. |
| Bináris operátor | A vermen ≥ 2 elem kell. **Először a jobb, aztán a bal** operandus levétele. `BinaryNode(op, left, right)` feltevése. |
| `(` vagy `)` | `MALFORMED_EXPRESSION` (sikeres átalakítás után nem fordulhat elő). |

Az utolsó token után a vermen **pontosan egy** csomópontnak kell lennie: a gyökérnek. Egyébként `MALFORMED_EXPRESSION` (üres kifejezés, maradék értékek vagy hiányzó operandusok).

A bináris operátoroknál a levételek sorrendje kötelező, mert a verem LIFO:

```
right = stack.pop()   // a legutóbb felrakott operandus
left  = stack.pop()   // a korábbi operandus
result = left  op  right
stack.push(result)
```

A `9 7 6 × +` esetén a `×` `left=7`, `right=6` párost lát, majd a `+` `left=9`, `right=42` párost. A bal/jobb megcserélése a `6×7`-et még ugyanúgy adná, de a `42+9` a `9+42` helyett elrontaná a kivonást, osztást, `mod`-ot, `^`-et és `lsh`-t.

Unáris:

```
operand = stack.pop()
result = op(operand)
stack.push(result)
```

### Kidolgozott kiértékelés: `9, 7, 6, ×, +`

| Token | Értékverem a token után | Művelet |
| --- | --- | --- |
| `9` | `9` | fel |
| `7` | `9` `7` | fel |
| `6` | `9` `7` `6` | fel |
| `×` | `9` `42` | le `6` (jobb), le `7` (bal), `7×6` |
| `+` | `51` | le `42` (jobb), le `9` (bal), `9+42` |

Fa:

```
        + = 51
       / \
      9   × = 42
         / \
        7   6
```

### Kidolgozott kiértékelés: `2, 3, 2, ^, ^`

| Token | Verem | Művelet |
| --- | --- | --- |
| `2` | `2` | |
| `3` | `2` `3` | |
| `2` | `2` `3` `2` | |
| `^` | `2` `9` | `3^2` (belső, jobb asszociatív) |
| `^` | `512` | `2^9` |

Fa: gyökér `^`, bal gyerek `2`, jobb gyerek `(3 ^ 2)`.

### Kidolgozott kiértékelés: `9, √`

| Token | Verem |
| --- | --- |
| `9` | `9` |
| `√` | `3` |

### Hibás vermes esetek

| Postfix | Miért hibás |
| --- | --- |
| üres | a verem mérete ≠ 1 |
| `1` `2` | két maradék érték |
| `√` | unáris üres vermen |
| `1` `+` | bináris csak egy operandussal |
| maradék `(` | zárójel tilos a postfixben |

## Műveletek

A `UnaryNode` / `BinaryNode` az `Operations.executeUnary` / `executeBinary` függvényt hívja. Az értékek IEEE `Double` számok. A bitműveletek először 32 bites előjeles egésszé alakítanak.

### Bináris

| Fajta | Jelentés | Extra szabály |
| --- | --- | --- |
| `+` `−` `×` | szokásos számtan | |
| `÷` | `left / right` | `right == 0` és `left == 0` → `UNDEFINED`; egyéb `right == 0` → `DIVISION_BY_ZERO` |
| `mod` | `left % right` (IEEE maradék) | `right == 0` → `DIVISION_BY_ZERO` |
| `^` | `Math.pow(left, right)` | NaN vagy ∞ → `DOMAIN` (pl. `(-1)^0.5`) |
| `and` `or` `xor` | 32 bites bitművelet | mindkét operandus egész kell, és `[Int.MIN_VALUE, Int.MAX_VALUE]` között |
| `lsh` | `left << right` 32 bites intként | a `right` `0…31` között kell legyen; különben `DOMAIN` |

Unáris fajta `executeBinary`-ben (vagy fordítva) → `MALFORMED_EXPRESSION`.

### Unáris

| Fajta | Jelentés | Extra szabály |
| --- | --- | --- |
| `neg` | `-x` | prefix mínusznál és negatív literál kiürítésekor keletkezik |
| `not` | 32 bites bitenkénti komplementer (`0` → `-1`) | 32 bites egész kell |
| `√` | `sqrt` | `x < 0` → `DOMAIN` |
| `1/` | `1 / x` | `x == 0` → `DIVISION_BY_ZERO` |
| `sin` `cos` `tan` | alapból fok; radián, ha az `angleMode` `RADIANS` (`executeUnary(..., angleMode)`) | `tan` NaN/∞ → `DOMAIN` |
| `ln` | természetes logaritmus | `x ≤ 0` → `DOMAIN` |
| `log` | 10-es alapú logaritmus | `x ≤ 0` → `DOMAIN` |
| `exp` | `e^x` | túlcsordulás → `DOMAIN` |
| `²` `³` | `x*x`, `x*x*x` | |
| `!` | faktoriális | lásd alább |

Faktoriális:

- `x` nemnegatív egész kell, különben `INVALID_FACTORIAL`.
- `n > 170` → `DOMAIN` (túlcsordulás).
- `0! = 1! = 1`.

### Egészszám-teszt

`NumberFormatter.isWhole`: `|x − rint(x)| < 1e-9`. A bitenkénti átalakítás ezután `Math.rint` és `toInt()`. 32 bites tartományon kívül → `DOMAIN`. Nem egész → `BITWISE_NON_INTEGER`.

## Memória (`MC`, `MR`, `M+`, `M−`)

A memória egyetlen `Double` regiszter, független az infixtől, a kijelzőtől, az utolsó eredménytől és a `C` gombtól.

Az `M` jelző a `memorySet`, nem az, hogy „a memória nem nulla”. `M+ 8`, majd `M− 8` után `memory == 0`, de `memorySet == true` marad `MC`-ig.

### Melyik számot használja az `M+` / `M−`

A `currentNumericValue()` az első létezőt választja:

1. **Aktuális bevitel**, ha teljes szám (nem üres / `-` / `.` / `-.`). Az aktuális számrendszerben parse-olódik.
2. Egyébként **`lastResult`**, ha létezik, és (`afterEquals` **vagy** az infix üres). Így a `5+5=` utáni `M+` a `10`-et tárolja.
3. Egyébként az **utolsó infix token**, ha az `NumberLiteral`.
4. Egyébként `null` — az `M+` / `M−` nem csinál semmit (nincs hiba, a `memorySet` nem változik).

A parse-hiba is `null`-t ad.

### `M+` (`MEMORY_ADD`)

```
memory += value
memorySet = true
```

A kijelző, az infix és az `afterEquals` nem változik.

### `M−` (`MEMORY_SUB`)

```
memory -= value
memorySet = true
```

Ugyanúgy nem nyúl a kijelzőhöz.

### `MR` (`MEMORY_RECALL`)

1. Ha `afterEquals`, új kifejezést kezd (az eredmény nem marad operandus).
2. A `currentInput` a `memory` aktuális számrendszerben formázott értéke lesz.
3. Ez a szöveg megy a kijelzőre.
4. Az `errorKind` törlődik.

Az `MR` nem teszi a számot az `infix`-be a következő kiürítésig (`=`, operátor, zárójel, …). Törölt memória (`0`, `memorySet == false`) felidézése is `0`-t gépel.

### `MC` (`MEMORY_CLEAR`)

```
memory = 0.0
memorySet = false
```

Nem változtatja a kijelzőt és a kifejezést.

### Példák

**Tárolás, bevitel törlése, felidézés**

```
8  M+  C  MR  =     → kijelző 8, memória továbbra is 8, M jelző be
MC C  MR  =         → kijelző 0, M jelző ki
```

**Kivonás a memóriából**

```
10 M+  C  3 M−  C  MR  =     → 7
```

**Az utolsó eredmény hozzáadása**

```
5 + 5 =  M+  C  MR  =        → 10
```

**Felidézés egyenlő után új kifejezést kezd**

```
1 + 2 =  MR
```

Ha a memória `8`, a következő `=` a `8`-at értékeli ki, nem a `3`-at a `8`-cal kombinálva.

A memória túléli a `C`-t és a számrendszer-váltást. A pontos érték törtként tárolódik, ezért az `1/3` felidézése hexában `0.(5)`. A tárolt tízes `10` hexában `A`.

## Számrendszerek

Az összeadás, kivonás, szorzás, osztás, előjel, reciprok, négyzet, köb és az egész kitevős hatvány egyszerűsített `BigInteger` törtként marad. A gyök, a szögfüggvények, a logaritmus, az `exp`, a faktoriális, a `mod` és a bitműveletek `Double` eredményt adnak, és a tört elvész. A számrendszer a számjegyekre, a literálok parse-olására és a kijelzésre hat.

| Számrendszer | Radix | Törtek | Megjegyzés |
| --- | ---: | --- | --- |
| Kettes | 2 | igen | jegyek `0–1`; a `.` engedélyezett |
| Nyolcas | 8 | igen | jegyek `0–7`; a `.` engedélyezett |
| Tízes | 10 | igen | alapértelmezés; tudományos gombok és `π` |
| Tizenhatos | 16 | igen | jegyek `0–9A–F`; a betűk nagybetűsen tárolódnak; a `.` engedélyezett |

`setBase`:

- Nincs hatása, ha a számrendszer nem változik.
- Átírja a beírt számot, a kifejezésben lévő minden számot és az eredményt. A tízes `10` hexában `A`, kettesben `1010`. A tízes `0.5` hexában `0.8`, kettesben `0.1`. Az `1/3` kettesben `0.(01)`, nyolcasban `0.(25)`, hexában `0.(5)`.
- A lezáró pont megmarad (hexában `12.` → `C.`). A `π` token nem íródik át.
- Törli az `errorKind` értékét.

A literálok az aktuális radikszban pontos törtként parse-olódnak, a zárójeles szakasszal együtt. A hexadecimális `1.5` értéke `1.8`, nem kerekített egész.

## Számformázás és π

Parse:

- `"π"`, `"PI"`, `"pi"` → `Math.PI`.
- A tört literál, a `EGÉSZ.TÖRT(SZAKASZ)` alakkal együtt, a közelítő úton pontos `Double` értékké válik.
- A tízes tudományos szöveg, például `1e20`, `toDouble()`-t használ.
- A többi véges literál az aktuális radikszban, számjegyenként parse-olódik.
- Üres / `-` / `.` / `-.` / szemét → `MALFORMED_EXPRESSION`.

Formázás:

- NaN vagy végtelen → `DOMAIN`.
- Tízes egész (a `Long` tartományán belül) tizedespont nélkül.
- A pontos tört zárójelben mutatja a szakaszt (`0.(3)`, `0.1(6)`, `1.(3)`). A 12 jegynél hosszabb szakasz 12 jegyre kerekítve, zárójel nélkül jelenik meg.
- Az egész érték minden számrendszerben a saját radikszában jelenik meg. A nem egész közelítő érték legfeljebb 12 tizedesjegyet használ. A nagyon nagy nagyságrend tudományos alakot kap.
- A `-0` → `0`.

## Hibák

| `ErrorKind` | Tipikus ok | Felület (magyar) |
| --- | --- | --- |
| `EMPTY_EXPRESSION` | `=` begépelés és utolsó eredmény nélkül | Először adj meg egy kifejezést. |
| `UNBALANCED_PARENTHESES` | hiányzó `(` vagy `)` az átalakításkor | A zárójelek nincsenek párban. |
| `DIVISION_BY_ZERO` | `÷ 0`, `mod 0`, `1/0` (ha a számláló nem 0) | Nullával osztás. |
| `UNDEFINED` | `0 ÷ 0` | Nem definiált. |
| `DOMAIN` | negatív `√`, `ln`/`log` `≤0`-ra, faktoriális `>170`, hibás `^`, hibás `lsh`, túlcsordulás, NaN/∞ | Az érték kívül esik a művelet értelmezési tartományán. |
| `BITWISE_NON_INTEGER` | `and`/`or`/`xor`/`not`/`lsh` nem egészen | A bitenkénti műveletekhez egész szám kell. |
| `INVALID_DIGIT` | az adott számrendszerben érvénytelen jegy (ha eljut az `enterDigit`-ig) | Ez a számjegy ebben a számrendszerben érvénytelen. |
| `MALFORMED_EXPRESSION` | hiányos kifejezés, unáris operandus nélkül, maradék veremértékek, hibás parse | A kifejezés nem teljes. |
| `INVALID_FACTORIAL` | negatív vagy nem egész faktoriális-argumentum | A faktoriálishoz nemnegatív egész szám kell. |

A tiltott gombok figyelmen kívül maradnak, nem `INVALID_DIGIT` hibát dobnak.

## Lokalizáció

A `:engine` modulnak **nincs fordított szövege**. `ErrorKind` értékeket, operátorjeleket (`OperatorKind.displaySymbol` / `treeLabel`) és formázott számokat ad. Minden felhasználónak szánt szöveg az Android alkalmazás erőforrásaiban van. A felület `stringResource(R.string…)`-szel tölti be (Compose).

Az Android a **készülék nyelve** alapján választ fájlt. Nincs alkalmazáson belüli nyelvválasztó.

| Nyelv | Fájl |
| --- | --- |
| Angol (alap / tartalék) | [`app/src/main/res/values/strings.xml`](../app/src/main/res/values/strings.xml) |
| Magyar (`hu`) | [`app/src/main/res/values-hu/strings.xml`](../app/src/main/res/values-hu/strings.xml) |

Új nyelvhez másold a `values/strings.xml` fájlt `values-<bcp47>/strings.xml` névre (például `values-de/`), és fordítsd le minden `<string name="…">` szöveget. A `name` attribútumok maradjanak azonosak; csak a szövegcsomópontok változzanak.

### Mit tartalmaz a fájl

Menük, témanevek, képernyőcímek, splash szlogen, Súgó és Névjegy szövegek (köztük a szögegység súgó: `help_angle_title` / `help_angle_body`), számrendszer-chipek (`Bin` / `Oct` / `Dec` / `Hex`), billentyűzet szavas gombjai (`And`, `Or`, `Xor`, `Not`, `Lsh`, `Mod`, `Tst`, Fok/Rad a `key_degrees` / `key_radians` kulcsokkal), memória `M` jelző, tartalomleírások, és **hibaüzenetek** (köztük az `error_undefined`).

A hibaleképezés a `CalculatorScreen.kt` (`errorMessage`) fájlban van: `ErrorKind` → `R.string.error_*`. A Súgó szövege a `HelpScreen.kt`-ben, a Névjegy a `AboutScreen.kt`-ben, a billentyűzetfeliratok a `CalculatorKeypad.kt`-ben.

### Ami nem lokalizált

- Az operátorjelek (`+`, `×`, `√`, `sin`, `π`, …) az `OperatorKind` / `CalculatorKey` értékekből jönnek, nem a `strings.xml`-ből.
- A számjegygombok, `MC` / `MR` / `M+` / `M−`, `C`, `+/−` és `=` minden nyelven ugyanazok a karakterek.
- A postfix és a fa címkéi ugyanezeket a motorjeleket és a formázott számokat használják.

A Play Áruház listázás (angol és magyar) külön van: [`docs/play-listing/STORE_LISTING.md`](play-listing/STORE_LISTING.md).

## Nyilvános típusok (modul térkép)

| Típus | Felelősség |
| --- | --- |
| `CalculatorEngine` | Billentyű-állapotgép; a memória és az utolsó eredmény gazdája. |
| `CalculatorKey` | Minden billentyű, a `MEMORY_*`, a `TEST` és az `ANGLE_MODE` is. |
| `AngleMode` | `DEGREES` (alapértelmezés) vagy `RADIANS`. |
| `Token` | Infix/postfix token. |
| `OperatorKind` | Szimbólum, fa-címke, precedencia, arítás, asszociativitás. |
| `InfixToPostfix` | Shunting-yard. |
| `ExpressionTreeBuilder` | Postfix → fa a kiértékelő vermen. |
| `ExpressionNode` | `ValueNode`, `UnaryNode`, `BinaryNode`; `evaluate(angleMode)` és `displayLabel(base, angleMode)`. |
| `Operations` | Szám- és bitműveletek (`executeUnary` az `angleMode`-ot kapja). |
| `NumberFormatter` | Parse/formázás/`isWhole`/`π`. Csak a közelítő értékekre. |
| `Fraction` | Egyszerűsített `BigInteger` tört a racionális műveletekhez. |
| `DecimalDisplay` | Lezáruló és szakaszos szöveg, például `0.(3)`. |
| `PostfixPlayback` | Play, szünet, folytatás és chipkijelölés a fa képernyőhöz. |
| `NumericBase` | Radix és a megengedett gombok. A pont minden számrendszerben engedélyezett. |
| `EvaluationResult` | Snapshot a felületnek (benne az `angleMode`). |
| `CalculatorException` / `ErrorKind` | Típusos hibák (köztük `UNDEFINED`). |

A fa képernyő a `childCount()` / `childAt()` és a `displayLabel(base, angleMode)` alapján járja be a fát (az operátorok `× = 42` stílusú címkét mutatnak; a levelek az eredeti nyers literált).

## Fa képernyő — navigáció postfixszel

Ez a `:app` felület viselkedése (`ExpressionTreeScreen`, `ExpressionTreeView`), a motor utórendezett (post-order) útvonalaira építve.

`=` után a postfix az `EvaluationResult.postfixTokens` chipekből áll, nem egyetlen vesszős `Text`. A sor `LazyRow` (vízszintes görgetés). Fa nélkül a sor `—`.

**A(z) *i*. chipre koppintva** a(z) *i*. kiértékelési lépés kijelölődik. Ugyanarra újra koppintva a kijelölés törlődik. A sor fölötti Play végigsétál a chipeken; a Pause tartja a lépést, a Play a következőnél folytatja. Az utolsó chip után a Play elölről indul. Új postfix (új sikeres `=`) nullázza a kijelölést (`rememberSaveable(state.postfix)`).

Az *i* index a fa **utórendezett** bejárásának *i*. csomópontja: először a gyerekek balról jobbra, aztán a csomópont. Ez megegyezik a postfix sorrenddel és az `ExpressionTreeBuilder` sorrendjével. Motor API:

- `ExpressionNode.postOrderNodes()` — csomópontok ebben a sorrendben
- `ExpressionNode.pathForPostOrderIndex(i)` — a Compose fa útvonala (`root`, `root/0`, `root/1/0`, …), vagy `null`, ha az index érvénytelen

Példa: `9 + 7 × 6` (gyökér `+`, bal `9`, jobb `×` a `7` és `6` fölött):

| *i* | Token | Útvonal | Csomópont |
| ---: | --- | --- | --- |
| 0 | `9` | `root/0` | bal levél |
| 1 | `7` | `root/1/0` | a `×` bal gyereke |
| 2 | `6` | `root/1/1` | a `×` jobb gyereke |
| 3 | `×` | `root/1` | szorzás |
| 4 | `+` | `root` | összeadás |

Kijelölt lépésnél:

1. A chip `badgeFill` / `operandRing` színeket kap.
2. Az útvonal **ősai kinyílnak**, ha be voltak csukva.
3. A kijelölt sor **láthatóvá görgetődik** (`BringIntoViewRequester`) a fa panelen (függőleges és vízszintes görgetés).
4. A kijelölt csomópont és a **leszármazottai** kiemelődnek (`path == highlightedPath`, vagy a `path` a `highlightedPath/` előtaggal kezdődik). A `×` kijelölése a `×`, `7` és `6` csomópontokat emeli ki.

Az operátor sorok továbbra is a `+` / `−` dobozzal nyithatók és csukhatók. A postfix kijelölés csak az ősöket kényszeríti nyitva; a fa többi részét nem zárja le.

Teszt: `ExpressionNodePostOrderTest`.

## Kijelző

Az infix sor egyetlen, rögzített magasságú, vízszintesen görgethető sor. A hosszú kifejezés, a `Tst` mintája is, nem törik új sorba, és nem változtatja a billentyűoszlopok szélességét. A funkcióbillentyűk és a számbillentyűk ugyanazt az oszlopszélességet használják. A fa képernyő postfix chipjei továbbra is `LazyRow`-ban görgethetők.

A Play 700 ms-onként lép a chipeken, és kiemeli a fa megfelelő ágát. A Pause tartja a lépést, a Play a következő tokentől folytatja. Az utolsó token után a Play elölről indul. Egy chip koppintása kijelöli a lépést és megállítja a sétát. Ugyanarra a chipre újra koppintva a kijelölés törlődik. Ha a rendszeranimáció időtartam-skálája 0, a Play koppintásonként egy tokent lép, időzítő nélkül.
