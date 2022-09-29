package com.example.chat;

import com.example.server.MyServer;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Network {
    private static final String AUTH_CMD_PREFIX = "/auth";
    private static final String AUTHOK_CMD_PREFIX = "/authok ";
    private static final String AUTHERR_CMD_PREFIX = "/autherr";
    private static final String CLIENT_MSG_CMD_PREFIX = "/cMsg";
    private static final String SERVER_MSG_CMD_PREFIX = "/sMsg";
    private static final String PRIVATE_MSG_CMD_PREFIX = "/pMsg";
    private static final String STOP_SERVER_CMD_PREFIX = "/stop";
    private static final String END_CLIENT_CMD_PREFIX = "/end";
    private static final String INFO_CLIENTS_CMD_PREFIX = "/info";
    private static final String UPDATE_USERNAME_CMD_PREFIX = "/update";
    private static final String REGISTRATION_CLIENT_CMD_PREFIX = "/reg";
    private static final String REGISTRATIONOK_CLIENT_CMD_PREFIX = "/regok";
    private static final String REGISTRATIONERR_CLIENT_CMD_PREFIX = "/regerr";

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8888;
    private DataInputStream in;
    private DataOutputStream out;
    private final String host;
    private final int port;
    private String username;
    private Logger logger = Logger.getLogger("Network");


    public Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Network() {
        this.host = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
    }

    public void connect(){
        try {
            Socket socket = new Socket(host, port);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            logger.warning(e.getMessage());
            logger.info("Не удалось подключиться");
        }
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            logger.warning(e.getMessage());
            logger.info("Сообщение не отправлено");
        }
    }

    public void waitMessage(ChatController chatController) {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith(CLIENT_MSG_CMD_PREFIX)){
                        String[] parts = message.split("\\s+", 3);
                        String sender = parts[1];
                        String messageFromSender = parts[2];

                        Platform.runLater(() ->
                                chatController.append(String.format("%s: %s", sender, messageFromSender)));


                    } else if (message.startsWith(SERVER_MSG_CMD_PREFIX)) {
                        String[] parts = message.split("\\s+", 2);
                        String serverMessage = parts[1];

                        Platform.runLater(() ->
                                chatController.appendServerMessage(serverMessage));

                    } else if (message.startsWith(INFO_CLIENTS_CMD_PREFIX)){
                       message = message.substring(message.indexOf('[') + 1, message.indexOf(']'));
                       String[] users = message.split(", ");

                        Platform.runLater(() -> chatController.setUserList(users));

                    }
                }
            } catch(IOException e){
                logger.warning(e.getMessage());
            }
        });

        t.setDaemon(true);
        t.start();
    }

    public String sendAuthMessage(String login, String password) {
        try {
            out.writeUTF(String.format("%s %s %s", AUTH_CMD_PREFIX, login, password));
            String response = in.readUTF();
            if (response.startsWith(AUTHOK_CMD_PREFIX)) {
                this.username = response.split("\\s+", 2)[1];
                return null;
            } else {
                return response.split("\\s+", 2)[1];
            }
        } catch (IOException e) {
            logger.warning(e.getMessage());
            return e.getMessage();
        }
    }

    public String getUsername() {
        return username;
    }

    public void sendPrivateMessage(String selectedRecipient, String message) {
       sendMessage(String.format("%s %s %s", PRIVATE_MSG_CMD_PREFIX, selectedRecipient, message));
    }

    public String sendRegistrationMessage(String login, String username, String password) {
        try {
            out.writeUTF(String.format("%s %s %s %s", REGISTRATION_CLIENT_CMD_PREFIX, login, username, password));
            String answer = in.readUTF();

            if (answer.startsWith(REGISTRATIONOK_CLIENT_CMD_PREFIX)) {
                this.username = answer.split("\\s+")[1];
                return null;
            } else {
                return answer.split("\\s+")[1];
            }
        } catch (IOException e) {
            logger.warning(e.getMessage());
            return e.getMessage();
        }
    }

}


