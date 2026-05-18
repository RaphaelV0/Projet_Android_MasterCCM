package fr.upjv.geoevent.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.auth.AuthServiceFactory;
import fr.upjv.geoevent.services.IAuthService;
import fr.upjv.geoevent.ui.fragments.AdminEventsFragment;
import fr.upjv.geoevent.ui.fragments.AdminNotificationsFragment;

/**
 * Activité principale du panneau d'administration.
 *
 * Cette interface est exclusivement réservée aux utilisateurs possédant le rôle "admin".
 * Elle permet la gestion globale des événements (CRUD) et l'envoi de notifications push.
 * La redirection vers cette activité est orchestrée par la ConnexionActivity après vérification du profil.
 */
public class AdminActivity extends AppCompatActivity {

    /** Énumération définissant les différents modules d'administration disponibles. */
    private enum AdminTab { EVENTS, NOTIFICATIONS }
    
    /** État courant de la navigation admin. */
    private AdminTab currentTab = AdminTab.EVENTS;

    private TextView tabEvents, tabNotifications;
    private IAuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        authService  = AuthServiceFactory.create();
        tabEvents        = findViewById(R.id.adminTabEvents);
        tabNotifications = findViewById(R.id.adminTabNotifications);

        // Gestion du clic sur l'onglet de gestion des événements
        tabEvents.setOnClickListener(v -> switchTab(AdminTab.EVENTS));
        
        // Gestion du clic sur l'onglet d'envoi de notifications
        tabNotifications.setOnClickListener(v -> switchTab(AdminTab.NOTIFICATIONS));

        // Bouton de déconnexion administrative : réinitialise la session et retourne à l'écran de connexion
        findViewById(R.id.adminLogoutButton).setOnClickListener(v -> {
            authService.logout();
            startActivity(new Intent(this, ConnexionActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        // Initialisation de la vue sur l'onglet par défaut
        switchTab(AdminTab.EVENTS);
    }

    /**
     * Orchestre le basculement entre les modules d'administration (fragments).
     * 
     * @param tab Le module cible à afficher.
     */
    private void switchTab(AdminTab tab) {
        if (currentTab == tab) return;
        currentTab = tab;

        updateTabUI(tab);

        // Sélection du fragment correspondant à l'onglet choisi
        Fragment fragment = (tab == AdminTab.EVENTS)
                ? new AdminEventsFragment()
                : new AdminNotificationsFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Animation de transition pour une expérience utilisateur fluide
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.adminFragmentContainer, fragment);
        transaction.commit();
    }

    /**
     * Met à jour visuellement les indicateurs d'onglets pour refléter la sélection courante.
     * 
     * @param tab L'onglet actif à mettre en évidence.
     */
    private void updateTabUI(AdminTab tab) {
        if (tab == AdminTab.EVENTS) {
            tabEvents.setBackgroundResource(R.drawable.bg_tab_selected);
            tabEvents.setTextColor(getColor(R.color.white));
            tabNotifications.setBackgroundResource(android.R.color.transparent);
            tabNotifications.setTextColor(getColor(R.color.geo_text_secondary));
        } else {
            tabNotifications.setBackgroundResource(R.drawable.bg_tab_selected);
            tabNotifications.setTextColor(getColor(R.color.white));
            tabEvents.setBackgroundResource(android.R.color.transparent);
            tabEvents.setTextColor(getColor(R.color.geo_text_secondary));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**
         * Chargement initial sécurisé : garantit que l'interface admin n'est pas vide
         * si l'activité est recréée par le système.
         */
        if (getSupportFragmentManager().findFragmentById(R.id.adminFragmentContainer) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.adminFragmentContainer, new AdminEventsFragment())
                    .commit();
        }
    }
}
