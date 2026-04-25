package fr.upjv.geoevent.domain.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Evenement implements Serializable {

    private String titre;
    private String description;
    private String lieu;
    private double longitude;
    private double latitude;
    private List<Image> images;
    private int nombreParticipant;
    private Date dateEvenement;

    @ServerTimestamp
    private Date date_creation;

    public Evenement() {
    }

    public Evenement(String titre, String description, String lieu, Date dateEvenement) {
        this.titre = titre;
        this.description = description;
        this.lieu = lieu;
        this.dateEvenement = dateEvenement;
        this.nombreParticipant = 0;
    }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public List<Image> getImages() { return images; }
    public void setImages(List<Image> images) { this.images = images; }

    public int getNombreParticipant() { return nombreParticipant; }
    public void setNombreParticipant(int nombreParticipant) { this.nombreParticipant = nombreParticipant; }

    public Date getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(Date dateEvenement) { this.dateEvenement = dateEvenement; }

    public Date getDate_creation() { return date_creation; }
    public void setDate_creation(Date date_creation) { this.date_creation = date_creation; }
}