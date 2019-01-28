package ru.dorenskiy.javacore.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class MyWindow extends JFrame {

    private final String SERVER_ADDR = "localhost";
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    JTextField jtf = new JTextField();
    JTextArea jta = new JTextArea();
    JButton jbSend = new JButton("SEND");
    boolean authorized;

    public MyWindow() {     //Конструктор графического окна

        setBounds(600, 300, 500, 500);
        setTitle("client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jta.setEditable(false);
        jta.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jta);
        add(jsp, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(jbSend, BorderLayout.EAST);
        bottomPanel.add(jtf, BorderLayout.CENTER);
        setVisible(true);

        onAuthClick();
    }

    public void start(){
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            setAuthorized(false);
            authorized = false;
            long beginTime = System.currentTimeMillis();

            Thread t = new Thread(() -> {
                try {
                    while (true) {                                  // в цикле проверяется удачная ли у пользователя авторизация,
                                                                    // до тех пор, пока она не будет удачной,
                                                                    // выход из цикла для одного пользователя-один раз.
                        String str = in.readUTF();                  // приходит входящее с сервера сообщение
                        if (str.startsWith("/authok")) {
                            setAuthorized(true);
                            authorized = true;
                            break;
                        }

                        if(str.equals("Учетная запись уже используется")) { //для вывода сообщений в экране пользователя
                            jta.append(str + "\n");
                        }
                        if(str.equals("Неверные логин/пароль")) {           //для вывода сообщений в экране пользователя
                            jta.append(str + "\n");
                        }
                    }

                    while (true) {                      // проверяется приходящее сообщение с сервера. Если возвоащается /end, то выход из цикла и на переход на блок finally
                        String str = in.readUTF();      // приходит входящее с сервера сообщение
                        if (str.equals("/end")) break;  // если убрать break, то далее поймается исключение
                        jta.append(str + "\n");         // !!! вывод сообщений в чате в окне у клиента
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setAuthorized(false);
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            jta.append("Не удалось подключиться к серверу" + "\n");
            e.printStackTrace();
        }

        jbSend.addActionListener(new ActionListener() {     // действие по кнопке "Отправить" (jbSend)
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!jtf.getText().trim().isEmpty()) {      //trim() — удаляет пробелы в начале и конце строки
                    sendMsg();
                    jtf.grabFocus();
                }
            }
        });

        long beginTime = System.currentTimeMillis();
        long currentTime;

        while (!authorized) {   // Хитрый момент! Значение authorized меняется в параллельном потоке выше
            currentTime = System.currentTimeMillis();
            try {
                Thread.sleep(100);  // "притормозить" основной поток нужно, иначе всегда будет срабатывать разрыв соединения ниже (из-за дискретности вычислений). Необходимо, чтобы проверка условия авторизации проходила раз в какое-то время, а не без останвки - непрерывно.
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            if (authorized){
                break;
            }else if (currentTime - beginTime > 10000) {
                try {
                    out.writeUTF("/end");
                    out.flush();
                    socket.close();
                    System.out.println("Соединение разорвано, так как пользователь не авторизовался в течении 120 сек");
                    authorized = true;
                    break;
                } catch (IOException exp) {
                    exp.printStackTrace();
                }
            }
        }
    }

    public void onAuthClick() {
        if (socket == null || socket.isClosed())
            start();
            sendMsg();
    }

    public void sendMsg() {         // метод отправляет сообщения из строки ввода на сервер
        try {
            out.writeUTF(jtf.getText());
            jtf.setText("");
        } catch (IOException e) {
            System.out.println("Ошибка отправки сообщения");   //Срабатывает, когда пользователь вышел из чата
        }
    }

    private void setAuthorized(boolean var){
        if (var == true){
            System.out.println("Клиент авторизовался");
        }else System.out.println("Клиент не авторизовался");
    }
}
