package com.example.server;

import java.util.List;

public class BaseAuthentication implements AuthenticationService {

    private static final List<User> clients = List.of(
            new User("user1", "1111", "Тимофей"),
            new User("user2", "2222", "Дмитрий"),
            new User("user3", "3333", "Диана"),
            new User("user4", "4444", "Денис")
    );

    @Override
    public String getUsernameByLoginAndPassoword(String login, String password) {
        for (User client : clients) {
            if (client.getLogin().equals(login) && client.getPassword().equals(password)) {
                return client.getUsername();
            }
        }
        return null;
    }

    @Override
    public void startAuth() {
        System.out.println("Старт аутентификации");
    }

    @Override
    public void endAuth() {
        System.out.println("Конец аутентификации");
    }

    @Override
    public void updateUsername(String login, String username) {

    }

    @Override
    public void registration(String login, String username, String password) {

    }

    @Override
    public boolean isFreeLogin(String login) {
        return true;
    }
}
