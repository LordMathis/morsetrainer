
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
 * Tone settings
 * @author Matus Namesny
 */
public class Sound {

    private Clip clip;

    /**
     * Tone settings are stored in Clip class
     * @param intSR = sample rate
     * @param intFPW = frames per wavelength
     */
    public Sound(int intSR, int intFPW) {
        try {
            clip = AudioSystem.getClip();

            float sampleRate = intSR;

            int wavelengths = 20; 
            byte[] buf = new byte[2 * intFPW * wavelengths];
            
            AudioFormat af = new AudioFormat( // AudioFormat is needed to create AudioInputStream
                    sampleRate, 
                    8, // sample size in bits
                    2, // number of channels
                    true, // signed
                    false // big endian
            );

            // Buffer array for ByteArrayInputStream
            for (int i = 0; i < intFPW * wavelengths; i++) {
                double angle = ((i * 2) / ((float) intFPW)) * (Math.PI);
                buf[i * 2] = getByteValue(angle);
                buf[(i * 2) + 1] = buf[i * 2];
            }
            byte[] b = buf;
            
            AudioInputStream ais = new AudioInputStream(
                    new ByteArrayInputStream(b),
                    af, // AudioFormat
                    buf.length / 2); // length of AudioInputStream

            clip.open(ais); // Clip can be used by other methods

        } catch (LineUnavailableException | IOException ex) {
            Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // returns byte value at a point on sinus wave
    private byte getByteValue(double angle) {
        int maxVol = 127;
        return (new Integer(
                (int) Math.round(
                        Math.sin(angle) * maxVol))).
                byteValue();
    }

    /**
     * @return the clip
     */
    public Clip getClip() {
        return clip;
    }



}
