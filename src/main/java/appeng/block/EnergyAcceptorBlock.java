package appeng.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class EnergyAcceptorBlock extends Block {
    public EnergyAcceptorBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GRAY)
            .strength(4.0f, 5.0f));
    }
}
