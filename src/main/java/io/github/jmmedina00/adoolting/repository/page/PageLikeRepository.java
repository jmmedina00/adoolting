package io.github.jmmedina00.adoolting.repository.page;

import io.github.jmmedina00.adoolting.entity.page.PageLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PageLikeRepository extends JpaRepository<PageLike, Long> {
  @Query(
    "SELECT COUNT(l) FROM PageLike l WHERE l.receiverInteractor.id=:pageId AND l.deletedAt IS NULL"
  )
  Long countPageLikes(@Param("pageId") Long pageId);

  @Query(
    "SELECT l FROM PageLike l WHERE l.interactor.id=:personId AND l.receiverInteractor.id=:pageId AND l.deletedAt IS NULL"
  )
  PageLike findPageLikeFromPerson(
    @Param("personId") Long personId,
    @Param("pageId") Long pageId
  );
}
