package fr.upjv.geoevent.domain.models;

/**
 * Modèle représentant un utilisateur authentifié dans l'application.
 * Ce modèle est indépendant du provider (Firebase, Supabase, etc.)
 * pour respecter le principe d'abstraction du service d'authentification.
 */
public class User {

    private String uid;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String photoUrl;

    public static final String ROLE_USER  = "user";
    public static final String ROLE_ADMIN = "admin";

    public User() {
    }

    public User(String uid, String email, String firstName, String lastName) {
        this.uid       = uid;
        this.email     = email;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.role      = ROLE_USER;
    }

    public String getUid()       { return uid; }
    public String getEmail()     { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getRole()      { return role; }
    public String getPhotoUrl()  { return photoUrl; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    public void setUid(String uid)             { this.uid = uid; }
    public void setEmail(String email)         { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName)   { this.lastName = lastName; }
    public void setRole(String role)           { this.role = role; }
    public void setPhotoUrl(String photoUrl)   { this.photoUrl = photoUrl; }
}