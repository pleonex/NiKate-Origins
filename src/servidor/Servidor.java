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
package servidor;

import comun.Actualizacion;
import comun.Confirmacion;
import comun.Crc16;
import comun.Mensaje;
import comun.MessageFormatException;
import comun.RegistroCorrecto;
import comun.RegistroIncorrecto;
import comun.RegistroSolicitud;
import comun.TipoMensaje;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @verion 1.0
 * @author Benito Palacios Sánchez
 */
public class Servidor {    
    /**
     * @param args Argumentos pasados por la línea de comandos
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Argumentos inválidos.");
            System.out.println();
            System.out.println("USO: Servidor puerto");
            return;
        }

        int puerto = Integer.parseInt(args[0]);
        System.out.println("Iniciando servicio en puerto " + puerto);
        iniciarServicio(puerto);
    }

    private static void iniciarServicio(final int puerto) {
        // Diccionario para controlar de quién es cada paquete
        Map<SocketAddress, Servicio> addrServicio = new HashMap<>();
        
        // Diccionario para que los servicios se encuentren entre sí.
        Map<Short, List<Servicio>> mapServicio = new HashMap<>();
        
        try {
            DatagramSocket socket = new DatagramSocket(puerto);

            while (true) {
                // Recibo un nuevo paquete
                byte[] buffer = new byte[Mensaje.GetMaxMsgSize()];
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                socket.receive(paquete);

                // Compruebo si el servicio al que le corresponde esta dirección
                // existía de antes o no. En ese caso lo creo
                SocketAddress addr = paquete.getSocketAddress();
                if (!addrServicio.containsKey(addr)) {
                    Servicio serv = new Servicio(socket, addr, mapServicio);
                    addrServicio.put(addr, serv);
                }
                 
                // Le envío el paquete al servicio
                addrServicio.get(addr).recibe(paquete);
            }

        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private static class Servicio {

        private final DatagramSocket socket;
        private final SocketAddress address;
        
        private final Map<Short, List<Servicio>> servicios;
        private final Map<TipoMensaje, ProcesaAccion> accciones = creaAcciones();
        private final Map<Short, Short> usuariosPermitidos = creaUsuarios();
        
        private final static short MaxUsers = 3;
        private short userId = -1;
        private short mapaId = -1;

        public Servicio(final DatagramSocket socket, final SocketAddress address, 
                final Map<Short, List<Servicio>> servicios) {
            this.socket  = socket;
            this.address = address;
            this.servicios = servicios;
        }

        public void recibe(final DatagramPacket paquete) {
            // Nunca viene mal recomprobarlo...
            if (!paquete.getSocketAddress().equals(this.address))
                return;

            // Obtiene una petición del cliente.
            Mensaje mensaje = null;
            try {
                ByteArrayInputStream inStream = new ByteArrayInputStream(paquete.getData());
                mensaje = Mensaje.FromStream(inStream);
            } catch (MessageFormatException ex) {
                // Vale algo ha fallado por aquí...
                System.err.println(ex.getMessage());
            }

            // Si no ha habido fallo
            if (mensaje != null)
                this.procesaMensaje(mensaje);
        }

        public void enviaActualizacion(Actualizacion mensaje) {
            this.envia(mensaje);
        }
        
        private void envia(Mensaje mensaje) {
            try {
                byte[] data = mensaje.write();
                DatagramPacket paquete = new DatagramPacket(
                        data,
                        data.length,
                        this.address
                );
                this.socket.send(paquete);
            } catch (IOException ex) {
                System.err.println("ERROR: " + ex.getMessage());
            }            
        }
        
        private void procesaMensaje(Mensaje mensaje) {
            // DEBUG INFO
            if (this.userId != -1)
                System.out.printf("[%x][%s] ", this.userId, mensaje.getTipo().name());

            // Según el tipo de mensaje realiza una acción u otra.
            if (this.accciones.containsKey(mensaje.getTipo())) {
                this.accciones.get(mensaje.getTipo()).procesa(mensaje);
            } else {
                System.out.println();
            }
        }

        private Map<Short, Short> creaUsuarios() {
            Map<Short, Short> usuarios = new HashMap<>();
            usuarios.put((short)0x1F1F, (short)0x2EFE); // PASSWD: patatas
            //usuarios.put((short)0x3636, (short)0xBDC1); // PASSWD: password
            return usuarios;
        }

        private interface ProcesaAccion {
            void procesa(Mensaje mensaje);
        }

        private Map<TipoMensaje, ProcesaAccion> creaAcciones() {
            Map<TipoMensaje, ProcesaAccion> acciones = new HashMap<>();

            // El cliente se va a identificar
            acciones.put(
                    TipoMensaje.REGISTRO_SOLICITUD,
                    new ProcesaAccion() {
                        @Override
                        public void procesa(Mensaje mensaje) {
                            procesaSolicitudRegistro((RegistroSolicitud)mensaje);
                        }
                    }
            );

            // Vamos a añadir, modificar o eliminar un contacto
            acciones.put(
                    TipoMensaje.ACTUALIZACION,
                    new ProcesaAccion() {
                        @Override
                        public void procesa(Mensaje mensaje) {
                            procesaActualizacion((Actualizacion)mensaje);
                        }
                    }
            );

            return acciones;
        }

        private void procesaSolicitudRegistro(RegistroSolicitud mensaje) {
            short usId = mensaje.getUsuarioId();
            short passwd = mensaje.getUsuarioPassword();
            
            System.out.printf("Registro: 0x%X 0x%X\n", usId, passwd);
            
            Mensaje respuesta;
            if (this.usuariosPermitidos.get(usId) != passwd) {
                respuesta = new RegistroIncorrecto(mensaje.getNumSecuencia());
            } else {
                this.userId = usId;
                this.mapaId = this.setMapId();
                respuesta = new RegistroCorrecto(
                        mensaje.getNumSecuencia(),
                        mapaId,
                        (byte)this.servicios.get(this.mapaId).size()
                );
            }
            
            this.envia(respuesta);
        }

        private short setMapId() {
            short mapId = -1;
            
            for (short key : this.servicios.keySet())
                if (this.servicios.get(key).size() < MaxUsers)
                    mapId = key;
            
            if (mapId == -1) {
                mapId = (short)this.servicios.size();
                this.servicios.put(mapId, new ArrayList<Servicio>());
            }
            
            this.servicios.get(mapId).add(this);
            return mapId;
        }
        
        private void procesaActualizacion(Actualizacion mensaje) {
            // Pero si no te has autenticado -.-'
            if (this.userId == -1)
                return;
            
            // TODO: Antes de enviar si no es de la misma ID comprobar
            // que se ha podido atacar porque estaba cerca.
            
            // Envía la actualización a los otros
            for (Servicio serv : this.servicios.get(this.mapaId))
                if (serv != this)
                    serv.enviaActualizacion(mensaje);
            
            // Envía mensaje de confirmación
            Mensaje confirmacion = new Confirmacion(
                    mensaje.getNumSecuencia(),
                    Crc16.calculate(mensaje.write())
            );
            this.envia(confirmacion);
        }
    }
}
