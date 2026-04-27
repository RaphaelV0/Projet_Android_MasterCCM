package fr.upjv.geoevent.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.auth.AuthServiceFactory;
import fr.upjv.geoevent.services.IAuthService;
import fr.upjv.geoevent.ui.activities.ConnexionActivity;

public class ProfileFragment extends Fragment {

    private IAuthService authService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        authService = AuthServiceFactory.create();

        view.findViewById(R.id.logoutButton).setOnClickListener(v -> {
            authService.logout();
            
            // Rediriger vers l'écran de connexion
            Intent intent = new Intent(requireActivity(), ConnexionActivity.class);
            // On vide la pile d'activités pour éviter de revenir en arrière
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}
