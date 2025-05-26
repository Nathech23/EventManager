package com.model;

import javafx.beans.property.*;
import javafx.collections.*;
import com.fasterxml.jackson.annotation.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Classe représentant un organisateur d'événements
 * Hérite de Participant
 */
public class Organisateur extends Participant {

    @JsonIgnore
    private ObservableList<Evenement> evenementsOrganises;

    // Constructeur par défaut
    public Organisateur() {
        super();
        this.evenementsOrganises = FXCollections.observableArrayList();
    }

    /**
     * Constructeur avec paramètres
     */
    public Organisateur(String id, String nom, String email) {
        super(id, nom, email);
        this.evenementsOrganises = FXCollections.observableArrayList();
    }

    // ============ GETTERS ET SETTERS ============

    @JsonProperty("evenementsOrganises")
    public List<Evenement> getEvenementsOrganises() {
        return new ArrayList<>(evenementsOrganises);
    }

    public void setEvenementsOrganises(List<Evenement> evenements) {
        this.evenementsOrganises.clear();
        this.evenementsOrganises.addAll(evenements);
    }

    @JsonIgnore
    public ObservableList<Evenement> getObservableEvenementsOrganises() {
        return evenementsOrganises;
    }

    @JsonIgnore
    public int getNombreEvenementsOrganises() {
        return evenementsOrganises.size();
    }

    // ============ MÉTHODES MÉTIER ============

    /**
     * Ajoute un événement organisé par cette personne
     */
    public void ajouterEvenementOrganise(Evenement evenement) {
        if (!evenementsOrganises.contains(evenement)) {
            evenementsOrganises.add(evenement);
            System.out.println(String.format("Événement %s ajouté à la liste des événements organisés par %s",
                    evenement.getNom(), getNom()));
        }
    }

    /**
     * Retire un événement de la liste des événements organisés
     */
    public boolean retirerEvenementOrganise(Evenement evenement) {
        boolean removed = evenementsOrganises.remove(evenement);
        if (removed) {
            System.out.println(String.format("Événement %s retiré de la liste des événements organisés par %s",
                    evenement.getNom(), getNom()));
        }
        return removed;
    }

    /**
     * Vérifie si cet organisateur organise l'événement spécifié
     */
    public boolean organise(Evenement evenement) {
        return evenementsOrganises.contains(evenement);
    }

    /**
     * Affiche la liste des événements organisés
     */
    public void afficherEvenementsOrganises() {
        System.out.println(String.format("=== ÉVÉNEMENTS ORGANISÉS PAR %s ===", getNom().toUpperCase()));
        if (evenementsOrganises.isEmpty()) {
            System.out.println("Aucun événement organisé");
        } else {
            for (int i = 0; i < evenementsOrganises.size(); i++) {
                Evenement evt = evenementsOrganises.get(i);
                System.out.println(String.format("%d. %s (%s) - %d participants",
                        i + 1, evt.getNom(), evt.getClass().getSimpleName(),
                        evt.getNombreParticipants()));
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Organisateur{id='%s', nom='%s', email='%s', evenements=%d}",
                getId(), getNom(), getEmail(), getNombreEvenementsOrganises());
    }
}
