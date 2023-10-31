package comp128.translator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * A program to extract entities from a possibly multilingual text.
 *
 * @author Shilad Sen, Bret Jackson
 */
public class EntityExtractor {
    private final WikipediaProvider wikAPIdia;
    private final LanguageDetector detector;

    /**
     * Creates a new entity extractor.
     * @param wikAPIdia
     * @param detector
     */
    public EntityExtractor(WikipediaProvider wikAPIdia, LanguageDetector detector) {
        this.wikAPIdia = wikAPIdia;
        this.detector = detector;
    }

    /**
     * Extracts entities from a text in some unknown language and prints translations if found using System.out.println
     * @param text The text to extract entities from
     * @param goalLanguageCode The target language to translate entities to.
     */
    public void extract(String text, String goalLanguageCode) {
        String startLangCode = detector.detect(text);
        List<String> words = Utils.splitWords(text);
        System.out.println("Translating from " + startLangCode + " to " + goalLanguageCode);
        for (String word : words) {
            String translation = translateWord(startLangCode, word, goalLanguageCode);
            if (translation != null) {
                System.out.println("\t" + word + "=>" + translation);
            }   
        }
    }

    /**
     * Finds the translation for the word  if possibe 
     * @param startLangCode The language of the text being translated
     * @param word The word to be translated
     * @param goalLanguageCode The target language to translate entities to.
     * @return The translation of the word
     */
    private String translateWord(String startLangCode, String word, String goalLanguageCode) {
        LocalPage page = wikAPIdia.getLocalPageByTitle(startLangCode, word);
        String translation = findPageInLanguage(page, goalLanguageCode);
        if (translation == null) {
            translation = findOnWiki(word);   
        } 
        return translation;
    }

    /**
     * Finds the page in the given goal language if possible
     * @param page The page to be found in another language
     * @param goalLanguageCode The language that the page needs to be found in
     * @return
     */
    private String findPageInLanguage(LocalPage page, String goalLanguageCode) {
        if (page != null) {
            List<LocalPage> pages = wikAPIdia.getInOtherLanguages(page);
            for (LocalPage langPage : pages) {
                if (langPage.getLanguageCode().equals(goalLanguageCode)) {
                    return langPage.getTitle();
                }
            }
        }
        return null;
    }

    /**
     * Tries a search in the English Wikipedia for the given word and returns it if found
     * @param word The word to be searched for
     * @return the title of the English wikipedia page that contains
     */
    private String findOnWiki(String word) {
        LocalPage page = wikAPIdia.getLocalPageByTitle(Utils.LANG_ENGLISH, word);
        if (page == null) {
            return null;
        }
        return page.getTitle();
    }

    public static void main(String args[]) throws IOException {
        WikipediaProvider wrapper = new WikipediaProvider(Utils.ALL_LANGS);
        
        // prepare the detector
        LanguageDetector detector = new LanguageDetector(wrapper);
        detector.train();

        EntityExtractor extractor = new EntityExtractor(wrapper, detector);

        // extract and translate!
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "utf-8"));
        while (true) {
            System.out.println("Enter text to translate, or 'stop'.");
            String text = in.readLine();
            if (text.trim().equalsIgnoreCase("stop")) {
                System.out.println("bye :(");
                break;
            }
            extractor.extract(text, Utils.LANG_ENGLISH);
        }
    }
}
