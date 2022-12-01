package io.github.jmmedina00.adoolting.dto.interaction;

import javax.validation.constraints.NotNull;

public class NewPostOnPage extends NewPost {
  @NotNull
  private Long postAs;

  public Long getPostAs() {
    return postAs;
  }

  public void setPostAs(Long postAs) {
    this.postAs = postAs;
  }
}
