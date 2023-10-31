package comp128.translator;

import java.util.Objects;

/**
 * Represents a language specific wikipedia page
 */
public class LocalPage {
    private String languageCode;
    private String title;

    public LocalPage(String languageCode, String title){
        this.languageCode = languageCode;
        this.title = title;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalPage localPage = (LocalPage) o;
        return languageCode.equals(localPage.languageCode) &&
                title.equals(localPage.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(languageCode, title);
    }
}
