package io.github.jmmedina00.adoolting.repository.group;

import io.github.jmmedina00.adoolting.entity.group.JoinRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JoinRequestRepository
  extends JpaRepository<JoinRequest, Long> {
  @Query(
    "SELECT j FROM JoinRequest j WHERE j.group.id=:groupId AND " +
    "j.ignoredAt IS NULL AND j.deletedAt IS NULL"
  )
  List<JoinRequest> findExistingForGroup(@Param("groupId") Long groupId);

  @Query(
    "SELECT j FROM JoinRequest j WHERE j.group.id=:groupId AND " +
    "(j.interactor.id=:personId OR j.receiverInteractor.id=:personId) AND j.ignoredAt IS NULL AND j.deletedAt IS NULL"
  )
  JoinRequest findExistingForInteractorsAndGroup(
    @Param("personId") Long personId,
    @Param("groupId") Long groupId
  );
}
