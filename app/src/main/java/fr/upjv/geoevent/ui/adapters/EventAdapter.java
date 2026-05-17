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
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.models.Evenement;

/**
 * Adaptateur RecyclerView responsable de l'affichage de la liste des événements.
 * Chaque élément de la liste présente le titre, le lieu, la date, la distance
 * et le nombre de participants d'un événement. Un clic sur un élément navigue
 * vers le fragment de détail correspondant.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Evenement> eventList;

    /** Formateur de date utilisé pour l'affichage uniforme dans chaque carte événement. */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    /**
     * Constructeur de l'adaptateur.
     *
     * @param eventList La liste initiale des événements à afficher.
     * @param context   Le contexte Android (non utilisé directement mais conservé pour extensibilité).
     */
    public EventAdapter(List<Evenement> eventList, Context context) {
        this.eventList = eventList;
    }

    /**
     * Met à jour la liste affichée par l'adaptateur et notifie le RecyclerView
     * que les données ont changé afin de rafraîchir l'affichage.
     *
     * @param newList La nouvelle liste d'événements à afficher.
     */
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

    /**
     * Remplit les vues d'un item de la liste avec les données de l'événement correspondant.
     * Affiche la distance si elle est renseignée, et formate le nombre de participants
     * en accordant correctement le mot "participant" au singulier ou au pluriel.
     *
     * @param holder   Le ViewHolder contenant les vues de l'item.
     * @param position L'indice de l'événement dans la liste.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Evenement event = eventList.get(position);

        holder.title.setText(event.getTitre());
        holder.location.setText(event.getLieu());

        if (event.getDateEvenement() != null) {
            holder.date.setText(dateFormat.format(event.getDateEvenement()));
        }

        int nb = event.getNombreParticipant();
        String pLabel = nb > 1 ? " participants" : " participant";
        holder.participants.setText(nb + pLabel);

        if (event.getDistance() > 0) {
            holder.distance.setVisibility(View.VISIBLE);
            holder.distance.setText(String.format(Locale.getDefault(), "%.1f km", event.getDistance()));
        } else {
            holder.distance.setVisibility(View.GONE);
        }


        if (event.getImages() != null && !event.getImages().isEmpty() && event.getImages().get(0) != null) {
            String urlImage = event.getImages().get(0);

            // Chargement asynchrone optimisé de l'image distante via la bibliothèque Glide
            Glide.with(holder.itemView.getContext())
                    .load(urlImage)
                    .placeholder(android.R.drawable.ic_menu_gallery) // Image de substitution en attente du réseau
                    .error(android.R.drawable.ic_menu_report_image)    // Image d'erreur en cas de lien brisé
                    .into(holder.image);
        } else {
            // Image par défaut si aucune illustration n'est liée à l'événement (Sécurité de recyclage)
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
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

    /**
     * ViewHolder représentant les vues d'un seul item événement dans la liste.
     * Maintient des références directes aux composants UI pour éviter des appels
     * répétés à findViewById lors du défilement.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, location, date, distance, participants;
        ImageView image;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleEvent);
            location = itemView.findViewById(R.id.locationEvent);
            date = itemView.findViewById(R.id.dateEvent);
            image = itemView.findViewById(R.id.imageEvent);
            distance = itemView.findViewById(R.id.distanceEvent);
            participants = itemView.findViewById(R.id.participantsEvent);
        }
    }
}