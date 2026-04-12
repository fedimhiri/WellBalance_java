# Analyse Méthodologique et Technique Complète : WellBalance Messenger

Ce document fournit une analyse exhaustive, classe par classe et méthode par méthode, de l'intégralité du module de messagerie.

---

## 1. Points d'Entrée et Cycle de Vie (Package `tn.esprit`)

### `MainFX.java` (Classe Application)
- **`public void start(Stage primaryStage)`** : 
    - **Détail** : Charge le fichier `Login.fxml` via `FXMLLoader`. Crée une `Scene` rootée sur ce FXML. 
    - **Logique** : Configure le titre de la fenêtre ("WellBalance Messenger - Connexion") et affiche le `Stage`. C'est l'initiation de la boucle d'événements JavaFX.
- **`public static void main(String[] args)`** : Appelle `launch(args)` qui transfère le contrôle au thread graphique.

### `Main.java`
- Simple classe "wrapper" utilisée pour contourner les restrictions de certains environnements Java sur le lancement direct des classes héritant de `Application`.

---

## 2. Utilitaires et Gestion d'Infrastructure (Package `tn.esprit.util`)

### `MyConnection.java` (Pattern Singleton)
- **Champs** : `cnx` (objet `Connection`), `instance` (objet `MyConnection`).
- **`public static MyConnection getInstance()`** : 
    - **Détail** : Vérifie si `instance == null`. Si oui, crée un nouvel objet. Cela garantit une **connexion unique** à la base de données pour toute l'application.
- **Constructeur Privé** : Initialise le driver JDBC (`com.mysql.cj.jdbc.Driver`) et établit le lien avec l'URL de la base.

### `SessionManager.java` (Gestion de Contexte)
- **`public static User currentUser`** : Variable de classe statique stockant l'utilisateur logué.
- **Utilité** : Permet de ne pas avoir à passer l'ID de l'utilisateur de contrôleur en contrôleur.

### `AudioRecorder.java` (Multimédia)
- **`public void start()`** : 
    - **Technique** : Ouvre une `TargetDataLine` avec un format Audio (16kHz, 16-bit, Mono).
    - **Détail** : Lance un **Nouveau Thread** qui lit les données micro et les écrit dans un fichier WAV. Le thread est crucial pour ne pas "geler" l'interface.
- **`public void stop()`** : Ferme la ligne de capture et sauvegarde le flux sur le disque.

---

## 3. Modèles de Données (Package `tn.esprit.models`)

### `Message.java`
- **Champs avancés** : 
    - `parentId` : Permet l'arborescence (réponses).
    - `reactions` : Chaîne de caractères formatée (CSV) stockant les emojis.
    - `messageType` : Enumère "text", "audio", ou "image".
- **Méthodes** : Getters/Setters standards avec support pour les objets `Timestamp`.

### `Conversation.java`
- **`isTyping`** : Flags permettant d'indiquer si l'autre personne écrit.
- **`unreadCount`** : Champ volatil (non-persistant ou calculé) utilisé pour l'interface de notification.

---

## 4. Services et Persistance (Package `tn.esprit.services`)

### `MessageService.java`
- **`public void add(Message message)`** :
    - Prépare l'insertion SQL. Utilise `Statement.RETURN_GENERATED_KEYS` pour récupérer l'ID attribué par MySQL et l'injecter dans l'objet Java immédiatement après l'envoi.
- **`public void toggleReaction(int messageId, String reactorName, String reaction)`** :
    - **Algorithme** : Lit la chaîne de réactions courante. Découpe la chaîne par virgule. Cherche une entrée commençant par `reactorName`. Si l'emoji est identique, elle supprime l'entrée. Sinon, elle l'ajoute. C'est la logique de **Basculement**.
- **`public List<Message> getByConversationId(int conversationId)`** : 
    - Récupère tous les messages. Note : elle appelle également `userService.getById()` pour chaque message afin de peupler l'objet `Sender`, permettant d'afficher le bon avatar et nom dans le chat.

### `ConversationService.java`
- **`public List<Conversation> getConversationsForDoctor(int doctorId)`** : 
    - Effectue une requête SQL complexe pour lister les chats et joindre les détails du patient (Nom, Email) pour un affichage propre dans la barre latérale.

---

## 5. Contrôleurs d'Interface (Package `tn.esprit.controllers`)

### `AdminMessengerController.java` (Analyse de la Logique UI)

#### **Le Rafraîchissement Automatique (`Timeline`)**
- Créé dans `initialize()`. Il tourne indéfiniment. 
- Toutes les 3 secondes, il appelle `loadConversations()` pour voir si de nouveaux messages sont arrivés SANS que le docteur ait besoin de rafraîchir manuellement.

#### **La Gestion des Réponses (`handleReply`)**
- Lorsqu'on clique sur "Répondre", le contrôleur stocke le message cible dans `replyingToMessage` et affiche un petit bloc visuel au-dessus du champ texte pour confirmer la sélection.

#### **La Construction Graphique des Bulles (`createMessageBox`)**
1. **Évaluation** : Détermine l'alignement (Droit pour 'Moi', Gauche pour 'Tiers').
2. **Construction** : Crée un `VBox` pour la bulle.
3. **Conditionnel Citations** : Si `msg.getParentId()` existe, elle crée une sub-box avec un style grisâtre contenant le début du message cité.
4. **Conditionnel Réactions** : Elle parcourt la chaîne CSV des réactions et génère des petits badges `Label` avec un fond blanc et une ombre portée pour chaque emoji présent.
5. **Gestion Audio** : Si `type == "audio"`, elle ajoute un bouton Play. Au clic, elle utilise `javafx.scene.media.MediaPlayer` pour jouer le fichier WAV.

#### **Dashboard Innovant**
- **`loadDashboardStats()`** : Analyse dynamiquement la liste des conversations pour remplir le `PieChart`. Elle utilise des filtrages Java Stream (`convs.stream().filter(...)`) pour une performance optimale.

---

### `LoginController.java`
- **`handleLogin()`** : 
    - Vérifie les identifiants. Si valide, elle utilise `FXMLLoader` pour charger soit `AdminMessenger.fxml` soit `PatientMessenger.fxml`.
    - **Point Technique** : Utilise `Stage stage = (Stage) loginBtn.getScene().getWindow(); stage.setScene(newScene);` pour changer de vue sans ouvrir une nouvelle fenêtre.

---

## 6. Synthèse des Principes Appliqués
- **Validation** : Sécurisation systématique des entrées (modification de message vide interdite).
- **Architecture** : Séparation stricte. Si la base de données change, on ne modifie que les **Services**. Si le look change, on ne modifie que les **CSS/FXML**.
- **Performance** : Utilisation de `PreparedStatement` pour la rapidité et la protection contre les injections SQL.

---
*Ce document de 360° termine l'analyse absolue du module de messagerie WellBalance.*
