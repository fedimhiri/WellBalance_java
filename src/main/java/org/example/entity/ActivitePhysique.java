package org.example.entity;

public class ActivitePhysique {
    private int id;
    private String nom;
    private String description;
    private String typeActivite;
    private String niveau;
    private int dureeEstimee;
    private int caloriesEstimees;
    private boolean actif;
    private int objectifSportifId;

    public ActivitePhysique() {}

    public ActivitePhysique(int id, String nom, String description, String typeActivite, String niveau, int dureeEstimee, int caloriesEstimees, boolean actif, int objectifSportifId) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.typeActivite = typeActivite;
        this.niveau = niveau;
        this.dureeEstimee = dureeEstimee;
        this.caloriesEstimees = caloriesEstimees;
        this.actif = actif;
        this.objectifSportifId = objectifSportifId;
    }

    public ActivitePhysique(String nom, String description, String typeActivite, String niveau, int dureeEstimee, int caloriesEstimees, boolean actif, int objectifSportifId) {
        this.nom = nom;
        this.description = description;
        this.typeActivite = typeActivite;
        this.niveau = niveau;
        this.dureeEstimee = dureeEstimee;
        this.caloriesEstimees = caloriesEstimees;
        this.actif = actif;
        this.objectifSportifId = objectifSportifId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTypeActivite() { return typeActivite; }
    public void setTypeActivite(String typeActivite) { this.typeActivite = typeActivite; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public int getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(int dureeEstimee) { this.dureeEstimee = dureeEstimee; }

    public int getCaloriesEstimees() { return caloriesEstimees; }
    public void setCaloriesEstimees(int caloriesEstimees) { this.caloriesEstimees = caloriesEstimees; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public int getObjectifSportifId() { return objectifSportifId; }
    public void setObjectifSportifId(int objectifSportifId) { this.objectifSportifId = objectifSportifId; }

    @Override
    public String toString() {
        return "ActivitePhysique{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", actif=" + actif +
                '}';
    }
}
