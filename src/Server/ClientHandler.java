package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;

public class ClientHandler {
    private final Socket socket;
    private final ChatServer server;
    private final DataInputStream in;
    private final DataOutputStream out;

    private String name;

    public ClientHandler(Socket socket, ChatServer server) {
        try {
            this.name = "";
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            server.getExecutor().execute(() -> {
                try {
                    authenticate();
                    readMessages();
                } finally {
                    closeConnection();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Не могу создать обработчик для клиента", e);
        }
    }

    private void authenticate() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:chatbase.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }


        while (true) {
            try {
                final String str = in.readUTF();
                if (str.startsWith("/auth")) { // /auth login1 pass1
                    final String[] split = str.split("\\s");
                    final String login = split[1];
                    final String pass = split[2];
                    final String nickname = getNicknameByLoginAndPassword(login, pass, connection);
                    if (nickname != null) {
                        if (!server.isNicknameBusy(nickname)) {
                            sendMessage("/authok " + nickname);
                            this.name = nickname;
                            server.broadcast("SERVER: Пользователь " + nickname + " зашел в чат");
                            server.subscribe(this);
                            break;
                        } else {
                            sendMessage("SERVER: Уже произведен вход в учетную запись");
                        }
                    } else {
                        sendMessage("SERVER: Неверные логин / пароль");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getNicknameByLoginAndPassword(String ln, String ps, Connection connection) throws SQLException {
        String sql = "SELECT * FROM Users WHERE login == ? AND password == ?;";
        ResultSet rs;

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, ln);
        preparedStatement.setString(2, ps);

        rs = preparedStatement.executeQuery();
        if (rs.next()) {
            return rs.getString(3);
        } else {
            return null;
        }
    }

    private void closeConnection() {
        try {
            server.unsubscribe(this);
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String msg) {
        try {
            System.out.println("SERVER: Send message to " + name + ": " + msg);
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void readMessages() {
        try {
            while (true) {
                final String strFromClient = in.readUTF();
                if (strFromClient.startsWith("/")) {
                    if (strFromClient.equals("/end")) {
                        break;
                    }
                    if (strFromClient.startsWith("/w ")) {
                        String[] tokens = strFromClient.split("\\s");
                        String nick = tokens[1];
                        String msg = strFromClient.substring(4 + nick.length());
                        server.sendMsgToClient(this, nick, msg);
                    }
                    continue;
                }
                server.broadcast(name + ": " + strFromClient);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getName() {

        return name;
    }
}
