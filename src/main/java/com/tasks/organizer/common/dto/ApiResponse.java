package com.tasks.organizer.common.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public record ApiResponse<T>(int status, String message, T data) {

  public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
    return of(HttpStatus.OK, message, data);
  }

  public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
    return of(status, message, null);
  }

  public static <T> ResponseEntity<ApiResponse<T>> of(HttpStatus status, String message, T data) {
    return ResponseEntity.status(status).body(new ApiResponse<>(status.value(), message, data));
  }
}

// Then each 5–6 line ResponseEntity.status(...).body(new ApiResponse<>(...)) block becomes one line:

// return ApiResponse.error(HttpStatus.NOT_FOUND, "Topic not found");
// return ApiResponse.ok("Section overview", overview);