# AE2 Data Provider Notes

The simplified data generator entry points under `appeng.datagen` wrap the richer
provider set in this package. Each sub-package mirrors the categories used by the
production generators (loot, models, recipes, tags, etc.) so new stubs can be
filled in incrementally without needing to port every upstream provider at once.

When adding a new provider, prefer extending the existing base classes in this
package. They document the hooks that still need parity with the mainline mod and
make it easy to flag TODOs for any missing polish while the NeoForge port is
stabilised.
