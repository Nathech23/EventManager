package com.model;

import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.*;
import javafx.collections.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import com.exception.CapaciteMaxAtteinteException;
import com.observer.*;

/**
 * Classe abstraite Evenement qui implémente EvenementObservable
 * Notifie automatiquement tous les participants inscrits lors de modifications
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Conference.class, name = "conference"),
        @JsonSubTypes.Type(value = Concert.class, name = "concert")
})
public abstract class Evenement implements EvenementObservable {

    // Properties JavaFX
    @JsonIgnore
    protected StringProperty id;
    @JsonIgnore
    protected StringProperty nom;
    @JsonIgnore
    protected ObjectProperty<LocalDateTime> date;
    @JsonIgnore
    protected StringProperty lieu;
    @JsonIgnore
    protected IntegerProperty capaciteMax;
    @JsonIgnore
    protected ObservableList<Participant> participants;
    @JsonIgnore
    protected BooleanProperty annule;

    // PATTERN OBSERVER - Liste des observers (thread-safe)
    @JsonIgnore
    private final List<EvenementObserver> observers;

    // Constructeur par défaut
    public Evenement() {
        this.id = new SimpleStringProperty();
        this.nom = new SimpleStringProperty();
        this.date = new SimpleObjectProperty<>();
        this.lieu = new SimpleStringProperty();
        this.capaciteMax = new SimpleIntegerProperty();
        this.participants = FXCollections.observableArrayList();
        this.annule = new SimpleBooleanProperty(false);
        this.observers = new CopyOnWriteArrayList<>(); // Thread-safe
    }

    /**
     * Constructeur avec paramètres
     */
    public Evenement(String id, String nom, LocalDateTime date, String lieu, int capaciteMax) {
        this.id = new SimpleStringProperty(id);
        this.nom = new SimpleStringProperty(nom);
        this.date = new SimpleObjectProperty<>(date);
        this.lieu = new SimpleStringProperty(lieu);
        this.capaciteMax = new SimpleIntegerProperty(capaciteMax);
        this.participants = FXCollections.observableArrayList();
        this.annule = new SimpleBooleanProperty(false);
        this.observers = new CopyOnWriteArrayList<>();
    }

    // ============ IMPLÉMENTATION EVENEMENT OBSERVABLE ============

    @Override
    public void ajouterObserver(EvenementObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            System.out.println(String.format(
                    "👁️  [OBSERVER] Observer ajouté à '%s'. Total: %d observers",
                    getNom(), observers.size()
            ));
        }
    }

    @Override
    public void retirerObserver(EvenementObserver observer) {
        if (observers.remove(observer)) {
            System.out.println(String.format(
                    "👁️  [OBSERVER] Observer retiré de '%s'. Total: %d observers",
                    getNom(), observers.size()
            ));
        }
    }

    @Override
    public void notifierModification(String message) {
        if (!observers.isEmpty()) {
            System.out.println(String.format(
                    "📢 [OBSERVER] Notification modification '%s' → %d observers",
                    getNom(), observers.size()
            ));

            for (EvenementObserver observer : observers) {
                try {
                    observer.onEvenementModifie(getNom(), message);
                } catch (Exception e) {
                    System.err.println("❌ Erreur notification observer: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void notifierAnnulation(String message) {
        if (!observers.isEmpty()) {
            System.out.println(String.format(
                    "📢 [OBSERVER] Notification annulation '%s' → %d observers",
                    getNom(), observers.size()
            ));

            for (EvenementObserver observer : observers) {
                try {
                    observer.onEvenementAnnule(getNom(), message);
                } catch (Exception e) {
                    System.err.println("❌ Erreur notification observer: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void notifierChangementInfo(String message) {
        if (!observers.isEmpty()) {
            System.out.println(String.format(
                    "📢 [OBSERVER] Notification changement '%s' → %d observers",
                    getNom(), observers.size()
            ));

            for (EvenementObserver observer : observers) {
                try {
                    observer.onEvenementInfoModifiee(getNom(), message);
                } catch (Exception e) {
                    System.err.println("❌ Erreur notification observer: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public List<EvenementObserver> getObservers() {
        return new ArrayList<>(observers);
    }

    // ============ PROPERTIES JAVAFX ============

    public StringProperty idProperty() { return id; }
    public StringProperty nomProperty() { return nom; }
    public ObjectProperty<LocalDateTime> dateProperty() { return date; }
    public StringProperty lieuProperty() { return lieu; }
    public IntegerProperty capaciteMaxProperty() { return capaciteMax; }
    public BooleanProperty annuleProperty() { return annule; }

    // ============ GETTERS ET SETTERS AVEC NOTIFICATIONS OBSERVER ============

    @JsonProperty("id")
    public String getId() { return id.get(); }
    public void setId(String id) { this.id.set(id); }

    @JsonProperty("nom")
    public String getNom() { return nom.get(); }
    public void setNom(String nom) {
        String ancienNom = this.nom.get();
        this.nom.set(nom);

        // NOTIFICATION AUTOMATIQUE via Observer
        if (ancienNom != null && !nom.equals(ancienNom)) {
            notifierChangementInfo(String.format("Nom modifié: '%s' → '%s'", ancienNom, nom));
        }
    }

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getDate() { return date.get(); }
    public void setDate(LocalDateTime date) {
        LocalDateTime ancienneDate = this.date.get();
        this.date.set(date);

        // NOTIFICATION AUTOMATIQUE via Observer
        if (ancienneDate != null && !date.equals(ancienneDate)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            notifierChangementInfo(String.format("Date modifiée: %s → %s",
                    ancienneDate.format(formatter),
                    date.format(formatter)));
        }
    }

    @JsonProperty("lieu")
    public String getLieu() { return lieu.get(); }
    public void setLieu(String lieu) {
        String ancienLieu = this.lieu.get();
        this.lieu.set(lieu);

        // NOTIFICATION AUTOMATIQUE via Observer
        if (ancienLieu != null && !lieu.equals(ancienLieu)) {
            notifierChangementInfo(String.format("Lieu modifié: '%s' → '%s'", ancienLieu, lieu));
        }
    }

    @JsonProperty("capaciteMax")
    public int getCapaciteMax() { return capaciteMax.get(); }
    public void setCapaciteMax(int capaciteMax) {
        int ancienneCapacite = this.capaciteMax.get();
        this.capaciteMax.set(capaciteMax);

        // NOTIFICATION AUTOMATIQUE via Observer
        if (capaciteMax != ancienneCapacite) {
            notifierChangementInfo(String.format("Capacité modifiée: %d → %d places",
                    ancienneCapacite, capaciteMax));
        }
    }

    @JsonProperty("annule")
    public boolean isAnnule() { return annule.get(); }
    public void setAnnule(boolean annule) { this.annule.set(annule); }

    @JsonProperty("participants")
    public List<Participant> getParticipants() {
        return new ArrayList<>(participants);
    }

    public void setParticipants(List<Participant> participants) {
        this.participants.clear();
        this.participants.addAll(participants);

        // AUTOMATIQUEMENT ajouter tous les participants comme observers
        for (Participant participant : participants) {
            ajouterObserver(participant);
        }
    }

    @JsonIgnore
    public ObservableList<Participant> getObservableParticipants() {
        return participants;
    }

    @JsonIgnore
    public int getNombreParticipants() {
        return participants.size();
    }

    @JsonIgnore
    public int getPlacesDisponibles() {
        return getCapaciteMax() - getNombreParticipants();
    }

    // ============ MÉTHODES MÉTIER AVEC PATTERN OBSERVER ============

    /**
     * Ajoute un participant ET l'inscrit automatiquement comme observer
     */
    public void ajouterParticipant(Participant participant) throws CapaciteMaxAtteinteException {
        if (isAnnule()) {
            throw new IllegalStateException("Impossible d'ajouter un participant à un événement annulé");
        }

        if (participants.size() >= getCapaciteMax()) {
            throw new CapaciteMaxAtteinteException(
                    String.format("Capacité maximale atteinte pour l'événement '%s' (%d/%d)",
                            getNom(), participants.size(), getCapaciteMax()));
        }

        if (participants.contains(participant)) {
            throw new IllegalArgumentException("Le participant est déjà inscrit à cet événement");
        }

        participants.add(participant);

        // AUTOMATIQUEMENT ajouter comme observer
        ajouterObserver(participant);

        System.out.println(String.format("✅ Participant %s ajouté à '%s' et inscrit comme observer",
                participant.getNom(), getNom()));

        // Notifier les autres participants
        notifierModification(String.format("Nouveau participant: %s (%d/%d places)",
                participant.getNom(),
                getNombreParticipants(),
                getCapaciteMax()));
    }

    /**
     * Retire un participant ET le désabonne automatiquement
     */
    public boolean retirerParticipant(Participant participant) {
        boolean removed = participants.remove(participant);
        if (removed) {
            // AUTOMATIQUEMENT retirer des observers
            retirerObserver(participant);

            System.out.println(String.format("❌ Participant %s retiré de '%s' et désabonné",
                    participant.getNom(), getNom()));

            // Notifier les autres participants
            notifierModification(String.format("Départ participant: %s (%d/%d places)",
                    participant.getNom(),
                    getNombreParticipants(),
                    getCapaciteMax()));
        }
        return removed;
    }

    /**
     * Annule l'événement et notifie AUTOMATIQUEMENT tous les observers
     */
    public void annuler() {
        setAnnule(true);

        String messageAnnulation = String.format(
                "L'événement '%s' prévu le %s à %s a été annulé. Nous nous excusons pour la gêne occasionnée.",
                getNom(),
                getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                getLieu()
        );

        // NOTIFICATION AUTOMATIQUE à tous les observers
        notifierAnnulation(messageAnnulation);

        System.out.println(String.format("🚫 Événement '%s' annulé. %d observers notifiés automatiquement.",
                getNom(), observers.size()));
    }

    public boolean estInscrit(Participant participant) {
        return participants.contains(participant);
    }

    // ============ MÉTHODES ABSTRAITES ============

    public abstract void afficherDetails();

    public void afficherInfosGenerales() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        System.out.println("=== INFORMATIONS GÉNÉRALES ===");
        System.out.println("ID: " + getId());
        System.out.println("Nom: " + getNom());
        System.out.println("Date: " + getDate().format(formatter));
        System.out.println("Lieu: " + getLieu());
        System.out.println("Capacité: " + getNombreParticipants() + "/" + getCapaciteMax());
        System.out.println("Statut: " + (isAnnule() ? "ANNULÉ" : "ACTIF"));
        System.out.println("Places disponibles: " + getPlacesDisponibles());
        System.out.println("Observers actifs: " + observers.size());
    }

    // ============ MÉTHODES UTILITAIRES ============

    @Override
    public String toString() {
        return String.format("%s{id='%s', nom='%s', participants=%d/%d, observers=%d}",
                getClass().getSimpleName(), getId(), getNom(),
                getNombreParticipants(), getCapaciteMax(), observers.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Evenement that = (Evenement) obj;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}