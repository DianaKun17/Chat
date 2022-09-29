package com.example.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class MyServer {
    private final ServerSocket serverSocket;
    private final AuthenticationService authenticationService;
    private final List<ClientHandler> clients;
    private ClientHandler handler;
    private File history = new File("src/main/java/com/example/server/chatHistory/chatHistory.txt");
    private FileWriter input;
    private String timeStamp = DateFormat.getInstance().format(new Date());
    private Logger logger = Logger.getLogger("MyServer");

    public MyServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        authenticationService = new AuthenticationDataBase();
        clients = new ArrayList<>();

    }

    public void start() {
        logger.info("Сервер запущен");

        try {
            while (true) {
                waitAndProcessNewClientConnection();
            }
        } catch (IOException e){
            logger.warning(e.getMessage());
        }
    }

    private void waitAndProcessNewClientConnection() throws IOException {
        logger.info("Ожидание клиента");
        Socket socket = serverSocket.accept();
        logger.info("Клиент подключился");

        processClientConnection(socket);
    }

    private void processClientConnection(Socket socket) throws IOException {
        handler = new ClientHandler(this, socket);
        handler.handle();
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public synchronized void subscribe(ClientHandler clientHandler) throws IOException {
        clients.add(clientHandler);
    }

    public synchronized void unSubscribe(ClientHandler clientHandler) throws IOException {
        clients.remove(clientHandler);
        broadcastDisconnectClients(clientHandler);
    }

    public synchronized boolean isUsernameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastMessage(String message, ClientHandler sender, boolean isServerMessage) throws IOException {
        for (ClientHandler client : clients) {
            if (client == sender) {
                continue;
            }
            client.sendMessage(isServerMessage ? null : sender.getUsername(), message);
            createHistory(message, sender.getUsername());
        }
    }

    public synchronized void broadcastMessage(String message, ClientHandler sender) throws IOException{
        broadcastMessage(message,sender,false);
    }

    public synchronized void privateMessage(String message, ClientHandler clientHandler) throws IOException {
        String[] parts = message.split("\\s", 3);

        String username = parts[1];

        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
               client.sendMessage(clientHandler.getUsername(), parts[2]);
            }
        }
    }

    public synchronized void broadcastClientsList(ClientHandler handler) throws IOException {
        for (ClientHandler client : clients) {
            client.sendServerMessage(String.format("Подключился пользователь с ником %s", handler.getUsername()));
           // client.showHistory();
            client.sendClientsList(clients);
        }
    }

    public synchronized void broadcastDisconnectClients(ClientHandler handler) throws IOException {
        for (ClientHandler client : clients) {
            if (client == handler) {
                continue;
            }
            client.sendServerMessage(String.format("%s вышел из чата", handler.getUsername()));
            client.sendClientsList(clients);
        }
    }

    public synchronized void createHistory(String message, String sender) {
        try {
            input = new FileWriter(history, true);
            input.write(String.format("%s\n%s: %s\n",timeStamp, sender, message));
            input.close();
        } catch (IOException e) {
           logger.warning(e.getMessage());
        }
    }
}
