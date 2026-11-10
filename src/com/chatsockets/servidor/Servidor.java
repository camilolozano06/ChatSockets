
package com.chatsockets.servidor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class Servidor {

    private static final String IP = "127.0.0.1";
    private static final int PUERTO = 5000;

    public static void iniciar(Consumer<String> log) {

        GestorClientes gestor = new GestorClientes();

        try (ServerSocket servidor = new ServerSocket(
                PUERTO, 50, InetAddress.getByName(IP))) {

            log.accept("=== SERVIDOR CHAT SOCKETS ===");
            log.accept("IP: " + IP);
            log.accept("Puerto: " + PUERTO);
            log.accept("Protocolo: TCP");
            log.accept("Servidor iniciado correctamente.");
            log.accept("Esperando conexiones...");

            while (true) {

                Socket cliente = servidor.accept();

                log.accept("Cliente conectado: "
                        + cliente.getInetAddress());

                Thread hiloCliente = new Thread(
                        new ManejadorCliente(cliente, gestor, log)
                );

                hiloCliente.start();
            }

        } catch (IOException e) {
            log.accept("Error del servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        iniciar(System.out::println);
    }
}
