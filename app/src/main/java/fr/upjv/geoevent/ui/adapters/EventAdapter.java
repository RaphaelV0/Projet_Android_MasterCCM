package fr.upjv.geoevent.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.models.Evenement;
import fr.upjv.geoevent.ui.activities.EventRegister;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Evenement> eventList;
    private Context context;

    public EventAdapter(List<Evenement> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Evenement event = eventList.get(position);

        holder.title.setText(event.getTitre());
        holder.location.setText(event.getLieu());

        // Formatage de la date
        if (event.getDateEvenement() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.date.setText(sdf.format(event.getDateEvenement()));
        }

        // Gestion du clic pour aller vers le détail (EventRegister)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventRegister.class);
            // On glisse l'objet complet dans le "colis"
            intent.putExtra("EVENEMENT_EXTRA", event);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, location, date;
        ImageView image;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleEvent);
            location = itemView.findViewById(R.id.locationEvent);
            date = itemView.findViewById(R.id.dateEvent);
            image = itemView.findViewById(R.id.imageEvent);
        }
    }
}