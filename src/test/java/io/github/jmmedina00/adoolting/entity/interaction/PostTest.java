package io.github.jmmedina00.adoolting.entity.interaction;

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
public class PostTest {

  private static Stream<Arguments> contentTestCandidates() {
    return Stream.of(
      Arguments.of("   TEsting this  ", "TEsting this"),
      Arguments.of("   Nanananan", "Nanananan"),
      Arguments.of(
        "\r\n\r\nLorem ipsum\r\ndolor sit amet\r\n\r\n",
        "Lorem ipsum\r\ndolor sit amet"
      )
    );
  }

  @ParameterizedTest
  @MethodSource("contentTestCandidates")
  public void getContentIsAlwaysReturnedWithLeadingSpacesRemoved(
    String content,
    String expected
  ) {
    Post post = new Post();
    post.setContent(content);

    assertEquals(expected, post.getContent());
  }
}
