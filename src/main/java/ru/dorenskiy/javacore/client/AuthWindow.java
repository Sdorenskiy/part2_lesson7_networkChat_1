package ru.dorenskiy.javacore.client;

import javax.swing.*;
import java.awt.*;

class AuthWindow extends JFrame {

    public AuthWindow() {                 //Конструктор графического окна
        setBounds(300, 300, 300, 80);
        setTitle("Авторизация");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.CENTER);

        JTextField jtf = new JTextField();
        JTextField jtf1 = new JTextField();
        bottomPanel.add(jtf, BorderLayout.NORTH);
        bottomPanel.add(jtf1, BorderLayout.SOUTH);

        setVisible(true);
    }
}