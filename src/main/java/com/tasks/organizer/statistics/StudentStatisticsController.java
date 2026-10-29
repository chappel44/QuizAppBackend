package com.tasks.organizer.statistics;

import com.tasks.organizer.topic.TopicRepository;
import com.tasks.organizer.topic.entity.Topic;
import com.tasks.organizer.topic.entity.Topic.TopicType;

import org.springframework.web.bind.annotation.RestController;

import com.tasks.organizer.attempt.dto.response.AttemptResponse;
import com.tasks.organizer.attempt.dto.response.TopicBest;
import com.tasks.organizer.attempt.dto.response.VersionStats;
import com.tasks.organizer.attempt.entity.Attempt;
import com.tasks.organizer.attempt.mapper.AttemptMapper;
import com.tasks.organizer.attempt.repository.AttemptRepository;
import com.tasks.organizer.authorization.OrgAuthorization;
import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.organizationmember.OrganizationMemberRepository;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.organizationmember.entity.OrganizationMember;
import com.tasks.organizer.statistics.dto.response.StudentAverageResponse;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor 
public class StudentStatisticsController {
  //Repositories
  private final TopicRepository topicRepository;
  private final OrganizationMemberRepository organizationMemberRepository;
  private final AttemptRepository attemptRepository;

  //Services
  private final OrgAuthorization orgAuth;

  @GetMapping("/api/authenticated/organizations/topics/{topicId}/students/{userId}/statistics")
  public ResponseEntity<ApiResponse<Object>> getOrgStudentAttemptsPerTopicVersion(@PathVariable UUID userId, @PathVariable UUID topicId) {
    Topic topic = topicRepository.findById(topicId).orElse(null);

    if(topic == null){
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Topic not found. " + topicId.toString());
    }

    //Ensure admin has permissions to view statistics
    if(!orgAuth.hasPermissionVerified(topic.getOrganization().getId(), OrgPermissions.VIEW_STATISTICS)){
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Topic not found. " + topicId.toString());
    }

    UUID organizationId = orgAuth.currentOrgContext().organizationId();

    //Ensure the user is a member in their organization
    if(!organizationMemberRepository
      .existsByOrganizationIdAndUserIdAndIsActiveTrue(organizationId, userId)
    ) {
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Topic not found. " + topicId.toString());
    }

    List<VersionStats> userVersionStats = attemptRepository.findVersionStats(topicId, userId);

    return ApiResponse.ok("User versionStats retrieved", userVersionStats);
  }

  /*
    Any individual students average is calculated by taking the most recent 
  */
  @GetMapping("/api/authenticated/organizations/students/{userId}/statistics")
  public ResponseEntity<ApiResponse<Object>> getOrgStudentBestAttemptsOnLatestTopics(@PathVariable UUID userId) {
    UUID organizationId = orgAuth.currentOrgContext().organizationId();

    //Ensure admin has permissions to view statistics
    if(organizationId == null || !orgAuth.hasPermissionVerified(organizationId, OrgPermissions.VIEW_STATISTICS)){
      return ApiResponse.error(HttpStatus.NOT_FOUND, "user not found. " + userId);
    }

    //Ensure the user is a member in their organization
    if(!organizationMemberRepository
      .existsByOrganizationIdAndUserIdAndIsActiveTrue(organizationId, userId)
    ) {
      System.out.println("User is not an active organization member");
      return ApiResponse.error(HttpStatus.NOT_FOUND, "User not found. ");
    }

    List<TopicBest> bestAttemptOnLatestTopics = attemptRepository.findBestOnCurrentVersionPerTopic(userId);

    Map<TopicType, Double> pointsEarnedMap = new HashMap<>();
    Map<TopicType, Double> topicTotalPointsMap = new HashMap<>();

    Double pointsEarned = 0.0;
    Double totalPoints = 0.0;

    for (TopicBest tb : bestAttemptOnLatestTopics) {
      pointsEarnedMap.merge(tb.topicType(), (double) tb.pointsEarned(), Double::sum);
      topicTotalPointsMap.merge(tb.topicType(), (double) tb.totalPoints(), Double::sum);

      pointsEarned += tb.pointsEarned();
      totalPoints += tb.totalPoints();
    }
    Double studentAverage = pointsEarned / totalPoints;

    Map<TopicType, Double> topicTypeAverage = new HashMap<>();
    for (TopicType type : topicTotalPointsMap.keySet()) {
      double total = topicTotalPointsMap.get(type);
      double earned = pointsEarnedMap.get(type);
      topicTypeAverage.put(type, total == 0 ? 0.0 : earned / total);
    }

    StudentAverageResponse studentAverageResponse = StudentAverageResponse.builder()
      .studentAverage(studentAverage)
      .topicBest(bestAttemptOnLatestTopics)
      .averagesPerTopicType(topicTypeAverage)
    .build();

    return ApiResponse.ok("User average received: ", studentAverageResponse);
  }
}
