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
import java.util.List;
import java.util.Locale;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.models.Evenement;

/**
 * Adapter RecyclerView pour la liste des événements dans le panneau d'administration.
 * Chaque item expose des boutons Modifier et Supprimer.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminEventViewHolder> {

    public interface OnEventActionListener {
        void onEdit(Evenement event, int position);
        void onDelete(Evenement event, int position);
    }

    private final List<Evenement> fullList   = new ArrayList<>();
    private final List<Evenement> filteredList = new ArrayList<>();
    private final OnEventActionListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);

    public AdminEventAdapter(OnEventActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<Evenement> events) {
        fullList.clear();
        fullList.addAll(events);
        filteredList.clear();
        filteredList.addAll(events);
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
                if (matchTitle || matchLieu) {
                    filteredList.add(e);
                }
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

    public Evenement getItem(int position) {
        return filteredList.get(position);
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

        holder.title.setText(event.getTitre() != null ? event.getTitre() : "—");
        holder.location.setText(event.getLieu() != null ? event.getLieu() : "—");
        holder.participants.setText(event.getNombreParticipant() + " participant(s)");
        holder.date.setText(event.getDateEvenement() != null
                ? sdf.format(event.getDateEvenement()) : "Date non définie");

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(event, holder.getAdapterPosition());
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(event, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

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