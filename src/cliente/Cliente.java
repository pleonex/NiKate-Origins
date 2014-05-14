/*
 * Copyright (C) 2014 Benito Palacios Sánchez
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package cliente;

import comun.Crc16;
import comun.Mensaje;
import comun.MessageFormatException;
import comun.RegistroCorrecto;
import comun.RegistroSolicitud;
import comun.TipoMensaje;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Cliente del videojuego.
 * 
 * @author Benito Palacios Sánchez
 */
public class Cliente {
    // Variables de socket
    private final DatagramSocket socket;
    private final SocketAddress addr;

    // Hebra donde se esperará continuamente nuevos paquetes.
    private Thread mainThread;
    
    // Cola para implementar el defaultListener, se irán acumulando los mensajes
    // recibidos.
    private final Queue<Mensaje> msgRecibidos = new ArrayDeque<>();
    
    // Se ejecutará al recibir mensajes de todo tipo excepto ACTUALIZACION. 
    private MensajeListener defaultListener;
    
    // Se ejecutará al recibir mensajes de tipo ACTUALIZACION.
    // Si el método es null se ejecutará el defaultListener.
    private MensajeListener updateListener;

    /**
     * Crea una nueva instancia con el socket a usar.
     * 
     * @param socket Socket UDP para comunicación con servidor.
     * @param addr Dirección del servidor.
     */
    public Cliente(final DatagramSocket socket, final SocketAddress addr) {
        this.socket = socket;
        this.addr   = addr;
        this.defaultListener = new DefaultListenerImpl();
    }

    /**
     * Establece el listener por defecto para todos los mensajes.
     * En caso de que el listener de actualización sea null, también se recibirán
     * esos mensajes aquí.
     * 
     * @param value Nuevo listener.
     */
    public void setDefaultListener(final MensajeListener value) {
        this.defaultListener = value;
    }

    /**
     * Establece el listener para los mensajes de actualización.
     * En caso de ser null, se enviará hacia el defaultListener.
     * 
     * @param value Nuevo listener.
     */
    public void setUpdateListener(final MensajeListener value) {
        this.updateListener = value;
    }

    /**
     * Interrumpe la hebra que recibe mensajes del servidor.     * 
     */
    public void parar() {
        if (this.mainThread != null && this.mainThread.isAlive()) {
            this.mainThread.interrupt();
        }
    }

    /**
     * Comienza la hebra para recibir mensajes del servidor.     * 
     */
    public void comenzar() {
        this.mainThread = new Thread() {
            @Override
            public void run() {
                // Obtiene el siguiente mensaje de manera indefinida.
                while (!socket.isClosed())
                    siguienteMensaje();
            }
        };
        this.mainThread.start();
    }

    /**
     * Recibe un mensaje del servidor.     * 
     */
    private void siguienteMensaje() {
        // Primero recibe el paquete
        byte[] buffer = new byte[Mensaje.GetMaxMsgSize()];
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(paquete);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }

        // Comprueba que sea para nosotros
        if (!paquete.getSocketAddress().equals(this.addr))
            return;

        // Obtiene el mensaje asociado a esos datos.
        Mensaje mensaje = null;
        try {
            ByteArrayInputStream inStream = new ByteArrayInputStream(paquete.getData());
            mensaje = Mensaje.FromStream(inStream);
        } catch (MessageFormatException ex) {
            // Vale algo ha fallado por aquí...
            System.err.println(ex.getMessage());
        }

        // Comprueba que no haya habido errores.
        if (mensaje == null)
            return;

        // Finalmente lo envía al listener adecuado
        switch (mensaje.getTipo()) {
            // Respuestas normales para el por defecto, general.
            case REGISTRO_CORRECTO:
            case REGISTRO_INCORRECTO:
            case CONFIRMACION:
                this.defaultListener.mensajeRecibido(mensaje);
                break;

            // Respuesta de actualización.
            case ACTUALIZACION:
                if (this.updateListener != null)
                    this.updateListener.mensajeRecibido(mensaje);
                else
                    this.defaultListener.mensajeRecibido(mensaje);
                break;
        }
    }

    /**
     * Envía un mensaje hacia el servidor.
     * 
     * @param mensaje Mensaje a enviar.
     */
    public void envia(final Mensaje mensaje) {
        try {
            // Lo convierte a un vector de bytes.
            byte[] data = mensaje.write();
            
            // Crea el paquete con la dirección del servidor.
            DatagramPacket paquete = new DatagramPacket(
                    data,
                    data.length,
                    this.addr
            );
            
            // Lo envía por el socket.
            this.socket.send(paquete);
        } catch (IOException ex) {
            System.err.println("ERROR: " + ex.getMessage());
        }
    }

    /**
     * Bloquea la ejecución del programa hasta que se recibe un mensaje del
     * listener por defecto.
     *  
     * @return Mensaje recibido o null si ha habido error.
     */
    public Mensaje recibeBloqueante() {
        try {
            // Esta no es la mejor forma de hacerlo... habría que usar sólo
            // los listener, pero en algunos sitios simplifica la implementación.
            while (msgRecibidos.isEmpty())
                Thread.sleep(100);
            
            return msgRecibidos.poll();
        } catch (InterruptedException ex) {
        }
        
        return null;
    }

    /**
     * Inicia sesión en el servidor enviando un mensaje de REGISTRO_SOLICITUD.
     * 
     * @param userId ID del usuario.
     * @param password Contraseña en texto plano.
     * @return Mapa asignado por el servidor o -1 si no ha sido con éxito.
     */
    public short iniciaSesion(String userId, String password) {
        short hexId = Short.parseShort(userId, 16);
        short hashPass = Crc16.calculate(password.getBytes());
        
        // Envía la solicitud
        RegistroSolicitud solicitud = new RegistroSolicitud(
                (short)0,
                hexId,
                hashPass
        );
        this.envia(solicitud);
        
        // Espera la respuesta
        Mensaje respuesta = this.recibeBloqueante();
        
        // La procesa
        if (respuesta.getTipo() == TipoMensaje.REGISTRO_CORRECTO)
            return ((RegistroCorrecto)respuesta).getMapaId();
        else
            return -1;
    }
    
    /**
     * Implementación básica del listener por defecto.
     * Guarda los mensajes recibidos en una cola.
     */
    private class DefaultListenerImpl implements MensajeListener {

        @Override
        public void mensajeRecibido(Mensaje mensaje) {
            msgRecibidos.offer(mensaje);
        }

    }
}
