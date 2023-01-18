package io.github.jmmedina00.adoolting.repository.page;

import io.github.jmmedina00.adoolting.entity.page.PageManager;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PageManagerRepository
  extends JpaRepository<PageManager, Long> {
  @Query(
    "SELECT pm FROM PageManager pm WHERE pm.page.id=:pageId AND " +
    "pm.deletedAt IS NULL"
  )
  List<PageManager> findPageManagers(@Param("pageId") Long pageId);

  @Query(
    "SELECT pm FROM PageManager pm WHERE pm.person.id=:personId AND " +
    "pm.deletedAt IS NULL"
  )
  List<PageManager> findPersonManagements(@Param("personId") Long personId);

  @Query(
    "SELECT pm FROM PageManager pm WHERE pm.page.id=:pageId AND " +
    "pm.person.id=:personId AND pm.deletedAt IS NULL"
  )
  Optional<PageManager> findPageManagerInstance(
    @Param("pageId") Long pageId,
    @Param("personId") Long personId
  );
}
