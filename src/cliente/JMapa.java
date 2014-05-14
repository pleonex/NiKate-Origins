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

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author Benito Palacios Sánchez
 */
public class JMapa extends JPanel {
    private static final int CeldasPorFila = 10;
    private static final int TamanioCelda  = 40;
    
    private Personaje[] personajes;
    
    public JMapa() {
        this.setSize(100, 100);
        this.setBackground(Color.red);
        this.personajes = new Personaje[] {
            new Personaje((short)0, TipoPersonaje.DIOS, (byte)10, (byte)10, (byte)10, (short)0, (short)0)
        };
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // TODO: pintar mapa
        
        for (Personaje p : this.personajes)
            this.paintPersonaje(p, g);
    }
    
    private void paintPersonaje(Personaje p, Graphics g) {
        int pos = p.getPosicion();
        
        int x = (pos % CeldasPorFila) * TamanioCelda;
        int y = (pos / CeldasPorFila) * TamanioCelda;
        
        g.drawImage(p.getImage(), x, y, this);
        g.drawString("ID: " + p.getId(), x, y - 5);
    }
}
