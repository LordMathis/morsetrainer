
package morse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

/**
 * Trieda Sound obsahuje nastavenia tónu pre morzeovku.
 * @author Matus Namesny
 */
public class Sound {

    private Clip clip;

    /**
     * V konštruktore sa nastaví Clip, teda tón pre morzeovku
     * @param intSR Parameter intSR je vzorkovacia frekvencia pre samplovanie klipu.
     * @param intFPW Parameter intFPW je počet vzorkov na vlnovú dĺžku pre samplovanie klipu.
     */
    public Sound(int intSR, int intFPW) {
        try {
            clip = AudioSystem.getClip();

            float sampleRate = intSR;

            int wavelengths = 20; 
            byte[] buf = new byte[2 * intFPW * wavelengths];
            
            AudioFormat af = new AudioFormat( // Vytvorý AudioFormat, ktorý je potrebný na vytvorenie AudioInputStream
                    sampleRate, // vzorkovacia frekvencia
                    8, // veľkosť vzorku v bitoch
                    2, // počet kanálov
                    true, // so znamienkom
                    false // big endian
            );

            // Vytvorý pole bytových hodnôt v rôznych bodoch sínusoidy.
            // Toto pole slúži ako buffer pre ByteArrayInputStream
            for (int i = 0; i < intFPW * wavelengths; i++) {
                double angle = ((i * 2) / ((float) intFPW)) * (Math.PI);
                buf[i * 2] = getByteValue(angle);
                buf[(i * 2) + 1] = buf[i * 2];
            }
            byte[] b = buf;
            
            // Vytvorý AudioInputStream zo zadaných hodnôt InputStream, AudioFormat a dĺžky
            AudioInputStream ais = new AudioInputStream(
                    new ByteArrayInputStream(b), // ByteArrayInputStream, ktorý používa b ako buffer pole
                    af, // AudioFormat
                    buf.length / 2); // Dĺžka AudioInputStream

            clip.open(ais); // Otvorý Clip, to, že je clip otvorený znamená, že ho môžu používať ďalšie metódy

        } catch (LineUnavailableException | IOException ex) {
            Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // Vráti hodnotu v bytoch v danom bode sínusovej vlny
    private byte getByteValue(double angle) {
        int maxVol = 127;
        return (new Integer(
                (int) Math.round(
                        Math.sin(angle) * maxVol))).
                byteValue();
    }

    /**
     * @return the clip Vráti javax.sound.sampled.Clip, ktorý sa nastaví v konštruktore triedy Sound
     */
    public Clip getClip() {
        return clip;
    }



}
