package org.example.wellbalance.models;

import java.time.LocalTime;

public class ResultatRecommandation {

    private boolean estValide;
    private boolean estDisponible;
    private String statut;
    private String message;
    private LocalTime creneauPropose;
    private int score;

    public ResultatRecommandation() {
    }

    public ResultatRecommandation(boolean estValide, boolean estDisponible, String statut,
                                  String message, LocalTime creneauPropose, int score) {
        this.estValide = estValide;
        this.estDisponible = estDisponible;
        this.statut = statut;
        this.message = message;
        this.creneauPropose = creneauPropose;
        this.score = score;
    }

    public boolean isEstValide() {
        return estValide;
    }

    public void setEstValide(boolean estValide) {
        this.estValide = estValide;
    }

    public boolean isEstDisponible() {
        return estDisponible;
    }

    public void setEstDisponible(boolean estDisponible) {
        this.estDisponible = estDisponible;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalTime getCreneauPropose() {
        return creneauPropose;
    }

    public void setCreneauPropose(LocalTime creneauPropose) {
        this.creneauPropose = creneauPropose;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
