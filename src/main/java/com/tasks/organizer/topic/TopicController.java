package com.tasks.organizer.topic;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.topic.dto.request.CreateTopicDTO;
import com.tasks.organizer.topic.service.TopicService;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

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
    @PostMapping("/api/authenticated/topics")
    public ResponseEntity<ApiResponse<Object>> createTopic(@Validated @RequestBody CreateTopicDTO entity, @RequestParam(required = true) UUID sectionId, @RequestParam(required = false) UUID organizationId) {
        return topicService.createTopic(entity, sectionId, organizationId);
    }

    @PatchMapping ("/api/authenticated/topics")
    public ResponseEntity<ApiResponse<Object>> updateTopic(@RequestParam UUID topicId, @Validated @RequestBody CreateTopicDTO request){
        return topicService.updateTopic(topicId, request);
    }

    @DeleteMapping ("/api/authenticated/topics")
    public ResponseEntity<ApiResponse<Object>> deleteTopic(@RequestParam(required = true) UUID topicId) {
        return topicService.deleteTopic(topicId);
    }

    /* ========== Authenticated Mappings ========== */
    @GetMapping ("/api/authenticated/topics")
    public ResponseEntity<ApiResponse<Object>> getTopicWithQuestionsAndAnswers(@RequestParam(required = true) UUID topicId) {
        return topicService.getTopicWithQuestionsAndAnswers(topicId);
    }
}
