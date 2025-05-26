package com.model;

import javafx.beans.property.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;

/**
 * Classe représentant un concert
 */
@JsonTypeName("concert")
public class Concert extends Evenement {

    @JsonIgnore
    private StringProperty artiste;
    @JsonIgnore
    private StringProperty genreMusical;

    // Constructeur par défaut
    public Concert() {
        super();
        this.artiste = new SimpleStringProperty();
        this.genreMusical = new SimpleStringProperty();
    }

    /**
     * Constructeur avec paramètres
     */
    public Concert(String id, String nom, LocalDateTime date, String lieu,
                   int capaciteMax, String artiste, String genreMusical) {
        super(id, nom, date, lieu, capaciteMax);
        this.artiste = new SimpleStringProperty(artiste);
        this.genreMusical = new SimpleStringProperty(genreMusical);
    }

    // Constructeur simplifié
    public Concert(String id, String nom, LocalDateTime date, String lieu, int capaciteMax) {
        this(id, nom, date, lieu, capaciteMax, "", "");
    }

    // ============ PROPERTIES JAVAFX ============

    public StringProperty artisteProperty() { return artiste; }
    public StringProperty genreMusicalProperty() { return genreMusical; }

    // ============ GETTERS ET SETTERS ============

    @JsonProperty("artiste")
    public String getArtiste() { return artiste.get(); }
    public void setArtiste(String artiste) { this.artiste.set(artiste); }

    @JsonProperty("genreMusical")
    public String getGenreMusical() { return genreMusical.get(); }
    public void setGenreMusical(String genreMusical) { this.genreMusical.set(genreMusical); }

    // ============ MÉTHODES MÉTIER ============

    @Override
    public void afficherDetails() {
        afficherInfosGenerales();
        System.out.println("=== DÉTAILS CONCERT ===");
        System.out.println("Artiste: " + getArtiste());
        System.out.println("Genre musical: " + getGenreMusical());
    }

    /**
     * Vérifie si le concert est du genre spécifié
     */
    public boolean estDuGenre(String genre) {
        return getGenreMusical().toLowerCase().contains(genre.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("Concert{id='%s', nom='%s', artiste='%s', genre='%s'}",
                getId(), getNom(), getArtiste(), getGenreMusical());
    }
}

