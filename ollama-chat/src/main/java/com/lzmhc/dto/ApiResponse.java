package com.lzmhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse {
    private String status;
    private String message;
    private Object content;

    public static ApiResponse success(String message, Object content) {
        return new ApiResponse("success", message, content);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse("error", message, null);
    }
}
