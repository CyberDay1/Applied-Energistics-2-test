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
        add("item.appliedenergistics2.fluid_storage_cell_1k", "1k Fluid Storage Cell");
        add("item.appliedenergistics2.fluid_storage_cell_4k", "4k Fluid Storage Cell");
        add("item.appliedenergistics2.fluid_storage_cell_16k", "16k Fluid Storage Cell");
        add("item.appliedenergistics2.fluid_storage_cell_64k", "64k Fluid Storage Cell");
    }
}
