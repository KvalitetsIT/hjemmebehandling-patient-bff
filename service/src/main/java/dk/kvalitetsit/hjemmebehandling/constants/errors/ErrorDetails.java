package dk.kvalitetsit.hjemmebehandling.constants.errors;

public enum ErrorDetails {
    NO_ACTIVE_CAREPLAN_EXISTS("Den nuværende bruger har ingen aktiv behandlingsplan", 10),
    ACCESS_VIOLATION("Du har ikke rettigheder til at tilgå de forespurgte data.", 16),
    PARAMETERS_INCOMPLETE("Parametre er mangelfuldt udfyldt.", 17),
    INTERNAL_SERVER_ERROR("Der opstod en intern fejl i systemet.", 99);

    private String errorMessage;
    private int errorCode;

    ErrorDetails(String errorMessage, int errorCode) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
