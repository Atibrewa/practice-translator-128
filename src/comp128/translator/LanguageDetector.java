package comp128.translator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * The language detector should detect the language of a text.
 * It must be "trained" to learn the words that appear in each language.
 * After it is trained, detect will be called for each text.
 *
 * @author Shilad Sen
 */
public class LanguageDetector {
    /**
     * Wrapper over the WikAPIdia API.
     */
    private final WikipediaProvider wikAPIdia;

    private Map<String, Map<String, Integer>> languageWordMaps; // contains the lang code and the map of words and count
    private Map<String, Integer> languageTotalWords; // contains the lang code and the total number of words
    
    /**
     * Constructs a new language detector.
     * @param wikAPIdia
     */
    public LanguageDetector(WikipediaProvider wikAPIdia) {
        this.wikAPIdia = wikAPIdia;
        languageWordMaps = new HashMap<>(); 
        languageTotalWords = new HashMap<>();
    }

    /**
     * Learn the words used in each language.
     * Only needs to be called once each time your program is run.
     */
    public void train() {
        System.out.println("Starting training ...");
        List<String> languages = wikAPIdia.getLanguageCodes();
        for(String  lang : languages) {
            int totalCount = 0;
            System.out.println("\t Training " + lang + ":");
            Queue<String> texts = wikAPIdia.getRandomPageTexts(lang, 500);
            Map<String, Integer> newLang = new HashMap<>();
            for (String string : texts) {
                List<String> words = Utils.splitWords(string);
                for (String word : words) {
                    if (!newLang.containsKey(word)) {
                        newLang.put(word, 1);
                    } else {
                        int count = newLang.get(word);
                        newLang.put(word, count + 1);
                    }
                    totalCount++;
                }
            }
            languageWordMaps.put(lang, newLang);
            languageTotalWords.put(lang, totalCount);
        }
    }

    /**
     * Detect the language associated with a text.
     * @param text
     * @return the detected language code
     */
    public String detect(String text) {
        String bestLang = null;
        double bestScore = 0;
        List<String> words = Utils.splitWords(text);
        for (String lang : languageWordMaps.keySet()) {
            Map<String,Integer> langWords = languageWordMaps.get(lang);
            double totalScore = 0;
            for (String word : words) {
                if (langWords.get(word) != null) {
                    totalScore += langWords.get(word);
                }
            }
            double normalisedScore = totalScore/languageTotalWords.get(lang);
            if (normalisedScore > bestScore) {
                bestScore = normalisedScore;
                bestLang = lang;
            }
        }
        return bestLang;
    }

    public static void main(String args[]) throws IOException {
        WikipediaProvider wrapper = new WikipediaProvider(Utils.ALL_LANGS);

        // Check that the wikipedia api is working properly...
        // Example showing how to find the translation of apple in the other installed languages
        LocalPage page = wrapper.getLocalPageByTitle(Utils.LANG_ENGLISH, "Apple");
        System.out.println("Apple in other languages:");
        List<LocalPage> otherLangs = wrapper.getInOtherLanguages(page);
        for (LocalPage page2 : otherLangs) {
            System.out.println("\t" + page2.getLanguageCode() + ": " + page2.getTitle());
        }

        // prepare the detector
        LanguageDetector detector = new LanguageDetector(wrapper);
        detector.train();

        // use the detector
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "utf-8"));
        while (true) {
            System.out.println("Enter text to detect language, or 'stop'.");
            String text = in.readLine();
            if (text.trim().equalsIgnoreCase("stop")) {
                break;
            }
            System.out.println("language of text is " + detector.detect(text));
        }
    }
}
