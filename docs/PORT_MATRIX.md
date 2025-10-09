# Stonecutter Port Validation Matrix

Track validation status for each targeted Minecraft version, the NeoForge build
used during certification, and where the work was validated.

| Version | NeoForge Build | MVP | Commit/PR | Notes |
|---------|----------------|-----|-----------|-------|
| 1.21.1  | 21.1.209       | no  |           | `./gradlew -Psc.version=1.21.1 :1.21.1:build` still fails; NeoForge has not published `net.neoforged:minecraft-dependencies:1.21.1`. |
| 1.21.2  | 21.2.xxx       | no  |           | `stonecutter.json` still lists the placeholder NeoForge coordinate `21.2.x`; waiting on a published build before rerunning. |
| 1.21.3  | 21.3.xxx       | no  |           | `stonecutter.json` still lists the placeholder NeoForge coordinate `21.3.x`; waiting on a published build before rerunning. |
| 1.21.4  | 21.4.154       | no  |           | `./gradlew -Psc.version=1.21.4 :1.21.4:build` fails because `net.neoforged:minecraft-dependencies:1.21.4` remains unavailable on the release maven. |
| 1.21.5  | 21.5.95        | no  |           | `./gradlew -Psc.version=1.21.5 :1.21.5:build` fails: `net.neoforged:minecraft-dependencies:1.21.5` cannot be resolved. |
| 1.21.6  | 21.6.20-beta   | no  |           | `./gradlew -Psc.version=1.21.6 :1.21.6:build` fails immediately because the placeholder `net.neoforged:neoforge:21.6.x` artifact is not published yet. |
| 1.21.7  | 21.7.25-beta   | no  |           | `./gradlew -Psc.version=1.21.7 :1.21.7:build` fails immediately because the placeholder `net.neoforged:neoforge:21.7.x` artifact is not published yet. |
| 1.21.8  | 21.8.47        | no  |           | `./gradlew -Psc.version=1.21.8 :1.21.8:build` fails immediately because the placeholder `net.neoforged:neoforge:21.8.x` artifact is not published yet. |
| 1.21.9  | 21.9.2-beta    | no  |           | `./gradlew -Psc.version=1.21.9 :1.21.9:build` fails immediately because the placeholder `net.neoforged:neoforge:21.9.x` artifact is not published yet. |
| 1.21.10 | 21.10.2-beta   | no  |           | `./gradlew -Psc.version=1.21.10 :1.21.10:build` fails immediately because the placeholder `net.neoforged:neoforge:21.10.x` artifact is not published yet. |

_2025-04 compile sweep rerun confirms that no 1.21.x target reaches a clean
`build`; rerun the compile sweep and smoke tests as soon as NeoForge publishes
the missing dependency bundles. Set MVP to `yes` and link the validation
commit or pull request after both compile and smoke validations succeed._

Current blockers: NeoForge has not published the `minecraft-dependencies`
bundles for 1.21.1–1.21.5, and the 1.21.6–1.21.10 entries in `stonecutter.json`
still point at placeholder `net.neoforged:neoforge:21.x` coordinates. Every
`createMinecraftArtifacts` task fails while resolving these artifacts. Once the
release maven exposes the missing coordinates, rerun the compile sweep and
smoke tests, then flip the MVP entries to `yes` with the validation commit/PR
links.
