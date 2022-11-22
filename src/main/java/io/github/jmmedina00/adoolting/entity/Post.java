package io.github.jmmedina00.adoolting.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Post extends Interaction {
  @Column(columnDefinition = "TEXT")
  private String content;

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
