package com.shop.youssef.shop_service.web;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalErrors {

    // أخطاء إدخال من العميل → 400
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }

    // الخدمات التابعة (wallet / inventory) مش متاحة → 503
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> serviceUnavailable(IllegalStateException ex) {
        return Map.of("error", ex.getMessage());
    }

    // بيانات مش موجودة → 404
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NoSuchElementException ex) {
        return Map.of("error", ex.getMessage());
    }

    // لو في FeignException جاي من خدمة تانية، رجّع نفس الستاتس قدر الإمكان
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> feign(FeignException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) status = HttpStatus.BAD_GATEWAY; // احتياطي
        return ResponseEntity.status(status).body(Map.of("error", ex.getMessage()));
    }
}
