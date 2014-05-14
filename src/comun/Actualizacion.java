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
 * Representa un mensaje de tipo ACTUALIZACION.
 * 
 * @author Benito Palacios Sánchez
 */
public class Actualizacion extends Mensaje {

    private short userId;
    private byte clase;
    private byte experiencia;
    private byte salud;
    private byte vida;
    private short posicion;
    private short tiempo;
    
    /**
     * Crea una nueva instancia del mensaje a partir de sus parámetros.
     * 
     * @param numSec Número de secuencia.
     * @param userId ID del usuario.
     * @param clase Tipo / Clase del personaje.
     * @param experiencia Nivel de experiencia del personaje.
     * @param salud Nivel de salud del personaje.
     * @param vida Nivel de vida del personaje.
     * @param posicion Posición del personaje.
     * @param tiempo Tiempo del servidor.
     */
    public Actualizacion(final short numSec, final short userId,
            final byte clase, final byte experiencia, final byte salud,
            final byte vida, final short posicion, final short tiempo) {
        super(TipoMensaje.ACTUALIZACION, numSec);
        
        this.userId = userId;
        this.clase  = clase;
        this.experiencia = experiencia;
        this.salud = salud;
        this.vida  = vida;
        this.posicion = posicion;
        this.tiempo   = tiempo;
    }
    
    /**
     * Crea una nueva instancia del mensaje a partir de un flujo de entrada.
     * 
     * @param numSec Número de secuencia del mensaje.
     * @param inStream Flujo de entrada.
     */
    public Actualizacion(final short numSec, final InputStream inStream) {
        super(TipoMensaje.ACTUALIZACION, numSec);
        
        DataInputStream reader = new DataInputStream(inStream);
        try {
            this.userId = reader.readShort();
            
            short estado = reader.readShort();
            this.clase       = (byte)((estado >> 0x0C) & 0x0F);
            this.experiencia = (byte)((estado >> 0x08) & 0x0F);
            this.salud       = (byte)((estado >> 0x04) & 0x0F);
            this.vida        = (byte)((estado >> 0x00) & 0x0F);
            
            this.posicion = reader.readShort();
            this.tiempo   = reader.readShort();
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
        }
    }

    /**
     * Devuelve el ID del usuario.
     * 
     * @return ID del usuario.
     */
    public short getUserId() {
        return userId;
    }

    /**
     * Devuelve la clase del personaje.
     * 
     * @return CLase del personaje.
     */
    public byte getClase() {
        return clase;
    }

    /**
     * Devuelve la experiencia del personaje.
     * 
     * @return Experiencia del personaje.
     */
    public byte getExperiencia() {
        return experiencia;
    }

    /**
     * Devuelve el nivel de salud del personaje.
     * 
     * @return Nivel de salud.
     */
    public byte getSalud() {
        return salud;
    }

    /**
     * Devuelve el nivel de vida del personaje.
     * 
     * @return Nivel de vida.
     */
    public byte getVida() {
        return vida;
    }
    
    /**
     * Devuelve la posición del personaje.
     * 
     * @return Posición del personaje.
     */
    public short getPosicion() {
        return posicion;
    }
    
    /**
     * Devuelve el tiempo del servidor en el que se envió el mensaje.
     * Esto no está correctamente implementado. En su lugar se envía
     * el tiempo que ha transcurrido desde que se envió el primer mensaje
     * de actualización y el actual.
     * 
     * @return Tiempo del servidor.
     */
    public short getTiempo() {
        return tiempo;
    }
    
    @Override
    protected void writeData(final OutputStream outStream) {
        DataOutputStream writer = new DataOutputStream(outStream);
        try {
            writer.writeShort(this.userId);
            
            short estado = 0;
            estado |= (this.clase       & 0x0F) << 0x0C;
            estado |= (this.experiencia & 0x0F) << 0x08;
            estado |= (this.salud       & 0x0F) << 0x04;
            estado |= (this.vida        & 0x0F) << 0x00;
            
            writer.writeShort(estado);
            writer.writeShort(this.posicion);
            writer.writeShort(this.tiempo);
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
        }
    }
}
