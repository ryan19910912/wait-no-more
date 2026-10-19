package com.waitnomore.worker.config;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Data
@Configuration
@ConfigurationProperties(prefix = "custom")
public class CustomProperties {
    private Gov gov = new Gov();
    @Data public static class Gov { private Map<String, String> csvUrls; }
}
