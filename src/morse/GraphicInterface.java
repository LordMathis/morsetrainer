package morse;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.Clip;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Trieda GraphicInterface obsahuje grafické rozhranie programu a tak isto aj
 * ActionListenery obsluhujúce kľúčové časti programu
 *
 * @author Matus Namesny
 */
public class GraphicInterface extends JFrame {

    private final List<File> createdFiles; // zoznam súborov vytvorených počas behu programu

    private final JTextArea inputTextArea; // sem má užívateľ písať čo počuje aby to program potom mohol skontrolovať
    private final JScrollPane scrollPane;

    private File sourceFile = null; // zdrojový súbor
    private Writer writer; // na zapisovanie do súboru som vytvoril vlastnú triedu Writer

    private JRadioButton generRadioButton = new JRadioButton("Generate", true);
    private JRadioButton fileRadioButton = new JRadioButton("From file", false);
    private ButtonGroup buttonGroup = new ButtonGroup();
    // skupina tlačítok, ktoré zabezpečujú výber zdroja

    private final JButton startButton; // tlačítko štart
    private JButton browseButton; // tlačítko otvorí JFileChooser na vybranie zdrojového súboru
    private final JButton checkButton; // tlačítko skontrolovať

    private final JPanel startButtonPanel;
    private final JPanel checkButtonPanel;

    private final JSpinner timeSpinner; // slúži na zvolenie času (v minútach)
    private final JPanel timeSpinnerPanel;

    private final JSpinner speedSpinner; // zvolenie rýchlosti morzeovky
    private final JPanel speedSpinnerPanel;

    private final JComboBox<Integer> sampleRate; // samplovacia frekvencia
    private final JSlider framesPerWavelength; // počet vzorkov za vlnovú dĺžku

    private JFileChooser fileChooser = new JFileChooser(); // slúži na zvolenie zdrojového súboru

    private JCheckBox alphabetCheckBox = new JCheckBox("Alphabetical", true);
    private JCheckBox numbersCheckBox = new JCheckBox("Numbers");
    private JCheckBox specialCheckBox = new JCheckBox("Special characters");
    // skupina zaškrtávacých tlačítok na zvolenie rôznych tried znakov pre morzeovku

    private final JPanel sourcePanel;
    private final JPanel charPanel;

    private final JPanel mainPanel;

    private Timer t1; // prvý časovač meria celkovú dĺžku trénovania morzeovky
    private Timer t2; // druhý časovač meria meria čas potrebný na zahranie krátkeho znaku (bodky) morzeovky

    private Sound clip; // clip vytvorený na záklede nastavení užívatela

    private InputStreamReader input; // slúži na načítanie vstupu z triedy Source

    private Boolean timerPlay; // príznak, že prvý časovač beží
    private Boolean buttonPlay; // príznak, že bolo stlačené tlačítko štart

    /**
     * Konštruktor vytvorý všetky komponenty a rozloží ich na JFrame.
     * Inicializuje ostatné premenné na ich počiatočné hodnoty
     */
    public GraphicInterface() {

        super();
        setTitle("Morse trainer (by OM7MW)");

        GridBagConstraints frameConstraints = new GridBagConstraints();
        setLayout(new GridBagLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        // nastavý rozloženie okna na GridBagLayout, okno sa bude otvárať v strede obrazovky, a pri zatvorení okna sa okno skryje

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // nastavý vzhľad na systémový vzhľad
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
        }

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        // inicializácia hlavného panelu

        Integer[] rates = {
            8000, 11025, 16000, 22050};
        sampleRate = new JComboBox(rates);
        sampleRate.setToolTipText("Samples per second");
        sampleRate.setSelectedIndex(1);
        sampleRate.setBorder(new TitledBorder("Sample rate"));
        // inicializácia nastavenia samplovacej frekvencie

        framesPerWavelength = new JSlider(JSlider.HORIZONTAL, 10, 200, 25);
        framesPerWavelength.setPaintTicks(true);
        framesPerWavelength.setMajorTickSpacing(10);
        framesPerWavelength.setMinorTickSpacing(5);
        framesPerWavelength.setToolTipText("Frames per Wavelength");
        framesPerWavelength.setBorder(new TitledBorder("Frames per wavelength"));
        // inicializácia nastavenie počtu vzorkov za vlnovú dĺžku

        browseButton = new JButton("Browse");
        browseButton.setEnabled(false);
        browseButton.setToolTipText("Select file");
        // inicializácia tlačítka browse

        createdFiles = new ArrayList<>();
        // inicializácia zoznamu vytvorených súborov

        browseButton.addActionListener((ActionEvent e) -> {
            // pri kliknutí na tlačítko browse sa otvorý súborový manažér na vybranie zdrojového súboru
            int returnVal = fileChooser.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                sourceFile = fileChooser.getSelectedFile(); // nastaví sourceFile na zvolený súbor
                browseButton.setText(fileChooser.getName(sourceFile));
            }
        });

        fileRadioButton.addActionListener((ActionEvent e) -> {
            // ak užívateľ zvolí ako zdroj súbor tlačítko browse sa aktivuje a deaktivujú sa tlačítka na zvolenie tried znakov

            browseButton.setEnabled(true);
            alphabetCheckBox.setEnabled(false);
            numbersCheckBox.setEnabled(false);
            specialCheckBox.setEnabled(false);
        });

        generRadioButton.addActionListener((ActionEvent e) -> {
            // ak užívateľ zvolí ako zdroj generovanie znakov deaktivuje sa tlačítko browse a aktivujú sa tlačítka na zvolenie tried znakov
            // zdrojový súbor sa nastaví na null

            sourceFile = null;
            browseButton.setEnabled(false);
            browseButton.setText("Browse");

            alphabetCheckBox.setEnabled(true);
            numbersCheckBox.setEnabled(true);
            specialCheckBox.setEnabled(true);
        });

        buttonGroup.add(generRadioButton);
        buttonGroup.add(fileRadioButton);

        inputTextArea = new JTextArea(8, 40);
        inputTextArea.setLineWrap(true);
        // nastaví veľkosť a zalomenie textu

        scrollPane = new JScrollPane(getInputTextArea());
        scrollPane.setBorder(new TitledBorder("User input"));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        sourcePanel = new JPanel();
        sourcePanel.setBorder(new TitledBorder("Set source"));
        sourcePanel.add(generRadioButton);
        sourcePanel.add(fileRadioButton);
        sourcePanel.add(browseButton);
        // panel na nastavenie zdroja

        timeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        timeSpinner.setToolTipText("Set time in minutes");
        timeSpinnerPanel = new JPanel();
        timeSpinnerPanel.setBorder(new TitledBorder("Set time"));
        timeSpinnerPanel.add(timeSpinner);
        // inicializácia nastavenia času

        speedSpinner = new JSpinner(new SpinnerNumberModel(20, 5, 60, 1));
        speedSpinner.setToolTipText("Set speed in WPM (Words per minute)");
        speedSpinnerPanel = new JPanel();
        speedSpinnerPanel.setBorder(new TitledBorder("Set speed"));
        speedSpinnerPanel.add(speedSpinner);
        // inicializácia nastavenia rýchlosti

        charPanel = new JPanel();
        charPanel.setBorder(new TitledBorder("Set characters"));
        charPanel.add(alphabetCheckBox);
        charPanel.add(numbersCheckBox);
        charPanel.add(specialCheckBox);
        // inicializácia nastavenia tried znakov

        startButton = new JButton("Start");
        startButton.addActionListener(new Start());
        startButtonPanel = new JPanel(new GridLayout());
        startButtonPanel.add(startButton);
        startButtonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        // inicializácia tlačítka štart

        checkButton = new JButton("Check");
        checkButton.addActionListener(new Check());
        checkButtonPanel = new JPanel(new GridLayout());
        checkButtonPanel.add(checkButton);
        checkButtonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        // inicializácia tlačítka skontrolovať

        frameConstraints.gridx = 0;
        frameConstraints.gridy = 0;
        frameConstraints.gridwidth = 2;
        frameConstraints.gridheight = 1;
        frameConstraints.fill = GridBagConstraints.BOTH;
        frameConstraints.weightx = 1;
        frameConstraints.weighty = 1;
        mainPanel.add(sourcePanel, frameConstraints);

        frameConstraints.gridx = 2;
        frameConstraints.gridy = 0;
        frameConstraints.gridwidth = 2;
        frameConstraints.gridheight = 1;
        mainPanel.add(sampleRate, frameConstraints);

        frameConstraints.gridx = 0;
        frameConstraints.gridy = 1;
        frameConstraints.gridwidth = 2;
        frameConstraints.gridheight = 1;
        mainPanel.add(charPanel, frameConstraints);

        frameConstraints.gridx = 2;
        frameConstraints.gridy = 1;
        frameConstraints.gridwidth = 2;
        frameConstraints.gridheight = 1;
        mainPanel.add(framesPerWavelength, frameConstraints);

        frameConstraints.gridx = 0;
        frameConstraints.gridy = 2;
        frameConstraints.gridwidth = 2;
        frameConstraints.gridheight = 3;
        mainPanel.add(scrollPane, frameConstraints);

        frameConstraints.gridx = 2;
        frameConstraints.gridy = 2;
        frameConstraints.gridwidth = 1;
        frameConstraints.gridheight = 1;
        mainPanel.add(timeSpinnerPanel, frameConstraints);

        frameConstraints.gridx = 3;
        frameConstraints.gridy = 2;
        frameConstraints.gridwidth = 1;
        frameConstraints.gridheight = 1;
        mainPanel.add(speedSpinnerPanel, frameConstraints);

        frameConstraints.gridx = 2;
        frameConstraints.gridy = 3;
        frameConstraints.gridwidth = 2;
        frameConstraints.gridheight = 1;
        frameConstraints.fill = GridBagConstraints.BOTH;
        mainPanel.add(startButtonPanel, frameConstraints);

        frameConstraints.gridx = 2;
        frameConstraints.gridy = 4;
        frameConstraints.gridwidth = 2;
        frameConstraints.gridheight = 1;
        mainPanel.add(checkButtonPanel, frameConstraints);

        mainPanel.setSize(getPreferredSize());
        add(mainPanel);
        pack();
        // pridanie všetkých prvkov na hlavný JFrame

        timerPlay = false; // prvý časovač na začiatku nebeží
        buttonPlay = false; // tlačítko štart ešte nebolo stlačené

    }

    /**
     * @return the inputTextArea
     */
    public final JTextArea getInputTextArea() {
        return inputTextArea;
    }

    /**
     * @return the sourceFile
     */
    public File getSourceFile() {
        return sourceFile;
    }

    /**
     * @param sourceFile the sourceFile to set
     */
    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * @return the writer
     */
    private Writer getWriter() {
        return writer;
    }

    /**
     * @param writer the writer to set
     */
    private void setWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * @return the generRadioButton
     */
    public JRadioButton getGenerRadioButton() {
        return generRadioButton;
    }

    /**
     * @return the startButton
     */
    public JButton getStartButton() {
        return startButton;
    }

    /**
     * @return the checkButton
     */
    public JButton getCheckButton() {
        return checkButton;
    }

    /**
     * @return the timeSpinner
     */
    public JSpinner getTimeSpinner() {
        return timeSpinner;
    }

    /**
     * @return the speedSpinner
     */
    public JSpinner getSpeedSpinner() {
        return speedSpinner;
    }

    /**
     * @return the sampleRate
     */
    public JComboBox<Integer> getSampleRate() {
        return sampleRate;
    }

    /**
     * @return the framesPerWavelength
     */
    public JSlider getFramesPerWavelength() {
        return framesPerWavelength;
    }

    /**
     * @return the alphabetCheckBox
     */
    public JCheckBox getAlphabetCheckBox() {
        return alphabetCheckBox;
    }

    /**
     * @return the numbersCheckBox
     */
    public JCheckBox getNumbersCheckBox() {
        return numbersCheckBox;
    }

    /**
     * @return the specialCheckBox
     */
    public JCheckBox getSpecialCheckBox() {
        return specialCheckBox;
    }

    /**
     * @return the t1
     */
    public Timer getT1() {
        return t1;
    }

    /**
     * @param t1 the t1 to set
     */
    public void setT1(Timer t1) {
        this.t1 = t1;
    }

    /**
     * @return the t2
     */
    public Timer getT2() {
        return t2;
    }

    /**
     * @param t2 the t2 to set
     */
    public void setT2(Timer t2) {
        this.t2 = t2;
    }

    /**
     * @return the clip
     */
    public Sound getClip() {
        return clip;
    }

    /**
     * @param clip the clip to set
     */
    public void setClip(Sound clip) {
        this.clip = clip;
    }

    /**
     * @return the input
     */
    public InputStreamReader getInput() {
        return input;
    }

    /**
     * @param input the input to set
     */
    public void setInput(InputStreamReader input) {
        this.input = input;
    }

    /**
     * @return the timerPlay
     */
    public Boolean getTimerPlay() {
        return timerPlay;
    }

    /**
     * @param timerPlay the timerPlay to set
     */
    public void setTimerPlay(Boolean timerPlay) {
        this.timerPlay = timerPlay;
    }

    /**
     * @return the buttonPlay
     */
    public Boolean getButtonPlay() {
        return buttonPlay;
    }

    /**
     * @param buttonPlay the buttonPlay to set
     */
    public void setButtonPlay(Boolean buttonPlay) {
        this.buttonPlay = buttonPlay;
    }

    /**
     * @return the createdFiles
     */
    public List<File> getCreatedFiles() {
        return createdFiles;
    }

    /**
     * Trieda Beat načítava znaky zo zdroja (trieda Source), prekladá ich do
     * morzeovky a postupne ich prehrá
     */
    private class Beat implements ActionListener {

        private final Deque<Boolean> morseQ; // fronta preložených znakov do morzeovky, ak je daný prvok True tak clip hrá inak nie
        private final Deque<Integer> charQ; // fronta znakov načítaných zo zdroja

        private final Boolean write; // True ak sú znaky generované

        private int n; // počet prehraných znakov v jednej skupine piatich znakov
        Boolean readNext; // True ak ešte neskončil zdrojový súbor alebo časový limit

        char[] morseChars; // pole morzeovkových symbolov, ktoré reprezentujú daný znak

        /**
         * Konštruktor triedy inicializuje fronty a ďalšie premenné
         *
         * @param write Parameter write je True ak sú znaky generované
         */
        public Beat(Boolean write) {
            morseQ = new ArrayDeque<>();

            for (int i = 0; i < 5; i++) {
                morseQ.add(false);
            }
            // na začiatku program nehrá nič aby mal užívateľ čas sa pripravyť 

            charQ = new ArrayDeque<>();
            n = 0;
            readNext = true;
            this.write = write;

        }

        /**
         * Metóda sa spustí keď dobehne druhý časovač. Načíta znak zo vstupu,
         * preloží ho do morzeovky a zahrá jeden zo symbolov.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            if (charQ.size() < (5 - n) && (readNext)) {
                // veľkosť fronty znakov je obmedzená na práve hrajúcu skupinu piatich znakov aby sa tam zbytočne nehromadili neprehrané znaky 
                try {
                    int x = getInput().read(); // načíta znak zo zdroja
                    if (x == -1) { // koniec vstupu
                        readNext = false;
                    } else {
                        charQ.addLast(x);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (!getTimerPlay()) {
                readNext = false; // koniec časového limitu
            }

            if ((morseQ.size() < 20) && (charQ.size() > 0)) {
                // veľkosť morzeovkovej fronty je obmedzená na 20 aby sa nehromadili neprehrané symboly
                try {
                    int x = charQ.removeFirst(); // vybere prvý znak z fronty znakov

                    n = (n + 1) % 5; // počet znakov v práve hrannej skupine

                    morseChars = toMorse((char) x).toCharArray(); // preloží do morzeovky daný znak
                    for (char morseChar : morseChars) {
                        // danú postupnosť morzeovkových symbolov preloží do postupnosti booleanov
                        switch (morseChar) {
                            case '.':
                                morseQ.addLast(true);
                                morseQ.addLast(false);
                                break;

                            case '-':
                                morseQ.addLast(true);
                                morseQ.addLast(true);
                                morseQ.addLast(true);
                                morseQ.addLast(false);
                                break;
                            case ' ':
                                morseQ.addLast(false);
                                morseQ.addLast(false);
                                morseQ.addLast(false);
                                morseQ.addLast(false);
                                morseQ.addLast(false);
                        }
                    }
                    morseQ.addLast(false);
                    morseQ.addLast(false);

                    if (write) {
                        getWriter().write((char) x);
                        // zapíše znak do súboru pre neskoršiu kontrolu užívateľa
                    }

                    if ((n == 0) && write) { // v prípade, že je koniec skupiny 5 znakov
                        for (int i = 0; i < 4; i++) {
                            morseQ.addLast(false);
                        }
                        getWriter().write(' ');
                    }

                } catch (Exception e1) {
                    e1.printStackTrace(System.err);
                }
            }

            try {
                Boolean next = morseQ.removeFirst();
                // vybere nasledujúcu hodnotu z fronty, ak je True clip sa začne prehrávať. Ak je False clip sa zastaví

                if (next) {
                    if (!clip.getClip().isActive()) {
                        getClip().getClip().loop(Clip.LOOP_CONTINUOUSLY);
                    }
                } else {
                    if (getClip().getClip().isActive()) {
                        getClip().getClip().stop();
                    }
                }
            } catch (Exception e2) {
                // ak je morzeovková fronta prázdna, prehrávanie sa zastaví
                stop();
            }

        }

        /**
         * Zastaví prehrávanie. Zastaví časovač, obnoví nastavenia na tlačítkach
         * štart a skontrolovať a zatvorí výstup.
         */
        private void stop() {
            getT2().stop();
            getStartButton().setText("Start");
            getCheckButton().setEnabled(true);
            setButtonPlay(false);
            getClip().getClip().stop();
            if (write) {
                getWriter().close();
            }
        }

        /**
         * Preloží daný znak do morzeovky. Preklad prebieha jednoduchým
         * vyhľadávaním v poli.
         *
         * @param alpha znak na preloženie
         * @return postupnosť znakov morzeovky, ktoré reprezentujú daný znak
         */
        private String toMorse(char alpha) {
            char[] alphanumArr = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
                'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '/', '?', '=', ' '};

            String[] morseArr = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..",
                ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.",
                "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..",
                ".----", "..---", "...--", "....-", ".....", "-....", "--...",
                "---..", "----.", "-----", "-..-.", "..--..", "-...-", " "};

            int i = 0;
            while ((i < alphanumArr.length) && alpha != alphanumArr[i]) {
                i++;
            }
            if (i < alphanumArr.length) {
                return morseArr[i];
            } else {
                return null;
            }
        }
    }

    /**
     * Trieda Check zabezpečuje skontrolovanie užívateľovho vstupu
     */
    private class Check implements ActionListener {

        /**
         * Skontrolovanie vstupu prebieha postupným porovnávaním jednotlivých
         * znakov a vypísanie prípadných chýb
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            getInputTextArea().append("  ");

            String userString = getInputTextArea().getText(); // užívateľov vstup
            if (userString == null || userString.isEmpty()) {
                JOptionPane.showMessageDialog(rootPane, "There is nothing to check"); // v prípade, že užívateľ nič nenapísal
            } else {

                try (FileReader sourceFileReader = new FileReader(getSourceFile())) {
                    getInputTextArea().append("\n");
                    getInputTextArea().append("wrong | right - index\n");

                    int i = 0;
                    int x;
                    int y;
                    int misstakes = 0;

                    x = sourceFileReader.read();
                    while ((x != -1) && (x != 10)) { // prejde celý zdrojový súbor

                        if (i >= userString.length()) {
                            y = '_'; // v prípade, že užívateľov vstup už je na konci
                        } else {
                            y = Character.toUpperCase(userString.charAt(i)); // porovnávam len veľké písmená
                        }
                        if (Character.toUpperCase(x) != y) { // znaky sa nezhodujú
                            misstakes++;
                            getInputTextArea().append(String.format("%5s | %-5s - %d\n", String.valueOf((char) y), String.valueOf((char) x), i));
                            // vypíše chybný znak, správny znak a index chyby
                        }
                        i++;
                        x = sourceFileReader.read();
                    }

                    getInputTextArea().append("Misstakes: " + misstakes); // vypíše celkový počet chýb
                } catch (IOException ex) {
                    Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

    }

    /**
     * Trieda Start je potrebná na začatie prehrávania morzeovky
     */
    private class Start implements ActionListener {

        /**
         * Vytvorí potrebné objekty na prehrávanie morzeovky. Inicializuje
         * triedy Beat, Source a Sound. Nastaví prvý a druhý časovač a spustí
         * ich.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            if (!sourceCheck()) { // v prípade, že nie je nastavený zdroj
                JOptionPane.showMessageDialog(rootPane, "No source specified", "Source error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (getButtonPlay()) { // ak už bolo tlačítko štart stlačené a morzeovka sa prehráva, tlačítko potom slúži na zastavenie prehrávania

                setButtonPlay(false);
                getT2().stop();
                getT1().stop();
                getClip().getClip().stop();
                getStartButton().setText("Start");
                getCheckButton().setEnabled(true);
                getWriter().close();

            } else {
                setClip(new Sound((int) getSampleRate().getSelectedItem(), getFramesPerWavelength().getValue())); // nastaví clip
                getInputTextArea().setText(""); // vymaže užívateľov predchádzajúci vstup
                if (getGenerRadioButton().isSelected()) {
                    // ak je ako zdroj nastavené generovanie

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                    String fileName = sdf.format(new Date());
                    setSourceFile(new File(fileName));
                    try {
                        getSourceFile().createNewFile();
                    } catch (IOException ex) {
                        Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    getCreatedFiles().add(getSourceFile());
                    setWriter(new Writer(getSourceFile()));
                    // vytvorí súbor do ktorého bude trieda Beat zapisovať prehrávané znaky pre prípadnú kontrolu

                    Boolean[] chars = {false, false, false};
                    if (getAlphabetCheckBox().isSelected()) {
                        chars[0] = true;
                    }
                    if (getNumbersCheckBox().isSelected()) {
                        chars[1] = true;
                    }
                    if (getSpecialCheckBox().isSelected()) {
                        chars[2] = true;
                    }
                    // nastaví triedy znakov

                    setInput(new InputStreamReader(new Source(chars)));
                    // vytvorí vstup

                    setT1(new Timer((int) getTimeSpinner().getValue() * 60 * 1000, (ActionEvent e1) -> {
                        setTimerPlay(false); // keď dobehne prvý časovač nastaví sa príznak na False
                        getT1().stop();
                    }));
                    getT1().start();
                        // nastaví a spustí časovač

                } else { // ako zdroj je vybraný súbor
                    try {
                        setInput(new InputStreamReader(new Source(getSourceFile()))); // vytvorí vstup

                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                setTimerPlay(true);
                setButtonPlay(true);
                // nastaví oba príznaky na True

                int delay = 1200 / ((int) getSpeedSpinner().getValue());
                Beat beat = new Beat(getGenerRadioButton().isSelected());
                setT2(new Timer(delay, beat));
                getT2().start();
                // nastaví čas druhého časovača podľa zvolenej rýchlosti, vytvorí instanciu triedy Beat a spustí druhý časovač

                getStartButton().setText("Stop");
                getCheckButton().setEnabled(false);
            }

        }

        /**
         * Skontroluje či je zvolený zdroj
         *
         * @return true ak je zdroj zvolený, inak false
         */
        public Boolean sourceCheck() {

            if (getGenerRadioButton().isSelected()) {
                if (getAlphabetCheckBox().isSelected()) {
                    return true;
                } else if (getNumbersCheckBox().isSelected()) {
                    return true;
                } else {
                    return getSpecialCheckBox().isSelected();
                }
                // ak je zvolené generovanie, musí byť zaškrtnuté aspoň jedno tlačítko so symbolmi

            } else {
                return getSourceFile() != null;
                // ak je zvolený ako zdroj súbor, potom nesmie byť null
            }
        }

    }

    /**
     * Trieda Writer zabezpečuje zapisovanie prehrávaných znakov do súboru pre
     * prípadné skontrolovanie, v prípade, že je zvolené generovanie
     */
    private class Writer {

        File file;
        FileWriter fw;

        /**
         * Konštruktor vytvorí FileWriter k danému súboru
         *
         * @param file
         */
        public Writer(File file) {
            try {
                this.file = file;
                fw = new FileWriter(file);
            } catch (IOException ex) {
                Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * Zapíše do súboru zadaný znak, v prípade výnimky sa pokúsi výstup
         * zavrieť
         *
         * @param a znak, ktorý sa má zapísať
         */
        public void write(char a) {
            try {
                fw.append(a);
            } catch (IOException ex) {
                try {
                    fw.close();
                } catch (IOException ex1) {
                    Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex1);
                }
                Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * Zatvorí výstup
         */
        public void close() {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
