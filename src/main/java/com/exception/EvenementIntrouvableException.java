package com.exception;

public class EvenementIntrouvableException extends GestionEvenementsException {

    private final String evenementId;
    private final String critereRecherche;

   /** public EvenementIntrouvableException(String message) {
        super(message, "EVENEMENT_INTROUVABLE");
        this.evenementId = null;
        this.critereRecherche = null;
    }*/

    public EvenementIntrouvableException(String evenementId) {
        super(String.format("Aucun événement trouvé avec l'ID '%s'", evenementId),
                "EVENEMENT_INTROUVABLE");
        this.evenementId = evenementId;
        this.critereRecherche = "ID: " + evenementId;
    }

    public EvenementIntrouvableException(String message, String critereRecherche) {
        super(message, "EVENEMENT_INTROUVABLE");
        this.evenementId = null;
        this.critereRecherche = critereRecherche;
    }

    public EvenementIntrouvableException(String message, Throwable cause) {
        super(message, "EVENEMENT_INTROUVABLE", cause);
        this.evenementId = null;
        this.critereRecherche = null;
    }

    // Getters
    public String getEvenementId() { return evenementId; }
    public String getCritereRecherche() { return critereRecherche; }

    @Override
    public String getMessageUtilisateur() {
        if (critereRecherche != null) {
            return String.format("Aucun événement trouvé avec le critère : %s", critereRecherche);
        }
        return super.getMessageUtilisateur();
    }
}