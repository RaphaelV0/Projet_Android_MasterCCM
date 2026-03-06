## 🎨 Design System & Palette de Couleurs

Pour garantir la cohérence visuelle et une bonne expérience utilisateur (UI/UX), le projet utilise la palette de couleurs suivante :

### 🟠 Orange (`#F27A22`) - Couleur d'accentuation (Secondary Color)
C'est la couleur qui attire l'œil. Utilisée pour tout ce qui appelle à l'action.
* **Boutons principaux** : "Se connecter", "Valider", ou le bouton flottant (FAB - Floating Action Button) pour "Ajouter un point d'intérêt".
* **Marqueurs sur la carte** : Les "pins" GPS de nos événements sur Google Maps ou OpenStreetMap.
* **Badges & Notifications** : Alertes et notifications (push).
* **Interrupteurs (Switches)** : Indique qu'une option est activée (ex: filtrage par distance).

### 🔵 Bleu principal (`#1D4586`) - Couleur de marque (Primary Color)
C'est l'identité de base de notre application. Elle donne un côté sérieux et structuré.
* **Barre d'application (Top App Bar)** : Le bandeau en haut de l'écran contenant le titre de la page.
* **Barre de navigation (Bottom Navigation)** : Les icônes du menu en bas de l'écran (ex: Carte, Liste, Profil).
* **Titres principaux** : Les grands titres (H1, H2) dans les pages de détails d'un événement.
* **Boutons secondaires** : Pour des actions moins importantes ("Annuler", "Retour").

### 🟦 Bleu plus clair (`#26529C`) - Couleur variante (Primary Variant)
Elle vient soutenir le bleu principal pour créer de la nuance et de la profondeur.
* **États de sélection** : Pour montrer qu'un onglet ou une icône de menu est "actif".
* **Bordures ou séparateurs** : Les lignes qui séparent deux événements dans une liste.
* **Arrière-plan d'en-tête** : Fond de la zone supérieure du menu latéral (Drawer) contenant le profil de l'utilisateur.

### ⚪ Blanc (`#FFFFFF`) - Fond et surfaces (Background / Surface)
Pour aérer l'application et garantir la lisibilité.
* **Arrière-plan de l'application** : Le fond général de toutes nos fenêtres (Activities).
* **Cartes (CardViews)** : Le fond des blocs blancs surélevés qui contiendront la description et le titre de vos événements.
* **Texte sur fond sombre** : Tout texte placé au-dessus du bleu principal ou de l'orange.

### ⚫ Noir (`#000000`) - Texte principal (On Background / On Surface)
* **Texte courant** : Les descriptions d'événements, les formulaires de saisie.
* **Sous-titres** : Pour hiérarchiser l'information de manière claire.

> 💡 **Note UI/UX** : Pour éviter la fatigue visuelle sur les écrans, le noir pur (`#000000`) est réservé au logo. Le texte courant de l'application utilise un gris très foncé (comme `#212121` ou `#333333`).