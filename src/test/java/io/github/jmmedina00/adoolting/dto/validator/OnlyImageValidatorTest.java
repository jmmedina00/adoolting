package io.github.jmmedina00.adoolting.dto.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class OnlyImageValidatorTest {
  OnlyImageValidator validator = new OnlyImageValidator();

  private static Stream<Arguments> typesAndExpectedResults() {
    return Stream.of(
      Arguments.of("image/png", true),
      Arguments.of("image/jpeg", true),
      Arguments.of("image/svg+xml", false),
      Arguments.of("application/json", false),
      Arguments.of("audio/midi", false)
    );
  }

  @ParameterizedTest
  @MethodSource("typesAndExpectedResults")
  public void isValidReturnsTrueOrFalseDependingOnFileMimeType(
    String type,
    boolean expected
  ) {
    MockMultipartFile file = new MockMultipartFile(
      "test",
      "test",
      type,
      new byte[] {  }
    );

    assertEquals(expected, validator.isValid(file, null));
  }

  @Test
  public void isValidReturnsTrueWhenNoActualFileIsProvided() {
    MockMultipartFile notAFile = new MockMultipartFile(
      "test",
      "",
      "",
      new byte[] {  }
    );

    assertTrue(validator.isValid(notAFile, null));
  }
}
