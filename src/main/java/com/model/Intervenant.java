package com.model;

import javafx.beans.property.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Classe représentant un intervenant dans une conférence
 */
public class Intervenant {

    @JsonIgnore
    private StringProperty nom;
    @JsonIgnore
    private StringProperty specialite;
    @JsonIgnore
    private StringProperty biographie;

    // Constructeur par défaut
    public Intervenant() {
        this.nom = new SimpleStringProperty();
        this.specialite = new SimpleStringProperty();
        this.biographie = new SimpleStringProperty();
    }

    /**
     * Constructeur avec paramètres
     */
    public Intervenant(String nom, String specialite, String biographie) {
        this.nom = new SimpleStringProperty(nom);
        this.specialite = new SimpleStringProperty(specialite);
        this.biographie = new SimpleStringProperty(biographie);
    }

    // Constructeur simplifié
    public Intervenant(String nom, String specialite) {
        this(nom, specialite, "");
    }

    // ============ PROPERTIES JAVAFX ============

    public StringProperty nomProperty() { return nom; }
    public StringProperty specialiteProperty() { return specialite; }
    public StringProperty biographieProperty() { return biographie; }

    // ============ GETTERS ET SETTERS ============

    @JsonProperty("nom")
    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }

    @JsonProperty("specialite")
    public String getSpecialite() { return specialite.get(); }
    public void setSpecialite(String specialite) { this.specialite.set(specialite); }

    @JsonProperty("biographie")
    public String getBiographie() { return biographie.get(); }
    public void setBiographie(String biographie) { this.biographie.set(biographie); }

    // ============ MÉTHODES UTILITAIRES ============

    @Override
    public String toString() {
        return String.format("Intervenant{nom='%s', specialite='%s'}",
                getNom(), getSpecialite());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Intervenant that = (Intervenant) obj;
        return getNom().equals(that.getNom()) &&
                getSpecialite().equals(that.getSpecialite());
    }

    @Override
    public int hashCode() {
        return (getNom() + getSpecialite()).hashCode();
    }
}

