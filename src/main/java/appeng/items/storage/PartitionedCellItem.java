package appeng.items.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.items.contents.PartitionedCellMenuHost;
import appeng.menu.MenuOpener;
import appeng.menu.PartitionedCellMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;

/**
 * A basic 1k storage cell that restricts which items it can accept via a whitelist stored in NBT.
 */
public class PartitionedCellItem extends BasicCellItem implements IMenuItem {
    private static final int CAPACITY = 1024;
    private static final String CELL_TAG = "CellData";
    private static final String WHITELIST_TAG = "Whitelist";
    private static final String PRIORITY_TAG = "Priority";

    public PartitionedCellItem(Properties properties) {
        super(properties, CAPACITY);
    }

    @Override
    public List<ResourceLocation> getWhitelist(ItemStack cellItem) {
        var whitelistTag = getWhitelistTag(cellItem, false);
        if (whitelistTag == null || whitelistTag.isEmpty()) {
            return List.of();
        }

        List<ResourceLocation> whitelist = new ArrayList<>(whitelistTag.size());
        for (int i = 0; i < whitelistTag.size(); i++) {
            var id = ResourceLocation.tryParse(whitelistTag.getString(i));
            if (id != null) {
                whitelist.add(id);
            }
        }
        return whitelist;
    }

    public void setWhitelist(ItemStack cellItem, Collection<ResourceLocation> whitelist) {
        if (whitelist.isEmpty()) {
            clearWhitelist(cellItem);
            return;
        }

        var tag = cellItem.getOrCreateTag();
        CompoundTag cellTag;
        if (tag.contains(CELL_TAG, Tag.TAG_COMPOUND)) {
            cellTag = tag.getCompound(CELL_TAG);
        } else {
            cellTag = new CompoundTag();
            tag.put(CELL_TAG, cellTag);
        }

        ListTag whitelistTag = new ListTag();
        for (var id : whitelist) {
            whitelistTag.add(StringTag.valueOf(id.toString()));
        }
        cellTag.put(WHITELIST_TAG, whitelistTag);
    }

    public void clearWhitelist(ItemStack cellItem) {
        var tag = cellItem.getTag();
        if (tag == null || !tag.contains(CELL_TAG, Tag.TAG_COMPOUND)) {
            return;
        }
        var cellTag = tag.getCompound(CELL_TAG);
        cellTag.remove(WHITELIST_TAG);
        if (cellTag.contains(PRIORITY_TAG, Tag.TAG_INT) && cellTag.getInt(PRIORITY_TAG) == 0) {
            cellTag.remove(PRIORITY_TAG);
        }
        if (cellTag.isEmpty()) {
            tag.remove(CELL_TAG);
        }
        if (tag.isEmpty()) {
            cellItem.setTag(null);
        }
    }

    @Override
    public int getPriority(ItemStack cellItem) {
        var tag = cellItem.getTag();
        if (tag == null || !tag.contains(CELL_TAG, Tag.TAG_COMPOUND)) {
            return 0;
        }

        var cellTag = tag.getCompound(CELL_TAG);
        if (!cellTag.contains(PRIORITY_TAG, Tag.TAG_INT)) {
            return 0;
        }

        return cellTag.getInt(PRIORITY_TAG);
    }

    @Override
    public void setPriority(ItemStack cellItem, int priority) {
        var tag = cellItem.getTag();
        if (priority == 0) {
            if (tag == null || !tag.contains(CELL_TAG, Tag.TAG_COMPOUND)) {
                return;
            }
            var cellTag = tag.getCompound(CELL_TAG);
            cellTag.remove(PRIORITY_TAG);
            if (cellTag.isEmpty()) {
                tag.remove(CELL_TAG);
            }
            if (tag.isEmpty()) {
                cellItem.setTag(null);
            }
            return;
        }

        if (tag == null) {
            tag = new CompoundTag();
            cellItem.setTag(tag);
        }

        CompoundTag cellTag;
        if (tag.contains(CELL_TAG, Tag.TAG_COMPOUND)) {
            cellTag = tag.getCompound(CELL_TAG);
        } else {
            cellTag = new CompoundTag();
            tag.put(CELL_TAG, cellTag);
        }

        cellTag.putInt(PRIORITY_TAG, priority);
    }

    private static ListTag getWhitelistTag(ItemStack cellItem, boolean create) {
        CompoundTag tag = cellItem.getTag();
        if (tag == null) {
            if (!create) {
                return null;
            }
            tag = new CompoundTag();
            cellItem.setTag(tag);
        }

        CompoundTag cellTag;
        if (tag.contains(CELL_TAG, Tag.TAG_COMPOUND)) {
            cellTag = tag.getCompound(CELL_TAG);
        } else {
            if (!create) {
                return null;
            }
            cellTag = new CompoundTag();
            tag.put(CELL_TAG, cellTag);
        }

        if (cellTag.contains(WHITELIST_TAG, Tag.TAG_LIST)) {
            return cellTag.getList(WHITELIST_TAG, Tag.TAG_STRING);
        }

        if (!create) {
            return null;
        }

        ListTag whitelistTag = new ListTag();
        cellTag.put(WHITELIST_TAG, whitelistTag);
        return whitelistTag;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            MenuOpener.open(PartitionedCellMenu.TYPE, player, MenuLocators.forHand(player, hand));
        }
        player.swing(hand);
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()), stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        if (player != null && !level.isClientSide()) {
            MenuOpener.open(PartitionedCellMenu.TYPE, player, MenuLocators.forItemUseContext(context));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public ItemMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator, @Nullable BlockHitResult hitResult) {
        return new PartitionedCellMenuHost(this, player, locator);
    }
}
