package io.github.jmmedina00.adoolting.repository.page;

import io.github.jmmedina00.adoolting.entity.page.Page;
import org.springframework.data.jpa.domain.Specification;

public class PageSpecs {

  public static Specification<Page> pageNameContains(String term) {
    return (page, query, builder) ->
      builder.like(page.get("name"), "%" + term + "%");
  }
}
