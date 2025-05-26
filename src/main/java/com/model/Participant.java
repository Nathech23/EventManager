package com.model;

import javafx.beans.property.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.observer.EvenementObserver;


/**
 * Classe représentant un participant à un événement
 */
public class Participant implements EvenementObserver {

    // Properties JavaFX pour le binding avec l'interface
    @JsonIgnore
    private StringProperty id;
    @JsonIgnore
    private StringProperty nom;
    @JsonIgnore
    private StringProperty email;

    // Constructeur par défaut pour Jackson
    public Participant () {
        this.id = new SimpleStringProperty();
        this.nom = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
    }

    /**
     * Constructeur avec paramètres
     * @param id Identifiant unique du participant
     * @param nom Nom du participant
     * @param email Email du participant
     */
    public Participant(String id, String nom, String email) {
        this.id = new SimpleStringProperty(id);
        this.nom = new SimpleStringProperty(nom);
        this.email = new SimpleStringProperty(email);
    }

    // ============ IMPLÉMENTATION EVENEMENT OBSERVER ============

    @Override
    public void onEvenementModifie(String evenementNom, String message) {
        System.out.println(String.format(
                "🔔 [%s] ÉVÉNEMENT MODIFIÉ: %s\n   📝 %s",
                getNom(), evenementNom, message
        ));
    }

    @Override
    public void onEvenementAnnule(String evenementNom, String message) {
        System.out.println(String.format(
                "❌ [%s] ÉVÉNEMENT ANNULÉ: %s\n   📝 %s\n   ⚠️  Vous étiez inscrit(e) à cet événement!",
                getNom(), evenementNom, message
        ));
    }

    @Override
    public void onEvenementInfoModifiee(String evenementNom, String message) {
        System.out.println(String.format(
                "ℹ️  [%s] CHANGEMENT: %s\n   📝 %s",
                getNom(), evenementNom, message
        ));
    }

    // ============ PROPERTIES JAVAFX ============

    /**
     * Property pour l'ID (binding JavaFX)
     */
    public StringProperty idProperty() {
        return id;
    }

    /**
     * Property pour le nom (binding JavaFX)
     */
    public StringProperty nomProperty() {
        return nom;
    }

    /**
     * Property pour l'email (binding JavaFX)
     */
    public StringProperty emailProperty() {
        return email;
    }

    // ============ GETTERS ET SETTERS STANDARDS ============

    @JsonProperty("id")
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    @JsonProperty("nom")
    public String getNom() {
        return nom.get();
    }

    public void setNom(String nom) {
        this.nom.set(nom);
    }

    @JsonProperty("email")
    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    // ============ MÉTHODES UTILITAIRES ============

    @Override
    public String toString() {
        return String.format("Participant{id='%s', nom='%s', email='%s'}",
                getId(), getNom(), getEmail());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Participant that = (Participant) obj;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
