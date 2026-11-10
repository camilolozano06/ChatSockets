
package com.chatsockets.vista;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.chatsockets.cliente.ConexionCliente;

public class VentanaCliente extends JFrame {

    private final JTextField txtIP;
    private final JTextField txtPuerto;
    private final JTextField txtNombre;
    private final JTextField txtMensaje;

    private final JComboBox<String> comboUsuarios;
    private final JTextArea areaChat;

    private final JButton btnConectar;
    private final JButton btnEnviar;
    private final JButton btnChao;

    private volatile ConexionCliente conexion;
    private String nombreUsuario;

    public VentanaCliente() {

        setTitle("Cliente Chat");
        setSize(500, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        JPanel panelConexion = new JPanel(
                new GridLayout(4, 2, 5, 5)
        );

        panelConexion.setBorder(
                BorderFactory.createTitledBorder("Datos de conexión")
        );

        txtIP = new JTextField("127.0.0.1");
        txtPuerto = new JTextField("5000");
        txtNombre = new JTextField("Automático");
        txtNombre.setEditable(false);

        btnConectar = new JButton("Conectar");

        panelConexion.add(new JLabel("IP del servidor:"));
        panelConexion.add(txtIP);
        panelConexion.add(new JLabel("Puerto:"));
        panelConexion.add(txtPuerto);
        panelConexion.add(new JLabel("Nombre de usuario:"));
        panelConexion.add(txtNombre);
        panelConexion.add(new JLabel(""));
        panelConexion.add(btnConectar);

        JPanel panelUsuarios = new JPanel(
                new BorderLayout(5, 5)
        );

        panelUsuarios.setBorder(
                BorderFactory.createTitledBorder(
                        "Usuario para conversar"
                )
        );

        comboUsuarios = new JComboBox<>();
        panelUsuarios.add(comboUsuarios, BorderLayout.CENTER);

        JPanel panelSuperior = new JPanel(
                new BorderLayout(5, 5)
        );

        panelSuperior.add(panelConexion, BorderLayout.NORTH);
        panelSuperior.add(panelUsuarios, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);

        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setFont(new Font("Consolas", Font.PLAIN, 14));

        add(new JScrollPane(areaChat), BorderLayout.CENTER);

        JPanel panelMensaje = new JPanel(
                new BorderLayout(5, 5)
        );

        txtMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        btnChao = new JButton("Chao");

        JPanel panelBotones = new JPanel(
                new GridLayout(1, 2, 5, 5)
        );

        panelBotones.add(btnEnviar);
        panelBotones.add(btnChao);

        panelMensaje.add(txtMensaje, BorderLayout.CENTER);
        panelMensaje.add(panelBotones, BorderLayout.EAST);

        add(panelMensaje, BorderLayout.SOUTH);

        cambiarEstado(false);

        btnConectar.addActionListener(e -> conectar());
        btnEnviar.addActionListener(e -> enviarMensaje());
        txtMensaje.addActionListener(e -> enviarMensaje());
        btnChao.addActionListener(e -> desconectar());
    }

    private void conectar() {

        String ip = txtIP.getText().trim();
        String puertoTexto = txtPuerto.getText().trim();

        btnConectar.setEnabled(false);

        mostrarMensaje("Conectando al servidor...");

        Thread hilo = new Thread(() -> {

            ConexionCliente nuevaConexion = new ConexionCliente();

            try {

                int puerto = Integer.parseInt(puertoTexto);

                nuevaConexion.conectar(ip, puerto);
                nuevaConexion.enviarMensaje("CONECTAR");

                String respuesta = nuevaConexion.recibirMensaje();

                if (respuesta == null
                        || !respuesta.startsWith("OK|")) {

                    String error = respuesta == null
                            ? "El servidor cerró la conexión."
                            : respuesta.startsWith("ERROR|")
                                    ? respuesta.substring(6)
                                    : "Respuesta inesperada del servidor.";

                    nuevaConexion.desconectar();

                    SwingUtilities.invokeLater(() -> {
                        mostrarMensaje(error);
                        btnConectar.setEnabled(true);
                    });

                    return;
                }

                String nombreAsignado = respuesta.substring(3);

                SwingUtilities.invokeLater(() -> {

                    conexion = nuevaConexion;
                    nombreUsuario = nombreAsignado;

                    txtNombre.setText(nombreUsuario);

                    txtIP.setEnabled(false);
                    txtPuerto.setEnabled(false);

                    cambiarEstado(true);

                    mostrarMensaje(
                            "Conectado como: " + nombreUsuario
                    );

                    escucharMensajes(nuevaConexion);
                });

            } catch (Exception ex) {

                try {
                    nuevaConexion.desconectar();
                } catch (IOException ignored) {
                }

                SwingUtilities.invokeLater(() -> {
                    mostrarMensaje(
                            "Error de conexión: " + ex.getMessage()
                    );
                    btnConectar.setEnabled(true);
                });
            }

        }, "Hilo-Conexion");

        hilo.setDaemon(true);
        hilo.start();
    }

    private void escucharMensajes(ConexionCliente activa) {

        Thread hilo = new Thread(() -> {

            try {

                String mensaje;

                while ((mensaje = activa.recibirMensaje()) != null) {

                    String recibido = mensaje;

                    SwingUtilities.invokeLater(() -> {

                        if (conexion == activa) {
                            procesarMensaje(recibido);
                        }
                    });
                }

            } catch (IOException ex) {

                SwingUtilities.invokeLater(() -> {

                    if (conexion == activa) {
                        mostrarMensaje(
                                "Conexión interrumpida: "
                                + ex.getMessage()
                        );
                    }
                });

            } finally {

                SwingUtilities.invokeLater(() -> {

                    if (conexion == activa) {
                        mostrarMensaje("Desconectado del servidor.");
                        finalizarConexion(activa);
                    }
                });
            }

        }, "Hilo-Receptor");

        hilo.setDaemon(true);
        hilo.start();
    }

    private void procesarMensaje(String mensaje) {

        if (mensaje.startsWith("USUARIOS|")) {

            String usuarios = mensaje.substring(9);

            String seleccionado =
                    (String) comboUsuarios.getSelectedItem();

            comboUsuarios.removeAllItems();

            if (!usuarios.isBlank()) {

                Arrays.stream(usuarios.split(","))
                        .filter(usuario ->
                                !usuario.equals(nombreUsuario))
                        .forEach(comboUsuarios::addItem);
            }

            if (seleccionado != null) {

                for (int i = 0; i < comboUsuarios.getItemCount(); i++) {

                    if (seleccionado.equals(
                            comboUsuarios.getItemAt(i))) {

                        comboUsuarios.setSelectedItem(seleccionado);
                        break;
                    }
                }
            }

            actualizarEnvio();

        } else if (mensaje.startsWith("MENSAJE|")) {

            String[] partes = mensaje.split("\\|", 3);

            if (partes.length == 3) {
                mostrarMensaje(
                        partes[1] + ": " + partes[2]
                );
            }

        } else if (mensaje.startsWith("ENVIADO|")) {

            String[] partes = mensaje.split("\\|", 3);

            if (partes.length == 3) {
                mostrarMensaje(
                        nombreUsuario + ": " + partes[2]
                );
            }

        } else if (mensaje.startsWith("ERROR|")) {

            mostrarMensaje(mensaje.substring(6));
        }
    }

    private void enviarMensaje() {

        if (conexion == null || !conexion.estaConectado()) {
            return;
        }

        String destinatario =
                (String) comboUsuarios.getSelectedItem();

        String mensaje = txtMensaje.getText().trim();

        if (destinatario == null || mensaje.isEmpty()) {
            return;
        }

        conexion.enviarMensaje(
                "PRIVADO|" + destinatario + "|" + mensaje
        );

        txtMensaje.setText("");
        txtMensaje.requestFocusInWindow();
    }

    private void desconectar() {

        ConexionCliente activa = conexion;

        if (activa != null) {

            activa.enviarMensaje("chao");
            finalizarConexion(activa);

            mostrarMensaje("Desconectado del servidor.");
        }
    }

    private void finalizarConexion(ConexionCliente activa) {

        if (conexion != activa) {
            return;
        }

        conexion = null;

        try {
            activa.desconectar();
        } catch (IOException ignored) {
        }

        txtIP.setEnabled(true);
        txtPuerto.setEnabled(true);

        txtNombre.setText("Automático");
        nombreUsuario = null;

        comboUsuarios.removeAllItems();

        cambiarEstado(false);
    }

    private void cambiarEstado(boolean conectado) {

        btnConectar.setEnabled(!conectado);
        btnChao.setEnabled(conectado);

        comboUsuarios.setEnabled(conectado);
        txtMensaje.setEnabled(conectado);

        actualizarEnvio();
    }

    private void actualizarEnvio() {

        btnEnviar.setEnabled(
                conexion != null
                && conexion.estaConectado()
                && comboUsuarios.getItemCount() > 0
        );
    }

    private void mostrarMensaje(String mensaje) {

        areaChat.append(mensaje + "\n");

        areaChat.setCaretPosition(
                areaChat.getDocument().getLength()
        );
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            VentanaCliente ventana = new VentanaCliente();
            ventana.setVisible(true);
        });
    }
}
