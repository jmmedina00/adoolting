package io.github.jmmedina00.adoolting.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.List;
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
public class MediumTest {

  private static Arguments mapRef(String ref) {
    return Arguments.of(ref);
  }

  private static Stream<Arguments> cdnReferences() {
    return List
      .of("cdn:.jpg", "cdn:.jpeg", "cdn:.png", "cdn:whatever:P")
      .stream()
      .map(MediumTest::mapRef);
  }

  private static Stream<Arguments> nonCdnReferences() {
    return List
      .of(
        "http://test.local",
        "this is not correct",
        "png_cdn:",
        "https://wikipedia.org"
      )
      .stream()
      .map(MediumTest::mapRef);
  }

  @ParameterizedTest
  @MethodSource("cdnReferences")
  public void isInCDNReturnsTrueWhenCDNMarkIsPresentAtTheStartOfTheReference(
    String cdnReference
  ) {
    Medium medium = new Medium();
    medium.setReference(cdnReference);

    assertTrue(medium.isInCDN());
  }

  @ParameterizedTest
  @MethodSource("nonCdnReferences")
  public void isInCDNReturnsFalseWhenAnythingOtherThanCDNMarkIsAtStartOfTheReference(
    String nonCdnReference
  ) {
    Medium medium = new Medium();
    medium.setReference(nonCdnReference);

    assertFalse(medium.isInCDN());
  }
}
