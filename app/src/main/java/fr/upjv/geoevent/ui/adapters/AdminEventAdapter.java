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
 * Adapter RecyclerView pour la liste admin.
 * Le docId Firestore est stocké par événement dans une Map pour éviter
 * toute désynchronisation avec le filtrage/tri.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminEventViewHolder> {

    public interface OnEventActionListener {
        void onEdit(Evenement event, String docId);
        void onDelete(Evenement event, String docId);
    }

    private final List<Evenement> fullList     = new ArrayList<>();
    private final List<Evenement> filteredList = new ArrayList<>();

    // Clé = hashCode de l'objet Evenement, Valeur = docId Firestore
    private final Map<Evenement, String> docIdMap = new HashMap<>();

    private final OnEventActionListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);

    public AdminEventAdapter(OnEventActionListener listener) {
        this.listener = listener;
    }

    /**
     * Met à jour les données. Les événements et leurs docIds sont associés
     * via la même liste parallèle fournie par AdminEventsFragment.
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

    public void sortByParticipants() {
        filteredList.sort((a, b) -> Integer.compare(b.getNombreParticipant(), a.getNombreParticipant()));
        notifyDataSetChanged();
    }

    public void sortByTitle() {
        filteredList.sort((a, b) -> {
            if (a.getTitre() == null) return 1;
            if (b.getTitre() == null) return -1;
            return a.getTitre().compareToIgnoreCase(b.getTitre());
        });
        notifyDataSetChanged();
    }

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