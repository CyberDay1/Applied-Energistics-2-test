package appeng.items.storage;

public class BasicCell64kItem extends BasicCellItem {
    private static final int CAPACITY = 65_536;

    public BasicCell64kItem(Properties properties) {
        super(properties, CAPACITY);
    }
}
