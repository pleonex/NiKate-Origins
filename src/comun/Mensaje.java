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

package comun;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Mensaje para envío y recepción.
 * Es la clase abstracta que todos los mensajes tienen que heredar.
 * 
 * @version 1.0
 * @author  Benito Palacios Sánchez
 */
public abstract class Mensaje {
    private final TipoMensaje tipo;
    private final short numSec;
    
    /**
     * Crea una nueva instancia de mensaje.
     * 
     * @param tipo Tipo del mensaje.
     * @param numSec Número de secuencia del mensaje.
     */
    protected Mensaje(final TipoMensaje tipo, final short numSec) {
        this.tipo   = tipo;
        this.numSec = numSec;
    }
    
    /**
     * Tamaño máximo entre los mensajes implementados.
     * 
     * @return Tamaño en bytes.
     */
    public static int GetMaxMsgSize() {
        return 0x0A;
    }
    
    /**
     * Crea un nuevo mensaje del tipo correspondiente a partir de un flujo
     * de datos de entrada.
     * 
     * Para el caso de UDP se podría hacer simplemente con el vector de bytes,
     * pero para el caso de TCP esta forma proporciona un método mejor pues
     * usando la stream no hay que primero guardar en un buffer los datos y 
     * estar rellenándolo hasta tener el tamaño del mensaje, si no que se lee
     * conforme se necesita.
     * 
     * @param inStream Flujo de datos de entrada.
     * @return Mensaje que representa esos datos.
     * @throws MessageFormatException 
     */
    public static Mensaje FromStream(final InputStream inStream) 
            throws MessageFormatException {        
        DataInputStream reader = new DataInputStream(inStream);
        Mensaje msg = null;
        
        try {
            // Lee la cabecera
            short header = reader.readShort();
            TipoMensaje tipo = TipoMensaje.fromId(header >> 12);
            short numSec = (short)(header & 0x0FFF);

            // Crea la instancia correspondiente del mensaje
            switch (tipo) {
                case REGISTRO_SOLICITUD:
                    msg = new RegistroSolicitud(numSec, inStream);
                    break;

                case REGISTRO_CORRECTO:
                    msg = new RegistroCorrecto(numSec, inStream);
                    break;
                    
                case REGISTRO_INCORRECTO:
                    msg = new RegistroIncorrecto(numSec, inStream);
                    break;
                    
                case ACTUALIZACION:
                    msg = new Actualizacion(numSec, inStream);
                    break;
                    
                case CONFIRMACION:
                    msg = new Confirmacion(numSec, inStream);
                    break;
                    
                default:
                    throw new MessageFormatException(tipo, "");
            }
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
            throw new MessageFormatException(ex.getMessage(), ex);
        }

        return msg;
    }
    
    /**
     * Obtiene el tipo del mensaje.
     * 
     * @return Tipo de mensaje.
     */
    public TipoMensaje getTipo() {
        return this.tipo;
    }
    
    /**
     * Obtiene el número de secuencia del mensaje.
     * 
     * @return Número de secuencia del mensaje.
     */
    public short getNumSecuencia() {
        return this.numSec;
    }
    
    /**
     * Escribe el mensaje en un vector de bytes.
     * 
     * @return Vector de bytes con los datos del mensaje.
     */
    public byte[] write() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        this.write(outStream);
        return outStream.toByteArray();
    }
    
    /**
     * Escribe el mensaje en un flujo de datos.
     * 
     * Este método es realmente útil cuando se trabaja con socket TCP.
     * 
     * @param outStream Flujo de datos de salida.
     */
    public void write(final OutputStream outStream) {
        DataOutputStream writer = new DataOutputStream(outStream);
        
        try {
            // Escribe la cabecera
            writer.writeShort((this.tipo.getId() << 12) | this.numSec);
            
            // Escribe el cuerpo del mensaje
            this.writeData(outStream);
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
        }
    }
    
    /**
     * Método sobreescrito en el que se escriben los datos del mensaje
     * específicos a cada tipo de mensaje.
     * 
     * @param outStream Flujo de datos de salida.
     */
    protected abstract void writeData(final OutputStream outStream);
}