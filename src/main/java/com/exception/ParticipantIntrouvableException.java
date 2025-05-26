package com.exception;

public class ParticipantIntrouvableException extends GestionEvenementsException {

    private final String participantId;
    private final String critereRecherche;

    /**public ParticipantIntrouvableException(String message) {
        super(message, "PARTICIPANT_INTROUVABLE");
        this.participantId = null;
        this.critereRecherche = null;
    }*/

    public ParticipantIntrouvableException(String participantId) {
        super(String.format("Aucun participant trouvé avec l'ID '%s'", participantId),
                "PARTICIPANT_INTROUVABLE");
        this.participantId = participantId;
        this.critereRecherche = "ID: " + participantId;
    }

    public ParticipantIntrouvableException(String message, String critereRecherche) {
        super(message, "PARTICIPANT_INTROUVABLE");
        this.participantId = null;
        this.critereRecherche = critereRecherche;
    }

    public ParticipantIntrouvableException(String message, Throwable cause) {
        super(message, "PARTICIPANT_INTROUVABLE", cause);
        this.participantId = null;
        this.critereRecherche = null;
    }

    // Getters
    public String getParticipantId() { return participantId; }
    public String getCritereRecherche() { return critereRecherche; }

    @Override
    public String getMessageUtilisateur() {
        if (critereRecherche != null) {
            return String.format("Aucun participant trouvé avec le critère : %s", critereRecherche);
        }
        return super.getMessageUtilisateur();
    }
}