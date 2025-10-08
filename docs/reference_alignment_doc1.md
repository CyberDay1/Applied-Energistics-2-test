# Reference Alignment & Audit (Doc 1)

## Current AE2 NeoForge repository
- `settings.gradle` loads `stonecutter.json`, normalizes both map and list payloads, writes a legacy `stonecutter.generated.json`, and calls `stonecutter.create(rootProject, generatedFile)` to bootstrap version awareness. It then surfaces `${MC}` and `${NEOFORGE}` through `extraProperties` so they are available to all projects.
- Default coordinates originate from `stonecutter.json`, which stores the target matrix as a JSON object keyed by version (`"1.21.4"` → `"1.21.9"`) with MC/NeoForge pairs and a default of `1.21.4`.
- `stonecutter.gradle.kts` merely applies the Stonecutter plugin and activates the `1.21.4` target, with no centralized script forwarding.
- `gradle.properties` currently pins `${MC}` and `${NEOFORGE}` to the default branch values (`1.21.4` / `21.4.154`), matching the shared block in `settings.gradle`.
- `build.gradle` uses `${NEOFORGE}` directly in the dependency declaration but otherwise remains a single Groovy build script shared across loaders.

## Neruina Stonecutter reference snapshot
- Direct repository access is still pending; the expected `settings.gradle.kts` with `stonecutter { create(rootProject) { … } }`, `kotlinController`, `centralScript`, and an explicit `vcsVersion` could not be confirmed during this pass. (Action item captured below.)
- Documentation and partner notes suggest Neruina splits per-loader build logic into dedicated Kotlin DSL files (e.g., `build.neoforge.gradle.kts`, `build.fabric.gradle.kts`) and centralizes shared wiring through `stonecutter.gradle.kts`, but we still need to verify file contents and version declarations firsthand.

## Comparison summary
| Aspect | Current AE2 NeoForge repo | Neruina reference (target) |
| --- | --- | --- |
| Version declaration style | JSON map in `stonecutter.json` converted to Groovy lists before `stonecutter.create`. | **TBD** – need to inspect `settings.gradle.kts` to confirm `versions { ... }` / `vers(...)` usage and how defaults are represented. |
| Default / `vcsVersion` | Default is `1.21.4` via JSON, passed into `stonecutter.create` and `stonecutter { shared { ... } }`. | **TBD** – confirm `vcsVersion` in Kotlin DSL once repository access is available. |
| Buildscript strategy | Single Groovy `build.gradle` for NeoForge, no central Kotlin script. | Expected Kotlin DSL pair (`stonecutter.gradle.kts` + per-loader builds); needs verification. |
| Property resolution | `settings.gradle` injects `${MC}`/`${NEOFORGE}` into `extraProperties`; `gradle.properties` also hardcodes defaults. | Need to check how Neruina propagates placeholders from `gradle.properties`/`configurable.properties`. |

## TODOs for Doc 2
1. Gain read access to the Neruina Stonecutter reference repository and capture concrete excerpts from `settings.gradle.kts`, `stonecutter.gradle.kts`, and per-loader build scripts.
2. Confirm whether Neruina relies on `versions {}` or `vers("1.21.x", mc = …)` declarations and document the exact `vcsVersion`/`default` wiring.
3. Validate the presence of `kotlinController`, `centralScript`, and `mapBuilds` (or equivalent) in the reference setup.
4. Compare property propagation files (`gradle.properties`, `configurable.properties`) to ensure `${MC}` and `${NEOFORGE}` placeholders flow into Gradle tasks the same way.
5. Plan the Doc 2 migration to introduce Stonecutter targets `1.21.1` through `1.21.10`, updating JSON/Kotlin wiring accordingly once the reference pattern is confirmed.
