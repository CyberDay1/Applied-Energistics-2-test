package appeng.grid;

import java.util.UUID;

public record GridId(UUID id) {
    public static GridId random() {
        return new GridId(UUID.randomUUID());
    }
}
