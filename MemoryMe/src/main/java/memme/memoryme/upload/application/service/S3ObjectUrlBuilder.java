package memme.memoryme.upload.application.service;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Component
public class S3ObjectUrlBuilder {
    public String build(String key) {
        return "/v1/upload/object?key=" + UriUtils.encodeQueryParam(key, StandardCharsets.UTF_8);
    }
}
