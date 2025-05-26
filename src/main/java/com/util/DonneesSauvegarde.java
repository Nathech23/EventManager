package com.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.model.*;
import com.exception.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe conteneur complète pour sauvegarder toutes les données du système
 * avec métadonnées, validation et reconstruction des relations Observer
 */
public class DonneesSauvegarde {

    // ============ DONNÉES PRINCIPALES ============
    @JsonProperty("evenements")
    private List<Evenement> evenements = new ArrayList<>();

    @JsonProperty("participants")
    private List<Participant> participants = new ArrayList<>();

    // ============ MÉTADONNÉES DE SAUVEGARDE ============
    @JsonProperty("dateSauvegarde")
    private LocalDateTime dateSauvegarde;

    @JsonProperty("versionApplication")
    private String versionApplication = "1.0";

    @JsonProperty("versionFormatDonnees")
    private String versionFormatDonnees = "1.0";

    @JsonProperty("nombreObserversTotal")
    private int nombreObserversTotal;

    @JsonProperty("commentaireSauvegarde")
    private String commentaireSauvegarde;

    // ============ STATISTIQUES DE SAUVEGARDE ============
    @JsonProperty("statistiques")
    private StatistiquesSauvegarde statistiques;

    /**
     * Classe pour les statistiques de la sauvegarde
     */
    public static class StatistiquesSauvegarde {
        @JsonProperty("nombreConferences")
        private int nombreConferences;

        @JsonProperty("nombreConcerts")
        private int nombreConcerts;

        @JsonProperty("nombreOrganisateurs")
        private int nombreOrganisateurs;

        @JsonProperty("nombreParticipantsStandard")
        private int nombreParticipantsStandard;

        @JsonProperty("nombreInscriptionsTotal")
        private int nombreInscriptionsTotal;

        @JsonProperty("tauxOccupationMoyen")
        private double tauxOccupationMoyen;

        // Constructeurs
        public StatistiquesSauvegarde() {}

        public StatistiquesSauvegarde(List<Evenement> evenements, List<Participant> participants) {
            calculerStatistiques(evenements, participants);
        }

        private void calculerStatistiques(List<Evenement> evenements, List<Participant> participants) {
            // Compter par type d'événement
            nombreConferences = (int) evenements.stream()
                    .filter(e -> e instanceof Conference)
                    .count();
            nombreConcerts = (int) evenements.stream()
                    .filter(e -> e instanceof Concert)
                    .count();

            // Compter par type de participant
            nombreOrganisateurs = (int) participants.stream()
                    .filter(p -> p instanceof Organisateur)
                    .count();
            nombreParticipantsStandard = participants.size() - nombreOrganisateurs;

            // Inscriptions totales
            nombreInscriptionsTotal = evenements.stream()
                    .mapToInt(Evenement::getNombreParticipants)
                    .sum();

            // Taux d'occupation moyen
            tauxOccupationMoyen = evenements.stream()
                    .mapToDouble(e -> (double) e.getNombreParticipants() / e.getCapaciteMax() * 100)
                    .average()
                    .orElse(0.0);
        }

        // Getters et setters
        public int getNombreConferences() { return nombreConferences; }
        public void setNombreConferences(int nombreConferences) { this.nombreConferences = nombreConferences; }

        public int getNombreConcerts() { return nombreConcerts; }
        public void setNombreConcerts(int nombreConcerts) { this.nombreConcerts = nombreConcerts; }

        public int getNombreOrganisateurs() { return nombreOrganisateurs; }
        public void setNombreOrganisateurs(int nombreOrganisateurs) { this.nombreOrganisateurs = nombreOrganisateurs; }

        public int getNombreParticipantsStandard() { return nombreParticipantsStandard; }
        public void setNombreParticipantsStandard(int nombreParticipantsStandard) { this.nombreParticipantsStandard = nombreParticipantsStandard; }

        public int getNombreInscriptionsTotal() { return nombreInscriptionsTotal; }
        public void setNombreInscriptionsTotal(int nombreInscriptionsTotal) { this.nombreInscriptionsTotal = nombreInscriptionsTotal; }

        public double getTauxOccupationMoyen() { return tauxOccupationMoyen; }
        public void setTauxOccupationMoyen(double tauxOccupationMoyen) { this.tauxOccupationMoyen = tauxOccupationMoyen; }

        @Override
        public String toString() {
            return String.format(
                    "Statistiques{conférences=%d, concerts=%d, organisateurs=%d, participants=%d, inscriptions=%d, taux=%.1f%%}",
                    nombreConferences, nombreConcerts, nombreOrganisateurs,
                    nombreParticipantsStandard, nombreInscriptionsTotal, tauxOccupationMoyen
            );
        }
    }

    // ============ CONSTRUCTEURS ============

    /**
     * Constructeur par défaut pour Jackson
     */
    public DonneesSauvegarde() {
        this.dateSauvegarde = LocalDateTime.now();
        this.statistiques = new StatistiquesSauvegarde();
    }

    /**
     * Constructeur avec données
     */
    public DonneesSauvegarde(List<Evenement> evenements, List<Participant> participants) {
        this();
        this.evenements = new ArrayList<>(evenements);
        this.participants = new ArrayList<>(participants);
        this.nombreObserversTotal = calculerNombreObservers();
        this.statistiques = new StatistiquesSauvegarde(evenements, participants);
        this.commentaireSauvegarde = genererCommentaireAutomatique();
    }

    /**
     * Constructeur avec commentaire personnalisé
     */
    public DonneesSauvegarde(List<Evenement> evenements, List<Participant> participants, String commentaire) {
        this(evenements, participants);
        this.commentaireSauvegarde = commentaire;
    }

    // ============ MÉTHODES DE CALCUL ============

    private int calculerNombreObservers() {
        return evenements.stream()
                .mapToInt(e -> e.getObservers().size())
                .sum();
    }

    private String genererCommentaireAutomatique() {
        return String.format(
                "Sauvegarde automatique - %d événements, %d participants, %d relations observer",
                evenements.size(), participants.size(), nombreObserversTotal
        );
    }

    // ============ VALIDATION COMPLÈTE ============

    /**
     * Valide les données chargées de manière exhaustive
     */
    public void valider() throws ValidationException {
        List<String> erreurs = new ArrayList<>();

        // Validation des listes principales
        validerListeEvenements(erreurs);
        validerListeParticipants(erreurs);

        // Validation des métadonnées
        validerMetadonnees(erreurs);

        // Validation de la cohérence des données
        validerCoherenceDonnees(erreurs);

        // Validation des relations
        validerRelations(erreurs);

        if (!erreurs.isEmpty()) {
            throw new ValidationException(erreurs);
        }
    }

    private void validerListeEvenements(List<String> erreurs) {
        if (evenements == null) {
            erreurs.add("Liste des événements manquante");
            return;
        }

        // IDs uniques
        Set<String> idsUniques = new HashSet<>();
        for (Evenement evenement : evenements) {
            if (evenement.getId() == null || evenement.getId().trim().isEmpty()) {
                erreurs.add("Événement avec ID manquant détecté");
            } else if (!idsUniques.add(evenement.getId())) {
                erreurs.add("ID d'événement dupliqué: " + evenement.getId());
            }

            // Validation des champs obligatoires
            if (evenement.getNom() == null || evenement.getNom().trim().isEmpty()) {
                erreurs.add("Événement sans nom: " + evenement.getId());
            }
            if (evenement.getDate() == null) {
                erreurs.add("Événement sans date: " + evenement.getId());
            }
            if (evenement.getCapaciteMax() <= 0) {
                erreurs.add("Capacité invalide pour événement: " + evenement.getId());
            }
        }
    }

    private void validerListeParticipants(List<String> erreurs) {
        if (participants == null) {
            erreurs.add("Liste des participants manquante");
            return;
        }

        // IDs uniques
        Set<String> idsUniques = new HashSet<>();
        Set<String> emailsUniques = new HashSet<>();

        for (Participant participant : participants) {
            if (participant.getId() == null || participant.getId().trim().isEmpty()) {
                erreurs.add("Participant avec ID manquant détecté");
            } else if (!idsUniques.add(participant.getId())) {
                erreurs.add("ID de participant dupliqué: " + participant.getId());
            }

            // Validation nom
            if (participant.getNom() == null || participant.getNom().trim().isEmpty()) {
                erreurs.add("Participant sans nom: " + participant.getId());
            }

            // Validation email
            if (participant.getEmail() == null || !participant.getEmail().contains("@")) {
                erreurs.add("Email invalide pour participant: " + participant.getId());
            } else if (!emailsUniques.add(participant.getEmail().toLowerCase())) {
                erreurs.add("Email dupliqué: " + participant.getEmail());
            }
        }
    }

    private void validerMetadonnees(List<String> erreurs) {
        if (dateSauvegarde == null) {
            erreurs.add("Date de sauvegarde manquante");
        } else if (dateSauvegarde.isAfter(LocalDateTime.now().plusMinutes(5))) {
            erreurs.add("Date de sauvegarde dans le futur suspect");
        }

        if (versionApplication == null || versionApplication.trim().isEmpty()) {
            erreurs.add("Version application manquante");
        }

        if (nombreObserversTotal < 0) {
            erreurs.add("Nombre d'observers négatif");
        }
    }

    private void validerCoherenceDonnees(List<String> erreurs) {
        // Vérifier que le nombre d'observers est cohérent
        int observersCalcules = calculerNombreObservers();
        if (Math.abs(observersCalcules - nombreObserversTotal) > evenements.size()) {
            erreurs.add("Incohérence dans le nombre d'observers: calculé=" +
                    observersCalcules + ", sauvegardé=" + nombreObserversTotal);
        }

        // Vérifier les statistiques si présentes
        if (statistiques != null) {
            int totalEvenements = statistiques.getNombreConferences() + statistiques.getNombreConcerts();
            if (totalEvenements != evenements.size()) {
                erreurs.add("Incohérence dans les statistiques d'événements");
            }

            int totalParticipants = statistiques.getNombreOrganisateurs() + statistiques.getNombreParticipantsStandard();
            if (totalParticipants != participants.size()) {
                erreurs.add("Incohérence dans les statistiques de participants");
            }
        }
    }

    private void validerRelations(List<String> erreurs) {
        // Créer un index des participants pour vérification rapide
        Map<String, Participant> indexParticipants = participants.stream()
                .collect(Collectors.toMap(Participant::getId, p -> p));

        // Vérifier que tous les participants inscrits existent
        for (Evenement evenement : evenements) {
            for (Participant participant : evenement.getParticipants()) {
                if (!indexParticipants.containsKey(participant.getId())) {
                    erreurs.add("Participant inscrit introuvable: " + participant.getId() +
                            " dans événement " + evenement.getId());
                }
            }
        }
    }

    // ============ RECONSTRUCTION DES RELATIONS OBSERVER ============

    /**
     * Reconstruit les relations Observer après chargement
     */
    public void reconstruireRelationsObserver() {
        System.out.println("🔄 Reconstruction des relations Observer...");

        int observersReconstruits = 0;

        for (Evenement evenement : evenements) {
            // Nettoyer les observers existants (au cas où)
            evenement.getObservers().clear();

            // Ajouter chaque participant inscrit comme observer
            for (Participant participant : evenement.getParticipants()) {
                evenement.ajouterObserver(participant);
                observersReconstruits++;
            }
        }

        System.out.println("✅ Relations Observer reconstruites:");
        System.out.println("   • Événements: " + evenements.size());
        System.out.println("   • Observers reconstruits: " + observersReconstruits);
        System.out.println("   • Différence avec sauvegarde: " +
                Math.abs(observersReconstruits - nombreObserversTotal));
    }

    // ============ MÉTHODES D'INFORMATION ============

    /**
     * Retourne un résumé lisible de la sauvegarde
     */
    public String genererResume() {
        StringBuilder resume = new StringBuilder();

        resume.append("=== RÉSUMÉ DE LA SAUVEGARDE ===\n");
        resume.append("Date: ").append(dateSauvegarde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        resume.append("Version: ").append(versionApplication).append("\n");
        resume.append("Commentaire: ").append(commentaireSauvegarde).append("\n\n");

        resume.append("DONNÉES:\n");
        resume.append("• Événements: ").append(evenements.size()).append("\n");
        resume.append("• Participants: ").append(participants.size()).append("\n");
        resume.append("• Relations Observer: ").append(nombreObserversTotal).append("\n\n");

        if (statistiques != null) {
            resume.append("STATISTIQUES:\n");
            resume.append("• Conférences: ").append(statistiques.getNombreConferences()).append("\n");
            resume.append("• Concerts: ").append(statistiques.getNombreConcerts()).append("\n");
            resume.append("• Organisateurs: ").append(statistiques.getNombreOrganisateurs()).append("\n");
            resume.append("• Participants standard: ").append(statistiques.getNombreParticipantsStandard()).append("\n");
            resume.append("• Inscriptions totales: ").append(statistiques.getNombreInscriptionsTotal()).append("\n");
            resume.append("• Taux d'occupation moyen: ").append(String.format("%.1f%%", statistiques.getTauxOccupationMoyen())).append("\n");
        }

        return resume.toString();
    }

    /**
     * Vérifie la compatibilité avec la version actuelle
     */
    @JsonIgnore
    public boolean estCompatible() {
        // Logique de compatibilité selon la version
        return "1.0".equals(versionFormatDonnees) || "1.1".equals(versionFormatDonnees);
    }

    /**
     * Retourne l'âge de la sauvegarde en heures
     */
    @JsonIgnore
    public long getAgeSauvegardeHeures() {
        return java.time.Duration.between(dateSauvegarde, LocalDateTime.now()).toHours();
    }

    // ============ GETTERS ET SETTERS ============

    public List<Evenement> getEvenements() { return evenements; }
    public void setEvenements(List<Evenement> evenements) {
        this.evenements = evenements;
        // Recalculer les statistiques si changement
        if (this.participants != null) {
            this.statistiques = new StatistiquesSauvegarde(evenements, participants);
            this.nombreObserversTotal = calculerNombreObservers();
        }
    }

    public List<Participant> getParticipants() { return participants; }
    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
        // Recalculer les statistiques si changement
        if (this.evenements != null) {
            this.statistiques = new StatistiquesSauvegarde(evenements, participants);
        }
    }

    public LocalDateTime getDateSauvegarde() { return dateSauvegarde; }
    public void setDateSauvegarde(LocalDateTime dateSauvegarde) { this.dateSauvegarde = dateSauvegarde; }

    public String getVersionApplication() { return versionApplication; }
    public void setVersionApplication(String versionApplication) { this.versionApplication = versionApplication; }

    public String getVersionFormatDonnees() { return versionFormatDonnees; }
    public void setVersionFormatDonnees(String versionFormatDonnees) { this.versionFormatDonnees = versionFormatDonnees; }

    public int getNombreObserversTotal() { return nombreObserversTotal; }
    public void setNombreObserversTotal(int nombreObserversTotal) { this.nombreObserversTotal = nombreObserversTotal; }

    public String getCommentaireSauvegarde() { return commentaireSauvegarde; }
    public void setCommentaireSauvegarde(String commentaireSauvegarde) { this.commentaireSauvegarde = commentaireSauvegarde; }

    public StatistiquesSauvegarde getStatistiques() { return statistiques; }
    public void setStatistiques(StatistiquesSauvegarde statistiques) { this.statistiques = statistiques; }

    @Override
    public String toString() {
        return String.format("DonneesSauvegarde{date=%s, evenements=%d, participants=%d, observers=%d}",
                dateSauvegarde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                evenements.size(), participants.size(), nombreObserversTotal);
    }
}
