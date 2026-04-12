package model;

import java.sql.Timestamp;

public class Repas {
    private int id;
    private String typeRepas;
    private float calories;
    private float proteines;
    private float glucides;
    private float lipides;
    private String portionSize;
    private String barcode;
    private String description;
    private Timestamp dateRepas;
    private int planNutritionId;

    public Repas() {
    }

    public Repas(int id, String typeRepas, float calories, float proteines, float glucides, float lipides, String portionSize, String barcode, String description, Timestamp dateRepas, int planNutritionId) {
        this.id = id;
        this.typeRepas = typeRepas;
        this.calories = calories;
        this.proteines = proteines;
        this.glucides = glucides;
        this.lipides = lipides;
        this.portionSize = portionSize;
        this.barcode = barcode;
        this.description = description;
        this.dateRepas = dateRepas;
        this.planNutritionId = planNutritionId;
    }

    public Repas(String typeRepas, float calories, float proteines, float glucides, float lipides, String portionSize, String barcode, String description, Timestamp dateRepas, int planNutritionId) {
        this.typeRepas = typeRepas;
        this.calories = calories;
        this.proteines = proteines;
        this.glucides = glucides;
        this.lipides = lipides;
        this.portionSize = portionSize;
        this.barcode = barcode;
        this.description = description;
        this.dateRepas = dateRepas;
        this.planNutritionId = planNutritionId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTypeRepas() {
        return typeRepas;
    }

    public void setTypeRepas(String typeRepas) {
        this.typeRepas = typeRepas;
    }

    public float getCalories() {
        return calories;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }

    public float getProteines() {
        return proteines;
    }

    public void setProteines(float proteines) {
        this.proteines = proteines;
    }

    public float getGlucides() {
        return glucides;
    }

    public void setGlucides(float glucides) {
        this.glucides = glucides;
    }

    public float getLipides() {
        return lipides;
    }

    public void setLipides(float lipides) {
        this.lipides = lipides;
    }

    public String getPortionSize() {
        return portionSize;
    }

    public void setPortionSize(String portionSize) {
        this.portionSize = portionSize;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getDateRepas() {
        return dateRepas;
    }

    public void setDateRepas(Timestamp dateRepas) {
        this.dateRepas = dateRepas;
    }

    public int getPlanNutritionId() {
        return planNutritionId;
    }

    public void setPlanNutritionId(int planNutritionId) {
        this.planNutritionId = planNutritionId;
    }

    @Override
    public String toString() {
        return "Repas{" +
                "id=" + id +
                ", typeRepas='" + typeRepas + '\'' +
                ", calories=" + calories +
                ", proteines=" + proteines +
                ", glucides=" + glucides +
                ", lipides=" + lipides +
                ", portionSize='" + portionSize + '\'' +
                ", barcode='" + barcode + '\'' +
                ", description='" + description + '\'' +
                ", dateRepas=" + dateRepas +
                ", planNutritionId=" + planNutritionId +
                '}';
    }
}
