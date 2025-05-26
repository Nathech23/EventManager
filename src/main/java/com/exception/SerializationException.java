package com.exception;

public class SerializationException extends GestionEvenementsException {

    private final String nomFichier;
    private final String typeOperation; // "SAUVEGARDE" ou "CHARGEMENT"

    public SerializationException(String message, String typeOperation) {
        super(message, "SERIALIZATION_ERROR");
        this.typeOperation = typeOperation;
        this.nomFichier = null;
    }

    public SerializationException(String message, String typeOperation, String nomFichier) {
        super(message, "SERIALIZATION_ERROR");
        this.typeOperation = typeOperation;
        this.nomFichier = nomFichier;
    }

    public SerializationException(String message, String typeOperation, Throwable cause) {
        super(message, "SERIALIZATION_ERROR", cause);
        this.typeOperation = typeOperation;
        this.nomFichier = null;
    }

    public SerializationException(String message, String typeOperation, String nomFichier, Throwable cause) {
        super(message, "SERIALIZATION_ERROR", cause);
        this.typeOperation = typeOperation;
        this.nomFichier = nomFichier;
    }

    // Getters
    public String getNomFichier() { return nomFichier; }
    public String getTypeOperation() { return typeOperation; }

    @Override
    public String getMessageUtilisateur() {
        StringBuilder message = new StringBuilder();
        message.append("Erreur de ").append(typeOperation.toLowerCase());
        if (nomFichier != null) {
            message.append(" du fichier '").append(nomFichier).append("'");
        }
        message.append(": ").append(getMessage());
        return message.toString();
    }

    public boolean estErreurSauvegarde() {
        return "SAUVEGARDE".equals(typeOperation);
    }

    public boolean estErreurChargement() {
        return "CHARGEMENT".equals(typeOperation);
    }
}