package com.example.server;

import java.sql.*;

public class AuthenticationDataBase  implements AuthenticationService {
    /* users:
    * martin 1111
    * gena 2222
    * batman 3333
    * timofei 1q2w3e
    * denis 5555 */

    private Connection connection = null;
    private Statement stmt = null;

    @Override
    public String getUsernameByLoginAndPassoword(String login, String password) throws SQLException {
       startAuth();

       ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM auth WHERE login = '%s'" , login));

       if (rs.isClosed()) {
           return null;
       }

       String username = rs.getString("username");
       String passwordDB = rs.getString("password");

       return ((passwordDB != null) && (passwordDB.equals(password))) ? username : null;
    }

    @Override
    public void startAuth() {
        System.out.println("Cтарт аутентификации");
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/db/mainDB.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endAuth() throws SQLException {
        System.out.println("Конец аутентификации");
        connection.close();
    }

    @Override
    public void updateUsername (String login, String username) throws SQLException {
        stmt.executeUpdate(String.format("UPDATE auth SET username = '%s' WHERE login = '%s' ", username, login));
    }

    @Override
    public void registration(String login, String username, String password) throws SQLException {
        stmt.execute(String.format("INSERT INTO auth (login, password, username) VALUES ('%s', '%s', '%s')", login, password, username));
    }

    @Override
    public boolean isFreeLogin(String login) throws SQLException {

        ResultSet rs = stmt.executeQuery("SELECT login FROM auth");

        String logins = rs.getString("login");
        String[] parts = logins.split("\\s+");


        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals(login)) {
                return false;
            }
        }
        return true;
    }

}
