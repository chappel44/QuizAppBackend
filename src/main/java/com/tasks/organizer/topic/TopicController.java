package com.tasks.organizer.topic;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.ratelimit.KeyType;
import com.tasks.organizer.ratelimit.RateLimit;
import com.tasks.organizer.topic.dto.request.CreateTopicDTO;
import com.tasks.organizer.topic.service.TopicService;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
public class TopicController {
    final TopicRepository topicRepository;
    final TopicService topicService;

    /* ========== Admin Mappings ========== */
    @RateLimit(capacity = 100, period = 1, unit = ChronoUnit.HOURS, keyType = KeyType.USER_ORGANIZATION_ID)
    @PostMapping("/api/authenticated/topics")
    public ResponseEntity<ApiResponse<Object>> createTopic(@Validated @RequestBody CreateTopicDTO entity, @RequestParam(required = true) UUID sectionId) {
        return topicService.createTopic(entity, sectionId);
    }

    @RateLimit(capacity = 100, period = 1, unit = ChronoUnit.HOURS, keyType = KeyType.USER_ORGANIZATION_ID)
    @PatchMapping ("/api/authenticated/topics")
    public ResponseEntity<ApiResponse<Object>> updateTopic(@RequestParam UUID topicId, @Validated @RequestBody CreateTopicDTO request){
        return topicService.updateTopic(topicId, request);
    }

    @RateLimit(capacity = 50, period = 1, unit = ChronoUnit.HOURS, keyType = KeyType.USER_ORGANIZATION_ID)
    @DeleteMapping ("/api/authenticated/topics")
    public ResponseEntity<ApiResponse<Object>> deleteTopic(@RequestParam(required = true) UUID topicId) {
        return topicService.deleteTopic(topicId);
    }

    /* ========== Authenticated Mappings ========== */
    @RateLimit(capacity = 300, period = 1, unit = ChronoUnit.HOURS, keyType = KeyType.USER_ORGANIZATION_ID)
    @GetMapping ("/api/authenticated/topics")
    public ResponseEntity<ApiResponse<Object>> getTopicWithQuestionsAndAnswers(@RequestParam(required = true) UUID topicId) {
        return topicService.getTopicWithQuestionsAndAnswers(topicId);
    }
}
