package tn.esprit.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private int id;
    private Integer doctorId;
    private Integer userId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String type;
    private String sujet;
    private boolean isTypingPatient;
    private boolean isTypingDoctor;
    private boolean isGroup;

    // Navigation properties
    private User doctor;
    private User user;
    private List<Message> messages;
    private List<User> members;

    public Conversation() {
        this.messages = new ArrayList<>();
        this.members = new ArrayList<>();
    }

    public Conversation(int id, Integer doctorId, Integer userId, Timestamp createdAt, 
                        Timestamp updatedAt, String type, String sujet, 
                        boolean isTypingPatient, boolean isTypingDoctor, boolean isGroup) {
        this();
        this.id = id;
        this.doctorId = doctorId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.type = type;
        this.sujet = sujet;
        this.isTypingPatient = isTypingPatient;
        this.isTypingDoctor = isTypingDoctor;
        this.isGroup = isGroup;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public boolean isTypingPatient() {
        return isTypingPatient;
    }

    public void setTypingPatient(boolean typingPatient) {
        isTypingPatient = typingPatient;
    }

    public boolean isTypingDoctor() {
        return isTypingDoctor;
    }

    public void setTypingDoctor(boolean typingDoctor) {
        isTypingDoctor = typingDoctor;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public User getDoctor() {
        return doctor;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    // Helper methods
    public Message getLastMessage() {
        if (messages != null && !messages.isEmpty()) {
            return messages.get(messages.size() - 1);
        }
        return null;
    }

    public int getUnreadCountFor(User viewer) {
        if (messages == null) return 0;
        int count = 0;
        for (Message msg : messages) {
            if (!msg.isRead() && msg.getSenderId() != viewer.getId()) {
                count++;
            }
        }
        return count;
    }

    public String getOtherUserName(int currentUserId) {
        if (userId != null && doctorId != null) {
            if (currentUserId == userId && doctor != null) {
                return doctor.getUsername();
            } else if (currentUserId == doctorId && user != null) {
                return user.getUsername();
            }
        }
        return "Utilisateur";
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", doctorId=" + doctorId +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", sujet='" + sujet + '\'' +
                '}';
    }
}
