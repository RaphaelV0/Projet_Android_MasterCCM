package fr.upjv.geoevent.domain.models;

import java.io.Serializable;
import java.util.Date;

import fr.upjv.geoevent.domain.data.DataServiceFactory;

public class InscriptionEvent implements Serializable {
    private Date dateInscription;
    private String eventTitre;
    private String lastName;
    private String uid;


    public InscriptionEvent(String uid, String lastName, String eventTitre, Date dateInscription) {
        this.uid = uid;
        this.lastName = lastName;
        this.eventTitre = eventTitre;
        this.dateInscription = dateInscription;
    }


    public InscriptionEvent() {

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEventTitre() {
        return eventTitre;
    }

    public void setEventTitre(String eventTitre) {
        this.eventTitre = eventTitre;
    }

    public Date getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(Date dateInscription) {
        this.dateInscription = dateInscription;
    }
}
