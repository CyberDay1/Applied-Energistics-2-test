# Stonecutter Port Validation Matrix

Track validation status for each targeted Minecraft version, the NeoForge build
used during certification, and where the work was validated.

| Version | NeoForge Build | MVP | Commit/PR | Notes |
|---------|----------------|-----|-----------|-------|
| 1.21.1  | 21.1.209       | no  |           | `./gradlew clean build --no-daemon --stacktrace --console=plain -Psc.version=1.21.1` still stops in `:1.21.1:createMinecraftArtifacts` while searching for `net.neoforged:minecraft-dependencies:1.21.1`. |
| 1.21.2  | 21.2.x         | no  |           | The sweep with `-Psc.version=1.21.2` fails at `:1.21.1:createMinecraftArtifacts` because NeoForge has never published the placeholder coordinate `net.neoforged:neoforge:21.2.x`. |
| 1.21.3  | 21.3.x         | no  |           | The sweep with `-Psc.version=1.21.3` fails at `:1.21.1:createMinecraftArtifacts` because NeoForge has never published the placeholder coordinate `net.neoforged:neoforge:21.3.x`. |
| 1.21.4  | 21.4.154       | no  |           | `./gradlew clean build --no-daemon --stacktrace --console=plain -Psc.version=1.21.4` fails with `net.neoforged:minecraft-dependencies:1.21.4` unresolved. |
| 1.21.5  | 21.5.95        | no  |           | `./gradlew clean build --no-daemon --stacktrace --console=plain -Psc.version=1.21.5` fails with `net.neoforged:minecraft-dependencies:1.21.5` unresolved. |
| 1.21.6  | 21.6.x         | no  |           | `./gradlew clean build --no-daemon --stacktrace --console=plain -Psc.version=1.21.6` stops once `net.neoforged:neoforge:21.6.x` cannot be resolved. |
| 1.21.7  | 21.7.x         | no  |           | `./gradlew clean build --no-daemon --stacktrace --console=plain -Psc.version=1.21.7` stops once `net.neoforged:neoforge:21.7.x` cannot be resolved. |
| 1.21.8  | 21.8.x         | no  |           | `./gradlew clean build --no-daemon --stacktrace --console=plain -Psc.version=1.21.8` stops once `net.neoforged:neoforge:21.8.x` cannot be resolved. |
| 1.21.9  | 21.9.x         | no  |           | `./gradlew clean build --no-daemon --stacktrace --console=plain -Psc.version=1.21.9` stops once `net.neoforged:neoforge:21.9.x` cannot be resolved. |
| 1.21.10 | 21.10.x        | no  |           | `./gradlew clean build --no-daemon --stacktrace --console=plain -Psc.version=1.21.10` stops once `net.neoforged:neoforge:21.10.x` cannot be resolved. |

_2025-10-09 compile sweep rerun confirms that no 1.21.x target reaches a clean
`build`; rerun the compile sweep and smoke tests as soon as NeoForge publishes
the missing dependency bundles. Set MVP to `yes` and link the validation
commit or pull request after both compile and smoke validations succeed._

Current blockers: NeoForge has still not published the
`net.neoforged:minecraft-dependencies` bundles for 1.21.1, 1.21.4, or 1.21.5,
and `stonecutter.json` still points 1.21.2–1.21.3 and 1.21.6–1.21.10 at
placeholder `net.neoforged:neoforge:21.x` coordinates. Every
`createMinecraftArtifacts` task fails while resolving these artifacts. Once the
release maven exposes the missing coordinates, rerun the compile sweep and
smoke tests, then flip the MVP entries to `yes` with the validation commit/PR
links.
