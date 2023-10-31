package comp128.translator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.json.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * Utility class to get data from the Wikipedia API
 */
public class WikipediaProvider {

    private List<String> languageCodes; // The list of language codes to use when translating

    private static final String USER_AGENT = "128-translation/1.0 (https://macalester.edu/academics/mscs/; bjackson@macalester.edu)"; // DO NOT MODIFY THIS.
    private static final ExecutorService requestQueue = Executors.newCachedThreadPool();


    /**
     *
     * @param languageCodes List of language codes to use when translating
     */
    public WikipediaProvider(List<String> languageCodes) {
        this.languageCodes = languageCodes;
    }


    /**
     * @return The list of installed languages.
     */
    public List<String> getLanguageCodes() {
        return Collections.unmodifiableList(languageCodes);
    }

    /**
     * Returns a local page with a particular title or null if the page does not exist in wikipedia
     */
    public LocalPage getLocalPageByTitle(String languageCode, String title) {
        String pageQuery = "https://"+languageCode+".wikipedia.org//w/api.php?page="+encodeValue(title)+"&format=json&action=parse";
        JSONObject pageResult = sendGET(pageQuery);

        if (pageResult.keySet().contains("error") && pageResult.getJSONObject("error").getString("code").equals("missingtitle")){
            return null;
        }
        return new LocalPage(languageCode, title);
    }

    /**
     * Return n random page texts in a particular language
     * @param languageCode e.g. "en"
     * @param n the number of random pages to return. Must be between 1 and 500.
     * @return Queue of strings containing the text of the random pages
     */
    public Queue<String> getRandomPageTexts(String languageCode, int n) {
        assert(n > 0 && n <= 500);

        ConcurrentLinkedQueue<String> concurrentResults = new ConcurrentLinkedQueue<String>();
        List<Future<?>> futures = new ArrayList<>(n);

        String query = "https://"+languageCode+".wikipedia.org/w/api.php?action=query&format=json&list=random&rnnamespace=0&rnlimit="+n;
        JSONObject queryResult = sendGET(query);

        JSONArray pageObjects =  queryResult.getJSONObject("query").getJSONArray("random");
        for(int i=0; i < pageObjects.length(); i++){
            String title = pageObjects.getJSONObject(i).getString("title");
            PageQuery pageQuery = new PageQuery(concurrentResults, languageCode, title);
            futures.add(requestQueue.submit(pageQuery));
        }

        // Synchronize
        for(Future<?> future : futures) {
            try {
                future.get();
            }catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        }
        return concurrentResults;
    }

    /**
     * Returns a list of the pages that represent the same concept in other languages.
     *
     * @param page
     * @return  pages that represent the same concept
     */
    public List<LocalPage> getInOtherLanguages(LocalPage page) {
        List<LocalPage> results = new ArrayList<>(languageCodes.size());

        for(String langCode : languageCodes) {
            if (langCode.equals(page.getLanguageCode())){
                continue;
            }
            String query = "https://" + page.getLanguageCode() + ".wikipedia.org/w/api.php?action=query&format=json&titles=" + encodeValue(page.getTitle()) + "&prop=langlinks&formatversion=2&lllang=" + langCode;
            //System.out.println("GetInOtherLangs: " + query);
            JSONObject queryResult = sendGET(query);
            //System.out.println("\tResponse: "+queryResult.toString());

            try {
                JSONArray pages = queryResult.getJSONObject("query").getJSONArray("pages");
                for (int i = 0; i < pages.length(); i++) {
                    JSONArray languageLinks = pages.getJSONObject(i).getJSONArray("langlinks");
                    for (int j = 0; j < languageLinks.length(); j++) {
                        JSONObject langLink = languageLinks.getJSONObject(j);
                        if (langLink.getString("lang").equals(langCode)) {
                            results.add(new LocalPage(langCode, langLink.getString("title")));
                        }
                    }
                }
            }catch(JSONException e){
                // ignore the page if we can't find it in any other languages
            }
        }

        return results;

    }

    /**
     * Encode url strings into utf-8 format escaping characters as needed
     * @param value
     * @return
     */
    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    /**
     * Sends an HTTP get request to the url that was passed in as a parameter.
     * @param url
     * @return a json object with the response or null if there is an error.
     */
    private static JSONObject sendGET(String url) {
        StringBuffer response = new StringBuffer();
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = con.getResponseCode();
            //System.out.println("GET Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return new JSONObject(response.toString());
            } else {
                System.out.println("GET request failed with response code: "+responseCode);
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (JSONException e){
            System.out.println(response);
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Inner runnable class used for multithreading page requests to fill a queue with their text.
     */
    private static class PageQuery implements Runnable {
        private ConcurrentLinkedQueue<String> results;
        private String languageCode;
        private String title;

        public PageQuery(ConcurrentLinkedQueue<String> results, String languageCode, String title){
            this.results = results;
            this.languageCode = languageCode;
            this.title = title;
        }

        public void run() {
            String pageQuery = "https://"+languageCode+".wikipedia.org/w/api.php?page="+encodeValue(title)+"&format=json&action=parse";
            JSONObject pageResult = sendGET(pageQuery);

            if (pageResult.keySet().contains("error") && pageResult.getJSONObject("error").getString("code").equals("missingtitle")){
                return;
            }

            Document htmlDoc = Jsoup.parse(pageResult.getJSONObject("parse").getJSONObject("text").getString("*"));
            Element body = htmlDoc.body();
            results.offer(body.text());
        }
    }
}
