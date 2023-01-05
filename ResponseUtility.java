package com.example.FinalProject.utils;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ResponseUtility {

    public String errorResponse(String message) {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        return errorResponse.toString();
    }

    public String successResponse(String message) {
        JSONObject successResponse = new JSONObject();
        successResponse.put("status", "success");
        successResponse.put("message", message);
        return successResponse.toString();
    }
}
