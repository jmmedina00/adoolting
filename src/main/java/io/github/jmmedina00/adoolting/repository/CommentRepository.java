package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  List<Comment> findByReceiverInteractionId(Long interactionId);
}
