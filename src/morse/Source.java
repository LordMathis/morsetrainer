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
 * Trieda Source zabezpečuje zdroj znakov na prehrávanie v morzeovke, či už sa
 * majú generovať, alebo čítať zo súboru. Trieda rozširuje triedu InputStream a
 * návrat znakov na prehrávanie je zabezpečený pomocou metódy read()
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
     * Konštruktor triedy Source s parametrom File sa spustí v prípade, že
     * užívatel zvolí ako zdroj súbor
     *
     * @param file Parameter file reprezentuje zdrojový súbor z ktorého bude
     * morzeovka prehrávaná v prípade, že užívatel zvolil ako zdroj súbor
     * @throws FileNotFoundException Táto výnimka bude vyhodená v prípade, že
     * zadaný súbor neexistuje
     */
    public Source(File file) throws FileNotFoundException {
        gener = false;
        sourceFile = file;
        fr = new FileReader(sourceFile);
    }

    /**
     * Konštruktor triedy Source s parametrom Boolean[] sa spustí v prípade, že
     * užívatel zvolí možnosť vygenerovať znaky, ktoré sa budú hrať. V
     * konštruktore sa vytvorí pole možných znakov a nastavý sa príznak gener,
     * ktorý znamená, že pri spustení metódy read() sa má ďalší znak generovať a
     * nie čítať zo súboru
     *
     * @param chars Parameter chars je pole booleanov. Prvky poľa značia
     * jednotlivé triedy znakov, ktoré sa budú prehrávať.
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
     * Vygeneruje, alebo načíta zo súboru znaky, ktoré sa majú prehrávať
     *
     * @return znak, ktorý sa má prehrávať
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        if (gener) { //ak treba nový znak vygenerovať
            int x = random.nextInt(alphaArr.length);
            return alphaArr[x]; //vráti sa znak z tabuľky znakov s indexom náhodného integeru
        } else {

            int x = fr.read();

            // Vďaka tomuto while cyklu sa preskočia všetky znaky, ktoré nemajú svoj ekvivalent v morzeovke
            while (!Character.isLetterOrDigit(x) && x != '?' && x != '=' && x != '/' && x != ' ' && x != -1 && x != 10) {
                x = fr.read();
            }

            if ((x == 10) || (x == -1)) { // V prípade, že sme na konci súboru
                return -1;
            } else {
                return Character.toUpperCase(x);
            }

        }

    }
}
