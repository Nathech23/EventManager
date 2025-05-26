package com.controller;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.util.Callback;
import com.model.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Dialogue pour créer ou modifier un événement
 * Supporte les Conférences et Concerts avec le Pattern Observer
 */
public class DialogueEvenement extends Dialog<Evenement> {

    private TextField txtId;
    private TextField txtNom;
    private ComboBox<String> comboType;
    private DatePicker datePicker;
    private Spinner<Integer> spinnerHeure;
    private Spinner<Integer> spinnerMinute;
    private TextField txtLieu;
    private Spinner<Integer> spinnerCapacite;

    // Champs spécifiques Conference
    private TextField txtTheme;
    private TextArea txtIntervenants;

    // Champs spécifiques Concert
    private TextField txtArtiste;
    private TextField txtGenre;

    // Conteneurs pour organiser les champs spécifiques
    private VBox containerConference;
    private VBox containerConcert;

    private Evenement evenementAModifier;
    private boolean modeModification;

    /**
     * Constructeur pour créer un nouvel événement
     */
    public DialogueEvenement() {
        this(null);
    }

    /**
     * Constructeur pour modifier un événement existant
     */
    public DialogueEvenement(Evenement evenement) {
        this.evenementAModifier = evenement;
        this.modeModification = (evenement != null);

        configurerDialogue();
        creerInterface();
        configurerLogique();

        if (modeModification) {
            remplirChamps();
        }
    }

    private void configurerDialogue() {
        setTitle(modeModification ? "Modifier l'événement" : "Nouvel événement");
        setHeaderText(modeModification ?
                "Modifier les informations de l'événement (Pattern Observer actif)" :
                "Créer un nouvel événement avec notifications automatiques");

        // Boutons
        ButtonType btnCreer = new ButtonType(modeModification ? "Modifier" : "Créer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(btnCreer, btnAnnuler);

        // Désactiver le bouton Créer par défaut
        getDialogPane().lookupButton(btnCreer).setDisable(true);
    }

    private void creerInterface() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        int row = 0;

        // === INFORMATIONS GÉNÉRALES ===
        Label lblGeneral = new Label("Informations générales");
        lblGeneral.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(lblGeneral, 0, row++, 2, 1);

        // ID
        grid.add(new Label("ID:"), 0, row);
        txtId = new TextField();
        txtId.setPromptText("Ex: CONF001, CONCERT001");
        if (modeModification) {
            txtId.setEditable(false);
            txtId.setStyle("-fx-background-color: #f0f0f0;");
        }
        grid.add(txtId, 1, row++);

        // Nom
        grid.add(new Label("Nom:"), 0, row);
        txtNom = new TextField();
        txtNom.setPromptText("Nom de l'événement");
        txtNom.setPrefColumnCount(20);
        grid.add(txtNom, 1, row++);

        // Type
        grid.add(new Label("Type:"), 0, row);
        comboType = new ComboBox<>();
        comboType.getItems().addAll("Conference", "Concert");
        comboType.setPromptText("Sélectionner le type");
        if (modeModification) {
            comboType.setDisable(true);
        }
        grid.add(comboType, 1, row++);

        // Date
        grid.add(new Label("Date:"), 0, row);
        HBox dateBox = new HBox(10);
        datePicker = new DatePicker();
        datePicker.setPromptText("Sélectionner la date");
        datePicker.setValue(LocalDate.now().plusDays(1));

        spinnerHeure = new Spinner<>(0, 23, 9);
        spinnerHeure.setMaxWidth(80);
        spinnerHeure.setEditable(true);

        spinnerMinute = new Spinner<>(0, 59, 0, 15);
        spinnerMinute.setMaxWidth(80);
        spinnerMinute.setEditable(true);

        dateBox.getChildren().addAll(datePicker, new Label("à"), spinnerHeure, new Label("h"), spinnerMinute);
        grid.add(dateBox, 1, row++);

        // Lieu
        grid.add(new Label("Lieu:"), 0, row);
        txtLieu = new TextField();
        txtLieu.setPromptText("Lieu de l'événement");
        grid.add(txtLieu, 1, row++);

        // Capacité
        grid.add(new Label("Capacité max:"), 0, row);
        spinnerCapacite = new Spinner<>(1, 10000, 50);
        spinnerCapacite.setMaxWidth(100);
        spinnerCapacite.setEditable(true);
        grid.add(spinnerCapacite, 1, row++);

        // === CHAMPS SPÉCIFIQUES CONFERENCE ===
        containerConference = new VBox(10);
        containerConference.setPadding(new Insets(10, 0, 0, 0));

        Label lblConference = new Label("Spécifique à la Conférence");
        lblConference.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        containerConference.getChildren().add(lblConference);

        GridPane gridConf = new GridPane();
        gridConf.setHgap(15);
        gridConf.setVgap(10);

        gridConf.add(new Label("Thème:"), 0, 0);
        txtTheme = new TextField();
        txtTheme.setPromptText("Ex: Intelligence Artificielle, Management");
        gridConf.add(txtTheme, 1, 0);

        gridConf.add(new Label("Intervenants:"), 0, 1);
        txtIntervenants = new TextArea();
        txtIntervenants.setPromptText("Un intervenant par ligne:\nNom - Spécialité - Biographie");
        txtIntervenants.setPrefRowCount(3);
        txtIntervenants.setPrefColumnCount(30);
        gridConf.add(txtIntervenants, 1, 1);

        containerConference.getChildren().add(gridConf);

        // === CHAMPS SPÉCIFIQUES CONCERT ===
        containerConcert = new VBox(10);
        containerConcert.setPadding(new Insets(10, 0, 0, 0));

        Label lblConcert = new Label("Spécifique au Concert");
        lblConcert.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        containerConcert.getChildren().add(lblConcert);

        GridPane gridConcert = new GridPane();
        gridConcert.setHgap(15);
        gridConcert.setVgap(10);

        gridConcert.add(new Label("Artiste:"), 0, 0);
        txtArtiste = new TextField();
        txtArtiste.setPromptText("Nom de l'artiste ou du groupe");
        gridConcert.add(txtArtiste, 1, 0);

        gridConcert.add(new Label("Genre musical:"), 0, 1);
        txtGenre = new TextField();
        txtGenre.setPromptText("Ex: Rock, Jazz, Pop, Classique");
        gridConcert.add(txtGenre, 1, 1);

        containerConcert.getChildren().add(gridConcert);

        // === ASSEMBLAGE FINAL ===
        VBox mainContainer = new VBox(15);
        mainContainer.getChildren().addAll(grid, containerConference, containerConcert);

        // Initialement, masquer les conteneurs spécifiques
        containerConference.setVisible(false);
        containerConference.setManaged(false);
        containerConcert.setVisible(false);
        containerConcert.setManaged(false);

        getDialogPane().setContent(mainContainer);
    }

    private void configurerLogique() {
        // Validation en temps réel
        configurerValidation();

        // Changement de type d'événement
        comboType.valueProperty().addListener((obs, oldVal, newVal) -> {
            afficherChampsSpecifiques(newVal);
        });

        // Conversion du résultat
        setResultConverter(creerConvertisseurResultat());
    }

    private void configurerValidation() {
        // Listener pour valider les champs obligatoires
        Runnable validateur = () -> {
            boolean valide =
                    txtId.getText() != null && !txtId.getText().trim().isEmpty() &&
                            txtNom.getText() != null && !txtNom.getText().trim().isEmpty() &&
                            comboType.getValue() != null &&
                            datePicker.getValue() != null &&
                            txtLieu.getText() != null && !txtLieu.getText().trim().isEmpty();

            // Validation spécifique selon le type
            if ("Conference".equals(comboType.getValue())) {
                valide = valide && txtTheme.getText() != null && !txtTheme.getText().trim().isEmpty();
            } else if ("Concert".equals(comboType.getValue())) {
                valide = valide && txtArtiste.getText() != null && !txtArtiste.getText().trim().isEmpty();
            }

            Button btnCreer = (Button) getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0));
            btnCreer.setDisable(!valide);
        };

        // Ajouter les listeners
        txtId.textProperty().addListener((obs, old, newVal) -> validateur.run());
        txtNom.textProperty().addListener((obs, old, newVal) -> validateur.run());
        comboType.valueProperty().addListener((obs, old, newVal) -> validateur.run());
        datePicker.valueProperty().addListener((obs, old, newVal) -> validateur.run());
        txtLieu.textProperty().addListener((obs, old, newVal) -> validateur.run());
        txtTheme.textProperty().addListener((obs, old, newVal) -> validateur.run());
        txtArtiste.textProperty().addListener((obs, old, newVal) -> validateur.run());
    }

    private void afficherChampsSpecifiques(String type) {
        containerConference.setVisible("Conference".equals(type));
        containerConference.setManaged("Conference".equals(type));

        containerConcert.setVisible("Concert".equals(type));
        containerConcert.setManaged("Concert".equals(type));

        // Redimensionner le dialogue
        getDialogPane().getScene().getWindow().sizeToScene();
    }

    private void remplirChamps() {
        if (evenementAModifier == null) return;

        txtId.setText(evenementAModifier.getId());
        txtNom.setText(evenementAModifier.getNom());
        datePicker.setValue(evenementAModifier.getDate().toLocalDate());
        spinnerHeure.getValueFactory().setValue(evenementAModifier.getDate().getHour());
        spinnerMinute.getValueFactory().setValue(evenementAModifier.getDate().getMinute());
        txtLieu.setText(evenementAModifier.getLieu());
        spinnerCapacite.getValueFactory().setValue(evenementAModifier.getCapaciteMax());

        if (evenementAModifier instanceof Conference) {
            comboType.setValue("Conference");
            Conference conf = (Conference) evenementAModifier;
            txtTheme.setText(conf.getTheme());

            StringBuilder intervenants = new StringBuilder();
            for (Intervenant intervenant : conf.getIntervenants()) {
                intervenants.append(intervenant.getNom())
                        .append(" - ")
                        .append(intervenant.getSpecialite())
                        .append(" - ")
                        .append(intervenant.getBiographie())
                        .append("\n");
            }
            txtIntervenants.setText(intervenants.toString());

        } else if (evenementAModifier instanceof Concert) {
            comboType.setValue("Concert");
            Concert concert = (Concert) evenementAModifier;
            txtArtiste.setText(concert.getArtiste());
            txtGenre.setText(concert.getGenreMusical());
        }

        afficherChampsSpecifiques(comboType.getValue());
    }

    private Callback<ButtonType, Evenement> creerConvertisseurResultat() {
        return dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    return creerEvenement();
                } catch (Exception e) {
                    Alert erreur = new Alert(Alert.AlertType.ERROR);
                    erreur.setTitle("Erreur de validation");
                    erreur.setHeaderText("Impossible de créer l'événement");
                    erreur.setContentText(e.getMessage());
                    erreur.showAndWait();
                    return null;
                }
            }
            return null;
        };
    }

    private Evenement creerEvenement() throws Exception {
        // Données communes
        String id = txtId.getText().trim();
        String nom = txtNom.getText().trim();
        LocalDateTime dateTime = LocalDateTime.of(
                datePicker.getValue(),
                LocalTime.of(spinnerHeure.getValue(), spinnerMinute.getValue())
        );
        String lieu = txtLieu.getText().trim();
        int capacite = spinnerCapacite.getValue();

        // Validation de la date
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de l'événement doit être dans le futur");
        }

        Evenement evenement;
        String type = comboType.getValue();

        if ("Conference".equals(type)) {
            String theme = txtTheme.getText().trim();
            Conference conference = new Conference(id, nom, dateTime, lieu, capacite, theme);

            // Ajouter les intervenants
            String[] lignesIntervenants = txtIntervenants.getText().split("\n");
            for (String ligne : lignesIntervenants) {
                ligne = ligne.trim();
                if (!ligne.isEmpty()) {
                    String[] parties = ligne.split(" - ");
                    if (parties.length >= 2) {
                        String nomIntervenant = parties[0].trim();
                        String specialite = parties[1].trim();
                        String biographie = parties.length > 2 ? parties[2].trim() : "";

                        Intervenant intervenant = new Intervenant(nomIntervenant, specialite, biographie);
                        conference.ajouterIntervenant(intervenant);
                    }
                }
            }

            evenement = conference;

        } else if ("Concert".equals(type)) {
            String artiste = txtArtiste.getText().trim();
            String genre = txtGenre.getText().trim();
            evenement = new Concert(id, nom, dateTime, lieu, capacite, artiste, genre);

        } else {
            throw new IllegalArgumentException("Type d'événement non supporté: " + type);
        }

        return evenement;
    }
}
