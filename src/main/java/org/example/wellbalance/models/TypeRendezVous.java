package org.example.wellbalance.models;

public class TypeRendezVous {
    private int id;
    private String libelle;
    private String description;
    private int duree;
    private double prix;
    private String categorie;

    public TypeRendezVous() {
    }

    public TypeRendezVous(int id, String libelle, String description, int duree, double prix, String categorie) {
        this.id = id;
        this.libelle = libelle;
        this.description = description;
        this.duree = duree;
        this.prix = prix;
        this.categorie = categorie;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    @Override
    public String toString() {
        return libelle;
    }
}