package comp128.translator;

import java.util.Arrays;
import java.util.List;

/**
 * Utilities for HW2
 * @author Shilad Sen, Bret Jackson
 */
public class Utils {
    /**
     * Individual languages installed in the database.
     */
    public static final String LANG_ENGLISH = "en";
    public static final String LANG_HINDI = "hi";
    public static final String LANG_SCOTS = "sco";

    /**
     * A list of all languages to check for when detecting the language or translating.
     * Other languages can be added by adding their language code to the list.
     * Language codes can be found here: https://en.wikipedia.org/wiki/List_of_Wikipedias
     */
    public static final List<String> ALL_LANGS = Arrays.asList(
            LANG_ENGLISH, LANG_HINDI,
            LANG_SCOTS);

    /**
     * Splits a string into a list of words
     * @param string
     * @return
     */
    public static List<String> splitWords(String string) {
        return Arrays.asList(string.split("\\s+"));
    }
}
