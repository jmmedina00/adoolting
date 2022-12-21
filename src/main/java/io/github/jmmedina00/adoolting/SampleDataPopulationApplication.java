package io.github.jmmedina00.adoolting;

import io.github.jmmedina00.adoolting.dto.interaction.NewPost;
import io.github.jmmedina00.adoolting.service.interaction.PostService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SampleDataPopulationApplication implements CommandLineRunner {
  @Autowired
  private PostService postService;

  private static final Logger logger = LoggerFactory.getLogger(
    SampleDataPopulationApplication.class
  );

  public static void main(String[] args) {
    System.setProperty("spring.main.web-application-type", "NONE");

    logger.info("Running standalone application");
    ApplicationContext context = SpringApplication.run(
      SampleDataPopulationApplication.class,
      args
    );
    SpringApplication.exit(
      context,
      new ExitCodeGenerator() {

        @Override
        public int getExitCode() {
          return 0;
        }
      }
    );
    logger.info("Standalone application finished");
  }

  @Override
  public void run(String... args) throws Exception {
    RestTemplate template = new RestTemplate();
    String baconApi =
      "https://baconipsum.com/api/?type=meat-and-filler&paras=3&format=text";
    logger.debug("Calling Bacon Ipsum API...");
    String text = Optional
      .ofNullable(template.getForObject(baconApi, String.class))
      .orElse("");

    List<String> paragraphs = Arrays
      .asList(text.trim().split("\n"))
      .stream()
      .filter(p -> !p.isBlank())
      .toList();
    String sanitized = String.join("\r\n", paragraphs);

    logger.debug("Populating post DTO...");
    NewPost post = new NewPost();
    post.setUrl("");
    post.setMedia(new ArrayList<>());
    post.setContent(sanitized);

    logger.debug("Pushing for selected interactor...");
    postService.createPost(1L, post);
  }
}
