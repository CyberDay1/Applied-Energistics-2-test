//? if (>=1.21.4) {
package appeng.loot;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

/**
 * Injects inscriber presses into mysterious cube drops using NeoForge's loot modifier system.
 */
public class AE2PressLootModifier extends LootModifier {
    public static final MapCodec<AE2PressLootModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> codecStart(instance).apply(instance, AE2PressLootModifier::new));

    private static final List<ItemStack> PRESS_LOOT = List.of(
            AEItems.LOGIC_PROCESSOR_PRESS.stack(),
            AEItems.CALCULATION_PROCESSOR_PRESS.stack(),
            AEItems.ENGINEERING_PROCESSOR_PRESS.stack(),
            AEItems.SILICON_PRESS.stack(),
            AEItems.NAME_PRESS.stack());

    private static final ResourceLocation MYSTERIOUS_CUBE_LOOT = AppEng.makeId("blocks/mysterious_cube");

    protected AE2PressLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!AEConfig.instance().isSpawnPressesInMeteoritesEnabled()) {
            return generatedLoot;
        }

        var blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (blockState != null && blockState.getBlock() == AEBlocks.MYSTERIOUS_CUBE.block()) {
            addPressDrop(generatedLoot, context);
            return generatedLoot;
        }

        var lootTableId = context.getQueriedLootTableId();
        if (MYSTERIOUS_CUBE_LOOT.equals(lootTableId)) {
            addPressDrop(generatedLoot, context);
        }

        return generatedLoot;
    }

    private static void addPressDrop(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        var random = context.getRandom();
        var press = PRESS_LOOT.get(random.nextInt(PRESS_LOOT.size())).copy();
        generatedLoot.add(press);
    }
}
//? endif
