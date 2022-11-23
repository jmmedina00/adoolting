package io.github.jmmedina00.adoolting.entity.util;

import java.io.Serializable;
import java.util.HashMap;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("emaildata")
public class EmailData implements Serializable {
  private String id;
  private String destination;
  private String locale;
  private HashMap<String, String> parameters;

  public EmailData() {}

  public EmailData(String destination, String locale) {
    this.destination = destination;
    this.locale = locale;
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

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public HashMap<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(HashMap<String, String> parameters) {
    this.parameters = parameters;
  }
}
