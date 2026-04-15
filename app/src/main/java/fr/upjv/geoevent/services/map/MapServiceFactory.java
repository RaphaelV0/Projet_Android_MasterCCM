package fr.upjv.geoevent.services.map;

import android.content.Context;

/**
 * Factory pour créer des instances de MapService en fonction du type de fournisseur.
 * Permet de centraliser la création des services cartographiques.
 */
public class MapServiceFactory {

    /**
     * Crée une instance de MapService basée sur le type de fournisseur spécifié.
     * @param providerType Type de fournisseur cartographique
     * @param context Contexte de l'application
     * @return Instance de MapService
     */
    public static MapService create(MapProviderType providerType, Context context) {
        switch (providerType) {
            case OPEN_STREET_MAP:
                return new OpenStreetMapService();
            case GOOGLE_MAPS:
                return new GoogleMapService();
            default:
                throw new IllegalArgumentException("Type de fournisseur inconnu: " + providerType);
        }
    }
}
