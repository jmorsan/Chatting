package es.jms.dar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.ListIterator;

public class ClientHandler extends Thread{
    /** Almacena la lista de identificadores utilizados actualmente para mantenerlos únicos. */
    private static LinkedList<String> handles = new LinkedList<String>();

    /** Almacena la lista de todas las instancias de ClientHandler actualmente activas. */
    private static LinkedList<ClientHandler> clients = new LinkedList<ClientHandler>();

    /** Almacena el socket de red utilizado para interactuar con el cliente. */
    private Socket socket;

    /** Almacena el lector de flujo de entrada utilizado para recibir mensajes de red. */
    private DataInputStream incoming;

    /** Almacena el escritor de flujo de salida utilizado para enviar mensajes de red. */
    private DataOutputStream outgoing;

    /** Almacena el identificador que está utilizando actualmente el cliente. */
    String handle;

    /**
     * Transmite el mensaje especificado a todos los clientes conectados.
     * @param message el mensaje a compartir
     */
    protected static synchronized void broadcast(String message)
    {
        synchronized (clients)
        {
            ListIterator<ClientHandler> itr = clients.listIterator();
            ClientHandler current;

            while(itr.hasNext())
            {
                try
                {
                    current = itr.next();
                    current.outgoing.writeUTF(message);
                    current.outgoing.flush();
                }
                catch (Exception e)
                {
                    System.out.println(e.getStackTrace());
                }
            }
        }
    }

    /**
     * Comprueba si el identificador especificado es válido.
     */
    private static boolean isValidHandle(String handle)
    {
        for (int i = 0; i < handle.length(); i++)
            if (!Character.isLetterOrDigit(handle.charAt(i)))
                return false;
        return true;
    }

    /**
     * Construye un nuevo controlador de cliente para dar servicio al cliente especificado.
     */
    public ClientHandler(Socket socket) throws IOException
    {
        this.socket = socket;
        incoming = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        outgoing = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        handle = null;
    }

    /**
     * Escucha los mensajes del cliente y los transmite a todos los clientes conectados.
     */
    public void run()
    {
        try
        {
            clients.add(this);

            String message;

            while (true)
            {
                message = incoming.readUTF();

                if (message.startsWith("/usuario"))
                {
                    String[] parts = message.split(" ");

                    if (parts.length == 1)
                    {
                        outgoing.writeUTF("¡Debe especificar un identificador!");
                    }
                    else if (!isValidHandle(parts[1]) || parts.length > 2)
                    {
                        outgoing.writeUTF("¡Tu identificador contiene caracteres no válidos!");
                    }
                    else
                    {
                        if (handles.contains(parts[1]))
                        {
                            outgoing.writeUTF("¡Tu identificador ya esta en uso!");
                        }
                        else
                        {
                            handle = parts[1];
                            handles.add(handle);
                            outgoing.writeUTF("Ahora eres conocido como " + handle + "!");
                        }
                    }

                    outgoing.flush();
                }
                else
                {
                    if (handle == null)
                    {
                        outgoing.writeUTF("Identificate usado el comando /usuario <id>!");
                        outgoing.flush();
                    }
                    else
                    {
                        broadcast("[" + handle + "] dice: " + message);
                    }
                }
            }

        }
        catch (IOException e)
        {
            //e.printStackTrace();
        }
        finally
        {
            clients.remove(this);
            handles.remove(handle);
            if (handle != null)
                broadcast("[" + handle + "] ha salido de la sala!");

            try
            {
                socket.close ();
            }
            catch (IOException e)
            {
                //e.printStackTrace();
            }
        }
    }
}
