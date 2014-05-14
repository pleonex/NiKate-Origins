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
 *
 * @author Benito Palacios Sánchez
 */
public class Cliente {

    private final DatagramSocket socket;
    private final SocketAddress addr;

    private Thread mainThread;
    private final Queue<Mensaje> msgRecibidos = new ArrayDeque<>();
    private MensajeListener defaultListener;
    private MensajeListener updateListener;

    public Cliente(DatagramSocket socket, SocketAddress addr) {
        this.socket = socket;
        this.addr = addr;
        this.defaultListener = new DefaultListenerImpl();
    }

    public void setDefaultListener(MensajeListener value) {
        this.defaultListener = value;
    }

    public void setUpdateListener(MensajeListener value) {
        this.updateListener = value;
    }

    public void parar() {
        if (this.mainThread != null && this.mainThread.isAlive()) {
            this.mainThread.interrupt();
        }
    }

    public void comenzar() {
        this.mainThread = new Thread() {
            @Override
            public void run() {
                while (!socket.isClosed()) {
                    siguienteMensaje();
                }
            }
        };
        this.mainThread.start();
    }

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
        if (!paquete.getSocketAddress().equals(this.addr)) {
            return;
        }

        // Obtiene el mensaje
        Mensaje mensaje = null;
        try {
            ByteArrayInputStream inStream = new ByteArrayInputStream(paquete.getData());
            mensaje = Mensaje.FromStream(inStream);
        } catch (MessageFormatException ex) {
            // Vale algo ha fallado por aquí...
            System.err.println(ex.getMessage());
        }

        if (mensaje == null) {
            return;
        }

        // Finalmente lo envía al listener adecuado
        switch (mensaje.getTipo()) {
            // Respuestas normales para el por defecto, general.
            case REGISTRO_CORRECTO:
            case REGISTRO_INCORRECTO:
            case CONFIRMACION:
                this.defaultListener.mensajeRecibido(mensaje);
                break;

            case ACTUALIZACION:
                if (this.updateListener != null) {
                    this.updateListener.mensajeRecibido(mensaje);
                } else {
                    this.defaultListener.mensajeRecibido(mensaje);
                }
                break;
        }
    }

    public void envia(Mensaje mensaje) {
        try {
            byte[] data = mensaje.write();
            DatagramPacket paquete = new DatagramPacket(
                    data,
                    data.length,
                    this.addr
            );
            this.socket.send(paquete);
        } catch (IOException ex) {
            System.err.println("ERROR: " + ex.getMessage());
        }
    }

    public Mensaje recibeBloqueante() {
        try {
            // Aunque no es lo mejor, para pequeños mensajes al servidor está bien
            while (msgRecibidos.isEmpty()) {
                Thread.sleep(100);
            }
            
            return msgRecibidos.poll();
        } catch (InterruptedException ex) {
        }
        
        return null;
    }

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
    
    private class DefaultListenerImpl implements MensajeListener {

        @Override
        public void mensajeRecibido(Mensaje mensaje) {
            msgRecibidos.offer(mensaje);
        }

    }
}
