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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Representa un mensaje de REGISTRO_INCORRECTO.
 * 
 * @author Benito Palacios Sánchez
 */
public class RegistroIncorrecto extends Mensaje {

    /**
     * Crea una nueva instancia a partir de sus parámetros.
     * 
     * @param numSec Número de secuencia.
     */
    public RegistroIncorrecto(final short numSec) {
        super(TipoMensaje.REGISTRO_INCORRECTO, numSec);
    }
    
    /**
     * Crea una nueva instancia a partir de un flujo de entrada.
     * Este mensaje no tiene ningún parámetro y por tanto no se lee nada
     * del flujo de entrada. Este constructor está para hacerlo similar a otras
     * clases de mensajes.
     * 
     * @param numSec Número de secuencia.
     * @param inStream Flujo de entrada.
     */
    public RegistroIncorrecto(final short numSec, final InputStream inStream) {
        super(TipoMensaje.REGISTRO_INCORRECTO, numSec);
    }
    
    @Override
    protected void writeData(final OutputStream outStream) {
    }
    
}
