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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mensajes.*;

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
        Map<Short, List<Servicio>> servicios = new HashMap<>();

        try {
            ServerSocket socket = new ServerSocket(puerto);

            while (true) {
                // Espero a recibir una nueva petición del cliente.
                Socket userSocket = socket.accept();

                // Para cada cliente nuevo, creo una nueva hebra que tendrá
                // el socket para realizar la comunicación y los servicios
                Servicio serv = new Servicio(userSocket, servicios);
                serv.start();
            }

        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private static class Servicio extends Thread {

        private final Socket socket;
        private InputStream inStream;
        private OutputStream outStream;
        
        private final Map<Short, List<Servicio>> servicios;
        private final Map<TipoMensaje, ProcesaAccion> accciones = creaAcciones();
        private final Map<Short, Short> usuariosPermitidos = creaUsuarios();
        
        private final static short MaxUsers = 3;
        private short userId = -1;
        private short mapaId = -1;

        public Servicio(final Socket socket, 
                final Map<Short, List<Servicio>> servicios) {
            this.socket = socket;
            this.servicios = servicios;
            
            try {
                this.inStream  = socket.getInputStream();
                this.outStream = socket.getOutputStream();
            } catch (IOException ex) {
            }
        }

        @Override
        public void run() {
            // Mientras haya conexión
            while (!this.socket.isClosed()) {
                // Obtiene una petición del cliente. Depende del estado en el
                // que se encuentre el servidor.
                Mensaje mensaje = null;
                try {
                    mensaje = Mensaje.FromStream(this.inStream);
                } catch (MessageFormatException ex) {
                    // Vale algo ha fallado por aquí...
                    System.err.println(ex.getMessage());
                }
                
                // Si no ha habido fallo
                if (mensaje != null)
                    this.procesaMensaje(mensaje);
            }

            // Eliminamos este servicio cuando haya desconexión
            if (this.mapaId != -1)
                this.servicios.remove(this.mapaId);

            // Cierra la conexión
            try {
                this.socket.close();
            } catch (IOException ex) {
                System.err.println("ERROR: " + ex.getMessage());
            }
        }

        public void enviaActualizacion(Actualizacion mensaje) {
            mensaje.write(this.outStream);
        }
        
        private void procesaMensaje(Mensaje mensaje) {
            // DEBUG INFO
            if (this.userId != -1)
                System.out.printf("[%x4][%s] ", this.userId, mensaje.getTipo().name());

            // Según el tipo de mensaje realiza una acción u otra.
            if (this.accciones.containsKey(mensaje.getTipo())) {
                this.accciones.get(mensaje.getTipo()).procesa(mensaje);
            } else {
                System.out.println();
            }
        }

        private Map<Short, Short> creaUsuarios() {
            Map<Short, Short> usuarios = new HashMap<>();
            usuarios.put((short)0x1F1F, (short)0x5656); // TODO:
            usuarios.put((short)0x3636, (short)0x5C5C); // TODO:
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
            
            if (this.usuariosPermitidos.get(usId) != passwd) {
                Mensaje incorrecto = new RegistroIncorrecto(mensaje.getNumSecuencia());
                incorrecto.write(this.outStream);
            } else {
                this.userId = usId;
                this.mapaId = this.setMapId();
                Mensaje correcto = new RegistroCorrecto(
                        mensaje.getNumSecuencia(),
                        mapaId,
                        (byte)this.servicios.get(this.mapaId).size()
                );
                correcto.write(this.outStream);
            }
            
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
                    (short)0x00 // TODO:
            );
            confirmacion.write(this.outStream);
        }
    }
}
