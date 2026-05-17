package fr.upjv.geoevent.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.models.Evenement;
import fr.upjv.geoevent.domain.repository.EvenementRepository;
import fr.upjv.geoevent.services.FcmNotificationService;
import fr.upjv.geoevent.services.INotificationService;

/**
 * Fragment d'envoi de notifications push.
 *
 * Correction : pour cibler les inscrits d'un événement, on passe le TITRE de l'événement
 * (pas son docId) car la collection "inscriptionevent" est indexée sur "eventTitre".
 * La Cloud Function "sendToEventSubscribers" fait la jointure elle-même.
 */
public class AdminNotificationsFragment extends Fragment {

    private INotificationService notificationService;
    private EvenementRepository repository;

    private TextInputEditText notifTitleInput;
    private TextInputEditText notifBodyInput;
    private RadioGroup targetRadioGroup;
    private View eventSpinnerContainer;
    private Spinner eventSpinner;
    private MaterialButton sendButton;
    private View loadingIndicator;

    private final List<Evenement> events   = new ArrayList<>();
    private int selectedEventPosition = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_notifications, container, false);

        notificationService = new FcmNotificationService();
        repository          = new EvenementRepository();

        notifTitleInput       = view.findViewById(R.id.notifTitleInput);
        notifBodyInput        = view.findViewById(R.id.notifBodyInput);
        targetRadioGroup      = view.findViewById(R.id.targetRadioGroup);
        eventSpinnerContainer = view.findViewById(R.id.eventSpinnerContainer);
        eventSpinner          = view.findViewById(R.id.eventSpinner);
        sendButton            = view.findViewById(R.id.sendNotifButton);
        loadingIndicator      = view.findViewById(R.id.notifLoading);

        setupTargetToggle();
        loadEventsForSpinner();
        sendButton.setOnClickListener(v -> sendNotification());

        return view;
    }

    private void setupTargetToggle() {
        targetRadioGroup.setOnCheckedChangeListener((group, checkedId) ->
                eventSpinnerContainer.setVisibility(
                        checkedId == R.id.radioSpecificEvent ? View.VISIBLE : View.GONE));
    }

    private void loadEventsForSpinner() {
        repository.getEvents(new DataCallback() {
            @Override
            public void onSuccess(Object data) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (data instanceof QuerySnapshot) {
                        events.clear();
                        events.addAll(((QuerySnapshot) data).toObjects(Evenement.class));

                        List<String> titles = new ArrayList<>();
                        for (Evenement e : events) {
                            titles.add(e.getTitre() != null ? e.getTitre() : "Sans titre");
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_item, titles);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        eventSpinner.setAdapter(adapter);

                        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                                selectedEventPosition = pos;
                            }
                            @Override public void onNothingSelected(AdapterView<?> parent) {}
                        });
                    }
                });
            }
            @Override public void onError(Exception e) {}
        });
    }

    private void sendNotification() {
        String title = notifTitleInput.getText() != null ? notifTitleInput.getText().toString().trim() : "";
        String body  = notifBodyInput.getText()  != null ? notifBodyInput.getText().toString().trim()  : "";

        if (title.isEmpty()) { notifTitleInput.setError("Titre requis"); return; }
        if (body.isEmpty())  { notifBodyInput.setError("Message requis"); return; }

        setLoading(true);

        INotificationService.NotificationCallback callback = new INotificationService.NotificationCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    setLoading(false);
                    notifTitleInput.setText("");
                    notifBodyInput.setText("");
                    Toast.makeText(getContext(), "✓ Notification envoyée !", Toast.LENGTH_LONG).show();
                });
            }
            @Override
            public void onFailure(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(getContext(), "Erreur : " + error, Toast.LENGTH_LONG).show();
                });
            }
        };

        int checkedId = targetRadioGroup.getCheckedRadioButtonId();

        if (checkedId == R.id.radioAllUsers) {
            notificationService.sendToAllUsers(title, body, callback);

        } else if (checkedId == R.id.radioSpecificEvent) {
            if (events.isEmpty()) {
                setLoading(false);
                Toast.makeText(getContext(), "Aucun événement disponible.", Toast.LENGTH_SHORT).show();
                return;
            }
            // On passe le TITRE — c'est la clé utilisée dans la collection inscriptionevent
            String eventTitre = events.get(selectedEventPosition).getTitre();
            if (eventTitre == null || eventTitre.isEmpty()) {
                setLoading(false);
                Toast.makeText(getContext(), "Cet événement n'a pas de titre.", Toast.LENGTH_SHORT).show();
                return;
            }
            notificationService.sendToEventSubscribers(eventTitre, title, body, callback);
        }
    }

    private void setLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        sendButton.setEnabled(!isLoading);
    }
}