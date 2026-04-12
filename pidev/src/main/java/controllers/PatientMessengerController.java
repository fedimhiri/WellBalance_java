package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Conversation;
import model.Message;
import model.User;
import service.ConversationService;
import service.MessageService;
import service.UserService;
import service.UserSessionService;
import utils.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import utils.AudioRecorder;

public class PatientMessengerController implements Initializable {

    // Supprimés car header retiré du FXML
    // private Label userAvatarLabel;
    // private Label searchField;

    @FXML
    private ComboBox<User> doctorCombo;

    @FXML
    private ComboBox<String> typeCombo;

    @FXML
    private TextField sujetField;

    @FXML
    private TextArea newMessageArea;

    @FXML
    private TextField searchField;

    @FXML
    private VBox conversationsList;

    @FXML
    private VBox chatArea;

    @FXML
    private VBox emptyState;

    @FXML
    private Label chatAvatarLabel;

    @FXML
    private Label chatUserNameLabel;

    @FXML
    private Label chatStatusLabel;

    @FXML
    private ScrollPane messagesScroll;

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextArea messageInput;

    @FXML private HBox standardInputBox;
    @FXML private HBox voiceReviewBox;
    @FXML private Button recordBtn;
    @FXML private Button playPreviewBtn;
    
    // Reply UI elements
    @FXML private HBox replyPreviewBox;
    @FXML private Label replyPreviewText;
    private Message replyingToMessage = null;

    private AudioRecorder audioRecorder = new AudioRecorder();
    private String tempAudioPath = null;
    private MediaPlayer previewPlayer;

    @FXML
    private Label typingLabel;

    @FXML
    private Label statTotalConv;

    @FXML
    private Label statUnread;

        private ConversationService conversationService = new ConversationService();
    private MessageService messageService = new MessageService();
    private UserService userService = new UserService();

    private Conversation currentConversation;
    private Timeline refreshTimeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Check if logged in
        if (Session.getCurrentUser() == null || Session.getCurrentUser().getRoles().contains("ROLE_ADMIN")) {
            navigateToLogin();
            return;
        }

        // Set user info - commenté car labels supprimés du header
        User currentUser = Session.getCurrentUser();
        // searchField.setText(currentUser.getDisplayName());
        // userAvatarLabel.setText(currentUser.getAvatarLetter());

        // Setup type combo
        typeCombo.getItems().addAll("Normal", "Urgence", "Question", "Rendez-vous");
        typeCombo.setValue("Normal");

        // Load doctors for combo
        loadDoctors();

        // Load conversations
        loadConversations();

        // Setup search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                loadConversations();
            } else {
                searchConversations(newVal);
            }
        });

        // Setup typing listener
        messageInput.textProperty().addListener((obs, oldV, newV) -> {
            if (currentConversation != null) {
                boolean isTyping = !newV.trim().isEmpty();
                if (currentConversation.isTypingPatient() != isTyping) {
                    currentConversation.setTypingPatient(isTyping);
                    conversationService.update(currentConversation);
                }
            }
        });

        // Start auto-refresh
        startAutoRefresh();

        // Load dashboard
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        if (statTotalConv == null || Session.getCurrentUser() == null) return;
        List<Conversation> convs = conversationService.getConversationsForPatient(Session.getCurrentUser().getId());
        statTotalConv.setText(String.valueOf(convs.size()));
        
        int unread = convs.stream().mapToInt(c -> c.getUnreadCountFor(Session.getCurrentUser())).sum();
        statUnread.setText(String.valueOf(unread));
    }

    private void loadDoctors() {
        List<User> doctors = userService.getAllDoctors();
        System.out.println("Nombre de médecins chargés : " + doctors.size());
        for (User u : doctors) {
            System.out.println("  - " + u.getUsername() + " (roles: " + u.getRoles() + ")");
        }
        doctorCombo.getItems().clear();
        doctorCombo.getItems().addAll(doctors);

        // Display username in combo
        doctorCombo.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getUsername());
                }
            }
        });
        doctorCombo.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText("Selectionner un docteur...");
                } else {
                    setText(user.getUsername());
                }
            }
        });
    }

    private void loadConversations() {
        conversationsList.getChildren().clear();
        if (Session.getCurrentUser() == null) return;
        List<Conversation> conversations = conversationService.getConversationsForPatient(
            Session.getCurrentUser().getId()
        );

        for (Conversation conv : conversations) {
            HBox convBox = createConversationBox(conv);
            conversationsList.getChildren().add(convBox);
        }
    }

    private void searchConversations(String keyword) {
        conversationsList.getChildren().clear();
        List<Conversation> conversations = conversationService.search(
            keyword, Session.getCurrentUser().getId()
        );

        for (Conversation conv : conversations) {
            HBox convBox = createConversationBox(conv);
            conversationsList.getChildren().add(convBox);
        }
    }

    private HBox createConversationBox(Conversation conv) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12));
        box.getStyleClass().add("conversation-box");
        
        if (currentConversation != null && currentConversation.getId() == conv.getId()) {
            box.getStyleClass().add("conversation-box-active");
        }
        
        if ("urgence".equalsIgnoreCase(conv.getType())) {
            box.setStyle("-fx-border-color: #ff4444; -fx-border-width: 0 0 0 4; -fx-background-color: #fff3f3;");
        }

        // Avatar
        StackPane avatar = new StackPane();
        Circle circle = new Circle(22, Color.web("#0d6efd"));
        Label letter = new Label(conv.getDoctor() != null ? conv.getDoctor().getAvatarLetter() : "D");
        letter.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: white;");
        avatar.getChildren().addAll(circle, letter);

        // Info
        VBox info = new VBox(4);
        info.setAlignment(Pos.CENTER_LEFT);

        String doctorName = conv.getDoctor() != null ? "Dr. " + conv.getDoctor().getUsername() : "Docteur";
        Label nameLabel = new Label(doctorName);
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Last message preview
        Message lastMsg = conversationService.getById(conv.getId()).getLastMessage();
        String preview = lastMsg != null ?
            (lastMsg.getContent().length() > 30 ? lastMsg.getContent().substring(0, 30) + "..." : lastMsg.getContent()) :
            "Aucun message";
        Label previewLabel = new Label(preview);
        previewLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #65676b;");

        info.getChildren().addAll(nameLabel, previewLabel);

        // Unread badge
        int unread = conv.getUnreadCountFor(Session.getCurrentUser());
        HBox rightBox = new HBox();
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        if (unread > 0) {
            StackPane badge = new StackPane();
            Circle badgeCircle = new Circle(10);
            badgeCircle.getStyleClass().add("unread-badge");
            Label badgeText = new Label(String.valueOf(unread));
            badgeText.getStyleClass().add("unread-badge-text");
            badgeText.setStyle("-fx-font-size: 10; -fx-text-fill: white; -fx-font-weight: bold;");
            badge.getChildren().addAll(badgeCircle, badgeText);
            rightBox.getChildren().add(badge);
        }

        // Date
        Label dateLabel = new Label(formatDate(conv.getUpdatedAt()));
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(avatar, info, spacer, rightBox, dateLabel);

        // Click handler
        box.setOnMouseClicked(e -> selectConversation(conv));

        // Hover effect managed by CSS


        return box;
    }

    private void selectConversation(Conversation conv) {
        currentConversation = conversationService.getById(conv.getId());

        // Show chat area
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        chatArea.setVisible(true);
        chatArea.setManaged(true);

        // Update chat header
        User doctor = currentConversation.getDoctor();
        if (doctor != null) {
            chatUserNameLabel.setText("Dr. " + doctor.getUsername());
            chatAvatarLabel.setText(doctor.getAvatarLetter());
        }
        chatStatusLabel.setText("Docteur - " + (currentConversation.getType() != null ? currentConversation.getType() : "Normal"));

        // Load messages
        loadMessages();

        // Mark as read
        messageService.markAsRead(currentConversation.getId(), Session.getCurrentUser().getId());

        // Refresh conversations list
        loadConversations();
        loadDashboardStats();
    }

    @FXML
    private void handleReturnHome() {
        currentConversation = null;
        chatArea.setVisible(false);
        chatArea.setManaged(false);
        emptyState.setVisible(true);
        emptyState.setManaged(true);
        loadDashboardStats();
    }

    @FXML
    private void handleNewConversation() {
        User selectedDoctor = doctorCombo.getValue();
        String type = typeCombo.getValue();
        String sujet = sujetField.getText().trim();
        String content = newMessageArea.getText().trim();

        // Validation
        if (selectedDoctor == null) {
            showAlert("Erreur", "Veuillez selectionner un docteur", Alert.AlertType.ERROR);
            return;
        }
        if (content.isEmpty()) {
            showAlert("Erreur", "Veuillez ecrire un message", Alert.AlertType.ERROR);
            return;
        }

        // Check if conversation already exists
        Conversation existing = conversationService.getConversationBetween(
            selectedDoctor.getId(), Session.getCurrentUser().getId()
        );

        if (existing != null) {
            // Add message to existing conversation
            Message msg = new Message();
            msg.setConversationId(existing.getId());
            msg.setSenderId(Session.getCurrentUser().getId());
            msg.setContent(content);
            msg.setMessageType("text");
            msg.setRead(false);
            messageService.add(msg);

            // Select conversation
            selectConversation(existing);
        } else {
            // Create new conversation
            Conversation conv = new Conversation();
            conv.setDoctorId(selectedDoctor.getId());
            conv.setUserId(Session.getCurrentUser().getId());
            conv.setType(type != null ? type.toLowerCase() : "normal");
            conv.setSujet(sujet.isEmpty() ? null : sujet);
            conv.setGroup(false);
            conversationService.add(conv);

            // Add first message
            Message msg = new Message();
            msg.setConversationId(conv.getId());
            msg.setSenderId(Session.getCurrentUser().getId());
            msg.setContent(content);
            msg.setMessageType("text");
            msg.setRead(false);
            messageService.add(msg);

            // Select conversation
            Conversation newConv = conversationService.getById(conv.getId());
            selectConversation(newConv);
        }

        // Clear fields
        newMessageArea.clear();
        sujetField.clear();
        doctorCombo.setValue(null);

        showAlert("Succes", "Message envoye avec succes!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleEmojiPicker(javafx.event.ActionEvent event) {
        Button source = (Button) event.getSource();
        ContextMenu emojiMenu = new ContextMenu();
        for(String emoji : new String[]{"👍", "❤️", "😂", "😮", "😢", "🙏", "🔥", "😊", "🎉"}) {
            MenuItem item = new MenuItem();
            String hex = getEmojiHex(emoji);
            if (hex != null) {
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(new javafx.scene.image.Image("https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72/" + hex + ".png", true));
                iv.setFitWidth(24); iv.setFitHeight(24);
                item.setGraphic(iv);
            } else {
                item.setText(emoji);
            }
            item.setOnAction(e -> messageInput.appendText(emoji));
            emojiMenu.getItems().add(item);
        }
        emojiMenu.show(source, javafx.geometry.Side.TOP, 0, 0);
    }
    
    private String getEmojiHex(String emoji) {
        switch(emoji) {
            case "👍": return "1f44d";
            case "❤️": return "2764";
            case "😂": return "1f602";
            case "😮": return "1f62e";
            case "😢": return "1f622";
            case "🙏": return "1f64f";
            case "🔥": return "1f525";
            case "😊": return "1f60a";
            case "🎉": return "1f389";
            default: return null;
        }
    }

    private javafx.scene.text.TextFlow buildTextFlowWithEmojis(String text, boolean isMine) {
        javafx.scene.text.TextFlow flow = new javafx.scene.text.TextFlow();
        flow.setMaxWidth(400); 
        String[] knownEmojis = {"👍", "❤️", "😂", "😮", "😢", "🙏", "🔥", "😊", "🎉"};
        String temp = text;
        
        String textColor = isMine ? "white" : "#050505";
        
        while(!temp.isEmpty()) {
            int earliestIdx = temp.length();
            String foundEmoji = null;
            
            for(String em : knownEmojis) {
                int idx = temp.indexOf(em);
                if(idx != -1 && idx < earliestIdx) {
                    earliestIdx = idx;
                    foundEmoji = em;
                }
            }
            
            if(foundEmoji != null) {
                if(earliestIdx > 0) {
                    javafx.scene.text.Text t = new javafx.scene.text.Text(temp.substring(0, earliestIdx));
                    t.setStyle("-fx-fill: " + textColor + "; -fx-font-size: 15;");
                    flow.getChildren().add(t);
                }
                String hex = getEmojiHex(foundEmoji);
                if(hex != null) {
                    try {
                        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(new javafx.scene.image.Image("https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72/" + hex + ".png", true));
                        iv.setFitWidth(20); iv.setFitHeight(20);
                        iv.setTranslateY(4); 
                        flow.getChildren().add(iv);
                    } catch(Exception e) {
                        javafx.scene.text.Text t = new javafx.scene.text.Text(foundEmoji);
                        t.setStyle("-fx-fill: " + textColor + "; -fx-font-size: 15;");
                        flow.getChildren().add(t);
                    }
                }
                temp = temp.substring(earliestIdx + foundEmoji.length());
            } else {
                javafx.scene.text.Text t = new javafx.scene.text.Text(temp);
                t.setStyle("-fx-fill: " + textColor + "; -fx-font-size: 15;");
                flow.getChildren().add(t);
                temp = "";
            }
        }
        return flow;
    }
    
    private void downloadFile(File sourceFile) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier");
        String initName = sourceFile.getName();
        if(initName.contains("_")) initName = initName.substring(initName.indexOf("_") + 1);
        fileChooser.setInitialFileName(initName);
        File dest = fileChooser.showSaveDialog(messageInput.getScene().getWindow());
        if (dest != null) {
            try {
                java.nio.file.Files.copy(sourceFile.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAttachment(javafx.event.ActionEvent event) {
        if (currentConversation == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une piece jointe (Image/PDF)");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images/PDF", "*.png", "*.jpg", "*.jpeg", "*.pdf")
        );
        File selectedFile = fileChooser.showOpenDialog(messageInput.getScene().getWindow());
        if (selectedFile != null) {
            try {
                File uploadsDir = new File(System.getProperty("user.dir") + "/src/main/resources/uploads");
                if (!uploadsDir.exists()) uploadsDir.mkdirs();
                String newFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File dest = new File(uploadsDir, newFileName);
                java.nio.file.Files.copy(selectedFile.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                Message msg = new Message();
                msg.setConversationId(currentConversation.getId());
                msg.setSenderId(Session.getCurrentUser().getId());
                msg.setContent("Piece jointe : " + selectedFile.getName());
                msg.setAttachment(newFileName);
                msg.setRead(false);
                msg.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                
                messageService.add(msg);
                loadMessages();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleVoiceRecord(javafx.event.ActionEvent event) {
        if (currentConversation == null) return;
        if (!audioRecorder.isRecording()) {
            tempAudioPath = System.getProperty("user.dir") + "/src/main/resources/uploads/voice_" + System.currentTimeMillis() + ".wav";
            audioRecorder.startRecording(tempAudioPath);
            recordBtn.setText("⏹");
        } else {
            audioRecorder.stopRecording();
            recordBtn.setText("🎤");
            standardInputBox.setVisible(false);
            standardInputBox.setManaged(false);
            voiceReviewBox.setVisible(true);
            voiceReviewBox.setManaged(true);
            playPreviewBtn.setOnAction(e -> previewAudio(tempAudioPath));
        }
    }
    
    private void previewAudio(String path) {
        if (path == null) return;
        if (previewPlayer != null && previewPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            previewPlayer.stop();
            return;
        }
        File f = new File(path);
        Media m = new Media(f.toURI().toString());
        previewPlayer = new MediaPlayer(m);
        previewPlayer.play();
    }

    @FXML
    private void handleCancelVoice(javafx.event.ActionEvent event) {
        if (previewPlayer != null) previewPlayer.stop();
        if (tempAudioPath != null) {
            new File(tempAudioPath).delete();
            tempAudioPath = null;
        }
        standardInputBox.setVisible(true);
        standardInputBox.setManaged(true);
        voiceReviewBox.setVisible(false);
        voiceReviewBox.setManaged(false);
    }

    @FXML
    private void handleSendVoice(javafx.event.ActionEvent event) {
        if (previewPlayer != null) previewPlayer.stop();
        if (tempAudioPath != null) {
            Message msg = new Message();
            msg.setConversationId(currentConversation.getId());
            msg.setSenderId(Session.getCurrentUser().getId());
            msg.setContent("Message vocal");
            File f = new File(tempAudioPath);
            msg.setAudioPath(f.getName());
            msg.setRead(false);
            msg.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            messageService.add(msg);
            tempAudioPath = null;
            loadMessages();
        }
        standardInputBox.setVisible(true);
        standardInputBox.setManaged(true);
        voiceReviewBox.setVisible(false);
        voiceReviewBox.setManaged(false);
    }

    private void loadMessages() {
        if (currentConversation == null || Session.getCurrentUser() == null) return;

        List<Message> messages = messageService.getByConversationId(currentConversation.getId());

        // Full redraw needed for reactions/edits even if size is same
        // But we can still optimize by checking if anything actually changed in the database result
        
        messagesContainer.getChildren().clear();

        for (Message msg : messages) {
            HBox msgBox = createMessageBox(msg);
            messagesContainer.getChildren().add(msgBox);
        }

        // Mark as read when displaying
        messageService.markAsRead(currentConversation.getId(), Session.getCurrentUser().getId());

        // Scroll to bottom
        Platform.runLater(() -> messagesScroll.setVvalue(1.0));
    }

    private HBox createMessageBox(Message msg) {
        if (Session.getCurrentUser() == null) return new HBox();
        boolean isMine = msg.getSenderId() == Session.getCurrentUser().getId();

        HBox box = new HBox();
        box.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        box.setMaxWidth(Double.MAX_VALUE);

        try {
            // Message bubble
            VBox bubble = new VBox(5);
            bubble.getStyleClass().add(isMine ? "message-bubble-mine" : "message-bubble-theirs");
            
            // Show quoted message if this is a reply - Enhanced visual style like Messenger/Instagram
            if (msg.getParentId() != null) {
                Message parentMsg = messageService.getById(msg.getParentId());
                if (parentMsg != null) {
                    // Container for quoted message with left border accent
                    HBox quotedContainer = new HBox(0);
                    quotedContainer.setStyle("-fx-background-color: rgba(0,0,0,0.08); -fx-background-radius: 8; -fx-padding: 8 12;");
                    quotedContainer.setMaxWidth(320);
                    
                    // Left accent bar (blue line)
                    javafx.scene.layout.Region accentBar = new javafx.scene.layout.Region();
                    accentBar.setStyle("-fx-background-color: #0084ff; -fx-background-radius: 2;");
                    accentBar.setPrefWidth(4);
                    accentBar.setMinWidth(4);
                    accentBar.setMaxWidth(4);
                    HBox.setMargin(accentBar, new Insets(0, 8, 0, 0));
                    
                    // Content box
                    VBox quotedContentBox = new VBox(3);
                    quotedContentBox.setAlignment(Pos.CENTER_LEFT);
                    
                    // Sender name with icon
                    String senderName = parentMsg.getSender() != null ? parentMsg.getSender().getUsername() : "Utilisateur";
                    Label quotedSender = new Label("↩ " + senderName);
                    quotedSender.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #0084ff;");
                    
                    // Quoted text with better truncation
                    String quotedContent = parentMsg.getContent();
                    if (quotedContent.length() > 45) {
                        quotedContent = quotedContent.substring(0, 45) + "...";
                    }
                    Label quotedText = new Label(quotedContent);
                    quotedText.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");
                    quotedText.setWrapText(true);
                    quotedText.setMaxWidth(280);
                    
                    quotedContentBox.getChildren().addAll(quotedSender, quotedText);
                    quotedContainer.getChildren().addAll(accentBar, quotedContentBox);
                    
                    // Add spacing before the quoted message
                    VBox.setMargin(quotedContainer, new Insets(0, 0, 4, 0));
                    bubble.getChildren().add(0, quotedContainer); // Add at the beginning
                }
            }

        // Content
        javafx.scene.Node contentNode;
        if (msg.getContent() != null && !msg.getContent().isEmpty()) {
            contentNode = buildTextFlowWithEmojis(msg.getContent(), isMine);
            contentNode.getStyleClass().add(isMine ? "message-text-mine" : "message-text-theirs");
        } else {
            Label placeholder = new Label(msg.getContent());
            placeholder.getStyleClass().add(isMine ? "message-text-mine" : "message-text-theirs");
            contentNode = placeholder;
        }

        // Time
        Label timeLabel = new Label(msg.getFormattedTime());
        timeLabel.getStyleClass().add(isMine ? "message-time-mine" : "message-time-theirs");

        bubble.getChildren().addAll(contentNode, timeLabel);
        
        // Attachments
        if (msg.getAttachment() != null && !msg.getAttachment().isEmpty()) {
            File attachFile = new File(System.getProperty("user.dir") + "/src/main/resources/uploads/" + msg.getAttachment());
            if (attachFile.exists()) {
                if (msg.getAttachment().toLowerCase().endsWith(".pdf")) {
                    Button pdfBtn = new Button("📄 " + msg.getAttachment() + " (Ouvrir)");
                    pdfBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                    
                    Button dlBtn = new Button("⬇");
                    dlBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 2 6;");
                    dlBtn.setOnAction(e -> downloadFile(attachFile));
                    
                    HBox pdfBox = new HBox(10, pdfBtn, dlBtn);
                    pdfBox.setAlignment(Pos.CENTER_LEFT);
                    
                    bubble.getChildren().add(pdfBox);
                } else {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(attachFile.toURI().toString());
                    javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                    imgView.setFitWidth(200);
                    imgView.setPreserveRatio(true);
                    
                    Button dlBtn = new Button("⬇ Telecharger Image");
                    dlBtn.setStyle("-fx-background-color: rgba(255,255,255,0.8); -fx-text-fill: #333; -fx-cursor: hand; -fx-background-radius: 5;");
                    dlBtn.setOnAction(e -> downloadFile(attachFile));
                    
                    VBox imgBox = new VBox(5, imgView, dlBtn);
                    imgBox.setAlignment(Pos.CENTER);
                    
                    bubble.getChildren().add(imgBox);
                }
            }
        }
        
        // Voice
        if (msg.getAudioPath() != null && !msg.getAudioPath().isEmpty()) {
            File audioFile = new File(System.getProperty("user.dir") + "/src/main/resources/uploads/" + msg.getAudioPath());
            if (audioFile.exists()) {
                HBox audioBox = new HBox(10);
                audioBox.setAlignment(Pos.CENTER_LEFT);
                audioBox.setPadding(new Insets(5));
                Button playBtn = new Button("▶");
                playBtn.setStyle("-fx-background-color: white; -fx-text-fill: " + (isMine ? "#0084ff" : "#333") + "; -fx-background-radius: 50; -fx-cursor: hand;");
                Slider slider = new Slider();
                slider.setMinWidth(120);
                Label tLabel = new Label("00:00");
                tLabel.setStyle("-fx-text-fill: " + (isMine ? "white" : "#666") + "; -fx-font-size: 10;");
                try {
                    Media m = new Media(audioFile.toURI().toString());
                    MediaPlayer mp = new MediaPlayer(m);
                    playBtn.setOnAction(e -> {
                        if (mp.getStatus() == MediaPlayer.Status.PLAYING) {
                            mp.pause();
                            playBtn.setText("▶");
                        } else {
                            mp.play();
                            playBtn.setText("⏸");
                        }
                    });
                    mp.currentTimeProperty().addListener((obs, oldV, newV) -> {
                        if(!slider.isValueChanging()) {
                            slider.setValue(newV.toSeconds() / m.getDuration().toSeconds() * 100.0);
                        }
                        int secs = (int) newV.toSeconds();
                        tLabel.setText(String.format("%02d:%02d", secs / 60, secs % 60));
                    });
                    slider.valueProperty().addListener((obs, oldV, newV) -> {
                        if (slider.isValueChanging()) {
                            mp.seek(m.getDuration().multiply(newV.doubleValue() / 100.0));
                        }
                    });
                    mp.setOnEndOfMedia(() -> {
                        mp.stop();
                        playBtn.setText("▶");
                        slider.setValue(0);
                        tLabel.setText("00:00");
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                audioBox.getChildren().addAll(playBtn, slider, tLabel);
                bubble.getChildren().add(audioBox);
            }
        }
        
        // Add Reply menu item for ALL messages (not just mine)
        ContextMenu replyContextMenu = new ContextMenu();
        MenuItem replyItem = new MenuItem("↩ Repondre");
        replyItem.setOnAction(e -> handleReplyToMessage(msg));
        replyContextMenu.getItems().add(replyItem);
        bubble.setOnContextMenuRequested(e -> replyContextMenu.show(bubble, e.getScreenX(), e.getScreenY()));
        
        // Reply button (visible next to reaction button)
        Button replyBtn = new Button("↩");
        replyBtn.setStyle("-fx-background-color: #e4e6eb; -fx-background-radius: 50%; -fx-cursor: hand; -fx-font-size: 12; -fx-text-fill: #666; -fx-min-width: 24; -fx-min-height: 24;");
        replyBtn.setOnAction(e -> handleReplyToMessage(msg));

        // Double-click for heart reaction (like Instagram)
        bubble.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                // Add heart reaction on double click
                String reactorName = Session.getCurrentUser().getUsername();
                messageService.toggleReaction(msg.getId(), reactorName, "❤️");
                loadMessages();
            } else if (e.getClickCount() == 1 && msg.getOriginalContent() != null && !msg.getOriginalContent().isEmpty()) {
                // Single click with original content - show edit history
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Historique de modification");
                alert.setHeaderText("Message original :");
                alert.setContentText(msg.getOriginalContent());
                alert.show();
            }
        });

        if (isMine) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem editItem = new MenuItem("✏ Modifier");
            editItem.setOnAction(e -> handleEditMessage(msg));
            MenuItem deleteItem = new MenuItem("🗑 Supprimer");
            deleteItem.setOnAction(e -> handleDeleteMessage(msg));
            contextMenu.getItems().addAll(editItem, deleteItem);
            contextMenu.setStyle("-fx-font-size: 12;");
            
            HBox actionBox = new HBox(5);
            actionBox.setAlignment(Pos.CENTER_RIGHT);
            
            String status = msg.isRead() ? "✓✓ Lu" : "✓ Envoye";
            Label statusLabel = new Label(status);
            statusLabel.setStyle("-fx-font-size: 10; -fx-text-fill: rgba(255,255,255,0.7);");
            
            // Smaller options button (three dots)
            Button optionsBtn = new Button("⋯");
            optionsBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.8); -fx-padding: 0 3; -fx-min-width: 20; -fx-min-height: 20;");
            optionsBtn.setOnAction(e -> contextMenu.show(optionsBtn, javafx.geometry.Side.BOTTOM, 0, 0));
            
            actionBox.getChildren().addAll(statusLabel, optionsBtn);
            bubble.getChildren().add(actionBox);
        }

        StackPane bubbleStack = new StackPane(bubble);
        
        // Reactions section - simplified and stable
        if (msg.getReactions() != null && !msg.getReactions().isEmpty()) {
            String raw = msg.getReactions();
            String[] reactionsArr = raw.split(",");
            HBox reactionsBox = new HBox(4);
            reactionsBox.setAlignment(Pos.CENTER_LEFT);
            reactionsBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 4 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 4, 0, 0, 2);");
            
            for (String rea : reactionsArr) {
                if (!rea.isEmpty() && rea.contains(":")) {
                    String[] parts = rea.split(":");
                    if (parts.length < 2) continue;
                    String name = parts[0];
                    String em = parts[1];
                    
                    HBox badge = new HBox(2);
                    badge.setAlignment(Pos.CENTER_LEFT);
                    badge.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 10; -fx-padding: 2 6;");
                    
                    // Use text emoji with colored font - better emoji support
                    Label emojiLabel = new Label(em);
                    emojiLabel.setStyle("-fx-font-size: 14; -fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif;");
                    badge.getChildren().add(emojiLabel);
                    
                    if (!name.isEmpty()) {
                        Label nameLbl = new Label(name);
                        nameLbl.setStyle("-fx-font-size: 9; -fx-text-fill: #444; -fx-font-weight: bold;");
                        badge.getChildren().add(nameLbl);
                    }
                    
                    reactionsBox.getChildren().add(badge);
                }
            }
            
            if (!reactionsBox.getChildren().isEmpty()) {
                // Add reactions below the bubble, not overlayed
                VBox wrapper = new VBox(2);
                wrapper.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                wrapper.getChildren().addAll(bubble, reactionsBox);
                VBox.setMargin(reactionsBox, new Insets(2, 0, 0, isMine ? 20 : 10));
                bubbleStack = new StackPane(wrapper);
            }
        }
        
        Button reactBtn = new Button("+");
        reactBtn.setStyle("-fx-background-color: #e4e6eb; -fx-background-radius: 50%; -fx-cursor: hand; -fx-font-size: 12; -fx-text-fill: #666; -fx-min-width: 24; -fx-min-height: 24;");
        ContextMenu reactMenu = new ContextMenu();
        reactMenu.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 2);");
        
        for (String emoji : new String[]{"👍", "❤️", "😂", "😮", "😢"}) {
            MenuItem item = new MenuItem(emoji);
            item.setStyle("-fx-font-size: 20; -fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif;");
            item.setOnAction(e -> {
                String reactorName = Session.getCurrentUser().getUsername();
                messageService.toggleReaction(msg.getId(), reactorName, emoji);
                loadMessages();
            });
            reactMenu.getItems().add(item);
        }
        reactBtn.setOnAction(e -> reactMenu.show(reactBtn, javafx.geometry.Side.BOTTOM, 0, 0));

        HBox finalBox = new HBox(8);
        finalBox.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        finalBox.setMaxWidth(Double.MAX_VALUE);
        
        if (isMine) {
            // For my messages: [replyBtn] [reactBtn] [bubble]
            finalBox.getChildren().addAll(replyBtn, reactBtn, bubbleStack);
            HBox.setMargin(finalBox, new Insets(5, 15, 10, 50));
        } else {
            // For their messages: [bubble] [reactBtn] [replyBtn]
            finalBox.getChildren().addAll(bubbleStack, reactBtn, replyBtn);
            HBox.setMargin(finalBox, new Insets(5, 50, 10, 15));
        }
        
        box.getChildren().add(finalBox);
        HBox.setHgrow(finalBox, Priority.ALWAYS);

        } catch (Exception e) {
            e.printStackTrace();
            box.getChildren().add(new Label("Message: " + msg.getContent()));
        }

        return box;
    }

    @FXML
    private void handleSendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || currentConversation == null) {
            // Show alert for empty message
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Message vide");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez ecrire un message avant d'envoyer.");
            alert.showAndWait();
            return;
        }

        Message msg = new Message();
        msg.setConversationId(currentConversation.getId());
        msg.setSenderId(Session.getCurrentUser().getId());
        msg.setContent(content);
        msg.setMessageType("text");
        msg.setRead(false);
        msg.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // If replying to a message, set the parent_id
        if (replyingToMessage != null) {
            msg.setParentId(replyingToMessage.getId());
        }

        messageService.add(msg);

        messageInput.clear();
        handleCancelReply(); // Clear reply state
        loadMessages();
        loadConversations();
    }
    
    @FXML
    private void handleReplyToMessage(Message msg) {
        replyingToMessage = msg;
        String preview = msg.getContent();
        if (preview.length() > 60) {
            preview = preview.substring(0, 60) + "...";
        }
        replyPreviewText.setText(preview);
        replyPreviewBox.setVisible(true);
        replyPreviewBox.setManaged(true);
        messageInput.requestFocus();
    }
    
    @FXML
    private void handleCancelReply() {
        replyingToMessage = null;
        replyPreviewBox.setVisible(false);
        replyPreviewBox.setManaged(false);
    }

    private void handleEditMessage(Message msg) {
        TextInputDialog dialog = new TextInputDialog(msg.getContent());
        dialog.setTitle("Modifier le message");
        dialog.setHeaderText("Modifiez votre message:");
        dialog.setContentText("Message:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newContent = result.get().trim();
            if (newContent.isEmpty()) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("Attention");
                warn.setHeaderText("Message vide");
                warn.setContentText("Le contenu du message ne peut pas etre vide.");
                warn.showAndWait();
                return;
            }
            msg.setOriginalContent(msg.getContent());
            msg.setContent(newContent);
            messageService.update(msg);
            loadMessages();
        }
    }

    private void handleDeleteMessage(Message msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer le message?");
        alert.setContentText("Cette action est irreversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            messageService.delete(msg.getId());
            loadMessages();
            loadConversations();
        }
    }

    @FXML
    private void handleDeleteConversation() {
        if (currentConversation == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer la conversation?");
        alert.setContentText("Tous les messages seront supprimes. Cette action est irreversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            conversationService.delete(currentConversation.getId());
            currentConversation = null;
            chatArea.setVisible(false);
            emptyState.setVisible(true);
            loadConversations();
        }
    }

    @FXML
    private void handleLogout() {
        Session.clear();
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        navigateToLogin();
    }

    @FXML
    public void goToEditProfile(javafx.event.ActionEvent event) {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_profile_user.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1400, 850);
            if (getClass().getResource("/css/style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            }

            Stage stage = null;
            if (searchField != null && searchField.getScene() != null && searchField.getScene().getWindow() != null) {
                stage = (Stage) searchField.getScene().getWindow();
            } else {
                for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                    if (window instanceof Stage && window.isShowing()) {
                        stage = (Stage) window;
                        break;
                    }
                }
            }

            if (stage != null) {
                stage.setTitle("Mon Profil");
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1400, 850);
            if (getClass().getResource("/css/style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            }

            // Recuperer le Stage existant (ne jamais creer de nouvelle fenetre)
            Stage stage = null;
            if (searchField != null && searchField.getScene() != null && searchField.getScene().getWindow() != null) {
                stage = (Stage) searchField.getScene().getWindow();
            } else {
                // Chercher le Stage actuel parmi les fenetres ouvertes
                for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                    if (window instanceof Stage && window.isShowing()) {
                        stage = (Stage) window;
                        break;
                    }
                }
            }

            if (stage == null) {
                System.err.println("Aucun Stage trouvé pour la navigation");
                return;
            }

            stage.setTitle("Connexion - WellBalance");
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void startAutoRefresh() {
        refreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                if (Session.getCurrentUser() == null) {
                    if (refreshTimeline != null) refreshTimeline.stop();
                    return;
                }
                if (currentConversation != null) {
                    currentConversation = conversationService.getById(currentConversation.getId());
                    if (currentConversation != null) {
                        typingLabel.setVisible(currentConversation.isTypingDoctor());
                        typingLabel.setManaged(currentConversation.isTypingDoctor());
                    }
                    loadMessages();
                } else {
                    loadDashboardStats();
                }
                loadConversations();
            })
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM HH:mm");
        return sdf.format(timestamp);
    }
}
