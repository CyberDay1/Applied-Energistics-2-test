package appeng.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.RegistryObject;

import appeng.core.AppEng;
import appeng.registry.AE2Registries;

public final class AppEngSounds {
    private AppEngSounds() {
    }

    public static final ResourceLocation GUIDE_CLICK_ID = AppEng.makeId("guide.click");
    public static final RegistryObject<SoundEvent> GUIDE_CLICK_EVENT = AE2Registries.SOUNDS.register("guide.click",
            () -> SoundEvent.createVariableRangeEvent(GUIDE_CLICK_ID));
}
