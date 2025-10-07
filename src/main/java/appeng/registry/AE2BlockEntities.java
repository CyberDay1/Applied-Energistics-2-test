package appeng.registry;

import appeng.AE2Registries;
import appeng.blockentity.CableBlockEntity;
import appeng.blockentity.ChargerBlockEntity;
import appeng.blockentity.ControllerBlockEntity;
import appeng.blockentity.EnergyAcceptorBlockEntity;
import appeng.blockentity.InscriberBlockEntity;
import appeng.blockentity.io.AnnihilationPlaneBlockEntity;
import appeng.blockentity.simple.DriveBlockEntity;
import appeng.blockentity.spatial.SpatialIOPortBlockEntity;
import appeng.blockentity.terminal.CraftingTerminalBlockEntity;
import appeng.blockentity.crafting.PatternEncodingTerminalBlockEntity;
import appeng.blockentity.terminal.PatternTerminalBlockEntity;
import appeng.blockentity.terminal.TerminalBlockEntity;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.RegistryObject;

public final class AE2BlockEntities {
    public static final RegistryObject<BlockEntityType<InscriberBlockEntity>> INSCRIBER_BE =
        AE2Registries.BLOCK_ENTITIES.register("inscriber",
            () -> BlockEntityType.Builder.of(InscriberBlockEntity::new,
                AE2Blocks.INSCRIBER.get()).build(null));

    public static final RegistryObject<BlockEntityType<ChargerBlockEntity>> CHARGER_BE =
        AE2Registries.BLOCK_ENTITIES.register("charger",
            () -> BlockEntityType.Builder.of(ChargerBlockEntity::new,
                AE2Blocks.CHARGER.get()).build(null));

    public static final RegistryObject<BlockEntityType<DriveBlockEntity>> DRIVE_SIMPLE =
        AE2Registries.BLOCK_ENTITIES.register("drive",
            () -> BlockEntityType.Builder.of(DriveBlockEntity::new,
                AE2Blocks.DRIVE.get()).build(null));

    public static final RegistryObject<BlockEntityType<ControllerBlockEntity>> CONTROLLER =
        AE2Registries.BLOCK_ENTITIES.register("controller",
            () -> BlockEntityType.Builder.of(ControllerBlockEntity::new,
                AE2Blocks.CONTROLLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<EnergyAcceptorBlockEntity>> ENERGY_ACCEPTOR =
        AE2Registries.BLOCK_ENTITIES.register("energy_acceptor",
            () -> BlockEntityType.Builder.of(EnergyAcceptorBlockEntity::new,
                AE2Blocks.ENERGY_ACCEPTOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<CableBlockEntity>> CABLE =
        AE2Registries.BLOCK_ENTITIES.register("cable",
            () -> BlockEntityType.Builder.of(CableBlockEntity::new, AE2Blocks.CABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TerminalBlockEntity>> TERMINAL =
        AE2Registries.BLOCK_ENTITIES.register("terminal",
            () -> BlockEntityType.Builder.of(TerminalBlockEntity::new, AE2Blocks.TERMINAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<CraftingTerminalBlockEntity>> CRAFTING_TERMINAL =
        AE2Registries.BLOCK_ENTITIES.register("crafting_terminal",
            () -> BlockEntityType.Builder.of(CraftingTerminalBlockEntity::new,
                AE2Blocks.CRAFTING_TERMINAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<PatternTerminalBlockEntity>> PATTERN_TERMINAL =
        AE2Registries.BLOCK_ENTITIES.register("pattern_terminal",
            () -> BlockEntityType.Builder.of(PatternTerminalBlockEntity::new,
                AE2Blocks.PATTERN_TERMINAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<PatternEncodingTerminalBlockEntity>> PATTERN_ENCODING_TERMINAL =
        AE2Registries.BLOCK_ENTITIES.register("pattern_encoding_terminal",
            () -> BlockEntityType.Builder.of(PatternEncodingTerminalBlockEntity::new,
                AE2Blocks.PATTERN_ENCODING_TERMINAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<CraftingMonitorBlockEntity>> CRAFTING_MONITOR =
        AE2Registries.BLOCK_ENTITIES.register("crafting_monitor",
            () -> BlockEntityType.Builder.of(CraftingMonitorBlockEntity::new,
                AE2Blocks.CRAFTING_MONITOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<AnnihilationPlaneBlockEntity>> ANNIHILATION_PLANE =
        AE2Registries.BLOCK_ENTITIES.register("annihilation_plane",
            () -> BlockEntityType.Builder.of(AnnihilationPlaneBlockEntity::new,
                AE2Blocks.ANNIHILATION_PLANE.get()).build(null));

    public static final RegistryObject<BlockEntityType<SpatialIOPortBlockEntity>> SPATIAL_IO_PORT =
        AE2Registries.BLOCK_ENTITIES.register("spatial_io_port",
            () -> BlockEntityType.Builder.of(SpatialIOPortBlockEntity::new,
                AE2Blocks.SPATIAL_IO_PORT.get()).build(null));

    private AE2BlockEntities() {}
}
