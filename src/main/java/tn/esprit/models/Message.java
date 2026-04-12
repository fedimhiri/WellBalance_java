package tn.esprit.models;

import java.sql.Timestamp;

public class Message {
    private int id;
    private int conversationId;
    private int senderId;
    private String content;
    private String attachment;
    private Timestamp createdAt;
    private boolean isRead;
    private String aiAnalysis;
    private Integer parentId;
    private String messageType;
    private String audioPath;
    private String reactions;
    private String originalContent;

    // Navigation properties
    private User sender;
    private Conversation conversation;

    public Message() {}

    public Message(int id, int conversationId, int senderId, String content, String attachment,
                   Timestamp createdAt, boolean isRead, String aiAnalysis, Integer parentId,
                   String messageType, String audioPath, String reactions, String originalContent) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.attachment = attachment;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.aiAnalysis = aiAnalysis;
        this.parentId = parentId;
        this.messageType = messageType;
        this.audioPath = audioPath;
        this.reactions = reactions;
        this.originalContent = originalContent;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getAiAnalysis() {
        return aiAnalysis;
    }

    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getReactions() {
        return reactions;
    }

    public void setReactions(String reactions) {
        this.reactions = reactions;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    // Helper method to check if message is from a specific user
    public boolean isFromUser(int userId) {
        return this.senderId == userId;
    }

    // Helper method to format time
    public String getFormattedTime() {
        if (createdAt != null) {
            return new java.text.SimpleDateFormat("HH:mm").format(createdAt);
        }
        return "";
    }

    // Helper method to format date
    public String getFormattedDate() {
        if (createdAt != null) {
            return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(createdAt);
        }
        return "";
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", senderId=" + senderId +
                ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 20)) + "..." : "null") + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
