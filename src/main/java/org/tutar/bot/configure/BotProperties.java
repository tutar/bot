package org.tutar.bot.configure;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bot")
@Data
public class BotProperties {

    private Gitlab gitlab;

    @Data
    public static class Gitlab{
        private String jobBaseUrl;
        private String pipelineBaseUrl;
    }
}
