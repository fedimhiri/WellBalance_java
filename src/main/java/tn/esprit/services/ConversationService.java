package tn.esprit.services;

import tn.esprit.models.Conversation;
import tn.esprit.models.User;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationService {

    private Connection cnx = MyConnection.getInstance().getCnx();
    private UserService userService = new UserService();
    private MessageService messageService = new MessageService();

    // CREATE - Add new conversation
    public void add(Conversation conversation) {
        String req = "INSERT INTO conversation (doctor_id, user_id, type, sujet, is_group) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, conversation.getDoctorId(), Types.INTEGER);
            ps.setObject(2, conversation.getUserId(), Types.INTEGER);
            ps.setString(3, conversation.getType() != null ? conversation.getType() : "normal");
            ps.setString(4, conversation.getSujet());
            ps.setBoolean(5, conversation.isGroup());

            ps.executeUpdate();

            // Get generated ID
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                conversation.setId(rs.getInt(1));
            }

            System.out.println("Conversation creee avec succes! ID: " + conversation.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ - Get all conversations
    public List<Conversation> getAll() {
        List<Conversation> conversations = new ArrayList<>();
        String req = "SELECT * FROM conversation ORDER BY updated_at DESC";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                conversations.add(mapResultSetToConversation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    // READ - Get conversation by ID
    public Conversation getById(int id) {
        String req = "SELECT * FROM conversation WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Conversation conv = mapResultSetToConversation(rs);
                // Load messages
                conv.setMessages(messageService.getByConversationId(id));
                return conv;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // READ - Get conversations for a doctor
    public List<Conversation> getConversationsForDoctor(int doctorId) {
        List<Conversation> conversations = new ArrayList<>();
        String req = "SELECT * FROM conversation WHERE doctor_id = ? OR is_group = 1 ORDER BY updated_at DESC";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                conversations.add(mapResultSetToConversation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    // READ - Get conversations for a patient
    public List<Conversation> getConversationsForPatient(int patientId) {
        List<Conversation> conversations = new ArrayList<>();
        String req = "SELECT * FROM conversation WHERE user_id = ? OR is_group = 1 ORDER BY updated_at DESC";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                conversations.add(mapResultSetToConversation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    // READ - Get conversation between doctor and patient
    public Conversation getConversationBetween(int doctorId, int patientId) {
        String req = "SELECT * FROM conversation WHERE doctor_id = ? AND user_id = ? AND is_group = 0";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, doctorId);
            ps.setInt(2, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToConversation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE - Update conversation
    public void update(Conversation conversation) {
        String req = "UPDATE conversation SET type = ?, sujet = ?, is_typing_patient = ?, is_typing_doctor = ? WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, conversation.getType());
            ps.setString(2, conversation.getSujet());
            ps.setBoolean(3, conversation.isTypingPatient());
            ps.setBoolean(4, conversation.isTypingDoctor());
            ps.setInt(5, conversation.getId());
            ps.executeUpdate();
            System.out.println("Conversation mise a jour avec succes!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // UPDATE - Update timestamp
    public void updateTimestamp(int conversationId) {
        String req = "UPDATE conversation SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, conversationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE - Delete conversation
    public void delete(int id) {
        // First delete messages
        messageService.deleteByConversationId(id);

        String req = "DELETE FROM conversation WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Conversation supprimee avec succes!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // SEARCH - Search conversations
    public List<Conversation> search(String keyword, int userId) {
        List<Conversation> conversations = new ArrayList<>();
        String req = "SELECT c.* FROM conversation c " +
                     "LEFT JOIN message m ON c.id = m.conversation_id " +
                     "WHERE (c.doctor_id = ? OR c.user_id = ?) " +
                     "AND (m.content LIKE ? OR c.sujet LIKE ?) " +
                     "GROUP BY c.id ORDER BY c.updated_at DESC";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setString(3, "%" + keyword + "%");
            ps.setString(4, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                conversations.add(mapResultSetToConversation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    // Helper method to map ResultSet to Conversation
    private Conversation mapResultSetToConversation(ResultSet rs) throws SQLException {
        Conversation conv = new Conversation();
        conv.setId(rs.getInt("id"));
        conv.setDoctorId(rs.getObject("doctor_id") != null ? rs.getInt("doctor_id") : null);
        conv.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
        conv.setCreatedAt(rs.getTimestamp("created_at"));
        conv.setUpdatedAt(rs.getTimestamp("updated_at"));
        conv.setType(rs.getString("type"));
        conv.setSujet(rs.getString("sujet"));
        conv.setTypingPatient(rs.getBoolean("is_typing_patient"));
        conv.setTypingDoctor(rs.getBoolean("is_typing_doctor"));
        conv.setGroup(rs.getBoolean("is_group"));

        // Load associated users
        if (conv.getDoctorId() != null) {
            conv.setDoctor(userService.getById(conv.getDoctorId()));
        }
        if (conv.getUserId() != null) {
            conv.setUser(userService.getById(conv.getUserId()));
        }

        return conv;
    }
}
