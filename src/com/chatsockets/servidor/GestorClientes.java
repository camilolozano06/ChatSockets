
package com.chatsockets.servidor;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GestorClientes {

    private final Map<String, PrintWriter> clientes =
            new ConcurrentHashMap<>();

    public synchronized String registrarCliente(PrintWriter salida) {

        if (salida == null) {
            return null;
        }

        int numero = 1;
        String nombre;

        do {
            nombre = String.format("poli%02d", numero);
            numero++;
        } while (clientes.containsKey(nombre));

        clientes.put(nombre, salida);

        return nombre;
    }

    public synchronized void eliminarCliente(
            String nombre, PrintWriter salida) {

        clientes.remove(nombre, salida);
    }

    public List<String> obtenerUsuarios() {

        List<String> usuarios =
                new ArrayList<>(clientes.keySet());

        usuarios.sort(String.CASE_INSENSITIVE_ORDER);

        return usuarios;
    }

    public boolean enviarMensajePrivado(
            String remitente,
            String destinatario,
            String mensaje) {

        PrintWriter salida = clientes.get(destinatario);

        if (salida == null) {
            return false;
        }

        salida.println(
                "MENSAJE|" + remitente + "|" + mensaje
        );

        return !salida.checkError();
    }

    public void actualizarUsuarios() {

        String lista = String.join(",", obtenerUsuarios());

        for (PrintWriter salida : clientes.values()) {
            salida.println("USUARIOS|" + lista);
        }
    }
}
