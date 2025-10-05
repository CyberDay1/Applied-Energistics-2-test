package appeng.items.storage;

public class BasicCell4kItem extends BasicCellItem {
    private static final int CAPACITY = 4096;

    public BasicCell4kItem(Properties properties) {
        super(properties, CAPACITY);
    }
}
