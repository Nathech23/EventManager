package com.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.model.*;
import com.exception.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitaire avancé pour la sérialisation/désérialisation JSON
 * Compatible avec le Pattern Observer et gestion d'erreurs complète
 */
public class SerializationUtil {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    /**
     * Classe conteneur pour sauvegarder toutes les données du système
     */
    public static class DonneesSauvegarde {
        private List<Evenement> evenements = new ArrayList<>();
        private List<Participant> participants = new ArrayList<>();
        private LocalDateTime dateSauvegarde;
        private String versionApplication = "1.0";
        private int nombreObserversTotal;

        public DonneesSauvegarde() {
            this.dateSauvegarde = LocalDateTime.now();
        }

        public DonneesSauvegarde(List<Evenement> evenements, List<Participant> participants) {
            this();
            this.evenements = new ArrayList<>(evenements);
            this.participants = new ArrayList<>(participants);
            this.nombreObserversTotal = calculerNombreObservers();
        }

        private int calculerNombreObservers() {
            return evenements.stream()
                    .mapToInt(e -> e.getObservers().size())
                    .sum();
        }

        // Getters et setters
        public List<Evenement> getEvenements() { return evenements; }
        public void setEvenements(List<Evenement> evenements) { this.evenements = evenements; }

        public List<Participant> getParticipants() { return participants; }
        public void setParticipants(List<Participant> participants) { this.participants = participants; }

        public LocalDateTime getDateSauvegarde() { return dateSauvegarde; }
        public void setDateSauvegarde(LocalDateTime dateSauvegarde) { this.dateSauvegarde = dateSauvegarde; }

        public String getVersionApplication() { return versionApplication; }
        public void setVersionApplication(String versionApplication) { this.versionApplication = versionApplication; }

        public int getNombreObserversTotal() { return nombreObserversTotal; }
        public void setNombreObserversTotal(int nombreObserversTotal) { this.nombreObserversTotal = nombreObserversTotal; }

        /**
         * Valide les données chargées
         */
        public void valider() throws ValidationException {
            List<String> erreurs = new ArrayList<>();

            if (evenements == null) {
                erreurs.add("Liste des événements manquante");
            } else {
                // Valider les IDs uniques des événements
                long idsUniques = evenements.stream()
                        .map(Evenement::getId)
                        .distinct()
                        .count();
                if (idsUniques != evenements.size()) {
                    erreurs.add("IDs d'événements non uniques détectés");
                }
            }

            if (participants == null) {
                erreurs.add("Liste des participants manquante");
            } else {
                // Valider les IDs uniques des participants
                long idsUniques = participants.stream()
                        .map(Participant::getId)
                        .distinct()
                        .count();
                if (idsUniques != participants.size()) {
                    erreurs.add("IDs de participants non uniques détectés");
                }
            }

            if (!erreurs.isEmpty()) {
                throw new ValidationException(erreurs);
            }
        }

        /**
         * Reconstruit les relations Observer après chargement
         */
        public void reconstruireRelationsObserver() {
            System.out.println("🔄 Reconstruction des relations Observer...");

            for (Evenement evenement : evenements) {
                // Les participants sont déjà liés via la sérialisation
                // Ajouter chaque participant comme observer
                for (Participant participant : evenement.getParticipants()) {
                    evenement.ajouterObserver(participant);
                }
            }

            System.out.println("✅ Relations Observer reconstruites pour " + evenements.size() + " événements");
        }
    }

    /**
     * Sauvegarde complète du système avec gestion d'erreurs
     */
    public static void sauvegarderDonnees(List<Evenement> evenements, List<Participant> participants, File file)
            throws SerializationException {

        try {
            // Validation préalable
            validerDonneesSauvegarde(evenements, participants);

            // Créer le dossier parent si nécessaire
            creerDossierParentSiNecessaire(file);

            // Préparer les données
            DonneesSauvegarde donnees = new DonneesSauvegarde(evenements, participants);

            // Sauvegarde atomique (fichier temporaire puis renommage)
            File fichierTemporaire = new File(file.getAbsolutePath() + ".tmp");
            mapper.writeValue(fichierTemporaire, donnees);

            // Renommer le fichier temporaire
            if (!fichierTemporaire.renameTo(file)) {
                throw new IOException("Impossible de finaliser la sauvegarde");
            }

            System.out.println("💾 Sauvegarde réussie: " + file.getName());
            System.out.println("   • Événements: " + evenements.size());
            System.out.println("   • Participants: " + participants.size());
            System.out.println("   • Observers totaux: " + donnees.getNombreObserversTotal());

        } catch (ValidationException e) {
            throw new SerializationException("Données invalides: " + e.getMessageUtilisateur(),
                    "SAUVEGARDE", file.getName(), e);
        } catch (IOException e) {
            throw new SerializationException("Erreur d'écriture fichier",
                    "SAUVEGARDE", file.getName(), e);
        } catch (Exception e) {
            throw new SerializationException("Erreur inattendue lors de la sauvegarde",
                    "SAUVEGARDE", file.getName(), e);
        }
    }

    /**
     * Chargement complet du système avec reconstruction Observer
     */
    public static DonneesSauvegarde chargerDonnees(File file) throws SerializationException {

        try {
            // Validation du fichier
            validerFichierChargement(file);

            // Chargement
            DonneesSauvegarde donnees = mapper.readValue(file, DonneesSauvegarde.class);

            // Validation des données chargées
            donnees.valider();

            // Reconstruction des relations Observer
            donnees.reconstruireRelationsObserver();

            System.out.println("📂 Chargement réussi: " + file.getName());
            System.out.println("   • Date sauvegarde: " + donnees.getDateSauvegarde().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            System.out.println("   • Version: " + donnees.getVersionApplication());
            System.out.println("   • Événements: " + donnees.getEvenements().size());
            System.out.println("   • Participants: " + donnees.getParticipants().size());
            System.out.println("   • Relations Observer reconstruites");

            return donnees;

        } catch (ValidationException e) {
            throw new SerializationException("Données chargées invalides: " + e.getMessageUtilisateur(),
                    "CHARGEMENT", file.getName(), e);
        } catch (IOException e) {
            throw new SerializationException("Erreur de lecture fichier",
                    "CHARGEMENT", file.getName(), e);
        } catch (Exception e) {
            throw new SerializationException("Erreur inattendue lors du chargement",
                    "CHARGEMENT", file.getName(), e);
        }
    }

    /**
     * Export des données en JSON lisible pour inspection
     */
    public static String exporterEnJSON(List<Evenement> evenements, List<Participant> participants)
            throws SerializationException {
        try {
            DonneesSauvegarde donnees = new DonneesSauvegarde(evenements, participants);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(donnees);
        } catch (Exception e) {
            throw new SerializationException("Erreur lors de l'export JSON", "EXPORT", e);
        }
    }

    /**
     * Sauvegarde automatique avec nom de fichier horodaté
     */
    public static File sauvegardeAutomatique(List<Evenement> evenements, List<Participant> participants,
                                             String dossierSauvegarde) throws SerializationException {
        try {
            // Créer le dossier de sauvegarde
            Path dossier = Paths.get(dossierSauvegarde);
            Files.createDirectories(dossier);

            // Nom de fichier avec timestamp
            String nomFichier = "sauvegarde_auto_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                    ".json";

            File fichier = new File(dossier.toFile(), nomFichier);
            sauvegarderDonnees(evenements, participants, fichier);

            return fichier;

        } catch (IOException e) {
            throw new SerializationException("Impossible de créer le dossier de sauvegarde",
                    "SAUVEGARDE", e);
        }
    }

    /**
     * Crée une sauvegarde de sécurité avant une opération risquée
     */
    public static File creerSauvegardeSecurite(List<Evenement> evenements, List<Participant> participants)
            throws SerializationException {

        String dossierTemp = System.getProperty("java.io.tmpdir");
        String nomFichier = "backup_securite_" + System.currentTimeMillis() + ".json";
        File fichierBackup = new File(dossierTemp, nomFichier);

        sauvegarderDonnees(evenements, participants, fichierBackup);
        System.out.println("🛡️ Sauvegarde de sécurité créée: " + fichierBackup.getAbsolutePath());

        return fichierBackup;
    }

    // ============ MÉTHODES DE VALIDATION ============

    private static void validerDonneesSauvegarde(List<Evenement> evenements, List<Participant> participants)
            throws ValidationException {

        List<String> erreurs = new ArrayList<>();

        if (evenements == null) {
            erreurs.add("La liste des événements ne peut pas être null");
        }

        if (participants == null) {
            erreurs.add("La liste des participants ne peut pas être null");
        }

        if (evenements != null) {
            for (Evenement evenement : evenements) {
                if (evenement.getId() == null || evenement.getId().trim().isEmpty()) {
                    erreurs.add("Événement avec ID manquant détecté");
                }
                if (evenement.getNom() == null || evenement.getNom().trim().isEmpty()) {
                    erreurs.add("Événement avec nom manquant détecté");
                }
            }
        }

        if (participants != null) {
            for (Participant participant : participants) {
                if (participant.getId() == null || participant.getId().trim().isEmpty()) {
                    erreurs.add("Participant avec ID manquant détecté");
                }
                if (participant.getNom() == null || participant.getNom().trim().isEmpty()) {
                    erreurs.add("Participant avec nom manquant détecté");
                }
                if (participant.getEmail() == null || !participant.getEmail().contains("@")) {
                    erreurs.add("Participant avec email invalide détecté: " + participant.getId());
                }
            }
        }

        if (!erreurs.isEmpty()) {
            throw new ValidationException(erreurs);
        }
    }

    private static void validerFichierChargement(File file) throws SerializationException {
        if (!file.exists()) {
            throw new SerializationException("Le fichier n'existe pas", "CHARGEMENT", file.getName());
        }

        if (!file.isFile()) {
            throw new SerializationException("Le chemin ne pointe pas vers un fichier", "CHARGEMENT", file.getName());
        }

        if (!file.canRead()) {
            throw new SerializationException("Permissions de lecture insuffisantes", "CHARGEMENT", file.getName());
        }

        if (file.length() == 0) {
            throw new SerializationException("Le fichier est vide", "CHARGEMENT", file.getName());
        }

        if (file.length() > 100 * 1024 * 1024) { // 100 MB max
            throw new SerializationException("Le fichier est trop volumineux (> 100 MB)", "CHARGEMENT", file.getName());
        }
    }

    private static void creerDossierParentSiNecessaire(File file) throws IOException {
        File dossierParent = file.getParentFile();
        if (dossierParent != null && !dossierParent.exists()) {
            if (!dossierParent.mkdirs()) {
                throw new IOException("Impossible de créer le dossier: " + dossierParent.getAbsolutePath());
            }
        }
    }

    // ============ UTILITAIRES ============

    /**
     * Vérifie si un fichier est une sauvegarde valide sans le charger complètement
     */
    public static boolean estSauvegardeValide(File file) {
        try {
            validerFichierChargement(file);

            // Lecture rapide des métadonnées seulement
            var node = mapper.readTree(file);
            return node.has("evenements") && node.has("participants") && node.has("dateSauvegarde");

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retourne les informations d'une sauvegarde sans la charger complètement
     */
    public static String getInfosSauvegarde(File file) {
        try {
            var node = mapper.readTree(file);

            String dateSauvegarde = node.has("dateSauvegarde") ?
                    node.get("dateSauvegarde").asText() : "Inconnue";
            int nbEvenements = node.has("evenements") ?
                    node.get("evenements").size() : 0;
            int nbParticipants = node.has("participants") ?
                    node.get("participants").size() : 0;
            String version = node.has("versionApplication") ?
                    node.get("versionApplication").asText() : "Inconnue";

            return String.format("Date: %s | Événements: %d | Participants: %d | Version: %s",
                    dateSauvegarde, nbEvenements, nbParticipants, version);

        } catch (Exception e) {
            return "Erreur lecture fichier: " + e.getMessage();
        }
    }
}