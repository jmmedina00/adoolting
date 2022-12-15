package io.github.jmmedina00.adoolting.repository.interaction;

import io.github.jmmedina00.adoolting.entity.interaction.ProfilePicture;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfilePictureRepository
  extends JpaRepository<ProfilePicture, Long> {
  @Query(
    "SELECT p from ProfilePicture p WHERE " +
    "p.interaction.interactor.id=:interactorId AND p.deletedAt IS NULL AND " +
    "p.interaction.id NOT IN (SELECT c.id FROm Comment c) ORDER BY p.createdAt DESC"
  )
  List<ProfilePicture> findInteractorsProfilePictures(
    @Param("interactorId") Long interactorId
  );

  @Query(
    "SELECT p from ProfilePicture p WHERE p.interaction.id IN " +
    "(SELECT c.id FROM Comment c WHERE c.receiverInteraction.id=:groupId) AND " +
    "p.deletedAt IS NULL ORDER BY p.createdAt DESC"
  )
  List<ProfilePicture> findGroupsProfilePictures(
    @Param("groupId") Long groupId
  );
}
