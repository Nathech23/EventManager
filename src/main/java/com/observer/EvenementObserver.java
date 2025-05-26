package com.observer;

/**
 * Interface Observer - Les participants implémentent cette interface
 * pour recevoir automatiquement les notifications des événements
 */
public interface EvenementObserver {

    /**
     * Appelée automatiquement quand un événement est modifié
     */
    void onEvenementModifie(String evenementNom, String message);

    /**
     * Appelée automatiquement quand un événement est annulé
     */
    void onEvenementAnnule(String evenementNom, String message);

    /**
     * Appelée automatiquement quand des informations changent
     */
    void onEvenementInfoModifiee(String evenementNom, String message);
}