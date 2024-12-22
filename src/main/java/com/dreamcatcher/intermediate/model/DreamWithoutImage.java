package com.dreamcatcher.intermediate.model;

import java.time.LocalDateTime;

public interface DreamWithoutImage {
    Long getId();
    String getContent();
    LocalDateTime getCreatedAt();
    User getUser();
    String getJobId();
}


