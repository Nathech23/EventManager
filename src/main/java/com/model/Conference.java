package com.model;


import javafx.beans.property.*;
import javafx.collections.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Classe représentant une conférence
 */
@JsonTypeName("conference")
public class Conference extends Evenement {

    @JsonIgnore
    private StringProperty theme;
    @JsonIgnore
    private ObservableList<Intervenant> intervenants;

    // Constructeur par défaut
    public Conference() {
        super();
        this.theme = new SimpleStringProperty();
        this.intervenants = FXCollections.observableArrayList();
    }

    /**
     * Constructeur avec paramètres
     */
    public Conference(String id, String nom, LocalDateTime date, String lieu,
                      int capaciteMax, String theme) {
        super(id, nom, date, lieu, capaciteMax);
        this.theme = new SimpleStringProperty(theme);
        this.intervenants = FXCollections.observableArrayList();
    }

    // Constructeur sans thème (par défaut)
    public Conference(String id, String nom, LocalDateTime date, String lieu, int capaciteMax) {
        this(id, nom, date, lieu, capaciteMax, "");
    }

    // ============ PROPERTIES JAVAFX ============

    public StringProperty themeProperty() { return theme; }

    // ============ GETTERS ET SETTERS ============

    @JsonProperty("theme")
    public String getTheme() { return theme.get(); }
    public void setTheme(String theme) { this.theme.set(theme); }

    @JsonProperty("intervenants")
    public List<Intervenant> getIntervenants() {
        return new ArrayList<>(intervenants);
    }

    public void setIntervenants(List<Intervenant> intervenants) {
        this.intervenants.clear();
        this.intervenants.addAll(intervenants);
    }

    @JsonIgnore
    public ObservableList<Intervenant> getObservableIntervenants() {
        return intervenants;
    }

    // ============ MÉTHODES MÉTIER ============

    /**
     * Ajoute un intervenant à la conférence
     */
    public void ajouterIntervenant(Intervenant intervenant) {
        if (!intervenants.contains(intervenant)) {
            intervenants.add(intervenant);
            System.out.println(String.format("Intervenant %s ajouté à la conférence %s",
                    intervenant.getNom(), getNom()));
        }
    }

    /**
     * Retire un intervenant de la conférence
     */
    public boolean retirerIntervenant(Intervenant intervenant) {
        boolean removed = intervenants.remove(intervenant);
        if (removed) {
            System.out.println(String.format("Intervenant %s retiré de la conférence %s",
                    intervenant.getNom(), getNom()));
        }
        return removed;
    }

    @Override
    public void afficherDetails() {
        afficherInfosGenerales();
        System.out.println("=== DÉTAILS CONFÉRENCE ===");
        System.out.println("Thème: " + getTheme());
        System.out.println("Nombre d'intervenants: " + intervenants.size());

        if (!intervenants.isEmpty()) {
            System.out.println("Intervenants:");
            for (Intervenant intervenant : intervenants) {
                System.out.println("  - " + intervenant.getNom() +
                        " (" + intervenant.getSpecialite() + ")");
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Conference{id='%s', nom='%s', theme='%s', intervenants=%d}",
                getId(), getNom(), getTheme(), intervenants.size());
    }
}

