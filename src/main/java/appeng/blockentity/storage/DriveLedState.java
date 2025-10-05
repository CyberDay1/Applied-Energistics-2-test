package appeng.blockentity.storage;

/**
 * Represents the color shown for a storage cell LED in the ME drive bay.
 */
public enum DriveLedState {
    OFF,
    GREEN,
    YELLOW,
    RED,
    BLUE;

    public static DriveLedState fromOrdinal(int ordinal) {
        var states = DriveLedState.values();
        if (ordinal < 0 || ordinal >= states.length) {
            return OFF;
        }
        return states[ordinal];
    }
}
