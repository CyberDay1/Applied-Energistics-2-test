# Stonecutter Port Validation Matrix

Track validation status for each targeted Minecraft version, the NeoForge build
used during certification, and where the work was validated.

| Version | NeoForge Build | MVP | Commit/PR | Notes |
|---------|----------------|-----|-----------|-------|
| 1.21.1  | 21.1.209       | no  |           | `./gradlew clean build --stacktrace --info --console=plain -Psc.version=1.21.1-neoforge` aborts during configuration: `Stonecutter branch root : has not been initialized. Use stonecutter.init() or stonecutter.active() to initialize it.` |
| 1.21.2  | 21.2.x         | no  |           | Same configuration failure as 1.21.1; see `logs/build-1.21.2-neoforge.log` for the `Stonecutter branch root` initialization error emitted before dependency resolution starts. |
| 1.21.3  | 21.3.x         | no  |           | Same configuration failure as 1.21.1; `Stonecutter branch root : has not been initialized` prevents the build from reaching dependency resolution. |
| 1.21.4  | 21.4.154       | no  |           | Same configuration failure as 1.21.1; stonecutter workspace initialization aborts the build prior to resolving `net.neoforged` coordinates. |
| 1.21.5  | 21.5.95        | no  |           | Same configuration failure as 1.21.1; stonecutter workspace fails to initialize for the requested version. |
| 1.21.6  | 21.6.x         | no  |           | Same configuration failure as 1.21.1; stonecutter workspace fails to initialize for the requested version. |
| 1.21.7  | 21.7.x         | no  |           | Same configuration failure as 1.21.1; stonecutter workspace fails to initialize for the requested version. |
| 1.21.8  | 21.8.x         | no  |           | Same configuration failure as 1.21.1; stonecutter workspace fails to initialize for the requested version. |
| 1.21.9  | 21.9.x         | no  |           | Same configuration failure as 1.21.1; stonecutter workspace fails to initialize for the requested version. |
| 1.21.10 | 21.10.x        | no  |           | Same configuration failure as 1.21.1; stonecutter workspace fails to initialize for the requested version. |

_2025-10-09 compile sweep rerun confirms that no 1.21.x target reaches a clean
`build`; rerun the compile sweep and smoke tests once the stonecutter workspace
initialization succeeds (or the configuration DSL is patched) so dependency
resolution can run to completion. Set MVP to `yes` and link the validation
commit or pull request after both compile and smoke validations succeed._

Current blockers: the stonecutter plugin aborts during configuration with
`Stonecutter branch root : has not been initialized` for every 1.21.x target,
preventing the build from reaching dependency resolution. Investigate missing
workspace metadata (e.g., ensure `stonecutter.init()` is invoked or restore the
workspace files under `.stonecutter`) before retrying the NeoForge dependency
sweep.
