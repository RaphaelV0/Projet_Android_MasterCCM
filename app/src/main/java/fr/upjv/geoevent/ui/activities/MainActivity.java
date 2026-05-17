package fr.upjv.geoevent.ui.activities;


import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.receivers.BatteryReceiver;
import fr.upjv.geoevent.receivers.NetworkReceiver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    // Déclarations
    private NetworkReceiver networkReceiver;
    private BatteryReceiver batteryReceiver;
    private ActivityResultLauncher<String> requestPermissionLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Récupérer les vues
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // On dit à Android que notre Toolbar sert de barre d'action principale
        setSupportActionBar(topAppBar);

        // 2. Récupérer le NavController (Le chef d'orchestre des fragments)
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // 3. Configurer la barre du bas pour qu'elle change les fragments
        NavigationUI.setupWithNavController(bottomNav, navController);

        // 4. Configurer la barre du haut (pour afficher la flèche retour quand il faut)
        // On définit les "écrans racines" (ceux qui n'ont pas de flèche retour)
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_map, R.id.navigation_list, R.id.navigation_profile)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


        // Initialisation des BroadcastReceiver globaux
        networkReceiver = new NetworkReceiver(isConnected -> {
            if (!isConnected) {
                // Ici, plus tard, on pourra désactiver le chargement des événements
                // ou afficher un état "offline" dans l'application.
            }
        });

        batteryReceiver = new BatteryReceiver();

        // Initialisation du launcher de permission
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission accordée !
                    } else {
                        // Permission refusée, l'utilisateur ne verra pas les notifs
                    }
                }
        );

        // Demande la permission au démarrage
        askNotificationPermission();
    }

    private void askNotificationPermission() {
        // La permission n'est nécessaire qu'à partir d'Android 13 (Tiramisu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(
                networkReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        );

        registerReceiver(
                batteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_LOW)
        );
    }
    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(networkReceiver);
        unregisterReceiver(batteryReceiver);



        // 5. CACHER la barre du bas sur les écrans de détails ou d'ajout (comme sur ta maquette)
//        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
//            if (destination.getId() == R.id.navigation_event_details ||
//                    destination.getId() == R.id.navigation_add_event) {
//                // Si on est sur les détails ou l'ajout, on cache la barre du bas
//                bottomNav.setVisibility(View.GONE);
//            } else {
//                // Sinon, on l'affiche (sur la Map par exemple)
//                bottomNav.setVisibility(View.VISIBLE);
//            }
//        });
    }

    // Permet à la flèche retour en haut à gauche de fonctionner
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}