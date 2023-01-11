package io.github.jmmedina00.adoolting.entity.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class LinkInformationTest {

  private static Stream<Arguments> linksAndCorrespondingPages() {
    return Stream.of(
      Arguments.of("https://www.linkedin.com/notifications/", "linkedin.com"),
      Arguments.of("https://en.wikipedia.org/wiki/Main_Page", "wikipedia.org"),
      Arguments.of("https://stackoverflow.com/questions", "stackoverflow.com"),
      Arguments.of("https://www.amazon.es/", "amazon.es"),
      Arguments.of(
        "https://www.w3schools.com/css/css_rwd_intro.asp",
        "w3schools.com"
      ),
      Arguments.of(
        "https://dev.to/zenstack/a-brief-history-of-api-rpc-rest-graphql-trpc-fme",
        "dev.to"
      ),
      Arguments.of(
        "https://techcrunch.com/2023/01/10/salesforce-turmoil-continues-into-new-year-as-recent-layoffs-attest/",
        "techcrunch.com"
      ),
      Arguments.of("http://dns.test.local/article", "test.local"),
      Arguments.of(
        "https://www.latimes.com/business/story/2022-12-15/amazon-ukraine-war-cloud-data",
        "latimes.com"
      ),
      Arguments.of("https://www.baeldung.com/java-xor-operator", "baeldung.com")
    );
  }

  @ParameterizedTest
  @MethodSource("linksAndCorrespondingPages")
  public void getPageReturnsOnlyInterestingPartOfTheActualLink(
    String actualLink,
    String expected
  ) {
    LinkInformation info = new LinkInformation();
    info.setActualLink(actualLink);

    assertEquals(expected, info.getPage());
  }
}
