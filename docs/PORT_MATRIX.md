# Stonecutter Port Validation Matrix

Track validation status for each targeted Minecraft version, the NeoForge build
used during certification, and where the work was validated.

| Version | NeoForge Build | MVP | Commit/PR | Notes |
|---------|----------------|-----|-----------|-------|
| 1.21.1  | 21.1.209       | no  |           | `:1.21.1:compileJava` aborts because `net.neoforged:minecraft-dependencies:1.21.1` is not published on `https://maven.neoforged.net/releases`. |
| 1.21.2  | 21.2.xxx       | no  |           | Awaiting public NeoForge release; expected `minecraft-dependencies` bundle for 1.21.2 is still missing. |
| 1.21.3  | 21.3.xxx       | no  |           | Awaiting public NeoForge release; expected `minecraft-dependencies` bundle for 1.21.3 is still missing. |
| 1.21.4  | 21.4.154       | no  |           | `:1.21.4:createMinecraftArtifacts` fails due to missing `net.neoforged:minecraft-dependencies:1.21.4` artifact. |
| 1.21.5  | 21.5.95        | no  |           | `:1.21.5:createMinecraftArtifacts` fails: `net.neoforged:minecraft-dependencies:1.21.5` not yet available. |
| 1.21.6  | 21.6.20-beta   | no  |           | Blocked until a stable NeoForge build ships with published `minecraft-dependencies`. |
| 1.21.7  | 21.7.25-beta   | no  |           | Blocked until a stable NeoForge build ships with published `minecraft-dependencies`. |
| 1.21.8  | 21.8.47        | no  |           | Blocked until a stable NeoForge build ships with published `minecraft-dependencies`. |
| 1.21.9  | 21.9.2-beta    | no  |           | Blocked until a stable NeoForge build ships with published `minecraft-dependencies`. |
| 1.21.10 | 21.10.2-beta   | no  |           | Blocked until a stable NeoForge build ships with published `minecraft-dependencies`. |

_Re-run the compile sweep and smoke tests as soon as NeoForge publishes the
missing dependency bundles. Set MVP to `yes` and link the validation commit or
pull request after both compile and smoke validations succeed._

Current blocker: The NeoForge `minecraft-dependencies` bundles for 1.21.x are
not yet mirrored on the public `maven.neoforged.net` repository, so both
`createMinecraftArtifacts` and `compileJava` fail during dependency resolution.
Once NeoForge publishes these artifacts, rerun the compile sweep and smoke
tests, then flip the MVP entries to `yes` with the validation commit/PR links.
