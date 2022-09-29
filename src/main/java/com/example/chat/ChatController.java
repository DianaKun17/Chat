package com.example.chat;

import com.example.server.ClientHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class ChatController {

    @FXML
    private Button button;
    @FXML
    private Button exitButton;
    @FXML
    private Button helpButton;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField textField;
    @FXML
    private ListView<String> userList;
    @FXML
    private Label usernameTitle;
    private String selectedRecipient;

    private Network network;

    public void setNetwork(Network network) {
        this.network = network;
    }

    @FXML
    public void initialize() {
        button.setOnAction(event -> pushMessage());
        textField.setOnAction(event -> pushMessage());
        exitButton.setOnAction(event -> System.exit(0));
        helpButton.setOnAction(event -> helpButton());


        userList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = userList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                userList.requestFocus();
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.clearSelection(index);
                        selectedRecipient = null;
                    } else {
                        selectionModel.select(index);
                        selectedRecipient = cell.getItem();
                    }
                    event.consume();
                }
            });
            return cell;
        });
    }


    @FXML
    void pushMessage() {
        String message = textField.getText().trim();
        textField.clear();

        if (message.isBlank()) {
            return;
        }

        if (selectedRecipient != null) {
            network.sendPrivateMessage(selectedRecipient, message);
        } else {
            network.sendMessage(message);
        }

        append("Я: " + message);
    }

    public void append(String message){
        String timeStamp = DateFormat.getInstance().format(new Date());

        chatArea.appendText(timeStamp);
        chatArea.appendText(System.lineSeparator());
        chatArea.appendText(message);
        chatArea.appendText(System.lineSeparator());
        chatArea.appendText(System.lineSeparator());
    }

    public void helpButton() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);

        helpAlert.setTitle("Information");
        helpAlert.setHeaderText(null);
        helpAlert.setContentText("Приложение на стадии разработки." + System.lineSeparator() + "Версия 1.0");
        helpAlert.showAndWait();
    }

    public void setUsernameTitle(String username) {
        this.usernameTitle.setText(username);
    }

    public void appendServerMessage(String serverMessage) {
        chatArea.appendText(serverMessage);
        chatArea.appendText(System.lineSeparator());
        chatArea.appendText(System.lineSeparator());
    }

    public void setUserList(String[] users) {
        for (int i = 0; i < users.length; i++) {
            if (users[i].equals(network.getUsername())) {
                users[i] = "*** " + users[i] + " ***";
            }
        }

        userList.getItems().clear();
        Collections.addAll(userList.getItems(), users);
    }

}