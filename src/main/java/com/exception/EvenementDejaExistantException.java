package com.exception;

public class EvenementDejaExistantException extends GestionEvenementsException {

    private final String evenementId;
    private final String evenementNomExistant;
    private final String evenementNomTentative;

    public EvenementDejaExistantException(String message, String id) {
        super(message, "EVENEMENT_EXISTANT");
        this.evenementId = null;
        this.evenementNomExistant = null;
        this.evenementNomTentative = null;
    }

    public EvenementDejaExistantException(String evenementId, String evenementNomExistant, String evenementNomTentative) {
        super(String.format("Un événement avec l'ID '%s' existe déjà", evenementId),
                "EVENEMENT_EXISTANT");
        this.evenementId = evenementId;
        this.evenementNomExistant = evenementNomExistant;
        this.evenementNomTentative = evenementNomTentative;
    }

    public EvenementDejaExistantException(String message, Throwable cause) {
        super(message, "EVENEMENT_EXISTANT", cause);
        this.evenementId = null;
        this.evenementNomExistant = null;
        this.evenementNomTentative = null;
    }

    // Getters
    public String getEvenementId() { return evenementId; }
    public String getEvenementNomExistant() { return evenementNomExistant; }
    public String getEvenementNomTentative() { return evenementNomTentative; }

    @Override
    public String getMessageUtilisateur() {
        if (evenementId != null && evenementNomExistant != null) {
            return String.format("L'ID '%s' est déjà utilisé par l'événement '%s'",
                    evenementId, evenementNomExistant);
        }
        return super.getMessageUtilisateur();
    }

    /**
     * Suggère un nouvel ID disponible
     */
    public String suggererNouvelId() {
        if (evenementId != null) {
            return evenementId + "_" + System.currentTimeMillis();
        }
        return "EVT_" + System.currentTimeMillis();
    }
}
