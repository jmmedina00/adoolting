package io.github.jmmedina00.adoolting.repository;

import io.github.jmmedina00.adoolting.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {}
