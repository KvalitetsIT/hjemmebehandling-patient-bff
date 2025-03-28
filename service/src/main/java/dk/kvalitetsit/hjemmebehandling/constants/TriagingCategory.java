package dk.kvalitetsit.hjemmebehandling.constants;

public enum TriagingCategory {
    RED(1), YELLOW(2), GREEN(3);

    private final int priority;

    TriagingCategory(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
