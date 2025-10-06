package appeng.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import appeng.AE2Registries;

public class AELangProvider extends LanguageProvider {
    public AELangProvider(PackOutput output) {
        super(output, AE2Registries.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("block.appliedenergistics2.certus_quartz_ore", "Certus Quartz Ore");
        add("block.appliedenergistics2.inscriber", "Inscriber");
        add("block.appliedenergistics2.charger", "Charger");
        add("block.appliedenergistics2.sky_stone", "Sky Stone");
        add("block.appliedenergistics2.controller", "Controller");
        add("block.appliedenergistics2.energy_acceptor", "Energy Acceptor");
        add("block.appliedenergistics2.crafting_monitor", "Crafting Monitor");
        add("block.ae2.drive", "ME Drive");
        add("block.ae2.terminal", "ME Terminal");
        add("block.ae2.crafting_terminal", "ME Crafting Terminal");
        add("block.ae2.pattern_terminal", "ME Pattern Terminal");
        add("block.ae2.pattern_encoding_terminal", "Pattern Encoding Terminal");
        add("block.appliedenergistics2.storage_bus", "ME Storage Bus");
        add("block.appliedenergistics2.import_bus", "ME Import Bus");
        add("block.appliedenergistics2.export_bus", "ME Export Bus");
        add("block.appliedenergistics2.cable", "Cable");
        add("block.appliedenergistics2.meteorite", "Meteorite");
        add("block.appliedenergistics2.meteorite_hint", "Meteorite Hint");
        add("block.appliedenergistics2.molecular_assembler", "Molecular Assembler");
        add("block.appliedenergistics2.pattern_provider", "Pattern Provider");
        add("block.appliedenergistics2.crafting_co_processor", "Crafting Co-Processor");
        add("block.appliedenergistics2.crafting_unit", "Crafting Storage Unit");
        add("block.appliedenergistics2.crafting_accelerator", "Crafting Acceleration Unit");
        add("block.ae2.molecular_assembler", "Molecular Assembler");
        add("block.ae2.pattern_provider", "Pattern Provider");
        add("block.ae2.crafting_co_processor", "Crafting Co-Processor");
        add("block.ae2.crafting_unit", "Crafting Storage Unit");
        add("block.ae2.crafting_accelerator", "Crafting Acceleration Unit");

        add("item.appliedenergistics2.silicon", "Silicon");
        add("item.appliedenergistics2.certus_quartz_crystal", "Certus Quartz Crystal");
        add("item.appliedenergistics2.charged_certus_quartz_crystal", "Charged Certus Quartz Crystal");
        add("item.appliedenergistics2.printed_silicon", "Printed Silicon");
        add("item.appliedenergistics2.printed_logic_processor", "Printed Logic Circuit");
        add("item.appliedenergistics2.printed_engineering_processor", "Printed Engineering Circuit");
        add("item.appliedenergistics2.printed_calculation_processor", "Printed Calculation Circuit");
        add("item.appliedenergistics2.logic_processor", "Logic Processor");
        add("item.appliedenergistics2.engineering_processor", "Engineering Processor");
        add("item.appliedenergistics2.calculation_processor", "Calculation Processor");
        add("item.appliedenergistics2.capacity_card", "Capacity Card");
        add("item.appliedenergistics2.speed_card", "Speed Card");
        add("item.appliedenergistics2.redstone_card", "Redstone Card");
        add("item.appliedenergistics2.fuzzy_card", "Fuzzy Card");
        add("item.appliedenergistics2.blank_pattern", "Blank Pattern");
        add("item.appliedenergistics2.encoded_pattern", "Encoded Pattern");
        add("tooltip.appliedenergistics2.speed_card", "Speeds up transfer rate");
        add("tooltip.appliedenergistics2.capacity_card", "Moves more items per operation");
        add("tooltip.appliedenergistics2.redstone_card", "Enables redstone control for buses");
        add("tooltip.appliedenergistics2.fuzzy_card", "Ignores damage and NBT when filtering");
        add("tooltip.appliedenergistics2.encoded_pattern.output", "Output: %s");
        add("tooltip.appliedenergistics2.encoded_pattern.inputs", "Inputs: %s");
        add("tooltip.appliedenergistics2.encoded_pattern.mode.crafting", "Crafting Pattern");
        add("tooltip.appliedenergistics2.encoded_pattern.mode.processing", "Processing Pattern");
        add("tooltip.appliedenergistics2.processing_pattern_job", "Processing Pattern Job");
        add("message.appliedenergistics2.pattern_encoding.success", "Pattern encoded: %s");
        add("message.appliedenergistics2.pattern_encoding.internal", "Unable to encode pattern (internal error).");
        add("message.appliedenergistics2.pattern_encoding.output_occupied", "Remove the existing encoded pattern first.");
        add("message.appliedenergistics2.pattern_encoding.need_blank_pattern", "Insert a blank pattern to encode.");
        add("message.appliedenergistics2.pattern_encoding.no_ingredients", "Place a recipe in the crafting grid.");
        add("message.appliedenergistics2.pattern_encoding.invalid_recipe", "No valid crafting recipe found.");
        add("message.appliedenergistics2.pattern_encoding.no_processing_outputs", "Specify at least one output item.");
        add("integration.appliedenergistics2.processing_stub", "External Machine Integration (stub)");
        add("message.appliedenergistics2.processing_job.external_attempt",
                "Attempting to route processing job %s to external machinery.");
        add("message.appliedenergistics2.processing_job.external_fallback",
                "External routing unavailable for processing job %s. Using assembler.");
        add("message.appliedenergistics2.processing_job.distributed_start",
                "Distributed execution evaluation started for %s.");
        add("message.appliedenergistics2.processing_job.executor_at_capacity",
                "Executor %s (%s) is at capacity (%s/%s jobs).");
        add("message.appliedenergistics2.processing_job.executor_offline_fallback",
                "Executor %2$s (type %3$s) offline while handling job %1$s; falling back.");
        add("message.appliedenergistics2.processing_job.job_scheduled_on_executor",
                "Job %s scheduled on executor %s (type %s, load %s/%s).");
        add("message.appliedenergistics2.processing_job.high_priority_scheduled",
                "High priority job %s scheduled on %s (type %s).");
        add("message.appliedenergistics2.processing_job.executor_failed_reroute",
                "Executor %2$s (type %3$s) failed to complete job %1$s; rerouting.");
        add("message.appliedenergistics2.processing_job.external_furnace_started",
                "External Furnace Execution Started: %s");
        add("message.appliedenergistics2.processing_job.external_furnace_complete",
                "External Furnace Execution Complete: %s");
        add("message.appliedenergistics2.processing_job.external_furnace_failed",
                "External Furnace Execution Failed: %s");
        add("message.appliedenergistics2.processing_job.external_blast_furnace_started",
                "External Blast Furnace Execution Started: %s");
        add("message.appliedenergistics2.processing_job.external_blast_furnace_complete",
                "External Blast Furnace Execution Complete: %s");
        add("message.appliedenergistics2.processing_job.external_blast_furnace_failed",
                "External Blast Furnace Execution Failed: %s");
        add("message.appliedenergistics2.processing_job.external_brewing_started",
                "External Brewing Execution Started: %s");
        add("message.appliedenergistics2.processing_job.external_brewing_complete",
                "External Brewing Execution Complete: %s");
        add("message.appliedenergistics2.processing_job.external_brewing_failed",
                "External Brewing Execution Failed: %s");
        add("message.appliedenergistics2.processing_job.executor_queue_depth",
                "Executor %s queue depth updated: %s tasks waiting.");
        add("message.appliedenergistics2.processing_job.executor_resumed",
                "Executor %s resumed processing job %s after maintenance.");
        add("message.appliedenergistics2.processing_job.offline_buffer_hint",
                "Network offline. Buffered job count: %s.");
        add("message.appliedenergistics2.crafting_job.spawning_sub_job", "Creating sub-job %2$s for %1$s.");
        add("message.appliedenergistics2.crafting_job.sub_job_spawning", "Spawning sub-job: %s");
        add("message.appliedenergistics2.crafting_job.sub_job_running", "Sub-job running: %s");
        add("message.appliedenergistics2.crafting_job.sub_job_completed", "Sub-job completed: %s");
        add("message.appliedenergistics2.crafting_job.sub_job_failed", "Sub-job failed: %s");
        add("message.appliedenergistics2.crafting_job.sub_job_reserved", "Reserved sub-job %2$s for %1$s.");
        add("message.appliedenergistics2.crafting_job.sub_job_cycle",
                "Unable to create sub-job for %s due to dependency cycle.");
        add("message.appliedenergistics2.crafting_job.dependency_failed",
                "Crafting job %s failed due to missing dependency.");
        add("message.appliedenergistics2.crafting_job.unknown_output", "Unknown output");
        add("log.appliedenergistics2.interop.success", "Interop ready: %s");
        add("log.appliedenergistics2.interop.skipped", "Interop skipped (mod missing): %s");
        add("log.appliedenergistics2.interop.failed", "Interop failed: %s (%s)");
        add("gui.appliedenergistics2.pattern_encoding.mode.crafting", "Crafting Pattern");
        add("gui.appliedenergistics2.pattern_encoding.mode.processing", "Processing Pattern");
        add("gui.appliedenergistics2.pattern_encoding.mode_toggle_hint", "Click to switch pattern type.");
        add("gui.appliedenergistics2.crafting_job.dependencies", "Dependencies");
        add("item.appliedenergistics2.inscriber_silicon_press", "Inscriber Silicon Press");
        add("item.appliedenergistics2.inscriber_logic_press", "Inscriber Logic Press");
        add("item.appliedenergistics2.inscriber_engineering_press", "Inscriber Engineering Press");
        add("item.appliedenergistics2.inscriber_calculation_press", "Inscriber Calculation Press");
        add("item.appliedenergistics2.inscriber_press", "Inscriber Press");
        add("item.appliedenergistics2.cable", "Cable");
        add("item.appliedenergistics2.basic_cell_1k", "1k Storage Cell");
        add("item.appliedenergistics2.basic_cell_4k", "4k Storage Cell");
        add("item.appliedenergistics2.basic_cell_16k", "16k Storage Cell");
        add("item.appliedenergistics2.basic_cell_64k", "64k Storage Cell");
        add("item.appliedenergistics2.partitioned_cell", "Partitioned Storage Cell");
        add("item.appliedenergistics2.fluid_storage_cell_1k", "1k Fluid Storage Cell");
        add("item.appliedenergistics2.fluid_storage_cell_4k", "4k Fluid Storage Cell");
        add("item.appliedenergistics2.fluid_storage_cell_16k", "16k Fluid Storage Cell");
        add("item.appliedenergistics2.fluid_storage_cell_64k", "64k Fluid Storage Cell");

        add("tooltip.ae2.cell.used", "Used: %s / %s");
        add("tooltip.ae2.cell.types", "Types: %s / %s");
        add("tooltip.ae2.cell.priority", "Priority: %s");
        add("gui.appliedenergistics2.terminal.search", "Search Items");
        add("gui.appliedenergistics2.terminal.search_hint", "Type to filter stored items");
        add("gui.appliedenergistics2.terminal.offline_search_hint", "Network offline – search limited to cached view");
        add("gui.ae2.ClearWhitelist", "Clear Whitelist");
        add("ae2.hint.search", "Search…");
        add("ae2.partitioned_cell.mode.whitelist", "Whitelist");
        add("ae2.partitioned_cell.mode.blacklist", "Blacklist");
        add("ae2.partitioned_cell.mode.tooltip",
                "Controls whether the filter allows (whitelist) or blocks (blacklist) matching items.");
        add("ae2.partitioned_cell.priority.label", "Priority");
        add("ae2.partitioned_cell.priority.tooltip",
                "Higher numbers win when multiple storages can accept the same item.");
        add("ae2.partitioned_cell.title", "Partitioned Cell");
        add("gui.appliedenergistics2.offline.power", "Network offline – missing power");
        add("gui.appliedenergistics2.offline.channels", "Network offline – channel capacity exceeded");
        add("gui.appliedenergistics2.offline.redstone", "Network offline – redstone disabled");
        add("gui.appliedenergistics2.redstone.button", "Redstone Mode");
        add("gui.appliedenergistics2.redstone.mode.active_with_signal", "Active with signal");
        add("gui.appliedenergistics2.redstone.mode.active_without_signal", "Active without signal");
        add("gui.appliedenergistics2.redstone.mode.always_active", "Always active");
        add("gui.appliedenergistics2.redstone.mode.short.active_with_signal", "With Signal");
        add("gui.appliedenergistics2.redstone.mode.short.active_without_signal", "Without Signal");
        add("gui.appliedenergistics2.redstone.mode.short.always_active", "Always");
    }
}
