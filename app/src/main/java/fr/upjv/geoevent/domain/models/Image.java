package fr.upjv.geoevent.domain.models;

public class Image {

    private String nom;
    private String description;

    public Image() {
        // Required for Firestore
    }

    public Image(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
