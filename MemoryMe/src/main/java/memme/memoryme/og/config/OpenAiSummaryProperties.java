package memme.memoryme.og.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.openai")
public class OpenAiSummaryProperties {
    private String apiKey;
    private String model = "gpt-4o-mini";
    private int timeoutSeconds = 12;
    private int maxTokens = 500;
}
