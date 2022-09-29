package com.example.chat;

import com.example.server.MyServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientStart extends Application {

    private Network network;
    private Stage primaryStage;
    private Stage authStage;
    private ChatController chatController;


    @Override
    public void start(Stage stage) throws IOException {

        this.primaryStage = stage;

        network = new Network();
        network.connect();

        openAuthDialog();
        createChatDialog();
    }

     public void openAuthDialog() throws IOException {
        FXMLLoader authLoader = new FXMLLoader(ClientStart.class.getResource("auth-view.fxml"));

        Scene scene = new Scene(authLoader.load());
        authStage = new Stage();

        authStage.setScene(scene);
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.initOwner(primaryStage);
        authStage.setTitle("Authentication");
        authStage.setAlwaysOnTop(true);
        authStage.show();

        AuthController chatController = authLoader.getController();

        chatController.setNetwork(network);
        chatController.setStartClient(this);

    }

    private void createChatDialog() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientStart.class.getResource("chat-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(true);

        chatController = fxmlLoader.getController();

        chatController.setNetwork(network);

    }

    public void openChatDialog() {
        authStage.close();
        primaryStage.show();
        primaryStage.setTitle(network.getUsername());
        network.waitMessage(chatController);
        chatController.setUsernameTitle(network.getUsername());
    }

    public void showErrorAlert(String title, String errorMessage){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(errorMessage);
        alert.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
