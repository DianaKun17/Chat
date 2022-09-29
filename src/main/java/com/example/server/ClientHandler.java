package com.example.server;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class ClientHandler implements Serializable {
    private static final String AUTH_CMD_PREFIX = "/auth";
    private static final String AUTHOK_CMD_PREFIX = "/authok";
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

    private MyServer myServer;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private AuthenticationService auth;
    private BufferedReader bufferedReader;
    private Logger logger = Logger.getLogger("ClientHandler");

    public ClientHandler(MyServer myServer, Socket socket) {
        this.myServer = myServer;
        clientSocket = socket;
    }

    public void handle() throws IOException {
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());

        new Thread(() -> {
            try {
                sing();
                readMessage();
            } catch (IOException e) {
                logger.warning(e.getMessage());
                try {
                    myServer.unSubscribe(this);
                } catch (IOException ex) {
                    logger.warning(ex.getMessage());
                }
            }
        }).start();
    }

    private void sing() throws IOException {
        while (true){
            String message = in.readUTF();
            if (message.startsWith(AUTH_CMD_PREFIX)){
                boolean isSuccessAuth = processAuth(message);
                if (isSuccessAuth) {
                    break;
                }
            } else if (message.startsWith(REGISTRATION_CLIENT_CMD_PREFIX)) {
                try {
                    processRegistration(message);
                } catch (SQLException e) {
                   logger.warning(e.getMessage());
                }
            }
            else {
                out.writeUTF(AUTHERR_CMD_PREFIX + "Ошибка аутентификации");
                logger.warning("Неудачная попытка аутентификации");
            }
        }
    }

    private boolean processAuth(String message) throws IOException {
        String[] parts = message.split("\\s+");
        if (parts.length != 3){
            out.writeUTF(AUTHERR_CMD_PREFIX + "Ошибка аутентификации");
        }
        String login = parts[1];
        String password = parts[2];

        auth = myServer.getAuthenticationService();

        try {
            username = auth.getUsernameByLoginAndPassoword(login, password);
            auth.endAuth();
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }

        if (username != null) {
            if (myServer.isUsernameBusy(username)){
                out.writeUTF(AUTHERR_CMD_PREFIX + " " + " Логин уже используется");
                return false;
            }
            out.writeUTF(AUTHOK_CMD_PREFIX + " " + username);
            myServer.subscribe(this);
            logger.info("Пользователь " + username + " подключился к чату");
            myServer.broadcastClientsList(this);
            showHistory();

            return  true;
        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Логин или пароль не соответствуют действительности");
            return false;
        }
    }


    public String getUsername() {
        return username;
    }

    private void readMessage() throws IOException {
        while (true){
            String message = in.readUTF();
            System.out.println("message | " + username + ": " + message);
            if (message.startsWith(STOP_SERVER_CMD_PREFIX)) {
                System.exit(0);
            } else if (message.startsWith(UPDATE_USERNAME_CMD_PREFIX)) {
              String[] parts = message.split("\\s+",3);

              String login = parts[1];
              String newUsername = parts[2];

                try {
                    auth.startAuth();
                    auth.updateUsername(login, newUsername);
                    auth.endAuth();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                logger.info("updateOK");
                myServer.broadcastMessage(String.format("Пользователь %s изменил свой ник на %s. Для корректного отображения имен пользователей обновите сервер.", username, newUsername), this, true);

            } else if (message.startsWith(END_CLIENT_CMD_PREFIX)){
                return;
            } else if (message.startsWith(PRIVATE_MSG_CMD_PREFIX)){
                myServer.privateMessage(message, this);
            } else {
                myServer.broadcastMessage(message, this);
            }

        }
    }

    public void sendServerMessage (String message) throws IOException {
        out.writeUTF(String.format("%s %s", SERVER_MSG_CMD_PREFIX, message));
    }

    public void sendMessage(String sender, String message) throws IOException {
        if (sender != null) {
            out.writeUTF(String.format("%s %s %s", CLIENT_MSG_CMD_PREFIX, sender, message));
        } else {
            sendServerMessage(message);
        }
    }

    public void sendClientsList(List<ClientHandler> clients) throws IOException {
        String msg = String.format("%s %s",INFO_CLIENTS_CMD_PREFIX,clients.toString());
        out.writeUTF(msg);
        System.out.println(msg);
    }

    @Override
    public String toString() {
        return username;
    }

    public boolean processRegistration(String message) throws IOException, SQLException {
        String[] parts = message.split("\\s+");
        if (parts.length != 4) {
            out.writeUTF(REGISTRATIONERR_CLIENT_CMD_PREFIX + "Ошибка регистрации");
        }
        String login = parts[1];
        String username = parts[2];
        String password = parts[3];

        auth = myServer.getAuthenticationService();
        auth.startAuth();

        if (auth.isFreeLogin(login) == true) {
            auth.registration(login, username, password);
            out.writeUTF(REGISTRATIONOK_CLIENT_CMD_PREFIX + " " + username);
            return true;
        } else {
            out.writeUTF(REGISTRATIONERR_CLIENT_CMD_PREFIX + "Логин занят");
            return false;
        }
    }

    public synchronized void showHistory() {
        try {
            bufferedReader = new BufferedReader(new FileReader("src/main/java/com/example/server/chatHistory/chatHistory.txt"));
            String line = bufferedReader.readLine();
            while (line != null) {
                sendServerMessage(line);
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            logger.warning(e.getMessage());
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
    }
}


