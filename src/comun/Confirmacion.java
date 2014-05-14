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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package comun;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Representa el mensaje de tipo CONFIRMACION.
 * 
 * @author Benito Palacios Sánchez
 */
public class Confirmacion extends Mensaje {
    private short msgHash;
    
    /**
     * Crea una nueva instancia del mensaje a partir de sus parámetros.
     * 
     * @param numSec Número de secuencia.
     * @param msgHash Hash (CRC16) del mensaje de actualización.
     */
    public Confirmacion(final short numSec, final short msgHash) {
        super(TipoMensaje.CONFIRMACION, numSec);
        
        this.msgHash = msgHash;
    }
    
    /**
     * Crea una nueva instancia del mensaje a partir de un flujo de entrada.
     * 
     * @param numSec Número de secuencia.
     * @param inStream Flujo de entrada.
     */
    public Confirmacion(final short numSec, final InputStream inStream) {
        super(TipoMensaje.CONFIRMACION, numSec);
        
        DataInputStream reader = new DataInputStream(inStream);
        try {
            this.msgHash = reader.readShort();
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
        }
    }

    /**
     * Obtiene el hash (CRC16) del mensaje de actualización correspondiente.
     * 
     * @return Hash del mensaje de actualización.
     */
    public short getMsgHash() {
        return msgHash;
    }
    
    @Override
    protected void writeData(final OutputStream outStream) {
        DataOutputStream writer = new DataOutputStream(outStream);
        try {
            writer.writeShort(this.msgHash);
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
        }
    }
}
