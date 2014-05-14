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

/**
 * Identifica el tipo de mensaje.
 * 
 * @version 1.0
 * @author  Benito Palacios Sánchez
 */
public enum TipoMensaje {
    DESCONOCIDO(0xFF),
    
    REGISTRO_SOLICITUD(0x00),
    REGISTRO_CORRECTO(0x01),
    REGISTRO_INCORRECTO(0x02),
    
    ACTUALIZACION(0x03),
    CONFIRMACION(0x04);
    
    private final byte id;
    
    TipoMensaje(final int id) {
        this.id = (byte)id;
    }
    
    /**
     * Crea una nueva instancia de tipo a partir de su identificador.
     * 
     * @param id Identificador del mensaje.
     * @return Tipo del mensaje asociado o DESCONICIDO si no encontrado.
     */
    public static TipoMensaje fromId(final int id) {
        TipoMensaje tipo = TipoMensaje.DESCONOCIDO;
        
        for (TipoMensaje valor : TipoMensaje.values())
            if (valor.getId() == id)
                tipo = valor;
        
        return tipo;
    }
    
    /**
     * Obtiene el ID asociado a este tipo.
     * 
     * @return ID asociado.
     */
    public byte getId() {
            return this.id;
    }
}
