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

package cliente;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

/**
 *
 * @author Benito Palacios Sánchez
 */
public enum TipoPersonaje {
    DESCONOCIDO(0xFF),
    
    DIOS(0x00),
    MAGO(0x01),
    SABIO(0x02),
    ALDEANO(0x03);
    
    private final byte id;
    private final static Image[] Images = LoadImages();
    
    TipoPersonaje(final int id) {
        this.id = (byte)id;
    }
    
    public static TipoPersonaje fromId(int id) {
        TipoPersonaje tipo = TipoPersonaje.DESCONOCIDO;
        
        for (TipoPersonaje valor : TipoPersonaje.values())
            if (valor.getId() == id)
                tipo = valor;
        
        return tipo;
    }
    
    public byte getId() {
            return this.id;
    }    
    
    public Image getImage() {   
        return Images[this.id];
    }
    
    private static Image[] LoadImages() {
        String basePath = System.getProperty("user.dir") + "/res/";

        Image[] images = new Image[TipoPersonaje.values().length];
        for (TipoPersonaje value : TipoPersonaje.values()) {
            String path = basePath + value.name() + ".png";
            if (new File(path).exists())
                images[value.getId()] = Toolkit.getDefaultToolkit().createImage(path);
        }
        
        return images;
    }
}