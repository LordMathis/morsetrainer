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
 * Graphical User Interface and main program logic
 *
 * @author Matus Namesny
 */
public class GraphicInterface extends JFrame {

    // Definition of window and variables
    
    private final List<File> createdFiles;

    private final JTextArea inputTextArea;
    private final JScrollPane scrollPane;

    private File sourceFile = null;
    private Writer writer; 

    private JRadioButton generRadioButton = new JRadioButton("Generate", true);
    private JRadioButton fileRadioButton = new JRadioButton("From file", false);
    private ButtonGroup buttonGroup = new ButtonGroup();

    private final JButton startButton; 
    private JButton browseButton; 
    private final JButton checkButton;

    private final JPanel startButtonPanel;
    private final JPanel checkButtonPanel;

    private final JSpinner timeSpinner;
    private final JPanel timeSpinnerPanel;

    private final JSpinner speedSpinner;
    private final JPanel speedSpinnerPanel;

    private final JComboBox<Integer> sampleRate; 
    private final JSlider framesPerWavelength; 

    private JFileChooser fileChooser = new JFileChooser(); 

    private JCheckBox alphabetCheckBox = new JCheckBox("Alphabetical", true);
    private JCheckBox numbersCheckBox = new JCheckBox("Numbers");
    private JCheckBox specialCheckBox = new JCheckBox("Special characters");

    private final JPanel sourcePanel;
    private final JPanel charPanel;

    private final JPanel mainPanel;

    private Timer t1; 
    private Timer t2; 

    private Sound clip; 

    private InputStreamReader input; 

    private Boolean timerPlay;
    private Boolean buttonPlay; 

    /**
     * Creates window with all defined components
     * Initializes all variables
     */
    public GraphicInterface() {

        super();
        setTitle("Morse trainer (by OM7MW)");

        GridBagConstraints frameConstraints = new GridBagConstraints();
        setLayout(new GridBagLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
        }

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        Integer[] rates = {
            8000, 11025, 16000, 22050};
        sampleRate = new JComboBox(rates);
        sampleRate.setToolTipText("Samples per second");
        sampleRate.setSelectedIndex(1);
        sampleRate.setBorder(new TitledBorder("Sample rate"));
        // Sample rate settings

        framesPerWavelength = new JSlider(JSlider.HORIZONTAL, 10, 200, 25);
        framesPerWavelength.setPaintTicks(true);
        framesPerWavelength.setMajorTickSpacing(10);
        framesPerWavelength.setMinorTickSpacing(5);
        framesPerWavelength.setToolTipText("Frames per Wavelength");
        framesPerWavelength.setBorder(new TitledBorder("Frames per wavelength"));
        // Frames per wavelength settings

        browseButton = new JButton("Browse");
        browseButton.setEnabled(false);
        browseButton.setToolTipText("Select file");

        createdFiles = new ArrayList<>();

        browseButton.addActionListener((ActionEvent e) -> {
            // selecting input file
            int returnVal = fileChooser.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                sourceFile = fileChooser.getSelectedFile(); // nastaví sourceFile na zvolený súbor
                browseButton.setText(fileChooser.getName(sourceFile));
            }
        });

        fileRadioButton.addActionListener((ActionEvent e) -> {

            browseButton.setEnabled(true);
            alphabetCheckBox.setEnabled(false);
            numbersCheckBox.setEnabled(false);
            specialCheckBox.setEnabled(false);
        });

        generRadioButton.addActionListener((ActionEvent e) -> {

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

        scrollPane = new JScrollPane(getInputTextArea());
        scrollPane.setBorder(new TitledBorder("User input"));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        sourcePanel = new JPanel();
        sourcePanel.setBorder(new TitledBorder("Set source"));
        sourcePanel.add(generRadioButton);
        sourcePanel.add(fileRadioButton);
        sourcePanel.add(browseButton);

        timeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        timeSpinner.setToolTipText("Set time in minutes");
        timeSpinnerPanel = new JPanel();
        timeSpinnerPanel.setBorder(new TitledBorder("Set time"));
        timeSpinnerPanel.add(timeSpinner);
        // time settings

        speedSpinner = new JSpinner(new SpinnerNumberModel(20, 5, 60, 1));
        speedSpinner.setToolTipText("Set speed in WPM (Words per minute)");
        speedSpinnerPanel = new JPanel();
        speedSpinnerPanel.setBorder(new TitledBorder("Set speed"));
        speedSpinnerPanel.add(speedSpinner);
        // speed settings

        charPanel = new JPanel();
        charPanel.setBorder(new TitledBorder("Set characters"));
        charPanel.add(alphabetCheckBox);
        charPanel.add(numbersCheckBox);
        charPanel.add(specialCheckBox);
        // character classes settings

        startButton = new JButton("Start");
        startButton.addActionListener(new Start());
        startButtonPanel = new JPanel(new GridLayout());
        startButtonPanel.add(startButton);
        startButtonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        checkButton = new JButton("Check");
        checkButton.addActionListener(new Check());
        checkButtonPanel = new JPanel(new GridLayout());
        checkButtonPanel.add(checkButton);
        checkButtonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

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

        timerPlay = false; 
        buttonPlay = false;

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
     * Class Beat reads characters from Source, translates them into morse code and plays them
     */
    private class Beat implements ActionListener {

        private final Deque<Boolean> morseQ; // Queue of translated characters
        private final Deque<Integer> charQ; // Queue of input characters

        private final Boolean write; // True if characters are generated

        private int n; // number of played characters in a group of 5
        Boolean readNext;

        char[] morseChars;

        /**
         * Initializations of variables
         *
         * @param write = True if new characters are generated
         */
        public Beat(Boolean write) {
            morseQ = new ArrayDeque<>();

            for (int i = 0; i < 5; i++) {
                morseQ.add(false);
            }

            charQ = new ArrayDeque<>();
            n = 0;
            readNext = true;
            this.write = write;

        }

        /**
         * Reads character from Source, translates it and puts it into queue
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            if (charQ.size() < (5 - n) && (readNext)) {
                // size of character queue is limited to currently played group of 5
                try {
                    int x = getInput().read(); 
                    if (x == -1) { 
                        readNext = false;
                    } else {
                        charQ.addLast(x);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (!getTimerPlay()) {
                readNext = false; 
            }

            if ((morseQ.size() < 20) && (charQ.size() > 0)) {
                // the size of morse code queue is limited to 20 
                try {
                    int x = charQ.removeFirst(); 

                    n = (n + 1) % 5; 

                    morseChars = toMorse((char) x).toCharArray(); // translates character
                    for (char morseChar : morseChars) {

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
                        // writes character to file for later check
                    }

                    if ((n == 0) && write) {
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
                stop();
            }

        }

        /**
         * Stops playing and resets program
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
         * Translates character to morse code
         *
         * @param alpha = character to translate
         * @return = string of morse code
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
     * Checks user output
     */
    private class Check implements ActionListener {

        /**
         * checks user output
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            getInputTextArea().append("  ");

            String userString = getInputTextArea().getText(); // user output
            if (userString == null || userString.isEmpty()) {
                JOptionPane.showMessageDialog(rootPane, "There is nothing to check");
            } else {

                try (FileReader sourceFileReader = new FileReader(getSourceFile())) {
                    getInputTextArea().append("\n");
                    getInputTextArea().append("wrong | right - index\n");

                    int i = 0;
                    int x;
                    int y;
                    int misstakes = 0;

                    x = sourceFileReader.read();
                    while ((x != -1) && (x != 10)) { 

                        if (i >= userString.length()) {
                            y = '_'; 
                        } else {
                            y = Character.toUpperCase(userString.charAt(i)); 
                        }
                        if (Character.toUpperCase(x) != y) { 
                            misstakes++;
                            getInputTextArea().append(String.format("%5s | %-5s - %d\n", String.valueOf((char) y), String.valueOf((char) x), i));
                            // prints wrong character, correct character and index of mistake
                        }
                        i++;
                        x = sourceFileReader.read();
                    }

                    getInputTextArea().append("Misstakes: " + misstakes); // number of mistakes
                } catch (IOException ex) {
                    Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

    }

    /**
     * Start of playing of morse code
     */
    private class Start implements ActionListener {

        /**
         * Initialization of classes Beat, Source, Sound and necessary variables
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            if (!sourceCheck()) { 
                JOptionPane.showMessageDialog(rootPane, "No source specified", "Source error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (getButtonPlay()) { 

                setButtonPlay(false);
                getT2().stop();
                getT1().stop();
                getClip().getClip().stop();
                getStartButton().setText("Start");
                getCheckButton().setEnabled(true);
                getWriter().close();

            } else {
                setClip(new Sound((int) getSampleRate().getSelectedItem(), getFramesPerWavelength().getValue()));
                getInputTextArea().setText(""); 
                if (getGenerRadioButton().isSelected()) {

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

                    setInput(new InputStreamReader(new Source(chars)));

                    setT1(new Timer((int) getTimeSpinner().getValue() * 60 * 1000, (ActionEvent e1) -> {
                        setTimerPlay(false); 
                        getT1().stop();
                    }));
                    getT1().start();
                        

                } else { 
                    try {
                        setInput(new InputStreamReader(new Source(getSourceFile()))); 

                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(GraphicInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                setTimerPlay(true);
                setButtonPlay(true);

                int delay = 1200 / ((int) getSpeedSpinner().getValue());
                Beat beat = new Beat(getGenerRadioButton().isSelected());
                setT2(new Timer(delay, beat));
                getT2().start();
                // calculates speed and sets the second timer

                getStartButton().setText("Stop");
                getCheckButton().setEnabled(false);
            }

        }

        /**
         * Chcecks if source is selected
         *
         * @return 
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
                
            } else {
                return getSourceFile() != null;
            
            }
        }

    }

    /**
     * Writes played character into a file for check
     */
    private class Writer {

        File file;
        FileWriter fw;

        /**
         * Creates FileWriter 
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
         * Writes character to a file
         *
         * @param a = character to write
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
         * Closes the output
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
