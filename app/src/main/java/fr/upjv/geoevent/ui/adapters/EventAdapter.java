package fr.upjv.geoevent.ui.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.models.Evenement;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Evenement> eventList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public EventAdapter(List<Evenement> eventList, Context context) {
        this.eventList = eventList;
    }

    public void updateList(List<Evenement> newList) {
        this.eventList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Evenement event = eventList.get(position);

        holder.title.setText(event.getTitre());
        holder.location.setText(event.getLieu());

        if (event.getDateEvenement() != null) {
            holder.date.setText(dateFormat.format(event.getDateEvenement()));
        }

        if (event.getDistance() > 0) {
            holder.distance.setVisibility(View.VISIBLE);
            holder.distance.setText(String.format(Locale.getDefault(), "%.1f km", event.getDistance()));
        } else {
            holder.distance.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("EVENEMENT_EXTRA", event);
            Navigation.findNavController(v).navigate(R.id.navigation_event_detail, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, location, date, distance;
        ImageView image;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleEvent);
            location = itemView.findViewById(R.id.locationEvent);
            date = itemView.findViewById(R.id.dateEvent);
            image = itemView.findViewById(R.id.imageEvent);
            distance = itemView.findViewById(R.id.distanceEvent);
        }
    }
}