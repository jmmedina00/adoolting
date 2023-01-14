package io.github.jmmedina00.adoolting.dto.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jmmedina00.adoolting.dto.interaction.NewComment;
import io.github.jmmedina00.adoolting.dto.interaction.NewPost;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class OnlyOneKindOfMediaValidatorTest {
  OnlyOneKindOfMediaValidator validator = new OnlyOneKindOfMediaValidator();

  @Test
  public void isValidReturnsTrueWhenBothMediaAndUrlHaveNotBeenPopulated() {
    NewComment comment = new NewComment();
    comment.setFile(new MockMultipartFile("test", "", "", new byte[] {  }));
    comment.setUrl("");

    assertTrue(validator.isValid(comment, null));
  }

  @Test
  public void isValidReturnsTrueWhenOnlyFilesHaveBeenPopulated() {
    NewPost post = new NewPost();
    post.setUrl("");
    post.setMedia(
      List.of(
        new MockMultipartFile("test", "test", "image/jpeg", new byte[] {  }),
        new MockMultipartFile("test", "test", "image/png", new byte[] {  })
      )
    );

    assertTrue(validator.isValid(post, null));
  }

  @Test
  public void isValidReturnsTrueWhenOnlyUrlHasBeenPopulated() {
    NewComment comment = new NewComment();
    comment.setFile(new MockMultipartFile("test", "", "", new byte[] {  }));
    comment.setUrl("http://test.local");

    assertTrue(validator.isValid(comment, null));
  }

  @Test
  public void isValidReturnsFalseWhenBothUrlAndFilesHaveBeenPopulated() {
    NewPost post = new NewPost();
    post.setUrl("https://test.local");
    post.setMedia(
      List.of(
        new MockMultipartFile("test", "test", "image/jpeg", new byte[] {  }),
        new MockMultipartFile("test", "test", "image/png", new byte[] {  })
      )
    );

    assertFalse(validator.isValid(post, null));
  }
}
