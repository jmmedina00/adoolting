package io.github.jmmedina00.adoolting.dto.person;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class NewMessage {
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
