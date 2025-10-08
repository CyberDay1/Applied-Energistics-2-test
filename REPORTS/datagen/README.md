# Datagen Report Workflow

This directory tracks data generation experiments for multi-version ports.

1. Run `./gradlew runData -Psc.version=<target>` after selecting the desired Stonecutter version.
2. Review the generated outputs; promote confirmed assets into `src/generated/resources`.
3. Archive meaningful diffs inside `REPORTS/datagen/<version>/` for future reference.

Placeholder folders are provided for each supported target (`1.21.1` → `1.21.10`).
