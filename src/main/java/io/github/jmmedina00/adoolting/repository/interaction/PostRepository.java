package io.github.jmmedina00.adoolting.repository.interaction;

import io.github.jmmedina00.adoolting.entity.interaction.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {}
