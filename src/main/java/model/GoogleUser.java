package model;

public class GoogleUser {
    private String email;
    private String name;

    public GoogleUser(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}