package appeng.data.providers;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import appeng.core.AppEng;

public final class AELangProvider extends LanguageProvider {
    public AELangProvider(PackOutput output, String locale) {
        super(output, AppEng.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        // add("item." + AppEng.MOD_ID + ".certus_quartz", "Certus Quartz");
    }
}
