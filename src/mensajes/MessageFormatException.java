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

/**
 *
 * @author Benito Palacios Sánchez
 */
public class MessageFormatException extends Exception { 
    private final TipoMensaje tipo;
    private final String details;
    
    public MessageFormatException(TipoMensaje tipo, String details) {
        this.tipo    = tipo;
        this.details = details;
    }
    
    public MessageFormatException(String details, Throwable cause) {
        super(details, cause);
        this.tipo    = TipoMensaje.DESCONOCIDO;
        this.details = details;
    }
    
    @Override
    public String getMessage() {
        return String.format("Invalid message format.\nType: %s\nCause: %s",
                this.tipo.name(), this.details);
    }
}
