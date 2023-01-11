package io.github.jmmedina00.adoolting.entity.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class PageTest {

  @Test
  public void getFullNameReturnsPageNameAsIs() {
    String name = "This is a page";
    Page page = new Page();
    page.setName(name);

    assertEquals(name, page.getFullName());
    assertEquals(page.getName(), page.getFullName());
  }
}
