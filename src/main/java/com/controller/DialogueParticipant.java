package com.controller;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.util.Callback;
import com.model.*;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Dialogue pour créer un nouveau participant
 * Supporte Participant et Organisateur avec le Pattern Observer
 */
public class DialogueParticipant extends Dialog<Participant> {

    private TextField txtId;
    private TextField txtNom;
    private TextField txtEmail;
    private ComboBox<String> comboType;

    private Participant participantAModifier;
    private boolean modeModification;

    /**
     * Constructeur pour créer un nouveau participant
     */
    public DialogueParticipant() {
        this(null);
    }

    /**
     * Constructeur pour modifier un participant existant
     */
    public DialogueParticipant(Participant participant) {
        this.participantAModifier = participant;
        this.modeModification = (participant != null);

        configurerDialogue();
        creerInterface();
        configurerLogique();

        if (modeModification) {
            remplirChamps();
        }
    }

    private void configurerDialogue() {
        setTitle(modeModification ? "Modifier le participant" : "Nouveau participant");
        setHeaderText(modeModification ?
                "Modifier les informations du participant" :
                "Créer un nouveau participant (compatible Pattern Observer)");

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
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // ID
        grid.add(new Label("ID:"), 0, 0);
        txtId = new TextField();
        txtId.setPromptText("Ex: P001, ORG001");
        if (modeModification) {
            txtId.setEditable(false);
            txtId.setStyle("-fx-background-color: #f0f0f0;");
        }
        grid.add(txtId, 1, 0);

        // Nom
        grid.add(new Label("Nom complet:"), 0, 1);
        txtNom = new TextField();
        txtNom.setPromptText("Prénom et nom");
        txtNom.setPrefColumnCount(25);
        grid.add(txtNom, 1, 1);

        // Email
        grid.add(new Label("Email:"), 0, 2);
        txtEmail = new TextField();
        txtEmail.setPromptText("adresse@email.com");
        grid.add(txtEmail, 1, 2);

        // Type
        grid.add(new Label("Type:"), 0, 3);
        comboType = new ComboBox<>();
        comboType.getItems().addAll("Participant", "Organisateur");
        comboType.setValue("Participant");
        comboType.setPromptText("Type de participant");
        if (modeModification) {
            comboType.setDisable(true);
        }
        grid.add(comboType, 1, 3);

        // Note explicative
        Label noteObserver = new Label(
                "💡 Les participants deviennent automatiquement 'Observers' " +
                        "lorsqu'ils s'inscrivent à un événement et reçoivent les notifications."
        );
        noteObserver.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        noteObserver.setWrapText(true);
        noteObserver.setMaxWidth(400);
        grid.add(noteObserver, 0, 4, 2, 1);

        getDialogPane().setContent(grid);
    }

    private void configurerLogique() {
        // Validation en temps réel
        configurerValidation();

        // Conversion du résultat
        setResultConverter(creerConvertisseurResultat());
    }

    private void configurerValidation() {
        Runnable validateur = () -> {
            boolean valide =
                    txtId.getText() != null && !txtId.getText().trim().isEmpty() &&
                            txtNom.getText() != null && !txtNom.getText().trim().isEmpty() &&
                            txtEmail.getText() != null && !txtEmail.getText().trim().isEmpty() &&
                            estEmailValide(txtEmail.getText().trim()) &&
                            comboType.getValue() != null;

            Button btnCreer = (Button) getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0));
            btnCreer.setDisable(!valide);
        };

        // Ajouter les listeners
        txtId.textProperty().addListener((obs, old, newVal) -> validateur.run());
        txtNom.textProperty().addListener((obs, old, newVal) -> validateur.run());
        txtEmail.textProperty().addListener((obs, old, newVal) -> {
            validateur.run();
            // Changer la couleur du champ email selon la validité
            if (txtEmail.getText().trim().isEmpty()) {
                txtEmail.setStyle("");
            } else if (estEmailValide(txtEmail.getText().trim())) {
                txtEmail.setStyle("-fx-border-color: green;");
            } else {
                txtEmail.setStyle("-fx-border-color: red;");
            }
        });
        comboType.valueProperty().addListener((obs, old, newVal) -> validateur.run());
    }

    private boolean estEmailValide(String email) {
        String regexEmail = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(regexEmail, email);
    }

    private void remplirChamps() {
        if (participantAModifier == null) return;

        txtId.setText(participantAModifier.getId());
        txtNom.setText(participantAModifier.getNom());
        txtEmail.setText(participantAModifier.getEmail());

        if (participantAModifier instanceof Organisateur) {
            comboType.setValue("Organisateur");
        } else {
            comboType.setValue("Participant");
        }
    }

    private Callback<ButtonType, Participant> creerConvertisseurResultat() {
        return dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    return creerParticipant();
                } catch (Exception e) {
                    Alert erreur = new Alert(Alert.AlertType.ERROR);
                    erreur.setTitle("Erreur de validation");
                    erreur.setHeaderText("Impossible de créer le participant");
                    erreur.setContentText(e.getMessage());
                    erreur.showAndWait();
                    return null;
                }
            }
            return null;
        };
    }

    private Participant creerParticipant() throws Exception {
        String id = txtId.getText().trim();
        String nom = txtNom.getText().trim();
        String email = txtEmail.getText().trim();
        String type = comboType.getValue();

        // Validations supplémentaires
        if (id.isEmpty() || nom.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("Tous les champs sont obligatoires");
        }

        if (!estEmailValide(email)) {
            throw new IllegalArgumentException("L'adresse email n'est pas valide");
        }

        Participant participant;
        if ("Organisateur".equals(type)) {
            participant = new Organisateur(id, nom, email);
        } else {
            participant = new Participant(id, nom, email);
        }

        return participant;
    }
}
