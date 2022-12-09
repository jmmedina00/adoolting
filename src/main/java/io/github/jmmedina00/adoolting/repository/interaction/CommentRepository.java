package io.github.jmmedina00.adoolting.repository.interaction;

import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  @Query(
    "SELECT c FROM Comment c WHERE c.receiverInteraction.id=:interactionId AND " +
    "c.deletedAt IS NULL ORDER BY c.createdAt DESC"
  )
  Page<Comment> findByReceiverInteraction(
    @Param("interactionId") Long interactionId,
    Pageable pageable
  );
}
