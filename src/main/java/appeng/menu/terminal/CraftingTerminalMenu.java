package appeng.menu.terminal;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.blockentity.terminal.CraftingTerminalBlockEntity;
import appeng.core.AppEng;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.storage.impl.StorageService;

public class CraftingTerminalMenu extends TerminalMenu {
    public static final MenuType<CraftingTerminalMenu> TYPE = MenuTypeBuilder
            .create(CraftingTerminalMenu::new, CraftingTerminalBlockEntity.class)
            .buildUnregistered(AppEng.makeId("crafting_terminal"));

    private static final int GRID_COLUMNS = 3;
    private static final int GRID_ROWS = 3;
    private static final int SLOT_SIZE = 18;
    private static final int GRID_LEFT = 8;
    private static final int GRID_TOP = 28;
    private static final int RESULT_OFFSET_X = GRID_LEFT + GRID_COLUMNS * SLOT_SIZE + 12;
    private static final int RESULT_OFFSET_Y = GRID_TOP + SLOT_SIZE;

    private final CraftingTerminalBlockEntity craftingTerminal;
    private final Container craftingMatrix;
    private final Container resultInventory;
    @Nullable
    private RecipeHolder<CraftingRecipe> currentRecipe;

    public CraftingTerminalMenu(int id, Inventory inv, CraftingTerminalBlockEntity terminal) {
        super(TYPE, id, inv, terminal);
        this.craftingTerminal = terminal;
        this.craftingMatrix = terminal.getCraftingMatrix();
        this.resultInventory = terminal.getResultInventory();

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLUMNS; col++) {
                int slotIndex = row * GRID_COLUMNS + col;
                int x = GRID_LEFT + col * SLOT_SIZE;
                int y = GRID_TOP + row * SLOT_SIZE;
                Slot slot = new Slot(craftingMatrix, slotIndex, x, y);
                addSlot(slot, SlotSemantics.MACHINE_CRAFTING_GRID);
            }
        }

        Slot resultSlot = new Slot(resultInventory, 0, RESULT_OFFSET_X, RESULT_OFFSET_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                craftResult(player, stack);
                super.onTake(player, stack);
            }
        };
        addSlot(resultSlot, SlotSemantics.CRAFTING_RESULT);

        if (!inv.player.level().isClientSide()) {
            updateCraftingResult();
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == craftingMatrix && !getPlayer().level().isClientSide()) {
            updateCraftingResult();
        }
    }

    @Override
    protected int transferStackToMenu(ItemStack input) {
        if (isGridOnline()) {
            var node = craftingTerminal.getGridNode();
            var gridId = node != null ? node.getGridId() : null;
            int inserted = StorageService.insertIntoNetwork(gridId, input.getItem(), input.getCount(), false);
            if (inserted > 0) {
                return inserted;
            }
        }
        return super.transferStackToMenu(input);
    }

    private void updateCraftingResult() {
        Level level = getPlayer().level();
        if (level == null) {
            return;
        }

        currentRecipe = null;
        ItemStack result = ItemStack.EMPTY;

        List<ItemStack> contents = new ArrayList<>(GRID_ROWS * GRID_COLUMNS);
        for (int i = 0; i < GRID_ROWS * GRID_COLUMNS; i++) {
            contents.add(craftingMatrix.getItem(i).copy());
        }

        CraftingInput input = CraftingInput.of(GRID_COLUMNS, GRID_ROWS, contents);
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        if (recipe.isPresent()) {
            RecipeHolder<CraftingRecipe> holder = recipe.get();
            ItemStack assembled = holder.value().assemble(input, level.registryAccess());
            if (!assembled.isEmpty()) {
                result = assembled;
                currentRecipe = holder;
            }
        }

        resultInventory.setItem(0, result);
        broadcastChanges();
    }

    private void craftResult(Player player, ItemStack stack) {
        Level level = player.level();
        if (level.isClientSide()) {
            return;
        }
        if (currentRecipe == null) {
            updateCraftingResult();
            return;
        }

        CraftingInput input = CraftingInput.of(GRID_COLUMNS, GRID_ROWS, getMatrixSnapshot());
        RecipeHolder<CraftingRecipe> holder = currentRecipe;
        if (!holder.value().matches(input, level)) {
            updateCraftingResult();
            return;
        }

        RegistryAccess registries = level.registryAccess();
        ItemStack output = holder.value().assemble(input, registries);
        if (output.isEmpty()) {
            updateCraftingResult();
            return;
        }

        NonNullList<ItemStack> remaining = holder.value().getRemainingItems(input);
        for (int i = 0; i < remaining.size(); i++) {
            ItemStack slotStack = craftingMatrix.getItem(i);
            if (!slotStack.isEmpty()) {
                craftingMatrix.removeItem(i, 1);
                slotStack = craftingMatrix.getItem(i);
            }

            ItemStack remainder = remaining.get(i);
            if (!remainder.isEmpty()) {
                if (slotStack.isEmpty()) {
                    craftingMatrix.setItem(i, remainder);
                } else if (getPlayer() instanceof ServerPlayer serverPlayer) {
                    if (!serverPlayer.getInventory().add(remainder)) {
                        serverPlayer.drop(remainder, false);
                    }
                }
            }
        }

        stack.onCraftedBy(level, player, stack.getCount());
        resultInventory.setItem(0, ItemStack.EMPTY);
        updateCraftingResult();
    }

    private List<ItemStack> getMatrixSnapshot() {
        List<ItemStack> contents = new ArrayList<>(GRID_ROWS * GRID_COLUMNS);
        for (int i = 0; i < GRID_ROWS * GRID_COLUMNS; i++) {
            contents.add(craftingMatrix.getItem(i).copy());
        }
        return contents;
    }
}
