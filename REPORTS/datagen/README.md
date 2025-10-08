# Datagen Procedure

This directory tracks data generation runs performed while upgrading from 1.21.1 through 1.21.10.

## Workflow
1. Switch version: `./gradlew sc:useVersion -Psc.version=1.21.x`
2. Run datagen: `./gradlew runData`
3. Compare outputs against legacy 1.21.1 exports
4. Promote valid files into `src/generated/resources`
5. Archive full outputs under `REPORTS/datagen/<version>/`

## Validation Log
| Version | Run Date | Features Validated | Notes |
|---------|----------|--------------------|-------|
| 1.21.1  | TBD      | Tags, Recipes      |       |
| 1.21.4  | TBD      | Tags, Recipes      |       |
| 1.21.5  | TBD      | Payload/Codec      |       |
| …       | …        | …                  | …     |
