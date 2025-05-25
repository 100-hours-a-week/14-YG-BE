package com.moogsan.moongsan_backend.global.exception.base;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.base.GroupBuyException;
import com.moogsan.moongsan_backend.global.dto.ErrorResponse;
import com.moogsan.moongsan_backend.global.exception.code.ErrorCode;
import com.moogsan.moongsan_backend.global.exception.code.ErrorCodeType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

// 글로벌 예외 처리기
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 400
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        // FieldError 리스트를 {필드명 → 메시지} 맵으로 변환
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing  // 동일 필드 중복 시 첫 메시지 유지
                ));

        return ResponseEntity
                .badRequest()
                .body(WrapperResponse.<Map<String, String>>builder()
                        .message("입력 형식이 올바르지 않습니다.")
                        .data(errors)
                        .build());
    }

    // 도메인 예외 처리
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<Object> handleBusinessException(BusinessException ex) {
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(WrapperResponse.<Object>builder()
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    // 500
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAllExceptions(Exception ex) {
        log.error("Unhandled exception caught:", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(WrapperResponse.<Object>builder()
                        .message("내부 서버 오류 발생")
                        .data(null)
                        .build());
    }
}