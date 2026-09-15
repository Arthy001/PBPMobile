package com.example.uhf_bt.models;

public class ResponseDto {
    public boolean isSuccess;  // Changed from IsSuccess to isSuccess
    public String message;     // Changed from Message to message
    public int statusCode;     // Changed from StatusCode to statusCode
    public Object result;      // Changed from Result to result

    // Optional: Add getters and setters if you prefer
    public boolean isSuccess() {
        return isSuccess;
    }
    public boolean getIsSuccess() {
        return isSuccess;
    }
    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
