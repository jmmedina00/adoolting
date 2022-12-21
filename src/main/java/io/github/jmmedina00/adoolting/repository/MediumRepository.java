package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.Medium;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MediumRepository extends JpaRepository<Medium, Long> {
  @Query(
    "SELECT m FROM Medium m WHERE m.reference LIKE 'cdn:%' AND " +
    "m.interaction.interactor.id=(SELECT m2.interaction.interactor.id " +
    "FROM Medium m2 WHERE m2.id=:mediumId) ORDER BY m.createdAt"
  )
  List<Medium> findAllPicturesFromTheSameInteractor(
    @Param("mediumId") Long mediumId
  );

  @Query(
    "SELECT m FROM Medium m WHERE m.reference LIKE 'cdn:%' AND " +
    "m.interaction.interactor.id=:interactorId ORDER BY m.createdAt DESC"
  )
  Page<Medium> findPicturePageByInteractorId(
    @Param("interactorId") Long interactorId,
    Pageable pageable
  );
}
