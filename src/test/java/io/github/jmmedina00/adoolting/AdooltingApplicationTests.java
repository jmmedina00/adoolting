package io.github.jmmedina00.adoolting;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class AdooltingApplicationTests {

  @Test
  void contextLoads() {}
}
