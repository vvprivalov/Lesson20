package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private final List<ClientHandler> clients;
    private final ExecutorService executor;

    public ChatServer() {
        clients = new ArrayList<>();
        executor = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("СЕРВЕР: Сервер запущен...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("СЕРВЕР: Клиент подключился...");
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler client : clients) {
            sb.append(client.getName()).append(" ");
        }
        broadcast(sb.toString());
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void broadcast(String msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        System.out.println("СЕРВЕР: Клиент " + clientHandler.getName() + " подключился...");
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        System.out.println("СЕРВЕР: Клиент " + clientHandler.getName() + " отключился...");
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public boolean isNicknameBusy(String nickname) {
        for (ClientHandler client : clients) {
            if (client.getName().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public void sendMsgToClient(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nickTo)) {
                o.sendMessage("от " + from.getName() + ": " + msg);
                from.sendMessage("клиенту " + nickTo + ": " + msg);
                return;
            }
        }
        from.sendMessage("Участника с ником " + nickTo + " нет в чат-комнате");
    }
}
