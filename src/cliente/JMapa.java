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

import comun.Actualizacion;
import comun.Mensaje;
import comun.TipoMensaje;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

/**
 *
 * @author Benito Palacios Sánchez
 */
public class JMapa extends JPanel implements KeyListener, MouseListener,
        MensajeListener {
    private static final int CeldasPorFila = 10;
    private static final int TamanioCelda  = 40;
    private final String MapaPath = System.getProperty("user.dir") + "/res/mapa";
    private final String SoundMain = System.getProperty("user.dir") + "/res/main_inicio.wav";
    private final String SoundLoop = System.getProperty("user.dir") + "/res/main_loop.wav";
    
    private final Cliente cliente;
    private final short mapaId;
    private Image mapaImg;
    private short numSec = 0;
    private long inicio;
    
    private final Personaje principal;
    private final Map<Short, Personaje> personajes;
    
    public JMapa(Personaje principal, short mapaId, Cliente cliente) {
        Audio.playClipLoop(SoundMain, SoundLoop);
        
        this.inicio = new Date().getTime();
        this.cliente = cliente;
        cliente.setUpdateListener(this);
        this.mapaId = mapaId;
        this.loadMapa();
        
        this.principal  = principal;
        this.personajes = new HashMap<>();
        this.personajes.put(principal.getId(), principal);
        
        int size = CeldasPorFila * TamanioCelda;
        this.setSize(size, size);
        this.setBackground(Color.red);
        addKeyListener(this);
        addMouseListener(this);
        this.enviaActualizacion(this.principal);
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

        // Pinta el fondo
        if (this.mapaImg != null)
            g.drawImage(this.mapaImg, 0, 0, this);
        
        // DEBUG
        // Pinta la rejilla
        for (int i = 0; i < CeldasPorFila; i++) {
            int coord = i * TamanioCelda;
            
            // Vertical
            g.drawLine(coord, 0, coord, CeldasPorFila * TamanioCelda);
            
            // Horizontal
            g.drawLine(0, coord, CeldasPorFila * TamanioCelda, coord);
        }
        
        // Pinta a los personajes
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
    
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        short pos = principal.getPosicion();

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (pos / CeldasPorFila > 1) {
                    principal.setPosicion((short)(pos - CeldasPorFila));
                    repaint();
                    this.enviaActualizacion(this.principal);
                }
                break;

            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if (pos / CeldasPorFila < 10 - 1) {
                    principal.setPosicion((short)(pos + CeldasPorFila));
                    repaint();
                    this.enviaActualizacion(this.principal);
                }
                break;

            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if (pos % CeldasPorFila > 0) {
                    principal.setPosicion((short)(pos - 1));
                    repaint();
                    this.enviaActualizacion(this.principal);
                }
                break;

            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if (pos % CeldasPorFila < CeldasPorFila - 1) {
                    principal.setPosicion((short)(pos + 1));
                    repaint();
                    this.enviaActualizacion(this.principal);
                }
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        requestFocusInWindow();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mensajeRecibido(Mensaje mensaje) {
        Actualizacion actual = (Actualizacion)mensaje;
        short id = actual.getUserId();
        
        Personaje p;
        if (this.personajes.containsKey(id)) {
            // Actualizar personaje
            p = this.personajes.get(id);
            p.setExp(actual.getExperiencia());
            p.setVida(actual.getVida());
            p.setPosicion(actual.getPosicion());
        } else {
            p = new Personaje(
                    id,
                    TipoPersonaje.fromId(actual.getClase()),
                    actual.getVida(),
                    actual.getSalud(),
                    actual.getExperiencia(),
                    actual.getPosicion(),
                    this.mapaId
            );
            this.personajes.put(id, p);
        }
        
        repaint();
    }
    
    private void enviaActualizacion(Personaje p) {
        Actualizacion actual = new Actualizacion(
                numSec,
                p.getId(),
                p.getTipo().getId(),
                p.getExp(),
                p.getSalud(),
                p.getVida(),
                p.getPosicion(),
                (short)((new Date().getTime() - inicio) / 1000)
        );
        this.cliente.envia(actual);
        while (this.cliente.recibeBloqueante().getTipo() != TipoMensaje.CONFIRMACION)
            ;
    }
}
