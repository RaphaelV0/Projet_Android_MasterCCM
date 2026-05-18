package fr.upjv.geoevent.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.models.Evenement;

/**
 * Adaptateur RecyclerView pour l'affichage et la gestion des événements dans l'interface d'administration.
 * Supporte le filtrage, le tri dynamique et la liaison sécurisée avec les identifiants de documents Firestore.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminEventViewHolder> {

    /**
     * Interface de communication pour les actions effectuées sur un événement (édition, suppression).
     */
    public interface OnEventActionListener {
        void onEdit(Evenement event, String docId);
        void onDelete(Evenement event, String docId);
    }

    private final List<Evenement> fullList     = new ArrayList<>();
    private final List<Evenement> filteredList = new ArrayList<>();

    /** Association entre un objet Evenement et son identifiant unique Firestore (docId) */
    private final Map<Evenement, String> docIdMap = new HashMap<>();

    private final OnEventActionListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);

    public AdminEventAdapter(OnEventActionListener listener) {
        this.listener = listener;
    }

    /**
     * Met à jour la source de données de l'adaptateur.
     * Synchronise la liste des objets métier avec leurs identifiants Firestore respectifs.
     *
     * @param events La liste des événements récupérés.
     * @param docIds La liste correspondante des identifiants de documents.
     */
    public void setData(List<Evenement> events, List<String> docIds) {
        fullList.clear();
        filteredList.clear();
        docIdMap.clear();

        for (int i = 0; i < events.size(); i++) {
            Evenement e = events.get(i);
            fullList.add(e);
            filteredList.add(e);
            if (i < docIds.size()) {
                docIdMap.put(e, docIds.get(i));
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Filtre la liste des événements en fonction d'une saisie textuelle (titre ou lieu).
     * @param query La chaîne de caractères de recherche.
     */
    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String lower = query.trim().toLowerCase(Locale.FRANCE);
            for (Evenement e : fullList) {
                boolean matchTitle = e.getTitre() != null && e.getTitre().toLowerCase(Locale.FRANCE).contains(lower);
                boolean matchLieu  = e.getLieu()  != null && e.getLieu().toLowerCase(Locale.FRANCE).contains(lower);
                if (matchTitle || matchLieu) filteredList.add(e);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Trie la liste courante par nombre décroissant de participants.
     */
    public void sortByParticipants() {
        filteredList.sort((a, b) -> Integer.compare(b.getNombreParticipant(), a.getNombreParticipant()));
        notifyDataSetChanged();
    }

    /**
     * Trie la liste courante par ordre alphabétique des titres.
     */
    public void sortByTitle() {
        filteredList.sort((a, b) -> {
            if (a.getTitre() == null) return 1;
            if (b.getTitre() == null) return -1;
            return a.getTitre().compareToIgnoreCase(b.getTitre());
        });
        notifyDataSetChanged();
    }

    /**
     * Trie la liste courante par date (du plus récent au plus ancien).
     */
    public void sortByDate() {
        filteredList.sort((a, b) -> {
            if (a.getDateEvenement() == null) return 1;
            if (b.getDateEvenement() == null) return -1;
            return b.getDateEvenement().compareTo(a.getDateEvenement());
        });
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new AdminEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminEventViewHolder holder, int position) {
        Evenement event = filteredList.get(position);
        String docId    = docIdMap.get(event);

        holder.title.setText(event.getTitre() != null ? event.getTitre() : "—");
        holder.location.setText(event.getLieu() != null ? event.getLieu() : "—");
        holder.participants.setText(event.getNombreParticipant() + " participant(s)");
        holder.date.setText(event.getDateEvenement() != null
                ? sdf.format(event.getDateEvenement()) : "Date non définie");

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null && docId != null) listener.onEdit(event, docId);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null && docId != null) listener.onDelete(event, docId);
        });
    }

    @Override
    public int getItemCount() { return filteredList.size(); }

    /**
     * Vue mémorisée pour un élément de la liste administrative.
     */
    static class AdminEventViewHolder extends RecyclerView.ViewHolder {
        TextView title, location, participants, date;
        MaterialButton btnEdit, btnDelete;

        AdminEventViewHolder(@NonNull View itemView) {
            super(itemView);
            title        = itemView.findViewById(R.id.adminEventTitle);
            location     = itemView.findViewById(R.id.adminEventLocation);
            participants = itemView.findViewById(R.id.adminEventParticipants);
            date         = itemView.findViewById(R.id.adminEventDate);
            btnEdit      = itemView.findViewById(R.id.btnAdminEdit);
            btnDelete    = itemView.findViewById(R.id.btnAdminDelete);
        }
    }
}