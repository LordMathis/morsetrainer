package morse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Source class is the source of characters to be played in morse code. 
 * The characters are either generated or read from a file. The class extends InputStream.
 * New characters are returned by read method
 *
 * @author Matus Namesny
 */
public class Source extends InputStream {

    private File sourceFile = null;
    private FileReader fr;
    private Character[] alphaArr;
    private Random random;
    private final Boolean gener;

    /**
     * New characters will be read from a file instead of being generated
     *
     * @param file - the source file from which the new characters will be read
     * @throws FileNotFoundException
     */
    public Source(File file) throws FileNotFoundException {
        gener = false;
        sourceFile = file;
        fr = new FileReader(sourceFile);
    }

    /**
     * New characters will be randomly generated
     *
     * @param chars - each cell of array of booleans represents character class
     */
    public Source(Boolean[] chars) {
        gener = true;
        random = new Random();
        List<Character> alphaList = new ArrayList<>();
        if (chars[0]) {
            alphaList.add('A');
            alphaList.add('B');
            alphaList.add('C');
            alphaList.add('D');
            alphaList.add('E');
            alphaList.add('F');
            alphaList.add('G');
            alphaList.add('H');
            alphaList.add('I');
            alphaList.add('J');
            alphaList.add('K');
            alphaList.add('L');
            alphaList.add('M');
            alphaList.add('N');
            alphaList.add('O');
            alphaList.add('P');
            alphaList.add('Q');
            alphaList.add('R');
            alphaList.add('S');
            alphaList.add('T');
            alphaList.add('U');
            alphaList.add('V');
            alphaList.add('W');
            alphaList.add('X');
            alphaList.add('Y');
            alphaList.add('Z');
        }
        if (chars[1]) {
            alphaList.add('0');
            alphaList.add('1');
            alphaList.add('2');
            alphaList.add('3');
            alphaList.add('4');
            alphaList.add('5');
            alphaList.add('6');
            alphaList.add('7');
            alphaList.add('8');
            alphaList.add('9');
        }
        if (chars[2]) {
            alphaList.add('/');
            alphaList.add('?');
            alphaList.add('=');
        }

        alphaArr = new Character[alphaList.size()];
        alphaArr = alphaList.toArray(alphaArr);

    }

    /**
     * Either reads new character from a given file or generates one
     *
     * @return new character
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        if (gener) { //new character is generated
            int x = random.nextInt(alphaArr.length);
            return alphaArr[x];
        } else {

            int x = fr.read();

            // skips characters which don't have morse code equivalent
            while (!Character.isLetterOrDigit(x) && x != '?' && x != '=' && x != '/' && x != ' ' && x != -1 && x != 10) {
                x = fr.read();
            }

            if ((x == 10) || (x == -1)) { // end of file
                return -1;
            } else {
                return Character.toUpperCase(x);
            }

        }

    }
}
