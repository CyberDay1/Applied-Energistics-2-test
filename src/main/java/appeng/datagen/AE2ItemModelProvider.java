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
    }
}
