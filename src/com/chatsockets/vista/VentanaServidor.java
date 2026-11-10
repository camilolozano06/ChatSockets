
package com.chatsockets.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.chatsockets.servidor.Servidor;

public class VentanaServidor extends JFrame {

    private final JTextArea areaLog;
    private final JButton btnIniciar;

    public VentanaServidor() {

        setTitle("Log del Servidor");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel(
                "Log del Servidor",
                SwingConstants.CENTER
        );

        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        titulo.setOpaque(true);
        titulo.setBackground(new Color(0, 153, 76));

        add(titulo, BorderLayout.NORTH);

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setFont(
                new Font("Consolas", Font.PLAIN, 14)
        );

        JScrollPane scroll = new JScrollPane(areaLog);
        add(scroll, BorderLayout.CENTER);

        btnIniciar = new JButton("Iniciar Servidor");

        JPanel panelInferior = new JPanel(
                new BorderLayout()
        );

        panelInferior.add(
                btnIniciar,
                BorderLayout.CENTER
        );

        add(panelInferior, BorderLayout.SOUTH);

        btnIniciar.addActionListener(e -> {

            btnIniciar.setEnabled(false);

            agregarLog("Iniciando servidor...");

            Thread hiloServidor = new Thread(() -> {

                Servidor.iniciar(this::agregarLog);

            }, "Hilo-Servidor");

            hiloServidor.setDaemon(true);
            hiloServidor.start();

        });
    }

    public void agregarLog(String mensaje) {

        SwingUtilities.invokeLater(() -> {

            areaLog.append(mensaje + "\n");

            areaLog.setCaretPosition(
                    areaLog.getDocument().getLength()
            );

        });
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            VentanaServidor ventana =
                    new VentanaServidor();

            ventana.setVisible(true);

        });
    }
}
