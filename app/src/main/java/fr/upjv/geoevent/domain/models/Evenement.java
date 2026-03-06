package fr.upjv.geoevent.domain.models;

import java.util.Date;
import java.util.List;


public class Evenement {

    private String titre;
    private String description;

    private String lieu;
    private double longitude;
    private double latitude;
    private List<Image> images;

    private int nombreParticiant;

    private Date date_creation;
    private Date date_modification;


    public Evenement() {
        this.date_creation = new Date();
        this.date_modification = new Date();
    }



    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public int getNombreParticiant() {
        return nombreParticiant;
    }

    public void setNombreParticiant(int nombreParticiant) {
        this.nombreParticiant = nombreParticiant;
    }

    public Date getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(Date date_creation) {
        this.date_creation = date_creation;
    }

    public Date getDate_modification() {
        return date_modification;
    }

    public void setDate_modification(Date date_modification) {
        this.date_modification = date_modification;
    }
}
