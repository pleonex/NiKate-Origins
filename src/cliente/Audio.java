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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author http://stackoverflow.com/a/577926
 */
public class Audio {

    private static final List<Thread> Clips = new ArrayList<>();
    
    public static void playClip(String clipPath) {
        PlayClip clip = new PlayClip(clipPath);
        Clips.add(clip);
        clip.start();
    }
    
    public static void playClip(File clipFile) {
        PlayClip clip = new PlayClip(clipFile);
        Clips.add(clip);
        clip.start();
    }
    
    public static void playClipLoop(String clipMain, String clipLoop) {
        PlayClipLoop clip = new PlayClipLoop(clipMain, clipLoop);
        Clips.add(clip);
        clip.start();
    }
    
    public static void stopAll() {
        for (Thread t : Clips) {
            if (t != null && t.isAlive())
                t.interrupt();
        }
        
        Clips.clear();
    }

    private static class PlayClip extends Thread {
        
        private final File clipFile;
        private AudioListener listener;
        private Line line;
        
        public PlayClip(String clipPath) {
            this.clipFile = new File(clipPath);
        }
        
        public PlayClip(File clipFile) {
            this.clipFile = clipFile;
        }
        
        @Override
        public void run() {
            this.listener = new AudioListener();
            try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(clipFile)) {
                try (Clip clip = AudioSystem.getClip()) {
                    this.line = clip;
                    clip.addLineListener(listener);
                    clip.open(audioInputStream);
                    
                    clip.start();
                    listener.waitUntilDone();
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            } catch (IOException | UnsupportedAudioFileException ex) {
                System.err.println(ex.getMessage());
            }
        }
        
        @Override
        public void interrupt() {
            if (this.line != null && this.line.isOpen() && this.listener != null)
                this.listener.update(
                        new LineEvent(this.line, Type.STOP, AudioSystem.NOT_SPECIFIED)
                );
            super.interrupt();
        }
    }
    
    private static class PlayClipLoop extends Thread {
        
        private final File clipFileMain;
        private final File clipFileLoop;
        private AudioListener listener;
        private Line line;
        private boolean cancel = false;
        
        public PlayClipLoop(String clipPathMain, String clipPathLoop) {
            this.clipFileMain = new File(clipPathMain);
            this.clipFileLoop = new File(clipPathLoop);
        }
        
        @Override
        public void run() {
            this.play(clipFileMain, false);
            if (!cancel)
                this.play(clipFileLoop, true);
        }
        
        private void play(File clipFile, boolean loop) {
            this.listener = new AudioListener();
            try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(clipFile)) {
                try (Clip clip = AudioSystem.getClip()) {
                    this.line = clip;
                    clip.addLineListener(listener);
                    clip.open(audioInputStream);

                    if (loop)
                        clip.loop(Clip.LOOP_CONTINUOUSLY);
                    else
                        clip.start();
                    listener.waitUntilDone();
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            } catch (IOException | UnsupportedAudioFileException ex) {
                System.err.println(ex.getMessage());
            }
        }
        
        @Override
        public void interrupt() {
            this.cancel = true;
            if (this.line != null && this.line.isOpen() && this.listener != null)
                this.listener.update(
                        new LineEvent(this.line, Type.STOP, AudioSystem.NOT_SPECIFIED)
                );
            super.interrupt();
        } 
    }
    
    private static class AudioListener implements LineListener {

        private boolean done = false;

        @Override
        public synchronized void update(LineEvent event) {
            Type eventType = event.getType();
            if (eventType == Type.STOP || eventType == Type.CLOSE) {
                done = true;
                notifyAll();
            }
        }

        public synchronized void waitUntilDone() throws InterruptedException {
            while (!done) {
                wait();
            }
        }
    }
}
