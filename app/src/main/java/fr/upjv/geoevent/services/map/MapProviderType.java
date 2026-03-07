package fr.upjv.geoevent.services.map;

/**
 * Enumération des types de fournisseurs cartographiques disponibles.
 * Permet de sélectionner le service à utiliser (OpenStreetMap ou Google Maps).
 */
public enum MapProviderType {
    /**
     * Fournisseur OpenStreetMap utilisant OSMDroid.
     */
    OPEN_STREET_MAP,

    /**
     * Fournisseur Google Maps.
     */
    GOOGLE_MAPS
}
