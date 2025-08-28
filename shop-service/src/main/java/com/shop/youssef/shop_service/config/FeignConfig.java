package com.shop.youssef.shop_service.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return new ErrorDecoder() {
            private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

            @Override
            public Exception decode(String methodKey, Response response) {
                int status = response.status();

                // أي 5xx من الخدمة البعيدة → رجّع 503 عندنا
                if (status >= 500) {
                    return new ResponseStatusException(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            "Upstream service unavailable (" + methodKey + ", status " + status + ")"
                    );
                }

                // نحافظ على 400/404 كأخطاء منطقية
                if (status == 400) {
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upstream returned 400");
                }
                if (status == 404) {
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upstream resource not found");
                }

                // الباقي الافتراضي
                return defaultDecoder.decode(methodKey, response);
            }
        };
    }
}
