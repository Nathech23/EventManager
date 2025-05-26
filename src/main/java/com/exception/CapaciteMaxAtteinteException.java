package com.exception;

public class CapaciteMaxAtteinteException extends GestionEvenementsException {

    private final int capaciteMax;
    private final int capaciteActuelle;
    private final String evenementId;
    private final String evenementNom;

    public CapaciteMaxAtteinteException(String message) {
        super(message, "CAPACITE_MAX");
        this.capaciteMax = -1;
        this.capaciteActuelle = -1;
        this.evenementId = null;
        this.evenementNom = null;
    }

    public CapaciteMaxAtteinteException(String evenementId, String evenementNom,
                                        int capaciteMax, int capaciteActuelle) {
        super(String.format("Capacité maximale atteinte pour l'événement '%s'", evenementNom),
                "CAPACITE_MAX");
        this.evenementId = evenementId;
        this.evenementNom = evenementNom;
        this.capaciteMax = capaciteMax;
        this.capaciteActuelle = capaciteActuelle;
    }

    public CapaciteMaxAtteinteException(String message, Throwable cause) {
        super(message, "CAPACITE_MAX", cause);
        this.capaciteMax = -1;
        this.capaciteActuelle = -1;
        this.evenementId = null;
        this.evenementNom = null;
    }

    // Getters
    public int getCapaciteMax() { return capaciteMax; }
    public int getCapaciteActuelle() { return capaciteActuelle; }
    public String getEvenementId() { return evenementId; }
    public String getEvenementNom() { return evenementNom; }

    @Override
    public String getMessageUtilisateur() {
        if (evenementNom != null && capaciteMax > 0) {
            return String.format("L'événement '%s' est complet (%d/%d places occupées)",
                    evenementNom, capaciteActuelle, capaciteMax);
        }
        return super.getMessageUtilisateur();
    }

    /**
     * Retourne le nombre de places disponibles (peut être 0)
     */
    public int getPlacesDisponibles() {
        return Math.max(0, capaciteMax - capaciteActuelle);
    }

    /**
     * Vérifie s'il y a des places libérées depuis l'exception
     */
    public boolean aDesPlacesDisponibles() {
        return getPlacesDisponibles() > 0;
    }
}
