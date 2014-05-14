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

/**
 *
 * @author Benito Palacios Sánchez
 */
public class Personaje {
    private final TipoPersonaje tipo;
    private short id;
    
    private byte vida;
    private final byte salud;
    private byte exp;
    
    private short posicion;
    private final short mapaId;

    public Personaje(short id, TipoPersonaje tipo, byte vida, byte salud,
            byte exp, short posicion, short mapaId) {
        this.id    = id;
        this.tipo  = tipo;
        this.vida  = vida;
        this.salud = salud;
        this.exp   = exp;
        
        this.posicion = posicion;
        this.mapaId   = mapaId;
    }

    public static int getAtaque() {
        return 0x04;
    }
    
    public short getId() {
        return this.id;
    }
    
    public TipoPersonaje getTipo() {
        return tipo;
    }

    public byte getVida() {
        return vida;
    }

    public byte getSalud() {
        return salud;
    }

    public byte getExp() {
        return exp;
    }

    public short getPosicion() {
        return posicion;
    }

    public short getMapaId() {
        return mapaId;
    }

    public void setVida(byte vida) {
        this.vida = vida;
    }

    public void setExp(byte exp) {
        this.exp = exp;
    }

    public void setPosicion(short posicion) {
        this.posicion = posicion;
    }
    
    public Image getImage() {
        return this.tipo.getImage();
    }
}

