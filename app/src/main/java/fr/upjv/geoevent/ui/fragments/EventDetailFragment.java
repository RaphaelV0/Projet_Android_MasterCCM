package fr.upjv.geoevent.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.models.Evenement;

public class EventDetailFragment extends Fragment {

    private ImageView imageEventDetail;
    private TextView titreEvent, dateEvent, lieuEvent, descriptionEvent;
    private MaterialButton btnRegister;
    private ImageButton btnBack;
    private Evenement currentEvent;

    public EventDetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageEventDetail = view.findViewById(R.id.ImageEventDetail);
        titreEvent = view.findViewById(R.id.TitreEvent);
        dateEvent = view.findViewById(R.id.DateEvent);
        lieuEvent = view.findViewById(R.id.LieuEvent);
        descriptionEvent = view.findViewById(R.id.DescriptionEvent);
        btnRegister = view.findViewById(R.id.RegisterEvent);
        btnBack = view.findViewById(R.id.btnBack);

        if (getArguments() != null) {
            currentEvent = (Evenement) getArguments().getSerializable("EVENEMENT_EXTRA");
        }

        if (currentEvent != null) {
            displayEventDetails();
        }

        btnRegister.setOnClickListener(v -> inscrireEvent());
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void displayEventDetails() {
        titreEvent.setText(currentEvent.getTitre());
        lieuEvent.setText(currentEvent.getLieu());
        descriptionEvent.setText(currentEvent.getDescription());

        if (currentEvent.getDateEvenement() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.FRANCE);
            dateEvent.setText(sdf.format(currentEvent.getDateEvenement()));
        }
    }

    private void inscrireEvent() {
        currentEvent.setNombreParticipant(currentEvent.getNombreParticipant() + 1);
        Toast.makeText(getContext(), "Inscription réussie", Toast.LENGTH_SHORT).show();
        btnRegister.setEnabled(false);
        btnRegister.setText("Inscrit");
    }
}