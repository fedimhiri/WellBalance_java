package org.example.wellbalance.services;

import org.example.wellbalance.models.RendezVous;
import org.example.wellbalance.models.ResultatRecommandation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

public class ServiceRecommandationRendezVous {

    private static final LocalTime DEBUT_TRAVAIL = LocalTime.of(9, 0);
    private static final LocalTime FIN_TRAVAIL = LocalTime.of(15, 0);
    private static final int DUREE_RENDEZ_VOUS_MINUTES = 30;

    private final RendezVousService rendezVousService;

    public ServiceRecommandationRendezVous() {
        this(new RendezVousService());
    }

    public ServiceRecommandationRendezVous(RendezVousService rendezVousService) {
        this.rendezVousService = rendezVousService;
    }

    public boolean estCreneauValide(LocalTime heure) {
        return estDansHeuresTravail(heure)
                && heure.getSecond() == 0
                && heure.getNano() == 0
                && heure.getMinute() % DUREE_RENDEZ_VOUS_MINUTES == 0;
    }

    public boolean estDansHeuresTravail(LocalTime heure) {
        return heure != null
                && !heure.isBefore(DEBUT_TRAVAIL)
                && heure.plusMinutes(DUREE_RENDEZ_VOUS_MINUTES).compareTo(FIN_TRAVAIL) <= 0;
    }

    public List<LocalTime> genererCreneauxJournee() {
        java.util.ArrayList<LocalTime> creneaux = new java.util.ArrayList<>();
        LocalTime heure = DEBUT_TRAVAIL;

        while (heure.plusMinutes(DUREE_RENDEZ_VOUS_MINUTES).compareTo(FIN_TRAVAIL) <= 0) {
            creneaux.add(heure);
            heure = heure.plusMinutes(DUREE_RENDEZ_VOUS_MINUTES);
        }

        return creneaux;
    }

    public List<LocalTime> recupererCreneauxDisponibles(LocalDate date) {
        if (date == null) {
            return List.of();
        }

        return genererCreneauxJournee().stream()
                .filter(heure -> estCreneauDisponible(date, heure))
                .toList();
    }

    public boolean estCreneauDisponible(LocalDate date, LocalTime heure) {
        return estCreneauDisponible(date, heure, 0);
    }

    public boolean estCreneauDisponible(LocalDate date, LocalTime heure, int rendezVousIdAIgnorer) {
        if (date == null || !estCreneauValide(heure)) {
            return false;
        }

        return rendezVousService.afficher().stream()
                .filter(rendezVous -> rendezVous.getId() != rendezVousIdAIgnorer)
                .filter(rendezVous -> date.equals(rendezVous.getDateRdv()))
                .filter(this::occupeLePlanning)
                .noneMatch(rendezVous -> heure.equals(rendezVous.getHeureRdv()));
    }

    public ResultatRecommandation analyserCreneau(LocalDate date, LocalTime heure) {
        return analyserCreneau(date, heure, 0);
    }

    public ResultatRecommandation analyserCreneau(LocalDate date, LocalTime heure, int rendezVousIdAIgnorer) {
        if (date == null || heure == null) {
            return new ResultatRecommandation(false, false, "invalide",
                    "Date ou heure manquante.", trouverMeilleurCreneau(date), 0);
        }

        if (!estCreneauValide(heure)) {
            return new ResultatRecommandation(false, false, "invalide",
                    "Creneau invalide : choisissez une heure entre 09:00 et 14:30, par pas de 30 minutes.",
                    proposerCreneauAlternatif(date, heure), 0);
        }

        if (!estCreneauDisponible(date, heure, rendezVousIdAIgnorer)) {
            return new ResultatRecommandation(true, false, "occupe",
                    "Ce creneau est deja pris.", proposerCreneauAlternatif(date, heure), 0);
        }

        int score = calculerScoreCreneau(date, heure, rendezVousIdAIgnorer);
        String statut = determinerStatut(score);
        String message = switch (statut) {
            case "recommande" -> "Creneau recommande : bonne disponibilite et planning peu charge.";
            case "acceptable" -> "Creneau acceptable : disponible, avec une charge correcte.";
            default -> "Creneau deconseille : disponible, mais le planning est deja charge autour de cette heure.";
        };

        return new ResultatRecommandation(true, true, statut, message, null, score);
    }

    public int calculerScoreCreneau(LocalDate date, LocalTime heure) {
        return calculerScoreCreneau(date, heure, 0);
    }

    public int calculerScoreCreneau(LocalDate date, LocalTime heure, int rendezVousIdAIgnorer) {
        if (date == null || !estCreneauValide(heure) || !estCreneauDisponible(date, heure, rendezVousIdAIgnorer)) {
            return 0;
        }

        List<RendezVous> rendezVousDuJour = rendezVousService.afficher().stream()
                .filter(rendezVous -> rendezVous.getId() != rendezVousIdAIgnorer)
                .filter(rendezVous -> date.equals(rendezVous.getDateRdv()))
                .filter(this::occupeLePlanning)
                .toList();

        int score = 40;
        int nombreRendezVous = rendezVousDuJour.size();

        if (nombreRendezVous <= 3) {
            score += 20;
        } else if (nombreRendezVous <= 6) {
            score += 10;
        } else {
            score -= 20;
        }

        boolean voisinOccupe = rendezVousDuJour.stream()
                .map(RendezVous::getHeureRdv)
                .anyMatch(heureRdv -> heureRdv.equals(heure.minusMinutes(DUREE_RENDEZ_VOUS_MINUTES))
                        || heureRdv.equals(heure.plusMinutes(DUREE_RENDEZ_VOUS_MINUTES)));

        if (voisinOccupe) {
            score -= 30;
        } else {
            score += 20;
        }

        if (!heure.isBefore(LocalTime.of(10, 0)) && heure.isBefore(LocalTime.of(14, 0))) {
            score += 10;
        }

        return Math.max(0, Math.min(100, score));
    }

    public LocalTime trouverMeilleurCreneau(LocalDate date) {
        return recupererCreneauxDisponibles(date).stream()
                .max(Comparator.comparingInt(heure -> calculerScoreCreneau(date, heure)))
                .orElse(null);
    }

    public LocalTime proposerCreneauAlternatif(LocalDate date, LocalTime heure) {
        List<LocalTime> creneauxDisponibles = recupererCreneauxDisponibles(date);
        if (creneauxDisponibles.isEmpty()) {
            return null;
        }

        if (heure == null) {
            return trouverMeilleurCreneau(date);
        }

        return creneauxDisponibles.stream()
                .min(Comparator
                        .comparingInt((LocalTime creneau) -> Math.abs(minutesDepuisMinuit(creneau) - minutesDepuisMinuit(heure)))
                        .thenComparing((LocalTime gauche, LocalTime droite) ->
                                Integer.compare(calculerScoreCreneau(date, droite), calculerScoreCreneau(date, gauche))))
                .orElse(trouverMeilleurCreneau(date));
    }

    private boolean occupeLePlanning(RendezVous rendezVous) {
        return rendezVous != null && !RendezVous.STATUT_REFUSE.equals(rendezVous.getStatut());
    }

    private String determinerStatut(int score) {
        if (score >= 70) {
            return "recommande";
        }
        if (score >= 40) {
            return "acceptable";
        }
        return "deconseille";
    }

    private int minutesDepuisMinuit(LocalTime heure) {
        return heure.getHour() * 60 + heure.getMinute();
    }
}
