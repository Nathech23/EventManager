package com.observer;

import java.util.List;

/**
 * Interface Observable - Les événements implémentent cette interface
 * pour permettre aux participants de s'abonner aux notifications
 */
public interface EvenementObservable {

    /**
     * Ajoute un observer (participant) qui sera notifié automatiquement
     */
    void ajouterObserver(EvenementObserver observer);

    /**
     * Retire un observer
     */
    void retirerObserver(EvenementObserver observer);

    /**
     * Notifie tous les observers d'une modification
     */
    void notifierModification(String message);

    /**
     * Notifie tous les observers d'une annulation
     */
    void notifierAnnulation(String message);

    /**
     * Notifie tous les observers d'un changement d'information
     */
    void notifierChangementInfo(String message);

    /**
     * Obtient la liste des observers actuels
     */
    List<EvenementObserver> getObservers();
}