package fr.upjv.geoevent.ui.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.models.Evenement;
import fr.upjv.geoevent.domain.repository.EvenementRepository;
import fr.upjv.geoevent.ui.adapters.AdminEventAdapter;

/**
 * Fragment de gestion des événements pour l'administrateur.
 * Fonctionnalités : liste, recherche, tri, création, modification, suppression.
 */
public class AdminEventsFragment extends Fragment implements AdminEventAdapter.OnEventActionListener {

    private AdminEventAdapter adapter;
    private EvenementRepository repository;
    private List<Evenement> allEvents = new ArrayList<>();
    private List<DocumentSnapshot> eventDocuments = new ArrayList<>();

    private TextInputEditText searchInput;
    private Spinner sortSpinner;
    private TextView emptyStateText;
    private RecyclerView recyclerView;

    private int editYear, editMonth, editDay, editHour, editMinute;
    private boolean editDateSelected = false;
    private TextView editDateDisplay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_events, container, false);

        repository = new EvenementRepository();

        searchInput  = view.findViewById(R.id.adminSearchInput);
        sortSpinner  = view.findViewById(R.id.adminSortSpinner);
        emptyStateText = view.findViewById(R.id.adminEmptyState);
        recyclerView = view.findViewById(R.id.adminEventsRecycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminEventAdapter(this);
        recyclerView.setAdapter(adapter);

        setupSearch();
        setupSort();

        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdminAddEvent);
        fabAdd.setOnClickListener(v -> showCreateDialog());

        loadEvents();

        return view;
    }

    private void loadEvents() {
        repository.getEvents(new DataCallback() {
            @Override
            public void onSuccess(Object data) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (data instanceof QuerySnapshot) {
                        QuerySnapshot qs = (QuerySnapshot) data;
                        allEvents = qs.toObjects(Evenement.class);
                        eventDocuments = new ArrayList<>(qs.getDocuments());
                        adapter.setData(allEvents);
                        updateEmptyState();
                    }
                });
            }
            @Override
            public void onError(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Erreur chargement : " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateEmptyState();
            }
        });
    }

    private void setupSort() {
        String[] options = {"Trier par : Date (récent)", "Titre (A→Z)", "Plus d'inscrits"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: adapter.sortByDate();         break;
                    case 1: adapter.sortByTitle();        break;
                    case 2: adapter.sortByParticipants(); break;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateEmptyState() {
        emptyStateText.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEdit(Evenement event, int position) {
        String docId = (position < eventDocuments.size())
                ? eventDocuments.get(position).getId() : null;
        if (docId == null) {
            Toast.makeText(getContext(), "Impossible d'identifier l'événement.", Toast.LENGTH_SHORT).show();
            return;
        }
        showEditDialog(event, docId);
    }

    @Override
    public void onDelete(Evenement event, int position) {
        String docId = (position < eventDocuments.size())
                ? eventDocuments.get(position).getId() : null;
        if (docId == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer l'événement")
                .setMessage("Voulez-vous vraiment supprimer « " + event.getTitre() + " » ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    repository.deleteEvent(docId);
                    allEvents.remove(position);
                    eventDocuments.remove(position);
                    adapter.setData(allEvents);
                    updateEmptyState();
                    Toast.makeText(getContext(), "Événement supprimé", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showCreateDialog() {
        showEventDialog(null, null);
    }

    private void showEditDialog(Evenement event, String docId) {
        showEventDialog(event, docId);
    }

    private void showEventDialog(@Nullable Evenement event, @Nullable String docId) {
        boolean isEdit = event != null;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_event, null);

        TextInputEditText editTitle       = dialogView.findViewById(R.id.dialogEventTitle);
        TextInputEditText editDescription = dialogView.findViewById(R.id.dialogEventDescription);
        TextInputEditText editLieu        = dialogView.findViewById(R.id.dialogEventLieu);
        editDateDisplay                   = dialogView.findViewById(R.id.dialogEventDate);

        if (isEdit) {
            editTitle.setText(event.getTitre());
            editDescription.setText(event.getDescription());
            editLieu.setText(event.getLieu());
            if (event.getDateEvenement() != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(event.getDateEvenement());
                editYear  = c.get(Calendar.YEAR);
                editMonth = c.get(Calendar.MONTH);
                editDay   = c.get(Calendar.DAY_OF_MONTH);
                editHour  = c.get(Calendar.HOUR_OF_DAY);
                editMinute = c.get(Calendar.MINUTE);
                editDateSelected = true;
                updateEditDateDisplay();
            }
        }

        editDateDisplay.setOnClickListener(v -> showEditDatePicker());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? "Modifier l'événement" : "Créer un événement")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Enregistrer" : "Créer", (dialog, which) -> {
                    String titre = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";
                    String desc  = editDescription.getText() != null ? editDescription.getText().toString().trim() : "";
                    String lieu  = editLieu.getText() != null ? editLieu.getText().toString().trim() : "";

                    if (titre.isEmpty()) {
                        Toast.makeText(getContext(), "Le titre est obligatoire.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Date dateEvenement = null;
                    if (editDateSelected) {
                        Calendar c = Calendar.getInstance();
                        c.set(editYear, editMonth, editDay, editHour, editMinute, 0);
                        dateEvenement = c.getTime();
                    }

                    if (isEdit) {
                        event.setTitre(titre);
                        event.setDescription(desc);
                        event.setLieu(lieu);
                        if (dateEvenement != null) event.setDateEvenement(dateEvenement);
                        repository.updateEvent(docId, event);
                        adapter.setData(allEvents);
                        Toast.makeText(getContext(), "Événement mis à jour", Toast.LENGTH_SHORT).show();
                    } else {
                        Evenement nouvel = new Evenement(titre, desc, lieu, dateEvenement);
                        repository.createEvent(nouvel);
                        loadEvents();
                        Toast.makeText(getContext(), "Événement créé", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null);

        builder.create().show();
    }

    private void showEditDatePicker() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            editYear = year; editMonth = month; editDay = day;
            new TimePickerDialog(requireContext(), (tv, hour, minute) -> {
                editHour = hour; editMinute = minute;
                editDateSelected = true;
                updateEditDateDisplay();
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateEditDateDisplay() {
        if (editDateDisplay != null) {
            editDateDisplay.setText(String.format(Locale.FRANCE,
                    "%02d/%02d/%d %02d:%02d", editDay, editMonth + 1, editYear, editHour, editMinute));
        }
    }
}