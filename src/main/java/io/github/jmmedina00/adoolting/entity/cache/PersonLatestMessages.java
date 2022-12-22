package io.github.jmmedina00.adoolting.entity.cache;

import io.github.jmmedina00.adoolting.entity.cache.simple.SimpleMessage;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("latestmessages")
public class PersonLatestMessages implements Serializable {
  private Long id;
  private Map<Long, SimpleMessage> messages;
  private Date updatedAt;

  public PersonLatestMessages() {}

  public Long getId() {
    return id;
  }

  public void setId(Long personId) {
    this.id = personId;
  }

  public Map<Long, SimpleMessage> getMessages() {
    return messages;
  }

  public void setMessages(Map<Long, SimpleMessage> messages) {
    this.messages = messages;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }
}
