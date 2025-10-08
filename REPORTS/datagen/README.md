# Datagen Report Workflow

This directory tracks data generation experiments for multi-version ports.

1. Run `./gradlew sc:useVersion -Psc.version=1.21.x runData` for the desired Minecraft/NeoForge pair.
2. Review the generated outputs and promote confirmed assets into `src/generated/resources`.
3. Archive meaningful diffs inside `REPORTS/datagen/<version>/` for future reference.

| Version | Run Date | Validated Features |
|---------|----------|--------------------|

Placeholder folders are provided for each supported target (`1.21.1` → `1.21.10`).
