package io.github.jmmedina00.adoolting.dto.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class OnlyImageRequiredValidatorTest {
  OnlyImageRequiredValidator validator = new OnlyImageRequiredValidator();

  private static Stream<Arguments> typesAndExpectedResults() {
    return Stream.of(
      Arguments.of("image/png", true),
      Arguments.of("image/jpeg", true),
      Arguments.of("image/svg+xml", false),
      Arguments.of("application/json", false),
      Arguments.of("audio/midi", false),
      Arguments.of("", false)
    );
  }

  @ParameterizedTest
  @MethodSource("typesAndExpectedResults")
  public void isValidReturnsExpectedResultDependingOnMimeType(
    String type,
    boolean expected
  ) {
    MockMultipartFile file = new MockMultipartFile(
      "testfile",
      "",
      type,
      new byte[] {  }
    );
    assertEquals(expected, validator.isValid(file, null));
  }
}
