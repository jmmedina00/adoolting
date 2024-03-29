package io.github.jmmedina00.adoolting.entity.group;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.enums.AccessLevel;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class PeopleGroup extends Interaction {
  @Column
  private String name;

  @Column
  private String description;

  @Enumerated(EnumType.STRING)
  @Column
  private AccessLevel accessLevel;

  @UpdateTimestamp
  private Date updatedAt;

  @OneToMany(mappedBy = "group")
  private List<JoinRequest> joinRequests;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public AccessLevel getAccessLevel() {
    return accessLevel;
  }

  public void setAccessLevel(AccessLevel accessLevel) {
    this.accessLevel = accessLevel;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public List<JoinRequest> getJoinRequests() {
    return joinRequests;
  }
}
