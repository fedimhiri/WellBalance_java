package org.example.entity;

import java.time.LocalDate;

public class ObjectifSportif {
    private int id;
    private String libelle;
    private String description;
    private String typeObjectif;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut;
    private Double weight;
    private Double height;
    private Integer userId;

    public ObjectifSportif() {}

    public ObjectifSportif(int id, String libelle, String description, String typeObjectif, LocalDate dateDebut, LocalDate dateFin, String statut, Double weight, Double height, Integer userId) {
        this.id = id;
        this.libelle = libelle;
        this.description = description;
        this.typeObjectif = typeObjectif;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.weight = weight;
        this.height = height;
        this.userId = userId;
    }

    public ObjectifSportif(String libelle, String description, String typeObjectif, LocalDate dateDebut, LocalDate dateFin, String statut, Double weight, Double height, Integer userId) {
        this.libelle = libelle;
        this.description = description;
        this.typeObjectif = typeObjectif;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.weight = weight;
        this.height = height;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTypeObjectif() { return typeObjectif; }
    public void setTypeObjectif(String typeObjectif) { this.typeObjectif = typeObjectif; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "ObjectifSportif{" +
                "id=" + id +
                ", libelle='" + libelle + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
