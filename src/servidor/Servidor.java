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
 * Clase principal del servidor.
 *
 * @verion 1.0
 * @author Benito Palacios Sánchez
 */
public class Servidor {    
    /**
     * Inicia el servidor.
     * 
     * @param args Sólo se necesita un argumento, el número del puerto.
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

    /**
     * Inicia el servidor.
     * Crea un socket UDP y comienza a recibir peticiones.
     * 
     * @param puerto 
     */
    private static void iniciarServicio(final int puerto) {
        // Diccionario para controlar de quién es cada paquete.
        // Este método se encargar de añadirlos cuando se crea un servicio.
        Map<SocketAddress, Servicio> addrServicio = new HashMap<>();
        
        // Diccionario para que los servicios se encuentren entre sí.
        // Los servicios se añaden ellos sólo y se borran de la lista
        // cuando un jugador se loguea y cuando pierde.
        Map<Short, List<Servicio>> mapServicio = new HashMap<>();
        
        try {
            // Crea un socket UDP y comienza a escuchar en el puerto indicado
            DatagramSocket socket = new DatagramSocket(puerto);

            // Recibe y procesa paquetes de forma indefinida.
            while (true) {
                // Recibo un nuevo paquete.
                // Para ello creo un buffer del tamaño máximo que se podrían
                // recibir datos y lo mando para que lo rellene.
                byte[] buffer = new byte[Mensaje.GetMaxMsgSize()];
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                socket.receive(paquete);

                // Proceso el paquete.
                // Compruebo si el servicio al que le corresponde esta dirección
                // existía de antes o no. En ese caso lo creo
                SocketAddress addr = paquete.getSocketAddress();
                if (!addrServicio.containsKey(addr)) {
                    Servicio serv = new Servicio(socket, addr, mapServicio);
                    addrServicio.put(addr, serv);
                }
                 
                // Le envío el paquete al servicio
                // Él se encargará de convertir a mensaje y procesarlo.
                addrServicio.get(addr).recibe(paquete);
            }

        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Clase interna con la funcionalidad del servidor.
     * Esta clase se encarga de recibir paquetes y procesarlos.
     * Se crea una instancia de la clase por cada cliente.
     */
    private static class Servicio {
        // Variables de los sockets
        private final DatagramSocket socket;
        private final SocketAddress address;
        
        // Lista de servicios activos clasificados por el mapa en el que actúan.
        private final Map<Short, List<Servicio>> servicios;
        
        // Diccionario de métodos para procesar los mensajes recibidos.
        private final Map<TipoMensaje, ProcesaAccion> accciones = creaAcciones();
        
        // Diccionario con los usuarios y contraseñas (CRC16) admitidos.
        private final Map<Short, Short> usuariosPermitidos = creaUsuarios();
        
        // Número máximo de usuarios por mapa
        private final static short MaxUsers = 8;
        
        // Variables que identifican al usuario
        private short userId = -1;
        private short mapaId = -1;
        
        // Guarda el último mensaje de actualización recibido para que cuando
        // otro cliente se conecte al juego puede ser retransmitido y se pueda
        // conocer la posición de este usuario.
        private Actualizacion lastUpdate;
        
        // Indica si se ha recibido o no un mensaje de actualización todavía.
        // En el primer mensaje de actualización se pedirá a los otros jugadores
        // que retransmitan su último mensaje de actualización para saber su
        // posición.
        private boolean primeraActual = true;

        /**
         * Crea una nueva instancia del servicio.
         * 
         * @param socket Socket UDP para comunicación
         * @param address Dirección del cliente.
         * @param servicios Diccionario con lista de servicios actuales por mapa.
         */
        public Servicio(final DatagramSocket socket, final SocketAddress address, 
                final Map<Short, List<Servicio>> servicios) {
            this.socket  = socket;
            this.address = address;
            this.servicios = servicios;
        }
        
        /**
         * Obtiene el último mensaje de actualización.
         * 
         * @return Mensaje de actualización.
         */
        public Actualizacion getUltimaActual() {
            return this.lastUpdate;
        }
        
        /**
         * Obtiene el ID del usuario.
         * Este valor será -1 cuando todavía no se haya registrado.
         * 
         * @return ID del usuario.
         */
        public short getUserId() {
            return this.userId;
        }
        
        /**
         * Recibe un paquete y lo procesa.
         * Este método se encargará de convertirlo a mensaje correspondiente
         * y llamar a un método para que lo procese.
         * 
         * @param paquete Paquete recibido del cliente.
         */
        public void recibe(final DatagramPacket paquete) {
            // Nunca viene mal recomprobarlo...
            if (!paquete.getSocketAddress().equals(this.address))
                return;

            // Obtiene una petición del cliente.
            // Obtiene el mensaje contenido en los datos.
            Mensaje mensaje = null;
            try {
                ByteArrayInputStream inStream = new ByteArrayInputStream(paquete.getData());
                mensaje = Mensaje.FromStream(inStream);
            } catch (MessageFormatException ex) {
                // Vale algo ha fallado por aquí...
                System.err.println(ex.getMessage());
            }

            // Si no ha habido errores, procesamos el mensaje.
            if (mensaje != null)
                this.procesaMensaje(mensaje);
        }

        /**
         * Envía un mensaje de actualización al cliente.
         * Este método está expuesto al público para que otros Servicios
         * puedan enviar sus mensajes de actualización a nuestro cliente.
         * 
         * @param mensaje Mensaje de actualización.
         */
        public void enviaActualizacion(final Actualizacion mensaje) {
            this.envia(mensaje);
        }
        
        /**
         * Envía un mensaje por el socket UDP.
         * 
         * @param mensaje Mensaje a enviar.
         */
        private void envia(final Mensaje mensaje) {
            try {
                // Obtiene los datos del mensaje
                byte[] data = mensaje.write();
                
                // Crea el paquete añadiéndole la dirección del cliente.
                DatagramPacket paquete = new DatagramPacket(
                        data,
                        data.length,
                        this.address
                );
                
                // Lo envía.
                this.socket.send(paquete);
            } catch (IOException ex) {
                System.err.println("ERROR: " + ex.getMessage());
            }            
        }
        
        /**
         * Procesa el mensaje.
         * 
         * @param mensaje Mensaje a procesar.
         */
        private void procesaMensaje(final Mensaje mensaje) {
            // DEBUG INFO
            if (this.userId != -1)
                System.out.printf("[%x][%s]\n", this.userId, mensaje.getTipo().name());

            // Según el tipo de mensaje realiza una acción u otra.
            if (this.accciones.containsKey(mensaje.getTipo())) {
                this.accciones.get(mensaje.getTipo()).procesa(mensaje);
            } else {
                System.out.println();
            }
        }

        /**
         * Crea el diccionario con los usuarios y contraseñas.
         * Esto sería mejor leerlo desde un fichero externo, en lugar de
         * tener los usuarios hardcoded.
         * 
         * @return 
         */
        private Map<Short, Short> creaUsuarios() {
            Map<Short, Short> usuarios = new HashMap<>();
            usuarios.put((short)0x1F1F, (short)0x2EFE); // PASSWD: patatas
            return usuarios;
        }

        /**
         * Interfaz para métodos que implementar la acción de procesar un mensaje.
         * De esta forma se puede tener como una lista de métodos para cada
         * tipo de mensaje.
         */
        private interface ProcesaAccion {
            void procesa(Mensaje mensaje);
        }

        /**
         * Crea el diccionario con los métodos que procesan los mensajes.
         * 
         * @return Diccionario con métodos para procesar mensaje según el tipo.
         */
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

        /**
         * Procesa un mensaje de solicitud de registro en el servicio.
         * 
         * @param mensaje Mensaje de solicitud de registro.
         */
        private void procesaSolicitudRegistro(final RegistroSolicitud mensaje) {
            short usId   = mensaje.getUsuarioId();
            short passwd = mensaje.getUsuarioPassword();
            System.out.printf("Registro: 0x%X 0x%X\n", usId, passwd);
                        
            Mensaje respuesta;
            if (false) {    // He desactivado la comprobación de usuario.
            //if (this.usuariosPermitidos.get(usId) != passwd) {
                respuesta = new RegistroIncorrecto(mensaje.getNumSecuencia());
            } else {
                // En caso de realizar la comprobación con éxito...
                // actualizo las variables de identificación, asigando un mapa.
                this.userId = usId;
                this.mapaId = this.setMapId(usId); 
                
                // Envía mensaje de éxito.
                respuesta = new RegistroCorrecto(
                        mensaje.getNumSecuencia(),
                        mapaId,
                        (byte)this.servicios.get(this.mapaId).size()
                );
            }
            
            this.envia(respuesta);
        }

        /**
         * Establece el mapa en el que este jugador estará.
         * 
         * @param usId
         * @return 
         */
        private short setMapId(final short usId) {
            short mapId = -1;
            
            // Por cada mapa comprueba el número de jugadores y que este ID
            // no esté siendo usado en ese mapa.
            for (short key : this.servicios.keySet())
                if (this.servicios.get(key).size() < MaxUsers && !checkSpoofing(key, usId))
                    mapId = key;
            
            // Si no hay mapas libres, creamos uno nuevo.
            if (mapId == -1) {
                mapId = (short)this.servicios.size();
                this.servicios.put(mapId, new ArrayList<Servicio>());
            }
            
            // Nos registramos como servicio en dicho mapa.
            this.servicios.get(mapId).add(this);
            return mapId;
        }
        
        /**
         * Comprueba que no hay ningún usuario con el mismo ID en un mapa.
         * 
         * @param key ID del mapa
         * @param usId ID del usuario.
         * @return Verdadero si hay un jugador con el mismo ID, otra cosa falso.
         */
        private boolean checkSpoofing(final short key, final short usId) {
            boolean spoofing = false;
            for (Servicio serv : servicios.get(key))
                if (serv != this && serv.getUserId() == usId)
                    spoofing = true;
            
            return spoofing;
        }
        
        /**
         * Procesa el mensaje de actualización.
         * 
         * @param mensaje Mensaje de actualización.
         */
        private void procesaActualizacion(final Actualizacion mensaje) {
            // Pero si no te has autenticado -.-'
            if (this.userId == -1)
                return;
            
            // TODO: Antes de enviar si no es de la misma ID comprobar
            // que se ha podido atacar porque estaba cerca. Un anticheater.
                
            // Guarda este mensaje como el último recibido por el usuario.
            if (this.userId == mensaje.getUserId())
                this.lastUpdate = mensaje;
            
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
            
            // Si es la primera vez que actualiza, le enviamos la posición
            // del resto de jugadores
            if (primeraActual) {
                primeraActual = false;
                for (Servicio serv : this.servicios.get(this.mapaId))
                    if (serv != this)
                        this.envia(serv.getUltimaActual());
            }
            
            // Comprueba si nos han eliminado
            if (mensaje.getUserId() == this.userId && (
                    mensaje.getVida() == 0 || mensaje.getVida() >= 13)) {
                System.out.println("[" + Integer.toHexString(userId) + "] Eliminandome");
                this.servicios.get(this.mapaId).remove(this);
            }
        }
    }
}
