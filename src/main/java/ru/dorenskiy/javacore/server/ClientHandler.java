package ru.dorenskiy.javacore.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    public String getName() {
        return name;
    }
    public ClientHandler(MyServer myServer, Socket socket) {    // Конструктор класса
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            new Thread(() -> {
                try {
                    while (true) {                              // цикл авторизации
                        String str = in.readUTF();
                        if (str.startsWith("/auth")) {
                            String[] parts = str.split("\\s");  // расщепляет строку на массив строк с помощью разграничителя - пробел(строка вида "/auth login password")
                            String nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                            if (nick != null) {
                                if (!myServer.isNickBusy(nick)) {
                                    sendMsg("/authok " + nick);     //отправка сообщений на клиент
                                    name = nick;
                                    myServer.broadcastMsg(name + " зашел в чат");
                                    myServer.subscribe(this);
                                    break;
                                } else {
                                    System.out.println("Учетная запись уже используется");
                                    sendMsg("Учетная запись уже используется");//отправка сообщений для вывода в экране пользователя
                                }
                            } else {
                                System.out.println("Неверные логин/пароль");
                                sendMsg("Неверные логин/пароль");              //отправка сообщений для вывода в экране пользователя
                            }
                        }
                    }


                    while (true) {                                          // цикл получения сообщений и отправка их всем
                        String str = in.readUTF();
                        System.out.println("от " + name + ": " + str);
                        if (str.startsWith("/w")) {
                            String[] parts = str.split("\\s");      // расщепляет строку на массив строк с помощью разграничителя - пробел(строка вида "/auth login password")
                            String user = parts[1];
                            System.out.println("ЛС будет отправлено " + user);
                            myServer.sendMsgPrivate(str, user, this);             // отправка ЛС
                        } else if (str.equals("/end")) {
                            sendMsg("/end");
                            break;
                        }else myServer.broadcastMsg(name + ": " + str);     //отправка сообщений всем
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    myServer.unsubscribe(this);
                    myServer.broadcastMsg(name + " вышел из чата");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }
    public void sendMsg(String msg) {       // отправка сообщений на Клиент
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
