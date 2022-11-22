package io.github.jmmedina00.adoolting.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class NewPost {
  @NotNull
  @NotEmpty
  private String contents;

  public String getContents() {
    return contents;
  }

  public void setContents(String contents) {
    this.contents = contents;
  }
}
