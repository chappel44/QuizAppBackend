package com.tasks.organizer.attempt.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tasks.organizer.attempt.dto.response.TopicBest;
import com.tasks.organizer.attempt.dto.response.VersionStats;
import com.tasks.organizer.attempt.entity.Attempt;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {
  Optional<Attempt> findById(UUID id);

  @Query("""
  SELECT DISTINCT att FROM Attempt att
  LEFT JOIN FETCH att.topic t
  LEFT JOIN FETCH att.attemptQuestions aq
  LEFT JOIN FETCH aq.question q
  WHERE att.id = :attemptId
  """)
  Optional<Attempt> findByTopicIdWithAnswers(@Param("attemptId") UUID attemptId);

  Optional<List<Attempt>> findByUserIdAndTopicId(UUID userId, UUID topicId);

  @Query("""
    SELECT new com.tasks.organizer.attempt.dto.response.VersionStats(
      att.topicVersion, MAX(att.percentage), MIN(att.percentage), AVG(att.percentage), COUNT(att))
    FROM Attempt att
    WHERE att.topic.id = :topicId
      AND att.user.id = :userId
    GROUP BY att.topicVersion
    """)
  List<VersionStats> findVersionStats(@Param("topicId") UUID topicId, @Param("userId") UUID userId);

  @Query("""
    SELECT att FROM Attempt att
    WHERE att.user.id = :userId
      AND att.topicVersion = (
        SELECT MAX(t.version) FROM Topic t
        WHERE t.id = att.topic.id
      )
      AND att.percentage = (
        SELECT MAX(a3.percentage) FROM Attempt a3
        WHERE a3.user.id = att.user.id AND a3.topic.id = att.topic.id
          AND a3.topicVersion = att.topicVersion
      )
    """)
  List<Attempt> findBestAttemptOnLatestTopicVersion(@Param("userId") UUID userId);

  @Query("""
    SELECT new com.tasks.organizer.attempt.dto.response.TopicBest(
      t.id,
      t.name,
      t.version,
      t.topicType,
      t.dueDate,
      COALESCE(att.percentage, 0.0),
      COALESCE(att.totalPoints, 0),
      COALESCE(att.pointsEarned, 0)
    )
    FROM Topic t
    LEFT JOIN Attempt att
      ON att.topic.id = t.id
    AND att.topicVersion = t.version
    AND att.user.id = :userId
    AND att.id = (
      SELECT a2.id FROM Attempt a2
      WHERE a2.topic.id = t.id
        AND a2.topicVersion = t.version
        AND a2.user.id = :userId
      ORDER BY a2.percentage DESC, a2.createdAt DESC
      LIMIT 1
    )
    WHERE t.version = (
      SELECT MAX(t2.version) FROM Topic t2 WHERE t2.id = t.id
    )
    """)
  List<TopicBest> findBestOnCurrentVersionPerTopic(@Param("userId") UUID userId);
}
