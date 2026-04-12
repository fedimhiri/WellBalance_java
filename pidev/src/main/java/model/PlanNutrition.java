package model;

import java.sql.Timestamp;

public class PlanNutrition {
    private int id;
    private String objectif;
    private String description;
    private String periode;
    private Timestamp dateDebut;
    private Timestamp dateFin;
    private int userId;
    private int nutritionnisteId;

    public PlanNutrition() {
    }

    public PlanNutrition(int id, String objectif, String description, String periode, Timestamp dateDebut, Timestamp dateFin, int userId, int nutritionnisteId) {
        this.id = id;
        this.objectif = objectif;
        this.description = description;
        this.periode = periode;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.userId = userId;
        this.nutritionnisteId = nutritionnisteId;
    }

    public PlanNutrition(String objectif, String description, String periode, Timestamp dateDebut, Timestamp dateFin, int userId, int nutritionnisteId) {
        this.objectif = objectif;
        this.description = description;
        this.periode = periode;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.userId = userId;
        this.nutritionnisteId = nutritionnisteId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getObjectif() {
        return objectif;
    }

    public void setObjectif(String objectif) {
        this.objectif = objectif;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public Timestamp getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Timestamp dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Timestamp getDateFin() {
        return dateFin;
    }

    public void setDateFin(Timestamp dateFin) {
        this.dateFin = dateFin;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getNutritionnisteId() {
        return nutritionnisteId;
    }

    public void setNutritionnisteId(int nutritionnisteId) {
        this.nutritionnisteId = nutritionnisteId;
    }

    @Override
    public String toString() {
        return "PlanNutrition{" +
                "id=" + id +
                ", objectif='" + objectif + '\'' +
                ", description='" + description + '\'' +
                ", periode='" + periode + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", userId=" + userId +
                ", nutritionnisteId=" + nutritionnisteId +
                '}';
    }
}
