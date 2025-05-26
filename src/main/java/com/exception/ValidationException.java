package com.exception;

import java.util.List;
import java.util.ArrayList;

/**
 * Exception levée lors d'erreurs de validation des données
 */
public class ValidationException extends GestionEvenementsException {

    private final List<String> erreursValidation;
    private final String champConcerne;

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.erreursValidation = new ArrayList<>();
        this.erreursValidation.add(message);
        this.champConcerne = null;
    }

    public ValidationException(String message, String champConcerne) {
        super(message, "VALIDATION_ERROR");
        this.erreursValidation = new ArrayList<>();
        this.erreursValidation.add(message);
        this.champConcerne = champConcerne;
    }

    public ValidationException(List<String> erreursValidation) {
        super("Erreurs de validation détectées", "VALIDATION_ERROR");
        this.erreursValidation = new ArrayList<>(erreursValidation);
        this.champConcerne = null;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, "VALIDATION_ERROR", cause);
        this.erreursValidation = new ArrayList<>();
        this.erreursValidation.add(message);
        this.champConcerne = null;
    }

    // Getters
    public List<String> getErreursValidation() { return new ArrayList<>(erreursValidation); }
    public String getChampConcerne() { return champConcerne; }

    /**
     * Ajoute une erreur de validation
     */
    public void ajouterErreur(String erreur) {
        this.erreursValidation.add(erreur);
    }

    /**
     * Vérifie s'il y a plusieurs erreurs
     */
    public boolean aPlusieurErreurs() {
        return erreursValidation.size() > 1;
    }

    @Override
    public String getMessageUtilisateur() {
        if (erreursValidation.size() == 1) {
            return erreursValidation.get(0);
        }

        StringBuilder message = new StringBuilder("Erreurs de validation:\n");
        for (int i = 0; i < erreursValidation.size(); i++) {
            message.append("• ").append(erreursValidation.get(i));
            if (i < erreursValidation.size() - 1) {
                message.append("\n");
            }
        }
        return message.toString();
    }
}
