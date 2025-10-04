package appeng.storage.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import appeng.api.storage.IItemStorageChannel;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class StorageService implements IStorageService {
    private final Map<Class<?>, IStorageChannel<?>> channels = new HashMap<>();

    public StorageService() {
        channels.put(ItemStack.class, new ItemStorageChannel());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> IStorageChannel<T> getChannel(Class<T> type) {
        return (IStorageChannel<T>) channels.get(type);
    }

    @Override
    public List<IStorageChannel<?>> getAllChannels() {
        return List.copyOf(channels.values());
    }

    public IItemStorageChannel getItemChannel() {
        return (IItemStorageChannel) channels.get(ItemStack.class);
    }

    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        if (getItemChannel() instanceof ItemStorageChannel impl) {
            tag.put("ItemChannel", impl.saveNBT());
        }
        return tag;
    }

    public void loadNBT(CompoundTag tag) {
        if (tag.contains("ItemChannel")) {
            if (getItemChannel() instanceof ItemStorageChannel impl) {
                impl.loadNBT(tag.getCompound("ItemChannel"));
            }
        }
    }
}
