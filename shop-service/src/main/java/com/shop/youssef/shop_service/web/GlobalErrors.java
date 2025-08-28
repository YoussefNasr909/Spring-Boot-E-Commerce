package com.shop.youssef.shop_service.web;

import feign.FeignException;
import feign.RetryableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalErrors {

    // 400: إدخال غير سليم
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }

    // 503: خدمة تابعة غير متاحة
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> serviceUnavailable(IllegalStateException ex) {
        return Map.of("error", ex.getMessage());
    }

    // 404: بيانات غير موجودة
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NoSuchElementException ex) {
        return Map.of("error", ex.getMessage());
    }

    // لو الخدمة التابعة رجعت Status معروف (FeignException يحمل status من السيرفر)
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> feign(FeignException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) status = HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status).body(Map.of("error", ex.getMessage()));
    }

    // الأهم: لو الخدمة مش بترد أصلاً (اتصال فشل/Timeout) → RetryableException
    @ExceptionHandler(RetryableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> feignRetryable(RetryableException ex) {
        return Map.of("error", "Downstream service unavailable. Please try again.");
    }

    // اختياري: أي استثناء غير متوقع → 502 أشيك من 500
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> generic(Exception ex) {
        return Map.of("error", "Upstream error");
    }
}
