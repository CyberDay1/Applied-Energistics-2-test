package appeng.items.storage.fluid;

public class BasicFluidCell64kItem extends BasicFluidCellItem {
    private static final int CAPACITY = 65536;

    public BasicFluidCell64kItem(Properties properties) {
        super(properties, CAPACITY);
    }
}
