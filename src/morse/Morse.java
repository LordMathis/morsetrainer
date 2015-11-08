package morse;

import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.SwingUtilities;

/**
 * Trieda Morse obsahuje metódu main()
 *
 * @author Matus Namesny
 */
public class Morse {

    /**
     * V main() sa vytvorý nové vlákno s grapfickým rozhraním, ktoré je v triede
     * GraphicInterface. Zároveň sa pri zatvorení okna programu spustí metóda,
     * ktorá vymaže všetky súbory, ktoré boli vytvorené počas behu programu.
     *
     * @param args Parametre príkazovej riadky sa v tomto programe nevyužívajú.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            final GraphicInterface gi = new GraphicInterface();
            gi.setVisible(true);
            gi.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentHidden(ComponentEvent e) {
                    // Vymaže každý súbor zo zoznamu vytvorených súborov.
                    gi.getCreatedFiles().stream().forEach((file) -> { 
                        file.delete();
                    });
                    ((Window) (e.getComponent())).dispose(); // Zatvorý okno a ukončí program
                }
            });
        });

    }
}
