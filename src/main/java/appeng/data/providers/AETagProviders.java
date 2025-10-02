package appeng.data.providers;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import appeng.core.AppEng;

public final class AETagProviders {
    private AETagProviders() {
    }

    public static final class BlockTags extends BlockTagsProvider {
        public BlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
            super(output, lookup, AppEng.MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            // Example:
            // tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE).add(AEBlocks.QUARTZ_ORE.get());
        }
    }

    public static final class ItemTags extends ItemTagsProvider {
        public ItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, BlockTags blockTags) {
            super(output, lookup, blockTags.contentsGetter(), AppEng.MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            // Mirror block → item tags or add custom item tags.
        }
    }
}
