package com.apxpert.weien.exception;

import com.apxpert.weien.dto.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler  {
    // 處理 IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return new ResponseEntity<>(Result.fail(ex.getMessage()), HttpStatus.OK);
    }

    // 通用異常處理
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex, WebRequest request) {
        String message = "系統錯誤，請稍後再試。";
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
