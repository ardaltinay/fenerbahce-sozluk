package net.fenerbahcesozluk.repository;

import net.fenerbahcesozluk.entity.Vote;
import net.fenerbahcesozluk.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {

  Optional<Vote> findByEntryIdAndUserId(UUID entryId, UUID userId);

  boolean existsByEntryIdAndUserId(UUID entryId, UUID userId);

  List<Vote> findByUserId(UUID userId);

  List<Vote> findByUserIdAndVoteType(UUID userId, VoteType voteType);

  // Batch load votes for multiple entries - solves N+1 problem
  @Query("SELECT v FROM Vote v WHERE v.entry.id IN :entryIds AND v.user.id = :userId")
  List<Vote> findByEntryIdsAndUserId(@Param("entryIds") List<UUID> entryIds, @Param("userId") UUID userId);

  void deleteByEntryIdAndUserId(UUID entryId, UUID userId);

  long countByVoteType(VoteType voteType);

  @Query("SELECT COUNT(v) FROM Vote v WHERE v.entry.author.id = :authorId AND v.voteType = :voteType")
  long countByEntryAuthorIdAndVoteType(@Param("authorId") UUID authorId, @Param("voteType") VoteType voteType);
}
