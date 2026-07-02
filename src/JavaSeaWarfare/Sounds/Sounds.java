package JavaSeaWarfare.Sounds;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

public class Sounds {

    private URL start, defeat, win, myShipHit, oponentShipHit, miss;
    
    private URL[] playlist;
    private Clip clipBGM;
    private int currentTrack = 0;
    private boolean keepPlaying = false; // controlado para ver se o jogo acabou

    public Sounds() {
        
        this.start = getClass().getResource("../Sounds/start.wav");
        this.win = getClass().getResource("../Sounds/win.wav");
        this.myShipHit = getClass().getResource("../Sounds/hit.wav");
        this.oponentShipHit = getClass().getResource("../Sounds/hit.wav");
        this.miss = getClass().getResource("../Sounds/miss.wav");
        
        playlist = new URL[3];
        playlist[0] = getClass().getResource("../Sounds/Sabaton-Bismarck.wav");     //musicas do Sabaton 8bits
        playlist[1] = getClass().getResource("../Sounds/Sabaton-SmokingSnakes.wav");
        playlist[2] = getClass().getResource("../Sounds/Sabaton-Dreadnought.wav");
    }

    private void play(URL url) {
        if (url == null) {
            System.err.println("Arquivo de som não encontrado.");
            return; 
        }
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.err.println("Erro ao tocar som: " + e.getMessage());
        }
    }

    public void playStart() {
        play(start);
    }

    public void playWin() {
        play(win);
    }

    public void playMyShipHit() {
        play(myShipHit);
    }

    public void playOponentShipHit() {
        play(oponentShipHit);
    }
    
    public void playMiss() {
        play(miss);
    }
    
    public void playBGM() {
        keepPlaying = true; // Permite que a playlist avance
        currentTrack = 0;   // Começa sempre na primeira música
        playNextMusic();
    }

    private void playNextMusic() {
        // Se o jogo mandou parar ou se a música não existir, aborta
        if (!keepPlaying || playlist[currentTrack] == null) return;

        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(playlist[currentTrack]);
            clipBGM = AudioSystem.getClip();
            clipBGM.open(audioIn);
            
            
            clipBGM.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clipBGM.close(); // caso a a musca acabe limpa a memoria da música que acabou
                    
                    // se o jogo ainda estiver rolando passa para a próxima faixa
                    if (keepPlaying) {
                        // faz com que depois da faixa 2, volte à faixa 0
                        currentTrack = (currentTrack + 1) % playlist.length; 
                        playNextMusic();
                    }
                }
            });

            clipBGM.start();
            
        } catch (Exception e) {
            System.err.println("Erro ao tocar a playlist: " + e.getMessage());
        }
    }

    public void stopBGM() {
        keepPlaying = false; //corta  o ciclo da playlist
        if (clipBGM != null && clipBGM.isRunning()) {
            clipBGM.stop();
            clipBGM.close();
        }
    }
    
}
