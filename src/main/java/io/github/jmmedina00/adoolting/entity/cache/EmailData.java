package io.github.jmmedina00.adoolting.entity.cache;

import io.github.jmmedina00.adoolting.entity.person.Person;
import java.io.Serializable;
import java.util.HashMap;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("emaildata")
public class EmailData implements Serializable {
  private String id;
  private String destination;
  private Long receiverPersonId;
  private HashMap<String, String> parameters;

  public EmailData() {}

  public EmailData(Person person) {
    destination = person.getEmail();
    receiverPersonId = person.getId();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public Long getReceiverPersonId() {
    return receiverPersonId;
  }

  public void setReceiverPersonId(Long receiverPersonId) {
    this.receiverPersonId = receiverPersonId;
  }

  public HashMap<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(HashMap<String, String> parameters) {
    this.parameters = parameters;
  }
}
