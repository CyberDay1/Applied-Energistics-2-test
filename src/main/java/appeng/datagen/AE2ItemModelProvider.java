package appeng.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import appeng.AE2Registries;
import appeng.registry.AE2Items;

public class AE2ItemModelProvider extends ItemModelProvider {
    public AE2ItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, AE2Registries.MODID, helper);
    }

    @Override
    protected void registerModels() {
        withExistingParent(AE2Items.INSCRIBER.getId().getPath(), modLoc("block/inscriber"));
        withExistingParent(AE2Items.CHARGER.getId().getPath(), modLoc("block/charger"));
        withExistingParent(AE2Items.CERTUS_QUARTZ_ORE.getId().getPath(), modLoc("block/certus_quartz_ore"));
        withExistingParent(AE2Items.SKY_STONE.getId().getPath(), modLoc("block/sky_stone"));
        withExistingParent(AE2Items.CONTROLLER.getId().getPath(), modLoc("block/controller"));
        withExistingParent(AE2Items.ENERGY_ACCEPTOR.getId().getPath(), modLoc("block/energy_acceptor"));
        withExistingParent(AE2Items.CABLE.getId().getPath(), modLoc("block/cable"));
        withExistingParent(AE2Items.CRAFTING_MONITOR.getId().getPath(), modLoc("block/crafting_monitor"));
        withExistingParent(AE2Items.DRIVE.getId().getPath(), modLoc("block/drive"));
        withExistingParent(AE2Items.TERMINAL.getId().getPath(), modLoc("block/terminal"));
        withExistingParent(AE2Items.CRAFTING_TERMINAL.getId().getPath(), modLoc("block/crafting_terminal"));
        withExistingParent(AE2Items.PATTERN_TERMINAL.getId().getPath(), modLoc("block/pattern_terminal"));
        withExistingParent(AE2Items.PATTERN_ENCODING_TERMINAL_BLOCK.getId().getPath(),
                modLoc("block/pattern_encoding_terminal"));

        basicItem(AE2Items.SILICON.get());
        basicItem(AE2Items.CERTUS_QUARTZ_CRYSTAL.get());
        basicItem(AE2Items.CHARGED_CERTUS_QUARTZ_CRYSTAL.get());
        basicItem(AE2Items.PRINTED_SILICON.get());
        basicItem(AE2Items.PRINTED_LOGIC_PROCESSOR.get());
        basicItem(AE2Items.PRINTED_ENGINEERING_PROCESSOR.get());
        basicItem(AE2Items.PRINTED_CALCULATION_PROCESSOR.get());
        basicItem(AE2Items.LOGIC_PROCESSOR.get());
        basicItem(AE2Items.ENGINEERING_PROCESSOR.get());
        basicItem(AE2Items.CALCULATION_PROCESSOR.get());
        basicItem(AE2Items.INSCRIBER_SILICON_PRESS.get());
        basicItem(AE2Items.INSCRIBER_LOGIC_PRESS.get());
        basicItem(AE2Items.INSCRIBER_ENGINEERING_PRESS.get());
        basicItem(AE2Items.INSCRIBER_CALCULATION_PRESS.get());
        basicItem(AE2Items.CAPACITY_CARD.get());
        basicItem(AE2Items.SPEED_CARD.get());
        basicItem(AE2Items.REDSTONE_CARD.get());
        basicItem(AE2Items.FUZZY_CARD.get());
        basicItem(AE2Items.BASIC_CELL_1K.get());
        basicItem(AE2Items.BASIC_CELL_4K.get());
        basicItem(AE2Items.BASIC_CELL_16K.get());
        basicItem(AE2Items.BASIC_CELL_64K.get());
        basicItem(AE2Items.PARTITIONED_CELL.get());
        basicItem(AE2Items.BLANK_PATTERN.get());
        basicItem(AE2Items.ENCODED_PATTERN.get());
    }
}
