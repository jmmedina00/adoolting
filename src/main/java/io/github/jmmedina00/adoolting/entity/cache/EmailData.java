package io.github.jmmedina00.adoolting.entity.cache;

import io.github.jmmedina00.adoolting.entity.cache.simple.SimplePerson;
import io.github.jmmedina00.adoolting.entity.person.Person;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("emaildata")
public class EmailData implements Serializable {
  private String id;
  private SimplePerson person;
  private HashMap<String, String> parameters;
  private String subjectAddendum = "";
  private List<String> subjectArguments;

  public EmailData() {
    subjectArguments = new ArrayList<>();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public SimplePerson getPerson() {
    return person;
  }

  public void setPerson(SimplePerson person) {
    this.person = person;
  }

  public void setPerson(Person person) {
    this.person = new SimplePerson(person);
  }

  public HashMap<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(HashMap<String, String> parameters) {
    this.parameters = parameters;
  }

  public String getSubjectAddendum() {
    return subjectAddendum;
  }

  public void setSubjectAddendum(String subjectAddendum) {
    this.subjectAddendum = subjectAddendum;
  }

  public List<String> getSubjectArguments() {
    return subjectArguments;
  }

  public void setSubjectArguments(List<String> subjectArguments) {
    this.subjectArguments = subjectArguments;
  }
}
