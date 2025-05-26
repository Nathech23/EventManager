package com.util;

import com.exception.*;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Gestionnaire global d'erreurs pour l'application
 * Centralise l'affichage et la journalisation des erreurs
 */
public class GestionnaireErreurs {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Affiche une exception de manière appropriée selon son type
     */
    public static void afficherErreur(Exception exception, Stage parentStage) {

        if (exception instanceof GestionEvenementsException) {
            afficherErreurPersonnalisee((GestionEvenementsException) exception, parentStage);
        } else {
            afficherErreurGenerique(exception, parentStage);
        }

        // Journaliser l'erreur
        journaliserErreur(exception);
    }

    /**
     * Affiche une erreur personnalisée avec message utilisateur
     */
    private static void afficherErreurPersonnalisee(GestionEvenementsException exception, Stage parentStage) {

        Alert alert = new Alert(getTypeAlerte(exception));
        alert.setTitle("Erreur - " + exception.getCodeErreur());
        alert.setHeaderText(getEnteteErreur(exception));
        alert.setContentText(exception.getMessageUtilisateur());

        // Ajouter les détails techniques en expansion
        ajouterDetailsErreur(alert, exception);

        if (parentStage != null) {
            alert.initOwner(parentStage);
        }

        alert.showAndWait();
    }

    /**
     * Affiche une erreur générique avec stack trace
     */
    private static void afficherErreurGenerique(Exception exception, Stage parentStage) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur Système");
        alert.setHeaderText("Une erreur inattendue s'est produite");
        alert.setContentText(exception.getMessage());

        // Stack trace détaillée
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setExpandableContent(textArea);

        if (parentStage != null) {
            alert.initOwner(parentStage);
        }

        alert.showAndWait();
    }

    /**
     * Détermine le type d'alerte selon l'exception
     */
    private static Alert.AlertType getTypeAlerte(GestionEvenementsException exception) {
        if (exception instanceof ValidationException) {
            return Alert.AlertType.WARNING;
        } else if (exception instanceof CapaciteMaxAtteinteException) {
            return Alert.AlertType.WARNING;
        } else if (exception instanceof SerializationException) {
            return Alert.AlertType.ERROR;
        } else {
            return Alert.AlertType.ERROR;
        }
    }

    /**
     * Génère l'en-tête approprié selon l'exception
     */
    private static String getEnteteErreur(GestionEvenementsException exception) {
        if (exception instanceof CapaciteMaxAtteinteException) {
            return "Capacité maximale atteinte";
        } else if (exception instanceof EvenementDejaExistantException) {
            return "Événement déjà existant";
        } else if (exception instanceof EvenementIntrouvableException) {
            return "Événement introuvable";
        } else if (exception instanceof ParticipantIntrouvableException) {
            return "Participant introuvable";
        } else if (exception instanceof SerializationException) {
            SerializationException se = (SerializationException) exception;
            return "Erreur de " + se.getTypeOperation().toLowerCase();
        } else if (exception instanceof ValidationException) {
            return "Données invalides";
        } else {
            return "Erreur système";
        }
    }

    /**
     * Ajoute les détails techniques en zone expansible
     */
    private static void ajouterDetailsErreur(Alert alert, GestionEvenementsException exception) {

        StringBuilder details = new StringBuilder();
        details.append("Code erreur: ").append(exception.getCodeErreur()).append("\n");
        details.append("Timestamp: ").append(LocalDateTime.ofEpochSecond(
                exception.getTimestamp() / 1000, 0, (ZoneOffset) ZoneOffset.systemDefault()
        ).format(FORMATTER)).append("\n");
        details.append("Message détaillé: ").append(exception.getMessageDetaille()).append("\n\n");

        // Détails spécifiques selon le type d'exception
        ajouterDetailsSpecifiques(details, exception);

        // Stack trace
        if (exception.getCause() != null) {
            details.append("\nCause originale:\n");
            StringWriter sw = new StringWriter();
            exception.getCause().printStackTrace(new PrintWriter(sw));
            details.append(sw.toString());
        }

        TextArea textArea = new TextArea(details.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setExpandableContent(textArea);
    }

    /**
     * Ajoute des détails spécifiques selon le type d'exception
     */
    private static void ajouterDetailsSpecifiques(StringBuilder details, GestionEvenementsException exception) {

        if (exception instanceof CapaciteMaxAtteinteException) {
            CapaciteMaxAtteinteException ce = (CapaciteMaxAtteinteException) exception;
            details.append("Événement ID: ").append(ce.getEvenementId()).append("\n");
            details.append("Capacité max: ").append(ce.getCapaciteMax()).append("\n");
            details.append("Capacité actuelle: ").append(ce.getCapaciteActuelle()).append("\n");
            details.append("Places disponibles: ").append(ce.getPlacesDisponibles()).append("\n");

        } else if (exception instanceof SerializationException) {
            SerializationException se = (SerializationException) exception;
            details.append("Type opération: ").append(se.getTypeOperation()).append("\n");
            details.append("Fichier: ").append(se.getNomFichier()).append("\n");

        } else if (exception instanceof ValidationException) {
            ValidationException ve = (ValidationException) exception;
            details.append("Champ concerné: ").append(ve.getChampConcerne()).append("\n");
            details.append("Erreurs de validation:\n");
            for (String erreur : ve.getErreursValidation()) {
                details.append("  • ").append(erreur).append("\n");
            }
        }
    }

    /**
     * Journalise l'erreur dans la console et/ou fichier log
     */
    private static void journaliserErreur(Exception exception) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.err.println(String.format("[%s] ERREUR: %s", timestamp, exception.getMessage()));

        if (exception instanceof GestionEvenementsException) {
            GestionEvenementsException ge = (GestionEvenementsException) exception;
            System.err.println(String.format("  Code: %s", ge.getCodeErreur()));
            System.err.println(String.format("  Détail: %s", ge.getMessageDetaille()));
        }

        // Stack trace complète pour les erreurs graves
        if (!(exception instanceof ValidationException) && !(exception instanceof CapaciteMaxAtteinteException)) {
            exception.printStackTrace();
        }
    }

    /**
     * Affiche un avertissement simple
     */
    public static void afficherAvertissement(String titre, String message, Stage parentStage) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Avertissement");
        alert.setHeaderText(titre);
        alert.setContentText(message);

        if (parentStage != null) {
            alert.initOwner(parentStage);
        }

        alert.showAndWait();
    }

    /**
     * Affiche une information de succès
     */
    public static void afficherSucces(String titre, String message, Stage parentStage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(titre);
        alert.setContentText(message);

        if (parentStage != null) {
            alert.initOwner(parentStage);
        }

        alert.showAndWait();
    }
}
