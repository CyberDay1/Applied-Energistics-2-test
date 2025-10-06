package appeng.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import appeng.AE2Registries;

public class AE2LanguageProvider extends LanguageProvider {
    public AE2LanguageProvider(PackOutput output) {
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
        add("block.appliedenergistics2.storage_bus", "ME Storage Bus");
        add("block.appliedenergistics2.import_bus", "ME Import Bus");
        add("block.appliedenergistics2.export_bus", "ME Export Bus");
        add("block.appliedenergistics2.cable", "Cable");
        add("block.appliedenergistics2.meteorite", "Meteorite");
        add("block.appliedenergistics2.meteorite_hint", "Meteorite Hint");

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
        add("message.appliedenergistics2.pattern_encoding.success", "Pattern encoded: %s");
        add("message.appliedenergistics2.pattern_encoding.internal", "Unable to encode pattern (internal error).");
        add("message.appliedenergistics2.pattern_encoding.output_occupied", "Remove the existing encoded pattern first.");
        add("message.appliedenergistics2.pattern_encoding.need_blank_pattern", "Insert a blank pattern to encode.");
        add("message.appliedenergistics2.pattern_encoding.no_ingredients", "Place a recipe in the crafting grid.");
        add("message.appliedenergistics2.pattern_encoding.invalid_recipe", "No valid crafting recipe found.");
        add("message.appliedenergistics2.pattern_encoding.no_processing_outputs", "Specify at least one output item.");
        add("gui.appliedenergistics2.pattern_encoding.mode.crafting", "Crafting Pattern");
        add("gui.appliedenergistics2.pattern_encoding.mode.processing", "Processing Pattern");
        add("gui.appliedenergistics2.pattern_encoding.mode_toggle_hint", "Click to switch pattern type.");
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
    }
}
