package io.github.jmmedina00.adoolting.dto.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class OnlyImagesValidatorTest {
  OnlyImagesValidator validator = new OnlyImagesValidator();

  @Test
  public void isValidReturnsTrueIfAllImagesAreEitherJpegsOrPngs() {
    List<MultipartFile> files = List.of(
      new MockMultipartFile("testfile", "test", "image/jpeg", new byte[] {  }),
      new MockMultipartFile("testfile", "test", "image/png", new byte[] {  }),
      new MockMultipartFile("testfile", "test", "image/jpeg", new byte[] {  })
    );

    assertTrue(validator.isValid(files, null));
  }

  @Test
  public void isValidReturnsFalseIfAnyImagesAreOfBadTypes() {
    List<MultipartFile> files = List.of(
      new MockMultipartFile("testfile", "test", "image/jpeg", new byte[] {  }),
      new MockMultipartFile("testfile", "test", "image/png", new byte[] {  }),
      new MockMultipartFile(
        "testfile",
        "test",
        "application/json",
        new byte[] {  }
      )
    );

    assertFalse(validator.isValid(files, null));
  }

  @Test
  public void isValidReturnsFalseIfListHasMoreThanSixElements() {
    List<MultipartFile> files = Collections.nCopies(
      8,
      new MockMultipartFile("testfile", new byte[] {  })
    );

    assertFalse(validator.isValid(files, null));
  }

  @Test
  public void isValidReturnsTrueWithEssentiallyAnEmptyListIsReturned() {
    List<MultipartFile> emptyList = List.of(
      new MockMultipartFile("test", "", "", new byte[] {  })
    );

    assertTrue(validator.isValid(emptyList, null));
  }
}
