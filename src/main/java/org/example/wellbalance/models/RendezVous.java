package org.example.wellbalance.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class RendezVous {

    private int id;
    private LocalDate dateRdv;
    private LocalTime heureRdv;
    private String statut;
    private String remarque;

    // 🔥 RELATION
    private TypeRendezVous typeRendezVous;

    public RendezVous() {}

    public RendezVous(int id, LocalDate dateRdv, LocalTime heureRdv,
                      String statut, String remarque, TypeRendezVous typeRendezVous) {
        this.id = id;
        this.dateRdv = dateRdv;
        this.heureRdv = heureRdv;
        this.statut = statut;
        this.remarque = remarque;
        this.typeRendezVous = typeRendezVous;
    }

    // GETTERS & SETTERS

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDateRdv() { return dateRdv; }
    public void setDateRdv(LocalDate dateRdv) { this.dateRdv = dateRdv; }

    public LocalTime getHeureRdv() { return heureRdv; }
    public void setHeureRdv(LocalTime heureRdv) { this.heureRdv = heureRdv; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getRemarque() { return remarque; }
    public void setRemarque(String remarque) { this.remarque = remarque; }

    public TypeRendezVous getTypeRendezVous() { return typeRendezVous; }
    public void setTypeRendezVous(TypeRendezVous typeRendezVous) {
        this.typeRendezVous = typeRendezVous;
    }
}