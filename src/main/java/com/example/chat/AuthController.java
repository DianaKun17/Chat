package com.example.chat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class AuthController {
    @FXML
    private TextField newLoginField;

    @FXML
    private TextField newPasswordField;

    @FXML
    private TextField newUsernameField;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;
    private Network network;
    private ClientStart startClient;

    @FXML
    public void checkAuth() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        if (login.length() == 0 && password.length() == 0) {
            startClient.showErrorAlert("Ошибка ввода при аутентифиации", "Поля не должны быть пустыми");
            return;
        }

        String authErrorMessage;
        authErrorMessage = network.sendAuthMessage(login, password);

        if (authErrorMessage == null) {
            startClient.openChatDialog();
        } else {
            startClient.showErrorAlert("Ошибка аутетификации", authErrorMessage);
        }
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setStartClient(ClientStart startClient) {
        this.startClient = startClient;
    }

    @FXML
    void registration() {
        String newLogin = newLoginField.getText().trim();
        String newUsername = newUsernameField.getText().trim();
        String newPassword = newPasswordField.getText().trim();

        if (newLogin.length() == 0 && newPassword.length() == 0 && newUsername.length() == 0) {
            startClient.showErrorAlert("Ошибка регисстрации", "Поля не должны быть пустыми");
            return;
        }

        String registrationErrorMessage;
        registrationErrorMessage = network.sendRegistrationMessage(newLogin ,newUsername, newPassword);

        if (registrationErrorMessage == null) {
            try {
                startClient.openAuthDialog();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            startClient.showErrorAlert("Ошибка регистрации", registrationErrorMessage);
        }
    }

}

