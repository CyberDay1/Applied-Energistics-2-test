# Stonecutter Port Validation Matrix

Track validation status for each targeted Minecraft version, the NeoForge build
used during certification, and where the work was validated.

| Version | NeoForge Build | MVP | Commit/PR | Notes |
|---------|----------------|-----|-----------|-------|
| 1.21.1  | 21.1.xxx       | no  |           | Stonecutter fails while switching (`Set active project to 1.21.1`) due to `? if eval(...)` conditional parsing errors in multiple sources. |
| 1.21.2  | 21.2.xxx       | no  |           | Blocked pending Stonecutter parser fix; version switch cannot complete. |
| 1.21.3  | 21.3.xxx       | no  |           | Blocked pending Stonecutter parser fix; version switch cannot complete. |
| 1.21.4  | 21.4.154       | no  |           | Even resetting to 1.21.4 currently fails with the same Stonecutter parse errors, preventing baseline builds. |
| 1.21.5  | 21.5.95        | no  |           | Blocked pending Stonecutter parser fix; version switch cannot complete. |
| 1.21.6  | 21.6.20-beta   | no  |           | Blocked pending Stonecutter parser fix; version switch cannot complete. |
| 1.21.7  | 21.7.25-beta   | no  |           | Blocked pending Stonecutter parser fix; version switch cannot complete. |
| 1.21.8  | 21.8.47        | no  |           | Blocked pending Stonecutter parser fix; version switch cannot complete. |
| 1.21.9  | 21.9.2-beta    | no  |           | Blocked pending Stonecutter parser fix; version switch cannot complete. |
| 1.21.10 | 21.10.xxx      | no  |           | Blocked pending Stonecutter parser fix; version switch cannot complete. |

_Once the Stonecutter preprocessing errors are resolved, re-run the compile sweep
and smoke tests. Set MVP to `yes` and link the validation commit or pull request
after both compile and smoke validations succeed._

Current blocker: `./gradlew --console=plain "Set active project to 1.21.1"` and
`./gradlew --console=plain "Set active project to 1.21.4"` both abort during the
`stonecutterPrepare` phase with hundreds of conditional parsing failures on
files such as `src/main/java/appeng/AE2Capabilities.java`. Until the conditional
syntax is updated or the Stonecutter parser is adjusted, all downstream build
and smoke validations remain blocked.
