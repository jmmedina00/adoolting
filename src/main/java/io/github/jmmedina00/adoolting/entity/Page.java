package io.github.jmmedina00.adoolting.entity;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Page extends Interactor {
  @Column
  private String name;

  @Column
  private String url;

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  private Date deletedAt;

  @ManyToOne
  private Person createdByPerson;

  @OneToMany(mappedBy = "page")
  private List<PageManager> managers;
}
