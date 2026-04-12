package model;

public class User {

    private int id;
    private String email;
    private String roles;
    private String password;
    private String username;
    private String telephone;
    private int isBanned;

    public User() {
    }

    public User(int id, String email, String roles, String password, String username, String telephone, int isBanned) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.username = username;
        this.telephone = telephone;
        this.isBanned = isBanned;
    }

    public User(String email, String password, String username, String telephone) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.telephone = telephone;
    }

    public User(String email, String roles, String password, String username, String telephone, int isBanned) {
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.username = username;
        this.telephone = telephone;
        this.isBanned = isBanned;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int getIsBanned() {
        return isBanned;
    }

    public void setIsBanned(int isBanned) {
        this.isBanned = isBanned;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", roles='" + roles + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", telephone='" + telephone + '\'' +
                ", isBanned=" + isBanned +
                '}';
    }
}