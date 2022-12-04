package io.github.jmmedina00.adoolting.entity.cache;

import io.github.jmmedina00.adoolting.entity.person.PrivateMessage;
import java.io.Serializable;
import java.util.Date;

public class SimpleMessage implements Serializable, Comparable<SimpleMessage> {
  private String contents;
  private Date createdAt;
  private boolean isRead;

  public SimpleMessage() {}

  public SimpleMessage(PrivateMessage message) {
    contents = message.getContents();
    createdAt = message.getCreatedAt();
    isRead = true;
  }

  public String getContents() {
    return contents;
  }

  public void setContents(String contents) {
    this.contents = contents;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public boolean getIsRead() {
    return isRead;
  }

  public void setIsRead(boolean isRead) {
    this.isRead = isRead;
  }

  @Override
  public int compareTo(SimpleMessage other) {
    return createdAt.compareTo(other.getCreatedAt()) * -1;
  }
}
