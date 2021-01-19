package org.tutar.bot.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bot.ding-talk")
@Data
public class DingTalkProperties {

    /**
     * 钉钉安全设置-加签密钥
     */
    private String secret;

    private String webHookUrl;
}
