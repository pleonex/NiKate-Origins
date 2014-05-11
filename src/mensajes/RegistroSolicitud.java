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
public class RegistroSolicitud extends Mensaje {
    private short usuarioId;
    private short usuarioPasswd;
    
    public RegistroSolicitud(final short usuarioId, final short usuarioPasswd) {
        super(TipoMensaje.REGISTRO_SOLICITUD);
        
        this.usuarioId     = usuarioId;
        this.usuarioPasswd = usuarioPasswd;
    }
    
    public RegistroSolicitud(final short numSec, final InputStream inStream) {
        super(TipoMensaje.REGISTRO_SOLICITUD, numSec);
        
        DataInputStream reader = new DataInputStream(inStream);
        try {
            this.usuarioId     = reader.readShort();
            this.usuarioPasswd = reader.readShort();
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
        }
    }
    
    public short getUsuarioId() {
        return this.usuarioId;
    }
    
    public short getUsuarioPassword() {
        return this.usuarioPasswd;
    }
    
    @Override
    protected void writeData(OutputStream outStream) {
        DataOutputStream writer = new DataOutputStream(outStream);
        
        try {
            writer.writeShort(this.usuarioId);
            writer.writeShort(this.usuarioPasswd);
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
        }
    }
}
