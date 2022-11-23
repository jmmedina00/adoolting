package io.github.jmmedina00.adoolting.entity.util;

import java.io.Serializable;
import java.util.HashMap;

public class EmailData implements Serializable {
  private String destination;
  private String locale;
  private HashMap<String, String> parameters;

  public EmailData() {}

  public EmailData(String destination, String locale) {
    this.destination = destination;
    this.locale = locale;
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
