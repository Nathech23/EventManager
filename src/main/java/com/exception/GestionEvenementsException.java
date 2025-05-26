package com.exception;

public abstract class GestionEvenementsException extends Exception {

    private final String codeErreur;
    private final long timestamp;

    protected GestionEvenementsException(String message, String codeErreur) {
        super(message);
        this.codeErreur = codeErreur;
        this.timestamp = System.currentTimeMillis();
    }

    protected GestionEvenementsException(String message, String codeErreur, Throwable cause) {
        super(message, cause);
        this.codeErreur = codeErreur;
        this.timestamp = System.currentTimeMillis();
    }

    public String getCodeErreur() { return codeErreur; }
    public long getTimestamp() { return timestamp; }

    /**
     * Retourne un message d'erreur formaté pour l'utilisateur
     */
    public String getMessageUtilisateur() {
        return String.format("[%s] %s", codeErreur, getMessage());
    }

    /**
     * Retourne un message d'erreur détaillé pour les logs
     */
    public String getMessageDetaille() {
        return String.format("[%s] %s (Timestamp: %d)", codeErreur, getMessage(), timestamp);
    }
}
