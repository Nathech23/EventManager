package com.controller;

import com.util.SerializationUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.stage.FileChooser;
import com.model.*;
import com.service.GestionEvenements;
import com.application.MainApp;
import com.exception.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

/**
 * Contrôleur principal de l'interface JavaFX
 * Gère toutes les interactions utilisateur avec le Pattern Observer
 */
public class MainController implements Initializable {

    // ============ FXML CONTROLS - ÉVÉNEMENTS ============
    @FXML private TabPane tabPane;
    @FXML private TableView<Evenement> tableEvenements;
    @FXML private TableColumn<Evenement, String> colEvenementId;
    @FXML private TableColumn<Evenement, String> colEvenementNom;
    @FXML private TableColumn<Evenement, String> colEvenementType;
    @FXML private TableColumn<Evenement, String> colEvenementDate;
    @FXML private TableColumn<Evenement, String> colEvenementLieu;
    @FXML private TableColumn<Evenement, String> colEvenementCapacite;
    @FXML private TableColumn<Evenement, String> colEvenementParticipants;
    @FXML private TableColumn<Evenement, String> colEvenementObservers;
    @FXML private TableColumn<Evenement, String> colEvenementStatut;
    @FXML private TextField txtRechercheEvenements;

    // ============ FXML CONTROLS - PARTICIPANTS ============
    @FXML private TableView<Participant> tableParticipants;
    @FXML private TableColumn<Participant, String> colParticipantId;
    @FXML private TableColumn<Participant, String> colParticipantNom;
    @FXML private TableColumn<Participant, String> colParticipantEmail;
    @FXML private TableColumn<Participant, String> colParticipantType;
    @FXML private TextField txtRechercheParticipants;

    // ============ FXML CONTROLS - INSCRIPTIONS ============
    @FXML private ComboBox<Evenement> comboEvenementsInscription;
    @FXML private ListView<Participant> listParticipantsInscrits;
    @FXML private ListView<Participant> listParticipantsDisponibles;

    // ============ FXML CONTROLS - LOGS ============
    @FXML private TextArea textAreaLogs;

    // ============ FXML CONTROLS - STATISTIQUES ============
    @FXML private Label lblNbEvenements;
    @FXML private Label lblNbParticipants;
    @FXML private Label lblNbInscriptions;
    @FXML private Label lblTauxOccupation;
    @FXML private Label lblNbObservers;
    @FXML private Label lblNbNotifications;
    @FXML private Label lblNbConferences;
    @FXML private Label lblNbConcerts;

    // ============ FXML CONTROLS - BARRE DE STATUT ============
    @FXML private Label lblStatut;
    @FXML private Label lblNombreEvenementsStatut;
    @FXML private Label lblNombreParticipantsStatut;
    @FXML private Label lblObserversStatut;

    // ============ PROPRIÉTÉS ============
    private GestionEvenements gestionEvenements;
    private FilteredList<Evenement> evenementsFiltres;
    private FilteredList<Participant> participantsFiltres;
    private int nombreNotifications = 0;

    // ============ INITIALISATION ============

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gestionEvenements = MainApp.getGestionEvenements();

        // Initialiser les tableaux
        initialiserTableEvenements();
        initialiserTableParticipants();
        initialiserInscriptions();
        initialiserLogs();

        // Configurer les listeners
        configurerListeners();

        // Actualiser l'affichage
        actualiserInterface();

        // Redirection des logs vers l'interface
        configurerRedirectionLogs();

        mettreAJourStatut("Interface JavaFX initialisée avec Pattern Observer");
    }

    private void initialiserTableEvenements() {
        // Configuration des colonnes
        colEvenementId.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colEvenementNom.setCellValueFactory(cellData -> cellData.getValue().nomProperty());
        colEvenementType.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getClass().getSimpleName()));
        colEvenementDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        colEvenementLieu.setCellValueFactory(cellData -> cellData.getValue().lieuProperty());
        colEvenementCapacite.setCellValueFactory(cellData ->
                cellData.getValue().capaciteMaxProperty().asString());
        colEvenementParticipants.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getNombreParticipants() + "/" + cellData.getValue().getCapaciteMax()));
        colEvenementObservers.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(cellData.getValue().getObservers().size())));
        colEvenementStatut.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().isAnnule() ? "ANNULÉ" : "ACTIF"));

        // Binding avec les données
        evenementsFiltres = new FilteredList<>(gestionEvenements.getObservableEvenements());
        tableEvenements.setItems(evenementsFiltres);

        // Double-clic pour voir les détails
        tableEvenements.setRowFactory(tv -> {
            TableRow<Evenement> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    afficherDetailsEvenement(row.getItem());
                }
            });
            return row;
        });
    }

    private void initialiserTableParticipants() {
        // Configuration des colonnes
        colParticipantId.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colParticipantNom.setCellValueFactory(cellData -> cellData.getValue().nomProperty());
        colParticipantEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        colParticipantType.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getClass().getSimpleName()));

        // Binding avec les données
        participantsFiltres = new FilteredList<>(gestionEvenements.getObservableParticipants());
        tableParticipants.setItems(participantsFiltres);
    }

    private void initialiserInscriptions() {
        // Configuration du ComboBox des événements
        comboEvenementsInscription.setItems(gestionEvenements.getObservableEvenements());
        comboEvenementsInscription.setCellFactory(listView -> new ListCell<Evenement>() {
            @Override
            protected void updateItem(Evenement evenement, boolean empty) {
                super.updateItem(evenement, empty);
                if (empty || evenement == null) {
                    setText(null);
                } else {
                    setText(evenement.getNom() + " (" + evenement.getClass().getSimpleName() + ")");
                }
            }
        });
        comboEvenementsInscription.setButtonCell(new ListCell<Evenement>() {
            @Override
            protected void updateItem(Evenement evenement, boolean empty) {
                super.updateItem(evenement, empty);
                if (empty || evenement == null) {
                    setText("Sélectionner un événement...");
                } else {
                    setText(evenement.getNom() + " (" + evenement.getClass().getSimpleName() + ")");
                }
            }
        });
    }

    private void initialiserLogs() {
        textAreaLogs.setText("=== JOURNAL DES NOTIFICATIONS PATTERN OBSERVER ===\n");
        textAreaLogs.appendText("Interface initialisée. Les notifications apparaîtront ici en temps réel.\n\n");
    }

    private void configurerListeners() {
        // Recherche d'événements
        txtRechercheEvenements.textProperty().addListener((observable, oldValue, newValue) -> {
            rechercherEvenements();
        });

        // Recherche de participants
        txtRechercheParticipants.textProperty().addListener((observable, oldValue, newValue) -> {
            rechercherParticipants();
        });
    }

    private void configurerRedirectionLogs() {
        // Rediriger System.out vers l'interface pour capturer les notifications Observer
        PrintStream originalOut = System.out;
        PrintStream customOut = new PrintStream(new ByteArrayOutputStream() {
            @Override
            public void write(byte[] b, int off, int len) {
                String text = new String(b, off, len);
                if (text.contains("[OBSERVER]") || text.contains("🔔") || text.contains("❌")) {
                    javafx.application.Platform.runLater(() -> {
                        textAreaLogs.appendText(text);
                        textAreaLogs.positionCaret(textAreaLogs.getLength());
                        nombreNotifications++;
                        actualiserStatistiques();
                    });
                }
                // Aussi écrire vers la console originale
                originalOut.write(b, off, len);
            }
        });
        System.setOut(customOut);
    }

    // ============ ACTIONS ÉVÉNEMENTS ============

    @FXML
    private void creerNouvelEvenement() {
        try {
            DialogueEvenement dialogue = new DialogueEvenement();
            Optional<Evenement> result = dialogue.showAndWait();

            if (result.isPresent()) {
                gestionEvenements.ajouterEvenement(result.get());
                actualiserInterface();
                mettreAJourStatut("Nouvel événement créé: " + result.get().getNom());
            }
        } catch (Exception e) {
            MainApp.afficherErreur("Erreur", "Impossible de créer l'événement: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerEvenementSelectionne() {
        Evenement evenementSelectionne = tableEvenements.getSelectionModel().getSelectedItem();
        if (evenementSelectionne == null) {
            MainApp.afficherErreur("Aucune sélection", "Veuillez sélectionner un événement à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer l'événement '" + evenementSelectionne.getNom() + "' ?");
        confirmation.setContentText("Cette action annulera l'événement et notifiera automatiquement tous les participants inscrits via le Pattern Observer.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    gestionEvenements.supprimerEvenement(evenementSelectionne.getId());
                    actualiserInterface();
                    mettreAJourStatut("Événement supprimé et participants notifiés automatiquement");
                } catch (Exception e) {
                    MainApp.afficherErreur("Erreur", "Impossible de supprimer l'événement: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void modifierEvenementSelectionne() {
        Evenement evenementSelectionne = tableEvenements.getSelectionModel().getSelectedItem();
        if (evenementSelectionne == null) {
            MainApp.afficherErreur("Aucune sélection", "Veuillez sélectionner un événement à modifier.");
            return;
        }

        try {
            DialogueEvenement dialogue = new DialogueEvenement(evenementSelectionne);
            Optional<Evenement> result = dialogue.showAndWait();

            if (result.isPresent()) {
                Evenement evenementModifie = result.get();
                gestionEvenements.modifierEvenement(
                        evenementModifie.getId(),
                        evenementModifie.getNom(),
                        evenementModifie.getDate(),
                        evenementModifie.getLieu()
                );
                actualiserInterface();
                mettreAJourStatut("Événement modifié - Participants notifiés automatiquement via Observer");
            }
        } catch (Exception e) {
            MainApp.afficherErreur("Erreur", "Impossible de modifier l'événement: " + e.getMessage());
        }
    }

    // ============ ACTIONS PARTICIPANTS ============

    @FXML
    private void creerNouveauParticipant() {
        try {
            DialogueParticipant dialogue = new DialogueParticipant();
            Optional<Participant> result = dialogue.showAndWait();

            if (result.isPresent()) {
                gestionEvenements.ajouterParticipant(result.get());
                actualiserInterface();
                mettreAJourStatut("Nouveau participant ajouté: " + result.get().getNom());
            }
        } catch (Exception e) {
            MainApp.afficherErreur("Erreur", "Impossible de créer le participant: " + e.getMessage());
        }
    }

    // ============ ACTIONS PATTERN OBSERVER ============

    @FXML
    private void testerNotifications() {
        if (gestionEvenements.getObservableEvenements().isEmpty()) {
            MainApp.afficherInfo("Aucun événement", "Créez d'abord un événement et inscrivez des participants pour tester les notifications.");
            return;
        }

        Evenement premierEvenement = gestionEvenements.getObservableEvenements().get(0);

        textAreaLogs.appendText("=== TEST MANUEL DES NOTIFICATIONS ===\n");
        textAreaLogs.appendText("Test de modification sur: " + premierEvenement.getNom() + "\n");

        // Test de modification qui déclenchera automatiquement les notifications
        String nouveauLieu = "Lieu de Test - " + System.currentTimeMillis();
        premierEvenement.setLieu(nouveauLieu);

        textAreaLogs.appendText("=== FIN DU TEST ===\n\n");
        actualiserInterface();
        mettreAJourStatut("Test de notifications effectué");
    }

    // ============ AUTRES MÉTHODES ============

    @FXML
    private void rechercherEvenements() {
        String recherche = txtRechercheEvenements.getText().toLowerCase();
        evenementsFiltres.setPredicate(evenement -> {
            if (recherche == null || recherche.isEmpty()) {
                return true;
            }
            return evenement.getNom().toLowerCase().contains(recherche) ||
                    evenement.getLieu().toLowerCase().contains(recherche) ||
                    evenement.getClass().getSimpleName().toLowerCase().contains(recherche);
        });
    }

    @FXML
    private void rechercherParticipants() {
        String recherche = txtRechercheParticipants.getText().toLowerCase();
        participantsFiltres.setPredicate(participant -> {
            if (recherche == null || recherche.isEmpty()) {
                return true;
            }
            return participant.getNom().toLowerCase().contains(recherche) ||
                    participant.getEmail().toLowerCase().contains(recherche);
        });
    }

    private void actualiserInterface() {
        // Actualiser tous les éléments de l'interface
        javafx.application.Platform.runLater(() -> {
            tableEvenements.refresh();
            tableParticipants.refresh();
            actualiserInscriptions();
            actualiserStatistiques();
            mettreAJourBarreStatut();
        });
    }

    private void mettreAJourBarreStatut() {
        int nbEvenements = gestionEvenements.getObservableEvenements().size();
        int nbParticipants = gestionEvenements.getObservableParticipants().size();
        int nbObservers = gestionEvenements.getTotalObservers();

        lblNombreEvenementsStatut.setText(nbEvenements + " événement" + (nbEvenements > 1 ? "s" : ""));
        lblNombreParticipantsStatut.setText(nbParticipants + " participant" + (nbParticipants > 1 ? "s" : ""));
        lblObserversStatut.setText(nbObservers + " observer" + (nbObservers > 1 ? "s" : ""));
    }

    private void mettreAJourStatut(String message) {
        lblStatut.setText(message);
    }

    // Autres méthodes FXML (simplifiées pour l'exemple)
    @FXML private void effacerRechercheEvenements() { txtRechercheEvenements.clear(); }
    @FXML private void effacerRechercheParticipants() { txtRechercheParticipants.clear(); }


    private void afficherDetailsEvenement(Evenement evenement) {
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Détails de l'événement");
        details.setHeaderText(evenement.getNom());
        details.setContentText(String.format(
                "Type: %s\nDate: %s\nLieu: %s\nCapacité: %d\nParticipants: %d\nObservers: %d\nStatut: %s",
                evenement.getClass().getSimpleName(),
                evenement.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                evenement.getLieu(),
                evenement.getCapaciteMax(),
                evenement.getNombreParticipants(),
                evenement.getObservers().size(),
                evenement.isAnnule() ? "ANNULÉ" : "ACTIF"
        ));
        details.showAndWait();
    }

    @FXML
    private void selectionnerEvenementInscription() {
        Evenement evenementSelectionne = comboEvenementsInscription.getValue();
        if (evenementSelectionne != null) {
            actualiserListesInscription(evenementSelectionne);
            mettreAJourStatut("Événement sélectionné: " + evenementSelectionne.getNom());
        }
    }

    @FXML
    private void actualiserInscriptions() {
        Evenement evenementSelectionne = comboEvenementsInscription.getValue();
        if (evenementSelectionne != null) {
            actualiserListesInscription(evenementSelectionne);
        }
        actualiserInterface();
        mettreAJourStatut("Listes d'inscription actualisées");
    }

    @FXML
    private void inscrireParticipantAEvenement() {
        try {
            DialogueInscription dialogue = new DialogueInscription();
            Optional<Boolean> result = dialogue.showAndWait();

            if (result.isPresent() && result.get()) {
                actualiserInterface();
                // Basculer vers l'onglet des logs pour voir les notifications Observer
                tabPane.getSelectionModel().select(3);
                mettreAJourStatut("Inscription effectuée - Nouveau observer ajouté");
            }
        } catch (Exception e) {
            MainApp.afficherErreur("Erreur", "Impossible d'ouvrir le dialogue d'inscription: " + e.getMessage());
        }
    }

    @FXML
    private void inscrireParticipantSelectionne() {
        Participant participantSelectionne = listParticipantsDisponibles.getSelectionModel().getSelectedItem();
        Evenement evenementSelectionne = comboEvenementsInscription.getValue();

        if (participantSelectionne == null) {
            MainApp.afficherErreur("Aucune sélection", "Veuillez sélectionner un participant à inscrire.");
            return;
        }

        if (evenementSelectionne == null) {
            MainApp.afficherErreur("Aucune sélection", "Veuillez sélectionner un événement.");
            return;
        }

        try {
            gestionEvenements.inscrireParticipant(participantSelectionne.getId(), evenementSelectionne.getId());
            actualiserListesInscription(evenementSelectionne);
            actualiserInterface();

            textAreaLogs.appendText(String.format(
                    "✅ INSCRIPTION: %s ajouté comme observer de '%s'\n",
                    participantSelectionne.getNom(), evenementSelectionne.getNom()
            ));

            mettreAJourStatut(String.format("%s inscrit et devient observer automatiquement",
                    participantSelectionne.getNom()));

        } catch (Exception e) {
            MainApp.afficherErreur("Erreur d'inscription", "Impossible d'inscrire le participant: " + e.getMessage());
        }
    }

    @FXML
    private void desinscrireParticipantSelectionne() {
        Participant participantSelectionne = listParticipantsInscrits.getSelectionModel().getSelectedItem();
        Evenement evenementSelectionne = comboEvenementsInscription.getValue();

        if (participantSelectionne == null) {
            MainApp.afficherErreur("Aucune sélection", "Veuillez sélectionner un participant à désinscrire.");
            return;
        }

        if (evenementSelectionne == null) {
            MainApp.afficherErreur("Aucune sélection", "Veuillez sélectionner un événement.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la désinscription");
        confirmation.setHeaderText("Désinscrire " + participantSelectionne.getNom() + " ?");
        confirmation.setContentText("Le participant sera retiré de l'événement et ne recevra plus les notifications (Pattern Observer).");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    gestionEvenements.desinscrireParticipant(participantSelectionne.getId(), evenementSelectionne.getId());
                    actualiserListesInscription(evenementSelectionne);
                    actualiserInterface();

                    textAreaLogs.appendText(String.format(
                            "❌ DÉSINSCRIPTION: %s retiré des observers de '%s'\n",
                            participantSelectionne.getNom(), evenementSelectionne.getNom()
                    ));

                    mettreAJourStatut(String.format("%s désinscrit et retiré des observers",
                            participantSelectionne.getNom()));

                } catch (Exception e) {
                    MainApp.afficherErreur("Erreur", "Impossible de désinscrire le participant: " + e.getMessage());
                }
            }
        });
    }

    private void actualiserListesInscription(Evenement evenement) {
        if (evenement == null) {
            listParticipantsInscrits.getItems().clear();
            listParticipantsDisponibles.getItems().clear();
            return;
        }

        // Participants inscrits (= observers automatiques)
        listParticipantsInscrits.setItems(evenement.getObservableParticipants());

        // Participants disponibles (non inscrits)
        List<Participant> disponibles = new ArrayList<>();
        for (Participant participant : gestionEvenements.getObservableParticipants()) {
            if (!evenement.estInscrit(participant)) {
                disponibles.add(participant);
            }
        }
        listParticipantsDisponibles.setItems(FXCollections.observableArrayList(disponibles));

        // Configuration de l'affichage des listes
        listParticipantsInscrits.setCellFactory(listView -> new ListCell<Participant>() {
            @Override
            protected void updateItem(Participant participant, boolean empty) {
                super.updateItem(participant, empty);
                if (empty || participant == null) {
                    setText(null);
                } else {
                    setText(String.format("👁️ %s (%s) - Observer actif",
                            participant.getNom(), participant.getId()));
                }
            }
        });

        listParticipantsDisponibles.setCellFactory(listView -> new ListCell<Participant>() {
            @Override
            protected void updateItem(Participant participant, boolean empty) {
                super.updateItem(participant, empty);
                if (empty || participant == null) {
                    setText(null);
                } else {
                    setText(String.format("%s (%s)", participant.getNom(), participant.getId()));
                }
            }
        });
    }

    // ============ SÉRIALISATION ET PERSISTANCE ============

    @FXML
    private void sauvegarderDonnees() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder les données");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers JSON", "*.json"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        fileChooser.setInitialFileName("evenements_" +
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) +
                ".json");

        File file = fileChooser.showSaveDialog(MainApp.getPrimaryStage());
        if (file != null) {
            try {
                // Sauvegarder les événements avec leurs participants (= observers)
                SerializationUtil.sauvegarderDonnees(
                        gestionEvenements.getObservableEvenements(),
                        gestionEvenements.getObservableParticipants(),
                        file
                );

                textAreaLogs.appendText(String.format(
                        "💾 SAUVEGARDE: %d événements et %d participants sauvegardés dans %s\n",
                        gestionEvenements.getObservableEvenements().size(),
                        gestionEvenements.getObservableParticipants().size(),
                        file.getName()
                ));

                MainApp.afficherInfo("Sauvegarde réussie",
                        "Les données ont été sauvegardées avec succès.\nFichier: " + file.getName());

                mettreAJourStatut("Données sauvegardées: " + file.getName());

            } catch (Exception e) {
                MainApp.afficherErreur("Erreur de sauvegarde",
                        "Impossible de sauvegarder les données: " + e.getMessage());
            }
        }
    }

    @FXML
    private void chargerDonnees() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger des données");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers JSON", "*.json"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File file = fileChooser.showOpenDialog(MainApp.getPrimaryStage());
        if (file != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Charger des données");
            confirmation.setHeaderText("Remplacer les données actuelles ?");
            confirmation.setContentText("Cette action va remplacer tous les événements et participants actuels. " +
                    "Les relations Observer seront restaurées automatiquement.");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Charger les données
                        var donnees = SerializationUtil.chargerDonnees(file);

                        // Vider les données actuelles
                        gestionEvenements.viderTout();

                        // Restaurer les données avec reconstruction automatique des observers
                        for (Participant participant : donnees.getParticipants()) {
                            gestionEvenements.ajouterParticipant(participant);
                        }

                        for (Evenement evenement : donnees.getEvenements()) {
                            gestionEvenements.ajouterEvenement(evenement);
                            // Les observers sont automatiquement restaurés via setParticipants()
                        }

                        textAreaLogs.appendText(String.format(
                                "📂 CHARGEMENT: %d événements et %d participants chargés depuis %s\n",
                                donnees.getEvenements().size(),
                                donnees.getParticipants().size(),
                                file.getName()
                        ));
                        textAreaLogs.appendText("🔄 Relations Observer automatiquement restaurées\n");

                        actualiserInterface();
                        MainApp.afficherInfo("Chargement réussi",
                                "Les données ont été chargées avec succès.\nRelations Observer restaurées automatiquement.");

                        mettreAJourStatut("Données chargées: " + file.getName());

                    } catch (Exception e) {
                        MainApp.afficherErreur("Erreur de chargement",
                                "Impossible de charger les données: " + e.getMessage());
                    }
                }
            });
        }
    }

    // ============ DÉMONSTRATIONS ET TESTS ============

    @FXML
    private void afficherAPropos() {
        Alert apropos = new Alert(Alert.AlertType.INFORMATION);
        apropos.setTitle("À propos");
        apropos.setHeaderText("Système de Gestion d'Événements");
        apropos.setContentText(
                "Version 1.0 - TP POO\n\n" +
                        "Fonctionnalités principales:\n" +
                        "• Pattern Observer pour notifications automatiques\n" +
                        "• Gestion d'événements (Conférences, Concerts)\n" +
                        "• Inscription automatique comme Observers\n" +
                        "• Notifications en temps réel\n" +
                        "• Sérialisation JSON des données\n" +
                        "• Interface JavaFX moderne\n\n" +
                        "Architecture:\n" +
                        "• Design Patterns: Observer, Singleton\n" +
                        "• JavaFX Properties pour binding\n" +
                        "• Exceptions personnalisées\n" +
                        "• Tests JUnit avec couverture 70%"
        );
        apropos.getDialogPane().setPrefWidth(500);
        apropos.showAndWait();
    }

    /**
     * Démonstration avancée du Pattern Observer avec interactions utilisateur
     */
    private void demonstrationPatternObserverAvancee() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Démonstration Pattern Observer Avancée");
        info.setHeaderText("Cette démonstration va créer des données de test et montrer le Pattern Observer en action");
        info.setContentText(
                "Étapes de la démonstration:\n" +
                        "1. Création d'événements de test\n" +
                        "2. Création de participants de test\n" +
                        "3. Inscriptions automatiques (= abonnement Observer)\n" +
                        "4. Modifications d'événements (= notifications automatiques)\n" +
                        "5. Annulation d'événement (= notification à tous les observers)\n\n" +
                        "Surveillez l'onglet 'Notifications & Logs' pour voir les notifications en temps réel."
        );

        info.showAndWait().ifPresent(response -> {
            // Basculer vers l'onglet des logs
            tabPane.getSelectionModel().select(3);

            new Thread(() -> {
                try {
                    demonstrationEtapeParEtape();
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        MainApp.afficherErreur("Erreur", "Erreur pendant la démonstration: " + e.getMessage());
                    });
                }
            }).start();
        });
    }

    private void demonstrationEtapeParEtape() throws Exception {
        textAreaLogs.appendText("\n" + "=".repeat(60) + "\n");
        textAreaLogs.appendText("🎭 DÉMONSTRATION PATTERN OBSERVER AVANCÉE\n");
        textAreaLogs.appendText("=".repeat(60) + "\n");

        // Étape 1: Créer des événements
        textAreaLogs.appendText("\n📅 ÉTAPE 1: Création d'événements de test\n");
        Conference demoConf = new Conference(
                "DEMO_CONF", "Conférence Demo Observer",
                LocalDateTime.now().plusDays(5), "Auditorium Demo", 15, "Pattern Observer"
        );
        Concert demoConcert = new Concert(
                "DEMO_CONCERT", "Concert Demo Observer",
                LocalDateTime.now().plusDays(8), "Scène Demo", 100, "Demo Band", "Demo Rock"
        );

        javafx.application.Platform.runLater(() -> {
            try {
                gestionEvenements.ajouterEvenement(demoConf);
                gestionEvenements.ajouterEvenement(demoConcert);
                actualiserInterface();
            } catch (Exception e) {
                textAreaLogs.appendText("❌ Erreur: " + e.getMessage() + "\n");
            }
        });
        Thread.sleep(1000);

        // Étape 2: Créer des participants
        textAreaLogs.appendText("\n👥 ÉTAPE 2: Création de participants de test\n");
        Participant alice = new Participant("DEMO_ALICE", "Alice Observer", "alice@demo.com");
        Participant bob = new Participant("DEMO_BOB", "Bob Observer", "bob@demo.com");
        Participant charlie = new Participant("DEMO_CHARLIE", "Charlie Observer", "charlie@demo.com");

        javafx.application.Platform.runLater(() -> {
            gestionEvenements.ajouterParticipant(alice);
            gestionEvenements.ajouterParticipant(bob);
            gestionEvenements.ajouterParticipant(charlie);
            actualiserInterface();
        });
        Thread.sleep(500);

        // Étape 3: Inscriptions (= abonnement Observer automatique)
        textAreaLogs.appendText("\n🔔 ÉTAPE 3: Inscriptions automatiques comme Observers\n");
        try {
            gestionEvenements.inscrireParticipant("DEMO_ALICE", "DEMO_CONF");
            Thread.sleep(300);
            gestionEvenements.inscrireParticipant("DEMO_BOB", "DEMO_CONF");
            Thread.sleep(300);
            gestionEvenements.inscrireParticipant("DEMO_CHARLIE", "DEMO_CONCERT");
            Thread.sleep(300);
            gestionEvenements.inscrireParticipant("DEMO_ALICE", "DEMO_CONCERT"); // Alice dans les 2
            Thread.sleep(500);

            javafx.application.Platform.runLater(() -> actualiserInterface());

        } catch (Exception e) {
            textAreaLogs.appendText("❌ Erreur inscription: " + e.getMessage() + "\n");
        }

        // Étape 4: Modifications (= notifications Observer automatiques)
        textAreaLogs.appendText("\n🔄 ÉTAPE 4: Modifications déclenchant les notifications Observer\n");
        textAreaLogs.appendText("Modification 1: Changement de lieu de la conférence\n");
        demoConf.setLieu("Nouveau Centre de Conférences Demo");
        Thread.sleep(1000);

        textAreaLogs.appendText("Modification 2: Changement de date du concert\n");
        demoConcert.setDate(LocalDateTime.now().plusDays(10));
        Thread.sleep(1000);

        textAreaLogs.appendText("Modification 3: Changement de capacité de la conférence\n");
        demoConf.setCapaciteMax(25);
        Thread.sleep(1000);

        javafx.application.Platform.runLater(() -> actualiserInterface());

        // Étape 5: Annulation (= notification à tous les observers)
        textAreaLogs.appendText("\n❌ ÉTAPE 5: Annulation d'événement (notification à tous les observers)\n");
        textAreaLogs.appendText("Annulation de la conférence → Alice et Bob seront notifiés automatiquement\n");
        Thread.sleep(500);

        try {
            gestionEvenements.supprimerEvenement("DEMO_CONF");
            Thread.sleep(1500);

            javafx.application.Platform.runLater(() -> actualiserInterface());

        } catch (Exception e) {
            textAreaLogs.appendText("❌ Erreur annulation: " + e.getMessage() + "\n");
        }

        // Conclusion
        textAreaLogs.appendText("\n✅ DÉMONSTRATION TERMINÉE\n");
        textAreaLogs.appendText("Le Pattern Observer a fonctionné automatiquement:\n");
        textAreaLogs.appendText("• Inscriptions → Abonnement automatique aux notifications\n");
        textAreaLogs.appendText("• Modifications → Notifications automatiques aux observers\n");
        textAreaLogs.appendText("• Annulation → Notification à tous les observers concernés\n");
        textAreaLogs.appendText("=".repeat(60) + "\n\n");

        javafx.application.Platform.runLater(() -> {
            mettreAJourStatut("Démonstration Pattern Observer terminée avec succès");
        });
    }

    // ============ MISE À JOUR DE LA MÉTHODE LANCERDEMOOBSERVER ============

    @FXML
    private void lancerDemoObserver() {
        Alert choix = new Alert(Alert.AlertType.CONFIRMATION);
        choix.setTitle("Démonstration Pattern Observer");
        choix.setHeaderText("Choisir le type de démonstration");
        choix.setContentText("Quelle démonstration souhaitez-vous lancer ?");

        ButtonType btnRapide = new ButtonType("Démo Rapide (30s)");
        ButtonType btnAvancee = new ButtonType("Démo Avancée (2 min)");
        ButtonType btnAnnuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        choix.getButtonTypes().setAll(btnRapide, btnAvancee, btnAnnuler);

        choix.showAndWait().ifPresent(response -> {
            if (response == btnRapide) {
                // Démonstration rapide existante
                tabPane.getSelectionModel().select(3);
                mettreAJourStatut("Démonstration Pattern Observer rapide en cours...");

                new Thread(() -> {
                    try {
                        gestionEvenements.demonstrationPatternObserver();
                        javafx.application.Platform.runLater(() -> {
                            actualiserInterface();
                            mettreAJourStatut("Démonstration rapide terminée");
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            MainApp.afficherErreur("Erreur", "Erreur pendant la démonstration: " + e.getMessage());
                        });
                    }
                }).start();

            } else if (response == btnAvancee) {
                // Nouvelle démonstration avancée
                demonstrationPatternObserverAvancee();
            }
        });
    }

    // ============ ACTUALISATION DES STATISTIQUES AMÉLIORÉE ============

    @FXML
    private void actualiserStatistiques() {
        int nbEvenements = gestionEvenements.getObservableEvenements().size();
        int nbParticipants = gestionEvenements.getObservableParticipants().size();
        int nbInscriptions = gestionEvenements.getTotalParticipants();
        int nbObservers = gestionEvenements.getTotalObservers();

        lblNbEvenements.setText("Événements: " + nbEvenements);
        lblNbParticipants.setText("Participants: " + nbParticipants);
        lblNbInscriptions.setText("Inscriptions totales: " + nbInscriptions);
        lblNbObservers.setText("Observers actifs: " + nbObservers);
        lblNbNotifications.setText("Notifications envoyées: " + nombreNotifications);

        var stats = gestionEvenements.getStatistiquesParType();
        lblNbConferences.setText("Conférences: " + stats.getOrDefault("Conference", 0L));
        lblNbConcerts.setText("Concerts: " + stats.getOrDefault("Concert", 0L));

        var tauxMoyen = gestionEvenements.getTauxOccupationMoyen();
        if (tauxMoyen.isPresent()) {
            lblTauxOccupation.setText(String.format("Taux d'occupation: %.1f%%", tauxMoyen.getAsDouble()));
        } else {
            lblTauxOccupation.setText("Taux d'occupation: N/A");
        }

        mettreAJourStatut("Statistiques actualisées");
    }

    // ============ MÉTHODES UTILITAIRES SUPPLÉMENTAIRES ============

    /**
     * Efface tous les logs et remet les compteurs à zéro
     */
    @FXML
    private void effacerLogs() {
        textAreaLogs.clear();
        nombreNotifications = 0;
        initialiserLogs();
        actualiserStatistiques();
        mettreAJourStatut("Logs effacés et compteurs remis à zéro");
    }

    /**
     * Export des statistiques en format texte
     */
    public void exporterStatistiques() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les statistiques");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers texte", "*.txt")
        );
        fileChooser.setInitialFileName("statistiques_" +
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) +
                ".txt");

        File file = fileChooser.showSaveDialog(MainApp.getPrimaryStage());
        if (file != null) {
            try {
                java.nio.file.Files.write(file.toPath(), genererRapportStatistiques().getBytes());
                MainApp.afficherInfo("Export réussi", "Statistiques exportées vers: " + file.getName());
            } catch (Exception e) {
                MainApp.afficherErreur("Erreur d'export", "Impossible d'exporter: " + e.getMessage());
            }
        }
    }

    private String genererRapportStatistiques() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=".repeat(50)).append("\n");
        rapport.append("RAPPORT STATISTIQUES - SYSTÈME DE GESTION D'ÉVÉNEMENTS\n");
        rapport.append("Généré le: ").append(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        )).append("\n");
        rapport.append("=".repeat(50)).append("\n\n");

        rapport.append("STATISTIQUES GÉNÉRALES:\n");
        rapport.append("- Nombre d'événements: ").append(gestionEvenements.getObservableEvenements().size()).append("\n");
        rapport.append("- Nombre de participants: ").append(gestionEvenements.getObservableParticipants().size()).append("\n");
        rapport.append("- Total des inscriptions: ").append(gestionEvenements.getTotalParticipants()).append("\n");
        rapport.append("- Observers actifs: ").append(gestionEvenements.getTotalObservers()).append("\n");
        rapport.append("- Notifications envoyées: ").append(nombreNotifications).append("\n\n");

        var stats = gestionEvenements.getStatistiquesParType();
        rapport.append("RÉPARTITION PAR TYPE:\n");
        stats.forEach((type, count) ->
                rapport.append("- ").append(type).append(": ").append(count).append("\n"));

        rapport.append("\nDÉTAIL DES ÉVÉNEMENTS:\n");
        for (Evenement evenement : gestionEvenements.getObservableEvenements()) {
            rapport.append(String.format("- %s (%s): %d/%d participants, %d observers\n",
                    evenement.getNom(), evenement.getClass().getSimpleName(),
                    evenement.getNombreParticipants(), evenement.getCapaciteMax(),
                    evenement.getObservers().size()));
        }

        return rapport.toString();
    }
}