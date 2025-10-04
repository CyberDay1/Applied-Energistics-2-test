# Datagen Enablement Plan

Datagen is currently deferred. When it is enabled, follow these steps to verify parity:

1. Run the Stonecutter target datagen task:
   ```sh
   ./gradlew runData
   ```
2. Compare the generated JSON assets under `build/generated` to the legacy 1.21.1 exports.
3. Validate parity across the following gates:
   - Tags
   - Recipes
   - Loot tables
   - World generation features
   - Biome modifiers
