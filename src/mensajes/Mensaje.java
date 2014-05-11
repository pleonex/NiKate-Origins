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

package mensajes;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @version 1.0
 * @author  Benito Palacios Sánchez
 */
public abstract class Mensaje {
    private final TipoMensaje tipo;
    private final short numSec;
    
    public Mensaje(final TipoMensaje tipo, final short numSec) {
        this.tipo   = tipo;
        this.numSec = numSec;
    }
    
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
                    
                default:
                    throw new MessageFormatException(tipo, "");
            }
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
        }

        return msg;
    }
    
    public TipoMensaje getTipo() {
        return this.tipo;
    }
    
    public short getNumSecuencia() {
        return this.numSec;
    }
    
    public byte[] write() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        this.write(outStream);
        return outStream.toByteArray();
    }
    
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
    
    protected abstract void writeData(final OutputStream outStream);
}