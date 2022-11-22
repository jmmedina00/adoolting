package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {}
