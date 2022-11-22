package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
  List<Post> findDistinctByInteractorOrReceiverInteractor(
    Interactor interactor,
    Interactor interactor2
  );
}
