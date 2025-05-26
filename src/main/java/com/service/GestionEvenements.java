package com.service;

import javafx.collections.*;
import javafx.beans.property.*;
import com.model.*;
import com.exception.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * Classe Singleton pour gérer tous les événements
 * Thread-safe et compatible JavaFX
 * Utilise uniquement le Pattern Observer pour les notifications
 */
public class GestionEvenements {

    // Instance unique (Singleton)
    private static volatile GestionEvenements instance;

    // Collections thread-safe pour les événements
    private final ObservableMap<String, Evenement> evenementsMap;
    private final ObservableList<Evenement> evenementsList;

    // Collections pour les participants
    private final ObservableMap<String, Participant> participantsMap;
    private final ObservableList<Participant> participantsList;

    // Constructeur privé (Singleton)
    private GestionEvenements() {
        this.evenementsMap = FXCollections.observableMap(new ConcurrentHashMap<>());
        this.evenementsList = FXCollections.observableArrayList();
        this.participantsMap = FXCollections.observableMap(new ConcurrentHashMap<>());
        this.participantsList = FXCollections.observableArrayList();

        // Synchronisation entre Map et List pour les événements
        evenementsMap.addListener((MapChangeListener<String, Evenement>) change -> {
            if (change.wasAdded()) {
                if (!evenementsList.contains(change.getValueAdded())) {
                    evenementsList.add(change.getValueAdded());
                }
            }
            if (change.wasRemoved()) {
                evenementsList.remove(change.getValueRemoved());
            }
        });

        // Synchronisation entre Map et List pour les participants
        participantsMap.addListener((MapChangeListener<String, Participant>) change -> {
            if (change.wasAdded()) {
                if (!participantsList.contains(change.getValueAdded())) {
                    participantsList.add(change.getValueAdded());
                }
            }
            if (change.wasRemoved()) {
                participantsList.remove(change.getValueRemoved());
            }
        });
    }

    /**
     * Obtient l'instance unique (thread-safe)
     * @return L'instance de GestionEvenements
     */
    public static GestionEvenements getInstance() {
        if (instance == null) {
            synchronized (GestionEvenements.class) {
                if (instance == null) {
                    instance = new GestionEvenements();
                }
            }
        }
        return instance;
    }

    // ============ GETTERS POUR JAVAFX ============

    /**
     * Retourne la liste observable des événements pour JavaFX
     */
    public ObservableList<Evenement> getObservableEvenements() {
        return evenementsList;
    }

    /**
     * Retourne la liste observable des participants pour JavaFX
     */
    public ObservableList<Participant> getObservableParticipants() {
        return participantsList;
    }

    /**
     * Retourne la map observable des événements
     */
    public ObservableMap<String, Evenement> getEvenementsMap() {
        return evenementsMap;
    }

    // ============ GESTION DES ÉVÉNEMENTS ============

    /**
     * Ajoute un événement
     * @param evenement L'événement à ajouter
     * @throws EvenementDejaExistantException Si l'ID existe déjà
     */
    public void ajouterEvenement(Evenement evenement) throws EvenementDejaExistantException {
        Objects.requireNonNull(evenement, "L'événement ne peut pas être null");
        Objects.requireNonNull(evenement.getId(), "L'ID de l'événement ne peut pas être null");

        if (evenementsMap.containsKey(evenement.getId())) {
            throw new EvenementDejaExistantException(
                    "Un événement avec l'ID '" + evenement.getId() + "' existe déjà",
                    evenement.getId());
        }

        evenementsMap.put(evenement.getId(), evenement);
        System.out.println("📅 [GESTION] Événement ajouté: " + evenement.getNom());

        // Notification globale via console (remplace NotificationService)
        System.out.println("🔔 [NOTIFICATION] Nouvel événement créé: " + evenement.getNom());
    }

    /**
     * Supprime un événement et notifie automatiquement via Pattern Observer
     * @param evenementId L'ID de l'événement à supprimer
     * @throws EvenementIntrouvableException Si l'événement n'existe pas
     */
    public void supprimerEvenement(String evenementId) throws EvenementIntrouvableException {
        Evenement evenement = evenementsMap.get(evenementId);
        if (evenement == null) {
            throw new EvenementIntrouvableException(
                    "Aucun événement trouvé avec l'ID: " + evenementId, evenementId);
        }

        // PATTERN OBSERVER : Annuler l'événement notifie automatiquement tous les participants
        if (!evenement.getParticipants().isEmpty()) {
            System.out.println(String.format(
                    "🔔 [OBSERVER] Annulation de l'événement '%s' - %d participants seront notifiés automatiquement",
                    evenement.getNom(), evenement.getNombreParticipants()
            ));

            // L'annulation déclenche automatiquement les notifications via Observer
            evenement.annuler();
        }

        // Supprimer de la collection
        evenementsMap.remove(evenementId);
        System.out.println("🗑️ [GESTION] Événement supprimé: " + evenement.getNom());
    }

    /**
     * Recherche un événement par ID
     * @param evenementId L'ID de l'événement
     * @return L'événement trouvé
     * @throws EvenementIntrouvableException Si l'événement n'existe pas
     */
    public Evenement rechercherEvenement(String evenementId) throws EvenementIntrouvableException {
        Evenement evenement = evenementsMap.get(evenementId);
        if (evenement == null) {
            throw new EvenementIntrouvableException(
                    "Aucun événement trouvé avec l'ID: " + evenementId, evenementId);
        }
        return evenement;
    }

    /**
     * Recherche des événements par critères
     */
    public List<Evenement> rechercherEvenementsParNom(String nom) {
        return evenementsList.stream()
                .filter(e -> e.getNom().toLowerCase().contains(nom.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Evenement> rechercherEvenementsParLieu(String lieu) {
        return evenementsList.stream()
                .filter(e -> e.getLieu().toLowerCase().contains(lieu.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Evenement> rechercherEvenementsParType(Class<? extends Evenement> type) {
        return evenementsList.stream()
                .filter(type::isInstance)
                .collect(Collectors.toList());
    }

    public List<Evenement> rechercherEvenementsParDate(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return evenementsList.stream()
                .filter(e -> !e.getDate().isBefore(dateDebut) && !e.getDate().isAfter(dateFin))
                .collect(Collectors.toList());
    }

    // ============ GESTION DES PARTICIPANTS ============

    /**
     * Ajoute un participant au système
     */
    public void ajouterParticipant(Participant participant) {
        Objects.requireNonNull(participant, "Le participant ne peut pas être null");
        Objects.requireNonNull(participant.getId(), "L'ID du participant ne peut pas être null");

        participantsMap.put(participant.getId(), participant);
        System.out.println("👤 [GESTION] Participant ajouté: " + participant.getNom());
    }

    /**
     * Recherche un participant par ID
     */
    public Participant rechercherParticipant(String participantId) throws ParticipantIntrouvableException {
        Participant participant = participantsMap.get(participantId);
        if (participant == null) {
            throw new ParticipantIntrouvableException(
                    "Aucun participant trouvé avec l'ID: " + participantId, participantId);
        }
        return participant;
    }

    /**
     * Inscrit un participant à un événement
     * Le participant devient automatiquement observer de l'événement
     */
    public void inscrireParticipant(String participantId, String evenementId)
            throws ParticipantIntrouvableException, EvenementIntrouvableException, CapaciteMaxAtteinteException {

        Participant participant = rechercherParticipant(participantId);
        Evenement evenement = rechercherEvenement(evenementId);

        // L'ajout du participant à l'événement le rend automatiquement observer
        evenement.ajouterParticipant(participant);

        System.out.println(String.format(
                "✅ [INSCRIPTION] %s inscrit à '%s' et devient observer automatiquement",
                participant.getNom(), evenement.getNom()
        ));
    }

    /**
     * Désinscrit un participant d'un événement
     * Le participant est automatiquement retiré des observers
     */
    public void desinscrireParticipant(String participantId, String evenementId)
            throws ParticipantIntrouvableException, EvenementIntrouvableException {

        Participant participant = rechercherParticipant(participantId);
        Evenement evenement = rechercherEvenement(evenementId);

        // Le retrait du participant le retire automatiquement des observers
        boolean removed = evenement.retirerParticipant(participant);

        if (removed) {
            System.out.println(String.format(
                    "❌ [DÉSINSCRIPTION] %s désinscrit de '%s' et retiré des observers",
                    participant.getNom(), evenement.getNom()
            ));
        }
    }

    /**
     * Modifie un événement et déclenche automatiquement les notifications Observer
     */
    public void modifierEvenement(String evenementId, String nouveauNom, LocalDateTime nouvelleDate, String nouveauLieu)
            throws EvenementIntrouvableException {

        Evenement evenement = rechercherEvenement(evenementId);
        boolean modification = false;

        // Modifications avec notifications automatiques via Observer
        if (nouveauNom != null && !nouveauNom.equals(evenement.getNom())) {
            evenement.setNom(nouveauNom);
            modification = true;
        }

        if (nouvelleDate != null && !nouvelleDate.equals(evenement.getDate())) {
            evenement.setDate(nouvelleDate);
            modification = true;
        }

        if (nouveauLieu != null && !nouveauLieu.equals(evenement.getLieu())) {
            evenement.setLieu(nouveauLieu);
            modification = true;
        }

        if (modification) {
            System.out.println(String.format(
                    "📝 [MODIFICATION] Événement '%s' modifié - Participants notifiés automatiquement via Observer",
                    evenement.getNom()
            ));
        }
    }

    // ============ STATISTIQUES ============

    /**
     * Obtient des statistiques sur les événements
     */
    public Map<String, Long> getStatistiquesParType() {
        return evenementsList.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getClass().getSimpleName(),
                        Collectors.counting()
                ));
    }

    public int getTotalParticipants() {
        return evenementsList.stream()
                .mapToInt(Evenement::getNombreParticipants)
                .sum();
    }

    public int getTotalObservers() {
        return evenementsList.stream()
                .mapToInt(e -> e.getObservers().size())
                .sum();
    }

    public OptionalDouble getTauxOccupationMoyen() {
        return evenementsList.stream()
                .mapToDouble(e -> (double) e.getNombreParticipants() / e.getCapaciteMax() * 100)
                .average();
    }

    /**
     * Statistiques du Pattern Observer
     */
    public Map<String, Integer> getStatistiquesObserver() {
        Map<String, Integer> stats = new HashMap<>();

        for (Evenement evenement : evenementsList) {
            stats.put(evenement.getNom(), evenement.getObservers().size());
        }

        return stats;
    }

    // ============ UTILITAIRES ============

    /**
     * Vide tous les événements et participants (pour les tests)
     */
    public void viderTout() {
        evenementsMap.clear();
        participantsMap.clear();
        System.out.println("🧹 [GESTION] Toutes les données ont été effacées");
    }

    /**
     * Affiche un résumé de tous les événements avec informations Observer
     */
    public void afficherResume() {
        System.out.println("=".repeat(50));
        System.out.println("📊 RÉSUMÉ DES ÉVÉNEMENTS (Pattern Observer)");
        System.out.println("=".repeat(50));
        System.out.println("Nombre total d'événements: " + evenementsList.size());
        System.out.println("Nombre total de participants: " + participantsList.size());
        System.out.println("Total des inscrits: " + getTotalParticipants());
        System.out.println("Total des observers: " + getTotalObservers());

        Map<String, Long> stats = getStatistiquesParType();
        System.out.println("\n📈 Répartition par type:");
        stats.forEach((type, count) ->
                System.out.println("  " + type + ": " + count));

        OptionalDouble tauxMoyen = getTauxOccupationMoyen();
        if (tauxMoyen.isPresent()) {
            System.out.printf("📊 Taux d'occupation moyen: %.1f%%\n", tauxMoyen.getAsDouble());
        }

        System.out.println("\n🔔 Statistiques Observer par événement:");
        Map<String, Integer> statsObserver = getStatistiquesObserver();
        if (statsObserver.isEmpty()) {
            System.out.println("  Aucun événement avec observers");
        } else {
            statsObserver.forEach((nomEvenement, nbObservers) ->
                    System.out.println(String.format("  %s: %d observers", nomEvenement, nbObservers)));
        }
        System.out.println("=".repeat(50));
    }

    /**
     * Démarre une démonstration du Pattern Observer
     */
    public void demonstrationPatternObserver() {
        System.out.println("\n🎭 DÉMONSTRATION PATTERN OBSERVER");
        System.out.println("-".repeat(40));

        try {
            // Créer un événement de test
            Conference demo = new Conference(
                    "DEMO001",
                    "Démo Pattern Observer",
                    LocalDateTime.now().plusDays(1),
                    "Salle Demo",
                    5,
                    "Démonstration"
            );

            // Créer des participants
            Participant alice = new Participant("DEMO_P1", "Alice Observer", "alice@demo.com");
            Participant bob = new Participant("DEMO_P2", "Bob Observer", "bob@demo.com");

            // Ajouter au système
            ajouterEvenement(demo);
            ajouterParticipant(alice);
            ajouterParticipant(bob);

            // Inscrire les participants (ils deviennent observers automatiquement)
            inscrireParticipant("DEMO_P1", "DEMO001");
            inscrireParticipant("DEMO_P2", "DEMO001");

            System.out.println("\n🔄 Test des modifications (déclenchent notifications Observer):");

            // Modifier l'événement → notifications automatiques
            modifierEvenement("DEMO001", null, null, "Nouvelle Salle Demo");
            Thread.sleep(100);

            modifierEvenement("DEMO001", "Démo Observer Modifiée", null, null);
            Thread.sleep(100);

            // Annuler → notification automatique à tous les observers
            System.out.println("\n❌ Test d'annulation:");
            supprimerEvenement("DEMO001");

            System.out.println("\n✅ Démonstration terminée!");

        } catch (Exception e) {
            System.err.println("Erreur durant la démonstration: " + e.getMessage());
        }
    }
}