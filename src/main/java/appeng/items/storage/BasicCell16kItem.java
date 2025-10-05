package appeng.items.storage;

public class BasicCell16kItem extends BasicCellItem {
    private static final int CAPACITY = 16_384;

    public BasicCell16kItem(Properties properties) {
        super(properties, CAPACITY);
    }
}
