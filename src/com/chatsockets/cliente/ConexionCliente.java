
package com.chatsockets.cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConexionCliente {

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    public void conectar(String ip, int puerto) throws IOException {

        socket = new Socket(ip, puerto);

        entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        salida = new PrintWriter(
                socket.getOutputStream(), true
        );
    }

    public void enviarMensaje(String mensaje) {

        if (estaConectado()) {
            salida.println(mensaje);
        }
    }

    public String recibirMensaje() throws IOException {

        if (entrada == null) {
            throw new IOException("No existe una conexión activa.");
        }

        return entrada.readLine();
    }

    public boolean estaConectado() {

        return socket != null
                && socket.isConnected()
                && !socket.isClosed();
    }

    public void desconectar() throws IOException {

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
