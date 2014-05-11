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

package mensajes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Benito Palacios Sánchez
 */
public class RegistroCorrecto extends Mensaje {
    private short mapaId;
    private byte numPersonajes;
    
    public RegistroCorrecto(final short mapaId, final byte numPersonajes) {
        super(TipoMensaje.REGISTRO_CORRECTO);
        
        this.mapaId = mapaId;
        this.numPersonajes = numPersonajes;
    }
    
    public RegistroCorrecto(final short numSec, final InputStream inStream) {
        super(TipoMensaje.REGISTRO_CORRECTO, numSec);
        
        try {
            DataInputStream reader = new DataInputStream(inStream);
            this.mapaId = reader.readShort();
            this.numPersonajes = reader.readByte();
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
        }
    }

    public short getMapaId() {
        return mapaId;
    }

    public byte getNumPersonajes() {
        return numPersonajes;
    }
        
    @Override
    protected void writeData(OutputStream outStream) {
        DataOutputStream writer = new DataOutputStream(outStream);
        
        try {
            writer.writeShort(this.mapaId);
            writer.writeByte(this.numPersonajes);
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
        }
    }
    
}
