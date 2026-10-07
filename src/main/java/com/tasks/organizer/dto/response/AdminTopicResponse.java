package com.tasks.organizer.dto.response;

import java.util.List;
import java.util.UUID;

import com.tasks.organizer.dto.response.base.BaseTopic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ToString
@SuperBuilder @Getter @Setter 
public class AdminTopicResponse extends BaseTopic{
  UUID sectionId;
  List<AdminQuestionResponse> questions;
  Integer questionPoolSize;
}
