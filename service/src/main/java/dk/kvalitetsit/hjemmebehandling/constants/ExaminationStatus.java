package dk.kvalitetsit.hjemmebehandling.constants;

public enum ExaminationStatus {
    NOT_EXAMINED(2), UNDER_EXAMINATION(1), EXAMINED(3);

    private final int priority;

    ExaminationStatus(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
