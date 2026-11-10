
package com.chatsockets.servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class ManejadorCliente implements Runnable {

    private final Socket socket;
    private final GestorClientes gestor;
    private final Consumer<String> log;

    private String nombreUsuario;

    public ManejadorCliente(
            Socket socket,
            GestorClientes gestor,
            Consumer<String> log) {

        this.socket = socket;
        this.gestor = gestor;
        this.log = log;
    }

    @Override
    public void run() {

        PrintWriter salida = null;
        boolean registrado = false;

        try (
            Socket conexion = socket;
            BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(conexion.getInputStream())
            )
        ) {

            salida = new PrintWriter(
                    conexion.getOutputStream(), true
            );

            String solicitud = entrada.readLine();

            if (!"CONECTAR".equals(solicitud)) {
                salida.println("ERROR|Solicitud de conexión inválida.");
                return;
            }

            nombreUsuario = gestor.registrarCliente(salida);

            if (nombreUsuario == null) {
                salida.println("ERROR|No se pudo registrar el usuario.");
                return;
            }

            registrado = true;

            salida.println("OK|" + nombreUsuario);

            log.accept(
                    "Usuario \"" + nombreUsuario + "\" conectado."
            );

            gestor.actualizarUsuarios();

            String mensaje;

            while ((mensaje = entrada.readLine()) != null) {

                if (mensaje.equalsIgnoreCase("chao")) {

                    log.accept(
                            "Usuario \"" + nombreUsuario
                            + "\" solicitó desconexión."
                    );

                    break;
                }

                if (mensaje.startsWith("PRIVADO|")) {

                    String[] partes = mensaje.split("\\|", 3);

                    if (partes.length != 3
                            || partes[1].isBlank()
                            || partes[2].isBlank()) {

                        salida.println("ERROR|Mensaje inválido.");
                        continue;
                    }

                    String destinatario = partes[1];
                    String contenido = partes[2];

                    boolean enviado = gestor.enviarMensajePrivado(
                            nombreUsuario,
                            destinatario,
                            contenido
                    );

                    if (enviado) {

                        salida.println(
                                "ENVIADO|" + destinatario
                                + "|" + contenido
                        );

                        log.accept(
                                nombreUsuario
                                + " envió mensaje a "
                                + destinatario
                        );

                    } else {

                        salida.println(
                                "ERROR|El usuario "
                                + destinatario
                                + " no está disponible."
                        );
                    }
                }
            }

        } catch (IOException e) {

            log.accept(
                    "Error de conexión"
                    + (nombreUsuario != null
                            ? " con " + nombreUsuario
                            : "")
                    + ": " + e.getMessage()
            );

        } finally {

            if (registrado) {

                gestor.eliminarCliente(nombreUsuario, salida);
                gestor.actualizarUsuarios();

                log.accept(
                        "Usuario \"" + nombreUsuario
                        + "\" desconectado."
                );
            }

            log.accept("Conexión finalizada.");
        }
    }
}
