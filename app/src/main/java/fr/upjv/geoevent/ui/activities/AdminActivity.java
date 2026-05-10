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
 * Accessible uniquement aux utilisateurs dont le rôle Firestore = "admin".
 * Le routing est géré dans ConnexionActivity.navigateBasedOnRole().
 *
 * Deux onglets :
 *   - Événements  : CRUD complet + filtres
 *   - Notifications : envoi FCM (topics)
 */
public class AdminActivity extends AppCompatActivity {

    private enum AdminTab { EVENTS, NOTIFICATIONS }
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

        tabEvents.setOnClickListener(v -> switchTab(AdminTab.EVENTS));
        tabNotifications.setOnClickListener(v -> switchTab(AdminTab.NOTIFICATIONS));

        findViewById(R.id.adminLogoutButton).setOnClickListener(v -> {
            authService.logout();
            startActivity(new Intent(this, ConnexionActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        switchTab(AdminTab.EVENTS);
    }

    private void switchTab(AdminTab tab) {
        if (currentTab == tab) return;
        currentTab = tab;

        updateTabUI(tab);

        Fragment fragment = (tab == AdminTab.EVENTS)
                ? new AdminEventsFragment()
                : new AdminNotificationsFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.adminFragmentContainer, fragment);
        transaction.commit();
    }

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
        // Premier chargement : initialiser avec le fragment Events
        if (getSupportFragmentManager().findFragmentById(R.id.adminFragmentContainer) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.adminFragmentContainer, new AdminEventsFragment())
                    .commit();
        }
    }
}