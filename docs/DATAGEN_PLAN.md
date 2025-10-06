# Datagen Enablement Plan (Phase 3)

Phase 3 moves the NeoForge 1.21.x datagen scaffolding from placeholder status to
an actively validated pipeline. The following guard rails must be satisfied for
each Stonecutter target:

1. Run the aggregated datagen task:
   ```sh
   ./gradlew runData
   ```
2. Review the generated assets under `build/generated` and promote verified
   outputs into `src/generated/resources`.
3. Validate coverage against the Phase 3 feature gates. ✅ indicates that the
   gate has now been fully covered by the NeoForge 1.21.x datagen pipeline.
   - ✅ **Storage cells** – item, fluid, spatial and partitioned cell content plus
     drive blockstates and loot.
   - ✅ **Crafting CPU blocks** – controller, co-processor, crafting monitor and
     molecular assembler states/models.
   - ✅ **Patterning terminals** – crafting, pattern and pattern encoding terminals
     including their language entries and models.
   - ✅ **IO busses** – import, export and storage bus data sets and their upgrade
     card recipes.
   - ✅ **Upgrade cards** – speed, capacity, redstone and fuzzy card recipes/tags.
   - ✅ **Processing machine registry** – furnace, blast furnace and brewing
     machine entries in generated data maps.
4. Archive validation notes for each run under `REPORTS/datagen/` including
   observed differences from legacy exports.
