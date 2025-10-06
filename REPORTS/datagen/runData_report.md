# Datagen Run Report – Phase 3 Integration

**Run command:** `./gradlew runData --console=plain --no-daemon`

**Result:** ⚠️ Failed – NeoForge neoForm merge pipeline is missing the stripped
client artifact (`steps/stripClient/output.jar`) for the 1.21.4 target. The
Stonecutter-managed NeoGradle tasks continue to require the merged client/server
jar before `runData` can execute, so the task aborts prior to invoking the AE2
providers.

**Investigation notes:**
- Regenerated the client extra jar via `create1.21.4ClientExtraJar` and reran the
  strip client step (`neoFormStripClient`). Despite the steps succeeding, the
  expected `output.jar` is not emitted into the Stonecutter cache.
- Manually staged the generated `client-extra.jar` in the `stripClient`
  directory; the subsequent merge still purged the file before execution and
  repeated the failure.
- The failure occurs for both the aggregated `runData` task and the scoped
  `:1.21.4:runData` invocation.

**Next steps:** Investigate the Stonecutter/NeoGradle configuration to ensure
`neoFormStripClient` writes its output, or adjust the datagen workflow to avoid
pulling the full merge pipeline when only the datagen classpath is required.
