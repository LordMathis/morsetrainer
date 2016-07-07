package morse;

import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.SwingUtilities;

/**
 * Morse class contains main() method
 *
 * @author Matus Namesny
 */
public class Morse {

    /**
     * main() creates new thread with gui (located in GraphicInterface class)
     * At the end the method cleans up all created files.
     *
     * @param args Command line arguments are not used
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            final GraphicInterface gi = new GraphicInterface();
            gi.setVisible(true);
            gi.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentHidden(ComponentEvent e) {
                    // Deletes file from list of temp files created during program run
                    gi.getCreatedFiles().stream().forEach((file) -> { 
                        file.delete();
                    });
                    ((Window) (e.getComponent())).dispose(); // Closes the window
                }
            });
        });

    }
}
