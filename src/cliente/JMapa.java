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
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

/**
 *
 * @author Benito Palacios Sánchez
 */
public class JMapa extends JPanel {
    private static final int CeldasPorFila = 10;
    private static final int TamanioCelda  = 40;
    private final String MapaPath = System.getProperty("user.dir") + "/res/mapa";
    
    private final short mapaId;
    private Image mapaImg;
    
    private final Personaje principal;
    private final Map<Short, Personaje> personajes;
    
    public JMapa(Personaje principal, short mapaId) {
        int size = CeldasPorFila * TamanioCelda;
        this.setSize(size, size);
        this.setBackground(Color.red);
        
        this.mapaId = mapaId;
        this.loadMapa();
        
        this.principal  = principal;
        this.personajes = new HashMap<>();
        this.personajes.put(principal.getId(), principal);
    }
    
    private void loadMapa() {
        String path = MapaPath + this.mapaId + ".png";
        if (new File(path).exists())
            this.mapaImg = Toolkit.getDefaultToolkit().createImage(path);
        else
            this.mapaImg = null;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (this.mapaImg != null)
            g.drawImage(this.mapaImg, 0, 0, this);
        
        for (Personaje p : this.personajes.values())
            this.paintPersonaje(p, g);
    }
    
    private void paintPersonaje(Personaje p, Graphics g) {
        int pos = p.getPosicion();
        
        int x = (pos % CeldasPorFila) * TamanioCelda;
        int y = (pos / CeldasPorFila) * TamanioCelda;
        
        g.drawImage(p.getImage(), x, y, this);
        g.drawString("ID: " + p.getId(), x + 2, y - 10);
        g.drawString(p.getVida() + "/" + p.getSalud(), x, y + 2);
    }
}
