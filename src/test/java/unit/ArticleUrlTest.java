package unit;

import captura.domain.ArticleUrl;
import captura.exceptions.InvalidArticleUrl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ArticleUrlTest {

    @Test
    @DisplayName("check if null value throw an exception")
    public void checkNullUrl() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> {
                    ArticleUrl.of(null);
                });
    }

    @Test
    @DisplayName("check if given url is valid for article")
    public void checkValidUrl() {
        final var url = "https://www.google.com/";
        var articleUrl = ArticleUrl.of(url);
        Assertions.assertEquals(articleUrl.getUrl(), url);
    }

    @Test
    @DisplayName("check if given url is invalid for article")
    public void checkInvalidUrl() {
        final var url = "://www.google.com/";
        var exception =
                Assertions.assertThrows(
                        InvalidArticleUrl.class,
                        () -> {
                            ArticleUrl.of(url);
                        });
        var expectedMessage = "Invalid url";
        var actualMessage = exception.getMessage();
        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }
}