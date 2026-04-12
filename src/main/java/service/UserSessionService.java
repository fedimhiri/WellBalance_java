package service;

import model.User;
import model.UserSession;
import utils.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserSessionService {

    private final Connection cnx;

    public UserSessionService() {
        cnx = DataBaseConnection.getConnection();
    }

    public String createSession(User user) throws SQLException {
        String token = UUID.randomUUID().toString();

        String sql = "INSERT INTO user_sessions " +
                "(user_id, username, role, login_time, session_status, session_token, ip_address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, user.getId());
        ps.setString(2, user.getUsername());
        ps.setString(3, user.getRoles());
        ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
        ps.setString(5, "ACTIVE");
        ps.setString(6, token);
        ps.setString(7, "LOCAL");

        ps.executeUpdate();
        return token;
    }

    public void closeSession(String sessionToken) throws SQLException {
        String sql = "UPDATE user_sessions " +
                "SET logout_time = ?, session_status = ? " +
                "WHERE session_token = ? AND session_status = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        ps.setString(2, "CLOSED");
        ps.setString(3, sessionToken);
        ps.setString(4, "ACTIVE");

        ps.executeUpdate();
    }

    public List<UserSession> getAllSessions() throws SQLException {
        List<UserSession> list = new ArrayList<>();

        String sql = "SELECT * FROM user_sessions ORDER BY login_time DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            UserSession s = new UserSession();

            s.setId(rs.getInt("id"));
            s.setUserId(rs.getInt("user_id"));
            s.setUsername(rs.getString("username"));
            s.setRole(rs.getString("role"));
            s.setLoginTime(rs.getTimestamp("login_time"));
            s.setLogoutTime(rs.getTimestamp("logout_time"));
            s.setSessionStatus(rs.getString("session_status"));
            s.setSessionToken(rs.getString("session_token"));
            s.setIpAddress(rs.getString("ip_address"));

            list.add(s);
        }

        return list;
    }

    public void supprimerSession(int id) throws SQLException {
        String sql = "DELETE FROM user_sessions WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}