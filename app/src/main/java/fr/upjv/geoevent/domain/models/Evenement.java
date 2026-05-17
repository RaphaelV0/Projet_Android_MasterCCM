package fr.upjv.geoevent.domain.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Entité métier représentant un événement géolocalisé.
 * Implémente Serializable pour permettre le transfert d'instances entre composants graphiques via les Bundles.
 */
public class Evenement implements Serializable {

    private String titre;
    private String description;
    private String lieu;
    private double longitude;
    private double latitude;

    /** Collection d'URLs de téléchargement direct (String) pointant vers Firebase Storage. */
    private List<String> images;
    private int nombreParticipant;
    private Date dateEvenement;

    /** Propriété locale calculée au runtime, représentant la distance physique séparant l'utilisateur de l'événement. */
    private float distance;

    /** Métadonnée temporelle injectée automatiquement par le serveur Cloud lors de la persistance. */
    @ServerTimestamp
    private Date date_creation;

    /** Constructeur par défaut requis par le SDK Firebase Firestore pour la désérialisation automatique des documents. */
    public Evenement() {
    }

    /** Constructeur complet servant à l'initialisation lors de la saisie d'un nouvel événement. */
    public Evenement(String titre, String description, String lieu, Date dateEvenement) {
        this.titre = titre;
        this.description = description;
        this.lieu = lieu;
        this.dateEvenement = dateEvenement;
        this.nombreParticipant = 0; // Initialisation explicite à zéro pour fiabiliser les incrémentations futures
    }

    /** Exclut la distance du stockage Firestore afin d'éviter la persistance d'une donnée contextuelle volatile. */
    @Exclude
    public float getDistance() {
        return distance;
    }

    @Exclude
    public void setDistance(float distance) {
        this.distance = distance;
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

    /** Permet de configurer les coordonnées spatiales de l'événement en une seule opération. */
    public void setPosition(double longitude, double latitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public int getNombreParticipant() { return nombreParticipant; }
    public void setNombreParticipant(int nombreParticipant) { this.nombreParticipant = nombreParticipant; }

    public Date getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(Date dateEvenement) { this.dateEvenement = dateEvenement; }

    public Date getDate_creation() { return date_creation; }
    public void setDate_creation(Date date_creation) { this.date_creation = date_creation; }
}