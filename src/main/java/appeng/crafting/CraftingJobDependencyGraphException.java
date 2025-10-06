package appeng.crafting;

/**
 * Indicates an invalid dependency graph operation, such as introducing a cycle.
 */
public class CraftingJobDependencyGraphException extends RuntimeException {
    public CraftingJobDependencyGraphException(String message) {
        super(message);
    }
}
