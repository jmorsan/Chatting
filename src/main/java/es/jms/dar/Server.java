package es.jms.dar;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
    public static void main(String[] args) throws IOException
    {
        ServerSocket server = new ServerSocket(5000);

        while (true)
        {
            // Espera a recepcionar el cliente
            Socket client = server.accept();
            System.out.println ("Conexion aceptada de: " + client.getInetAddress ());
            // crear un clienthandler para administrar la nueva conexión entrante
            ClientHandler c = new ClientHandler(client);
            c.start ();
        }
    }
}
