package io.github.jmmedina00.adoolting.entity;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
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

  @UpdateTimestamp
  private Date updatedAt;

  @OneToMany(mappedBy = "group")
  private List<JoinRequest> joinRequests;
}
