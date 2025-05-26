package com.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.service.GestionEvenements;

/**
 * Classe principale de l'application JavaFX
 * Point d'entrée de l'interface graphique
 */
public class MainApp extends Application {

    private static Stage primaryStage;
    private static GestionEvenements gestionEvenements;

    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;
            gestionEvenements = GestionEvenements.getInstance();

            // Charger la vue principale
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);

            // Ajouter le CSS (optionnel)
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            // Configuration de la fenêtre
            primaryStage.setTitle("Système de Gestion d'Événements - Pattern Observer");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.setResizable(true);

            // Icône de l'application (optionnel)
            // primaryStage.getIcons().add(new Image("/images/icon.png"));

            // Gestion de la fermeture
            primaryStage.setOnCloseRequest(e -> {
                e.consume(); // Empêche la fermeture automatique
                fermerApplication();
            });

            primaryStage.show();

            // Initialiser quelques données de démonstration
            initialiserDonneesDemo();

        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur de démarrage",
                    "Impossible de charger l'interface principale: " + e.getMessage());
        }
    }

    /**
     * Initialise quelques données de démonstration pour l'application
     */
    private void initialiserDonneesDemo() {
        // Cette méthode sera appelée au démarrage pour avoir des données à afficher
        System.out.println("🚀 Application JavaFX démarrée avec Pattern Observer");
        System.out.println("📊 Système de gestion prêt à l'utilisation");
    }

    /**
     * Gestion de la fermeture de l'application
     */
    private void fermerApplication() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Fermeture de l'application");
        alert.setHeaderText("Voulez-vous vraiment quitter ?");
        alert.setContentText("Les données non sauvegardées seront perdues.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                System.out.println("👋 Fermeture de l'application");
                primaryStage.close();
                System.exit(0);
            }
        });
    }

    /**
     * Affiche une erreur à l'utilisateur
     */
    public static void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une information à l'utilisateur
     */
    public static void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Getters pour accéder aux composants depuis les contrôleurs
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static GestionEvenements getGestionEvenements() {
        return gestionEvenements;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
