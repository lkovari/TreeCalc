# TreeCalc

Calculator based on an expression tree and Polish notation. The tree is shown for demonstration purposes.

## Modules

- `:engine` — infix to postfix (shunting-yard), expression tree, evaluation, bases 2/8/10/16
- `:app` — Jetpack Compose Android UI

Package: `com.lkovari.mobile.apps.treecalc`

## Theme

Light, Dark, or Auto (light 06:00–18:00). Interface language follows the phone locale (English default, Hungarian in `values-hu`).

## Website and privacy

Local copy: `docs/website/`

GitHub Pages (after KLHome deploy):

- https://lkovari.github.io/KLHome/assets/bigfiles/treecalc/index.html
- https://lkovari.github.io/KLHome/assets/bigfiles/treecalc/privacy.html

Play listing copy and graphics: `docs/play-listing/`

## Signing

Copy `keystore.properties.example` to `keystore.properties` and point it at the EKL release keystore (same as sensors-s). `keystore.properties` is gitignored.
