package com.controller;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.util.Callback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.model.*;
import com.service.GestionEvenements;
import com.application.MainApp;
import com.exception.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialogue complet pour inscrire un participant à un événement
 * Démontre le Pattern Observer en action avec explications détaillées
 */
public class DialogueInscription extends Dialog<Boolean> {

    private ComboBox<Participant> comboParticipant;
    private ComboBox<Evenement> comboEvenement;
    private Label lblInfoEvenement;
    private Label lblInfoParticipant;
    private Label lblInfoObserver;
    private ProgressBar progressCapacite;
    private VBox containerInfos;

    private GestionEvenements gestionEvenements;

    public DialogueInscription() {
        this.gestionEvenements = GestionEvenements.getInstance();

        configurerDialogue();
        creerInterface();
        configurerLogique();
        actualiserDonnees();
    }

    private void configurerDialogue() {
        setTitle("Inscription à un événement");
        setHeaderText("Inscrire un participant à un événement");

        ButtonType btnInscrire = new ButtonType("Inscrire", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(btnInscrire, btnAnnuler);

        getDialogPane().lookupButton(btnInscrire).setDisable(true);
    }

    private void creerInterface() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setPrefWidth(600);

        // Explication du Pattern Observer
        creerExplicationObserver(container);

        // Formulaire de sélection
        creerFormulaireSelection(container);

        // Zone d'informations dynamiques
        creerZoneInformations(container);

        getDialogPane().setContent(container);
    }

    private void creerExplicationObserver(VBox container) {
        VBox explicationBox = new VBox(10);
        explicationBox.setStyle("-fx-background-color: #e8f4fd; -fx-padding: 15; " +
                "-fx-border-color: #1e88e5; -fx-border-radius: 8; " +
                "-fx-background-radius: 8;");

        Label titreExplication = new Label("🔔 Pattern Observer en Action");
        titreExplication.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1565c0;");

        Label texteExplication = new Label(
                "Lorsque vous inscrivez un participant à un événement, plusieurs choses se passent automatiquement :\n\n" +
                        "✅ Le participant devient automatiquement 'Observer' de cet événement\n" +
                        "✅ Il recevra toutes les notifications de modifications (lieu, date, etc.)\n" +
                        "✅ Il sera notifié en cas d'annulation de l'événement\n" +
                        "✅ Les autres participants seront notifiés de sa participation\n\n" +
                        "Aucune intervention manuelle n'est nécessaire - tout est automatique !"
        );
        texteExplication.setWrapText(true);
        texteExplication.setStyle("-fx-font-size: 13px; -fx-text-fill: #424242;");

        explicationBox.getChildren().addAll(titreExplication, texteExplication);
        container.getChildren().add(explicationBox);
    }

    private void creerFormulaireSelection(VBox container) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        // Sélection du participant
        grid.add(new Label("Participant:"), 0, 0);
        comboParticipant = new ComboBox<>();
        comboParticipant.setPromptText("Sélectionner un participant");
        comboParticipant.setPrefWidth(300);
        configurerComboParticipant();
        grid.add(comboParticipant, 1, 0);

        // Sélection de l'événement
        grid.add(new Label("Événement:"), 0, 1);
        comboEvenement = new ComboBox<>();
        comboEvenement.setPromptText("Sélectionner un événement");
        comboEvenement.setPrefWidth(300);
        configurerComboEvenement();
        grid.add(comboEvenement, 1, 1);

        container.getChildren().add(grid);
    }

    private void configurerComboParticipant() {
        comboParticipant.setCellFactory(listView -> new ListCell<Participant>() {
            @Override
            protected void updateItem(Participant participant, boolean empty) {
                super.updateItem(participant, empty);
                if (empty || participant == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String type = participant instanceof Organisateur ? "👔 Organisateur" : "👤 Participant";
                    setText(String.format("%s %s (%s) - %s",
                            type, participant.getNom(), participant.getId(), participant.getEmail()));
                }
            }
        });

        comboParticipant.setButtonCell(new ListCell<Participant>() {
            @Override
            protected void updateItem(Participant participant, boolean empty) {
                super.updateItem(participant, empty);
                if (empty || participant == null) {
                    setText("Sélectionner un participant");
                } else {
                    setText(participant.getNom() + " (" + participant.getId() + ")");
                }
            }
        });
    }

    private void configurerComboEvenement() {
        comboEvenement.setCellFactory(listView -> new ListCell<Evenement>() {
            @Override
            protected void updateItem(Evenement evenement, boolean empty) {
                super.updateItem(evenement, empty);
                if (empty || evenement == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String type = evenement instanceof Conference ? "📊 Conférence" : "🎵 Concert";
                    String statut = evenement.isAnnule() ? " [ANNULÉ]" : "";
                    setText(String.format("%s %s (%d/%d places)%s",
                            type, evenement.getNom(),
                            evenement.getNombreParticipants(),
                            evenement.getCapaciteMax(), statut));
                }
            }
        });

        comboEvenement.setButtonCell(new ListCell<Evenement>() {
            @Override
            protected void updateItem(Evenement evenement, boolean empty) {
                super.updateItem(evenement, empty);
                if (empty || evenement == null) {
                    setText("Sélectionner un événement");
                } else {
                    setText(evenement.getNom() + " (" + evenement.getClass().getSimpleName() + ")");
                }
            }
        });
    }

    private void creerZoneInformations(VBox container) {
        containerInfos = new VBox(10);

        // Informations sur l'événement sélectionné
        lblInfoEvenement = new Label();
        lblInfoEvenement.setStyle("-fx-text-fill: #424242; -fx-font-size: 12px;");
        lblInfoEvenement.setWrapText(true);
        lblInfoEvenement.setMaxWidth(550);

        // Informations sur le participant sélectionné
        lblInfoParticipant = new Label();
        lblInfoParticipant.setStyle("-fx-text-fill: #424242; -fx-font-size: 12px;");
        lblInfoParticipant.setWrapText(true);
        lblInfoParticipant.setMaxWidth(550);

        // Barre de progression de la capacité
        VBox progressContainer = new VBox(5);
        Label lblProgress = new Label("Capacité de l'événement:");
        lblProgress.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        progressCapacite = new ProgressBar();
        progressCapacite.setPrefWidth(400);
        progressCapacite.setProgress(0);
        Label lblProgressText = new Label("0/0 places occupées");
        lblProgressText.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        progressContainer.getChildren().addAll(lblProgress, progressCapacite, lblProgressText);

        // Informations sur le Pattern Observer
        lblInfoObserver = new Label();
        lblInfoObserver.setStyle("-fx-text-fill: #1976d2; -fx-font-style: italic; -fx-font-size: 12px;");
        lblInfoObserver.setWrapText(true);
        lblInfoObserver.setMaxWidth(550);

        containerInfos.getChildren().addAll(
                lblInfoEvenement,
                lblInfoParticipant,
                progressContainer,
                lblInfoObserver
        );

        container.getChildren().add(containerInfos);
        containerInfos.setVisible(false);
    }

    private void configurerLogique() {
        // Validation et mise à jour des informations
        Runnable validateur = () -> {
            boolean valide = comboParticipant.getValue() != null &&
                    comboEvenement.getValue() != null;

            Button btnInscrire = (Button) getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0));
            btnInscrire.setDisable(!valide);

            mettreAJourInformations();
        };

        comboParticipant.valueProperty().addListener((obs, old, newVal) -> validateur.run());
        comboEvenement.valueProperty().addListener((obs, old, newVal) -> validateur.run());

        // Conversion du résultat
        setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return effectuerInscription();
            }
            return false;
        });
    }

    private void actualiserDonnees() {
        // Charger les participants
        ObservableList<Participant> participantsDisponibles = FXCollections.observableArrayList(
                gestionEvenements.getObservableParticipants()
        );
        comboParticipant.setItems(participantsDisponibles);

        // Charger les événements (seulement ceux qui ne sont pas annulés)
        ObservableList<Evenement> evenementsDisponibles = FXCollections.observableArrayList(
                gestionEvenements.getObservableEvenements().stream()
                        .filter(e -> !e.isAnnule())
                        .collect(Collectors.toList())
        );
        comboEvenement.setItems(evenementsDisponibles);
    }

    private void mettreAJourInformations() {
        Evenement evenement = comboEvenement.getValue();
        Participant participant = comboParticipant.getValue();

        if (evenement == null && participant == null) {
            containerInfos.setVisible(false);
            return;
        }

        containerInfos.setVisible(true);

        // Informations sur l'événement
        if (evenement != null) {
            lblInfoEvenement.setText(String.format(
                    "📅 Événement: %s\n" +
                            "📍 Lieu: %s\n" +
                            "🕒 Date: %s\n" +
                            "👥 Participants actuels: %d\n" +
                            "🎯 Capacité maximale: %d\n" +
                            "📊 Places disponibles: %d\n" +
                            "👁️ Observers actuels: %d",
                    evenement.getNom(),
                    evenement.getLieu(),
                    evenement.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    evenement.getNombreParticipants(),
                    evenement.getCapaciteMax(),
                    evenement.getPlacesDisponibles(),
                    evenement.getObservers().size()
            ));

            // Mise à jour de la barre de progression
            double pourcentage = (double) evenement.getNombreParticipants() / evenement.getCapaciteMax();
            progressCapacite.setProgress(pourcentage);

            // Couleur selon le taux de remplissage
            if (pourcentage < 0.5) {
                progressCapacite.setStyle("-fx-accent: green;");
            } else if (pourcentage < 0.8) {
                progressCapacite.setStyle("-fx-accent: orange;");
            } else {
                progressCapacite.setStyle("-fx-accent: red;");
            }
        }

        // Informations sur le participant
        if (participant != null) {
            String typeParticipant = participant instanceof Organisateur ? "Organisateur" : "Participant";
            lblInfoParticipant.setText(String.format(
                    "👤 %s: %s (%s)\n" +
                            "📧 Email: %s",
                    typeParticipant,
                    participant.getNom(),
                    participant.getId(),
                    participant.getEmail()
            ));
        }

        // Informations sur le Pattern Observer
        if (evenement != null && participant != null) {
            if (evenement.estInscrit(participant)) {
                lblInfoObserver.setText("⚠️ Ce participant est déjà inscrit à cet événement et est déjà Observer.");
                lblInfoObserver.setStyle("-fx-text-fill: #f57c00; -fx-font-style: italic; -fx-font-size: 12px;");
            } else if (evenement.getPlacesDisponibles() <= 0) {
                lblInfoObserver.setText("❌ Aucune place disponible pour cet événement.");
                lblInfoObserver.setStyle("-fx-text-fill: #d32f2f; -fx-font-style: italic; -fx-font-size: 12px;");
            } else {
                lblInfoObserver.setText(String.format(
                        "✅ %s deviendra automatiquement Observer de '%s' après inscription.\n" +
                                "🔔 Il/elle recevra toutes les notifications de modifications ou d'annulation.\n" +
                                "👥 Les %d autres participants seront notifiés de cette nouvelle inscription.",
                        participant.getNom(),
                        evenement.getNom(),
                        evenement.getNombreParticipants()
                ));
                lblInfoObserver.setStyle("-fx-text-fill: #1976d2; -fx-font-style: italic; -fx-font-size: 12px;");
            }
        } else {
            lblInfoObserver.setText("");
        }
    }

    private boolean effectuerInscription() {
        try {
            Participant participant = comboParticipant.getValue();
            Evenement evenement = comboEvenement.getValue();

            // Vérifications préalables
            if (evenement.estInscrit(participant)) {
                MainApp.afficherErreur("Déjà inscrit",
                        participant.getNom() + " est déjà inscrit à cet événement.");
                return false;
            }

            if (evenement.getPlacesDisponibles() <= 0) {
                MainApp.afficherErreur("Complet",
                        "Aucune place disponible pour cet événement.");
                return false;
            }

            if (evenement.isAnnule()) {
                MainApp.afficherErreur("Événement annulé",
                        "Impossible de s'inscrire à un événement annulé.");
                return false;
            }

            // Effectuer l'inscription via GestionEvenements
            gestionEvenements.inscrireParticipant(participant.getId(), evenement.getId());

            // Confirmation avec information détaillée sur le Pattern Observer
            Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
            confirmation.setTitle("Inscription réussie !");
            confirmation.setHeaderText("Pattern Observer activé automatiquement");
            confirmation.setContentText(String.format(
                    "✅ %s a été inscrit(e) à '%s'.\n\n" +
                            "🔔 Pattern Observer en action :\n" +
                            "• %s est maintenant 'Observer' de cet événement\n" +
                            "• Il/elle recevra automatiquement toutes les notifications\n" +
                            "• Les %d autres participants ont été notifiés de cette inscription\n\n" +
                            "📊 État de l'événement :\n" +
                            "• Participants: %d/%d\n" +
                            "• Observers actifs: %d\n" +
                            "• Places restantes: %d",
                    participant.getNom(),
                    evenement.getNom(),
                    participant.getNom(),
                    evenement.getNombreParticipants() - 1,
                    evenement.getNombreParticipants(),
                    evenement.getCapaciteMax(),
                    evenement.getObservers().size(),
                    evenement.getPlacesDisponibles()
            ));

            confirmation.getDialogPane().setPrefWidth(500);
            confirmation.showAndWait();

            return true;

        } catch (CapaciteMaxAtteinteException e) {
            Alert erreur = new Alert(Alert.AlertType.WARNING);
            erreur.setTitle("Capacité maximale atteinte");
            erreur.setHeaderText("Impossible d'inscrire le participant");
            erreur.setContentText(e.getMessageUtilisateur());
            erreur.showAndWait();
            return false;

        } catch (ParticipantIntrouvableException | EvenementIntrouvableException e) {
            MainApp.afficherErreur("Données introuvables", e.getMessageUtilisateur());
            return false;

        } catch (Exception e) {
            MainApp.afficherErreur("Erreur d'inscription",
                    "Impossible d'inscrire le participant : " + e.getMessage());
            return false;
        }
    }
}