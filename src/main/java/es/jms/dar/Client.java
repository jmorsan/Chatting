package es.jms.dar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client extends JFrame implements ActionListener, Runnable
{
    /** Almacena la identificación serializable de la clase. */
    private static final long serialVersionUID = 1397292566843594359L;

    /** Almacena el socket de red utilizado para interactuar con el servidor. */
    private Socket socket;

    /** Almacena el lector de flujo de entrada utilizado para recibir mensajes de red. */
    private DataInputStream incoming;

    /** Almacena el escritor de flujo de salida utilizado para enviar mensajes de red. */
    private DataOutputStream outgoing;

    /** Almacena la lista con todos los mensajes recibidos */
    private LinkedList<String> messages;

    /** Almacena todos los demas paneles */
    private JPanel contentPanel;

    /** Almacena el panel de deslizamiento  */
    private JScrollPane readFieldPane;

    /** Alamacena el area de texto para mostrar todos los mensajes recibidos */
    private JTextArea readField;

    /** Almacena los mensajes que van a ser enviados */
    private JTextField writeField;

    /**
     * Muesta y lanza la aplicacion cliente
     */
    public static void main(String[] args)
    {
        new Thread(new Client()).start();
    }

    /**
     * Inicializa todos los elementos de la aplicacion(JFrame)
     */
    public Client()
    {
        // initialize and setup content panel
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());

        // initialize and setup incoming message text area
        readField = new JTextArea();
        readField.setEditable(false);
        readField.setLineWrap(true);
        readField.setWrapStyleWord(true);
        readFieldPane = new JScrollPane(readField);
        readFieldPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        readFieldPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // initialize and setup outgoing message text field
        writeField = new JTextField();
        writeField.addActionListener(this);
        writeField.requestFocus();

        // add components to content panel
        contentPanel.add(readFieldPane, BorderLayout.CENTER);
        contentPanel.add(writeField, BorderLayout.SOUTH);

        // add components to frame
        getContentPane().add(contentPanel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(400, 310));
        setVisible(true);
    }

    /**
     * Envia el mensaje cuando pulsas intro.
     *
     */
    public void actionPerformed(ActionEvent evt)
    {
        try
        {
            if (!writeField.getText().isEmpty())
            {
                outgoing.writeUTF(writeField.getText());
                writeField.setText("");
                outgoing.flush();
            }
        }
        catch (Exception e)
        {
            System.out.println("Problema al enviar mensaje...");
        }
    }

    /**
     * Actualiza los mensajes leidos y actualiza el gui
     */
    public void repaint()
    {
        readField.setText("");
        synchronized (messages)
        {
            ListIterator<String> itr = messages.listIterator();

            while(itr.hasNext())
                readField.setText(readField.getText() + itr.next() + "\n");
        }
        readField.setCaretPosition(readField.getText().length()-1);
        super.repaint();
    }

    /**
     * Lanza el chat
     */
    public void run()
    {
        // Conecta con el servidor
        try
        {
            messages = new LinkedList<String>();
            socket = new Socket("localhost", 5000);
            incoming = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            outgoing = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            messages.add("Conectado al servidor!");
            repaint();
        }
        catch (Exception e)
        {
            messages.add("No has podido conectarte al server!");
            repaint();
            return;
        }

        // Escucha los mensajes recibidos hasta que cierra el servidor
        try
        {
            while (socket.isConnected() && !socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown())
            {
                messages.add(incoming.readUTF());
                repaint();
            }
        }
        catch (Exception e) {}

        // disconnected from server
        messages.add("Desconectado del servidor!");
        repaint();
    }
}
