package io.github.jmmedina00.adoolting.repository.interaction;

import io.github.jmmedina00.adoolting.entity.interaction.ProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilePictureRepository
  extends JpaRepository<ProfilePicture, Long> {}
