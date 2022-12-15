package io.github.jmmedina00.adoolting.repository.page;

import io.github.jmmedina00.adoolting.entity.page.Page;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PageRepository extends JpaRepository<Page, Long> {
  @Query(
    "SELECT p FROM Page p WHERE p.createdByPerson.id=:personId AND " +
    "p.deletedAt IS NULL"
  )
  List<Page> findPagesCreatedByPerson(@Param("personId") Long personId);

  @Query("SELECT p FROM Page p WHERE p.id=:pageId AND p.deletedAt IS NULL")
  Optional<Page> findActivePage(@Param("pageId") Long pageId);
}
