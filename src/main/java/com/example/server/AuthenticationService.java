package com.example.server;

import java.sql.SQLException;

public interface AuthenticationService {
    String getUsernameByLoginAndPassoword(String login, String password) throws SQLException;
    void startAuth();
    void endAuth() throws SQLException;
    void updateUsername(String login, String username) throws SQLException;
    void registration(String login, String username, String password) throws SQLException;
    boolean isFreeLogin(String login) throws SQLException;
}
