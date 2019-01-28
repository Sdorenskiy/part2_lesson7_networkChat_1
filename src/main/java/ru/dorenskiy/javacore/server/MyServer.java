package ru.dorenskiy.javacore.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class MyServer {

    private ServerSocket server;
    private Vector<ClientHandler> clients;
    private AuthService authService;
    public AuthService getAuthService() {
        return authService;
    }
    private final int PORT = 8189;
    public MyServer(){
        try{
            server = new ServerSocket(PORT);
            Socket socket = null;
            authService = new BaseAuthService();
            authService.start();
            clients = new Vector<>();
            while (true){
                System.out.println("Сервер ожидает подключения");
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        }catch (IOException e){
            System.out.println("Ошибка сервера");
        }finally {
            try{
                server.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            authService.stop();
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nick)) return true;
        }
        return false;
    }
    public synchronized void broadcastMsg(String msg) {     // сообщение всем в чате
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }
    public synchronized void unsubscribe(ClientHandler o) {
        clients.remove(o);
        broadcastClientList();
    }

    public synchronized void subscribe(ClientHandler o) {       //Клиент подключился
        clients.add(o);
        broadcastClientList();
    }

    public synchronized void sendMsgPrivate(String msg, String user, ClientHandler from) {     // ЛС в чате
        for (ClientHandler o : clients) {
            if (user.equals(o.getName())){
                o.sendMsg(msg);
                from.sendMsg(msg);
                return;
            }
        }
        from.sendMsg("участника с ником " + user + " нет в чате..");
    }

    public synchronized void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler o : clients) {
            sb.append(o.getName() + " ");
        }
        String msg = sb.toString();
        broadcastMsg(msg);
    }
}
