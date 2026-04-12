package service;

import model.Message;
import utils.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageService {

    private Connection cnx = DataBaseConnection.getConnection();
    private UserService userService = new UserService();

    // CREATE - Add new message
    public void add(Message message) {
        String req = "INSERT INTO message (conversation_id, sender_id, content, attachment, is_read, message_type, audio_path, parent_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, message.getConversationId());
            ps.setInt(2, message.getSenderId());
            ps.setString(3, message.getContent());
            ps.setString(4, message.getAttachment());
            ps.setBoolean(5, message.isRead());
            ps.setString(6, message.getMessageType() != null ? message.getMessageType() : "text");
            ps.setString(7, message.getAudioPath());
            
            // Set parent_id for reply messages
            if (message.getParentId() != null) {
                ps.setInt(8, message.getParentId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }

            ps.executeUpdate();

            // Get generated ID
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                message.setId(rs.getInt(1));
            }

            // Update conversation timestamp
            updateConversationTimestamp(message.getConversationId());

            System.out.println("Message envoye avec succes! ID: " + message.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ - Get all messages
    public List<Message> getAll() {
        List<Message> messages = new ArrayList<>();
        String req = "SELECT * FROM message ORDER BY created_at ASC";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // READ - Get messages by conversation ID
    public List<Message> getByConversationId(int conversationId) {
        List<Message> messages = new ArrayList<>();
        String req = "SELECT * FROM message WHERE conversation_id = ? ORDER BY created_at ASC";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, conversationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // READ - Get message by ID
    public Message getById(int id) {
        String req = "SELECT * FROM message WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToMessage(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE - Update message
    public void update(Message message) {
        String req = "UPDATE message SET content = ?, original_content = ?, is_read = ? WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, message.getContent());
            ps.setString(2, message.getOriginalContent());
            ps.setBoolean(3, message.isRead());
            ps.setInt(4, message.getId());
            ps.executeUpdate();
            System.out.println("Message modifie avec succes!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // UPDATE - Mark messages as read
    public void markAsRead(int conversationId, int userId) {
        String req = "UPDATE message SET is_read = 1 WHERE conversation_id = ? AND sender_id != ? AND is_read = 0";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, conversationId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // UPDATE - Add or toggle reaction to message
    public void toggleReaction(int messageId, String reactorName, String reaction) {
        // Fetch current reactions
        String currentReactions = "";
        String getReq = "SELECT reactions FROM message WHERE id = ?";
        try {
            PreparedStatement getPs = cnx.prepareStatement(getReq);
            getPs.setInt(1, messageId);
            ResultSet rs = getPs.executeQuery();
            if (rs.next()) {
                currentReactions = rs.getString("reactions");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (currentReactions == null) currentReactions = "";
        
        // Parse and update - toggle behavior
        String[] parts = currentReactions.split(",");
        StringBuilder newReactions = new StringBuilder();
        boolean foundSame = false;
        
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (p.startsWith(reactorName + ":")) {
                String existingEmoji = p.substring(p.indexOf(":") + 1);
                if (existingEmoji.equals(reaction)) {
                    // Same emoji clicked - remove reaction (toggle off)
                    foundSame = true;
                    // Skip adding this reaction (effectively removing it)
                } else {
                    // Different emoji - update to new one
                    newReactions.append(reactorName).append(":").append(reaction).append(",");
                }
            } else {
                newReactions.append(p).append(",");
            }
        }
        
        // If not found and not removing, add new reaction
        if (!foundSame && !currentReactions.contains(reactorName + ":")) {
            newReactions.append(reactorName).append(":").append(reaction).append(",");
        }

        String upReq = "UPDATE message SET reactions = ? WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(upReq);
            ps.setString(1, newReactions.toString());
            ps.setInt(2, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Legacy method for compatibility
    public void addReaction(int messageId, String reactorName, String reaction) {
        toggleReaction(messageId, reactorName, reaction);
    }

    // DELETE - Delete message
    public void delete(int id) {
        String req = "DELETE FROM message WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Message supprime avec succes!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE - Delete messages by conversation ID
    public void deleteByConversationId(int conversationId) {
        String req = "DELETE FROM message WHERE conversation_id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, conversationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get unread count for user in conversation
    public int getUnreadCount(int conversationId, int userId) {
        String req = "SELECT COUNT(*) FROM message WHERE conversation_id = ? AND sender_id != ? AND is_read = 0";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, conversationId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get last message for conversation
    public Message getLastMessage(int conversationId) {
        String req = "SELECT * FROM message WHERE conversation_id = ? ORDER BY created_at DESC LIMIT 1";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, conversationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToMessage(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper method to update conversation timestamp
    private void updateConversationTimestamp(int conversationId) {
        String req = "UPDATE conversation SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, conversationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to map ResultSet to Message
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message msg = new Message();
        msg.setId(rs.getInt("id"));
        msg.setConversationId(rs.getInt("conversation_id"));
        msg.setSenderId(rs.getInt("sender_id"));
        msg.setContent(rs.getString("content"));
        msg.setAttachment(rs.getString("attachment"));
        msg.setCreatedAt(rs.getTimestamp("created_at"));
        msg.setRead(rs.getBoolean("is_read"));
        msg.setAiAnalysis(rs.getString("ai_analysis"));
        msg.setParentId(rs.getObject("parent_id") != null ? rs.getInt("parent_id") : null);
        msg.setMessageType(rs.getString("message_type"));
        msg.setAudioPath(rs.getString("audio_path"));
        msg.setReactions(rs.getString("reactions"));
        msg.setOriginalContent(rs.getString("original_content"));

        // Load sender
        msg.setSender(userService.getById(msg.getSenderId()));

        return msg;
    }
}
