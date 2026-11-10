
package com.chatsockets.cliente;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    private static final String IP = "127.0.0.1";
    private static final int PUERTO = 5000;

    public static void main(String[] args) {

        try (
            Socket socket = new Socket(IP, PUERTO);
            PrintWriter salida = new PrintWriter(
                    socket.getOutputStream(), true);
            Scanner teclado = new Scanner(System.in)
        ) {

            System.out.println("=== CLIENTE CHAT SOCKETS ===");
            System.out.println("Conectado al servidor correctamente.");
            System.out.println("Escribe tus mensajes.");
            System.out.println("Para salir escribe: chao");

            String mensaje;

            while (true) {

                System.out.print("Tú: ");
                mensaje = teclado.nextLine();

                salida.println(mensaje);

                if (mensaje.equalsIgnoreCase("chao")) {
                    System.out.println("Desconectando del servidor...");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Error de conexión: "
                    + e.getMessage());
        }
    }
}
