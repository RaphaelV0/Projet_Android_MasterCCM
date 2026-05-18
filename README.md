# GeoEvent

GeoEvent est une application Android permettant la création, la visualisation et la gestion d'événements géolocalisés.

## Table des Matières
1. [Architecture](#architecture)
2. [Choix Techniques](#choix-techniques)
3. [Organisation des Packages](#organisation-des-packages)
4. [Procédure d'Installation](#procédure-dinstallation)
5. [Auteurs](#auteurs)

---

## Architecture

L'application suit une architecture **multicouche** inspirée des principes de la Clean Architecture, favorisant le découplage entre la logique métier, les données et l'interface utilisateur :

- **Couche UI (Présentation)** : Utilise le pattern **MVVM** (Model-View-ViewModel) pour séparer l'affichage de la logique de présentation.
- **Couche Domain (Métier)** : Contient les modèles de données, les interfaces de services et le **Repository** qui centralise l'accès aux données.
- **Couche Data (Infrastructure)** : Implémente l'accès aux données via des services concrets (Firestore, Storage) tout en restant interchangeable grâce au pattern Factory.
- **Services** : Gère les fonctionnalités transverses comme les notifications Push (FCM).

## Choix Techniques

### Stack Technologique
- **Langage** : Java (SDK 11)
- **Minimum SDK** : 26 (Android 8.0)
- **Target SDK** : 35 (Android 15)

### Bibliothèques et Frameworks
- **Firebase** : 
    - **Authentication** : Gestion des comptes utilisateurs (Google Sign-In).
    - **Firestore** : Base de données NoSQL en temps réel pour les événements.
    - **Storage** : Stockage des images d'événements.
    - **Cloud Messaging (FCM)** : Notifications push via Cloud Functions.
- **Cartographie** : **Osmdroid** (OpenStreetMap) pour l'affichage de la carte et la géolocalisation.
- **Navigation** : **Jetpack Navigation Component** avec `nav_graph` pour une gestion fluide des fragments.
- **Gestion d'images** : **Glide** pour le chargement et la mise en cache des images.
- **UI** : **Material Design Components**, `RecyclerView`, `ConstraintLayout`.

## Organisation des Packages

Le code source est organisé de manière modulaire sous `fr.upjv.geoevent` :

```text
├── domain/                # Logique métier et abstraction des données
│   ├── auth/              # Gestion de l'authentification
│   ├── data/              # Abstractions et implémentations de services (Firestore, etc.)
│   ├── models/            # Objets métier (Evenement, Utilisateur, etc.)
│   └── repository/        # Centralisation des accès aux données
├── ui/                    # Couche de présentation
│   ├── activities/        # Activités principales (MapActivity, etc.)
│   ├── fragments/         # Écrans de l'application (Détails, Création, Profil)
│   ├── viewmodels/        # Logique de présentation et état de l'UI
│   └── adapters/          # Adapteurs pour les listes (RecyclerView)
├── services/              # Services d'arrière-plan (Notifications FCM)
└── receivers/             # BroadcastReceivers (Boot, Connectivité)
```

## Procédure d'Installation

### Prérequis
- Android Studio (dernière version recommandée)
- JDK 11
- Un appareil Android physique ou un émulateur (API 26+)

### Étapes
1. **Cloner le projet** :
   ```bash
   git clone git@github.com:RaphaelV0/Projet_Android_MasterCCM.git
   ```
2. **Configuration Firebase** :
   - Le fichier `google-services.json` doit être présent dans le dossier `/app`.
   - Si vous utilisez votre propre projet Firebase, assurez-vous d'activer Firestore, Storage et Auth (Google).
3. **Synchronisation Gradle** :
   - Ouvrez le projet dans Android Studio.
   - Attendez la fin de la synchronisation Gradle.
4. **Déploiement** :
   - Cliquez sur "Run 'app'" pour installer l'application sur votre appareil.

## Auteurs

* **Verchain Raphaël**
* **Poncey--Valdemar Jayson**
* **Youkou Ngongang Brice**
* **Suamunu Miriam**
