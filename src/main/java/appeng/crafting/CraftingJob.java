package appeng.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.ItemStackView;
import appeng.items.patterns.EncodedPatternItem;

/**
 * Represents a planned crafting job. This is a lightweight description that only tracks the inputs and outputs of the
 * job for now. Actual execution is handled in later phases.
 */
public final class CraftingJob {
    public static final int DEFAULT_TICKS_REQUIRED = 200;

    public enum State {
        PLANNED,
        RESERVED,
        RUNNING,
        COMPLETE
    }
    private static final String INPUTS_TAG = "Inputs";
    private static final String OUTPUTS_TAG = "Outputs";
    private static final String ITEM_TAG = "item";
    private static final String LEGACY_ITEM_TAG = "id";
    private static final String COUNT_TAG = "count";
    private static final String LEGACY_COUNT_TAG = "Count";

    private final UUID id;
    private final List<ItemStackView> inputs;
    private final List<ItemStackView> outputs;
    private final boolean simulated;
    private final boolean processing;
    private final ItemStack patternStack;

    private State state;
    private int ticksCompleted;
    private int ticksRequired;
    private int insertedOutputs;
    private int droppedOutputs;

    private CraftingJob(UUID id, List<ItemStackView> inputs, List<ItemStackView> outputs, boolean simulated,
            boolean processing, ItemStack patternStack) {
        this.id = id;
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.simulated = simulated;
        this.processing = processing;
        this.patternStack = patternStack;
        this.state = State.PLANNED;
        this.ticksCompleted = 0;
        this.ticksRequired = DEFAULT_TICKS_REQUIRED;
        this.insertedOutputs = 0;
        this.droppedOutputs = 0;
    }

    public static CraftingJob fromPattern(ItemStack patternStack) {
        if (!(patternStack.getItem() instanceof EncodedPatternItem)) {
            throw new IllegalArgumentException("Stack is not an encoded pattern");
        }

        CompoundTag tag = patternStack.getTag();
        List<ItemStackView> inputs = readItemViews(tag, INPUTS_TAG);
        List<ItemStackView> outputs = readItemViews(tag, OUTPUTS_TAG);

        if (outputs.isEmpty()) {
            outputs = List.of(new ItemStackView(patternStack.getItem(), 1));
        }

        ItemStack storedPattern = patternStack.copy();
        storedPattern.setCount(1);
        boolean processing = false;
        if (patternStack.getItem() instanceof EncodedPatternItem encodedPattern) {
            processing = encodedPattern.isProcessing(patternStack);
        }

        return new CraftingJob(UUID.randomUUID(), inputs, outputs, true, processing, storedPattern);
    }

    public static CraftingJob fromPattern(EncodedPatternItem pattern) {
        return fromPattern(pattern.getDefaultInstance());
    }

    public UUID getId() {
        return id;
    }

    public List<ItemStackView> getInputs() {
        return inputs;
    }

    public List<ItemStackView> getOutputs() {
        return outputs;
    }

    public boolean isSimulated() {
        return simulated;
    }

    public boolean isProcessing() {
        return processing;
    }

    public ItemStack getPatternStack() {
        return patternStack;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getTicksCompleted() {
        return ticksCompleted;
    }

    public void setTicksCompleted(int ticksCompleted) {
        this.ticksCompleted = Math.max(0, ticksCompleted);
    }

    public void advanceTicksCompleted(int amount) {
        if (amount <= 0) {
            return;
        }
        setTicksCompleted(this.ticksCompleted + amount);
    }

    public int getTicksRequired() {
        return ticksRequired;
    }

    public void setTicksRequired(int ticksRequired) {
        this.ticksRequired = Math.max(1, ticksRequired);
    }

    /**
     * Records how many output items were successfully inserted into the ME network and how many had to be dropped in
     * the world once the job completed. Processing pattern executions use this as soon as the assembler pushes their
     * results, while regular crafting jobs update it after the CPU delivers items back into storage.
     */
    public void recordOutputDelivery(int inserted, int dropped) {
        this.insertedOutputs = Math.max(0, inserted);
        this.droppedOutputs = Math.max(0, dropped);
    }

    public int getInsertedOutputs() {
        return insertedOutputs;
    }

    public int getDroppedOutputs() {
        return droppedOutputs;
    }

    /**
     * Returns a string summary of the planned outputs suitable for logging.
     */
    public String describeOutputs() {
        if (outputs.isEmpty()) {
            return "<no outputs>";
        }

        return outputs.stream()
                .map(view -> view.count() + "x " + BuiltInRegistries.ITEM.getKey(view.item()))
                .collect(Collectors.joining(", "));
    }

    private static List<ItemStackView> readItemViews(@Nullable CompoundTag tag, String key) {
        if (tag == null || !tag.contains(key, Tag.TAG_LIST)) {
            return List.of();
        }

        ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
        List<ItemStackView> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            Item item = resolveItem(entry);
            int count = resolveCount(entry);
            if (item != null && count > 0) {
                result.add(new ItemStackView(item, count));
            }
        }
        return result;
    }

    @Nullable
    private static Item resolveItem(CompoundTag entry) {
        String name = entry.contains(ITEM_TAG, Tag.TAG_STRING) ? entry.getString(ITEM_TAG)
                : entry.contains(LEGACY_ITEM_TAG, Tag.TAG_STRING) ? entry.getString(LEGACY_ITEM_TAG) : null;
        if (name == null) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(name);
        if (id == null) {
            return null;
        }
        return BuiltInRegistries.ITEM.getOptional(id).orElse(null);
    }

    private static int resolveCount(CompoundTag entry) {
        if (entry.contains(COUNT_TAG)) {
            return entry.getInt(COUNT_TAG);
        }
        if (entry.contains(LEGACY_COUNT_TAG)) {
            return entry.getInt(LEGACY_COUNT_TAG);
        }
        return 0;
    }
}
