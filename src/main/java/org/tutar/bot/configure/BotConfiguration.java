package org.tutar.bot.configure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.tutar.bot.service.DingTalkSendService;
import org.tutar.bot.service.FeishuTalkSendService;

@Configuration
@EnableConfigurationProperties
public class BotConfiguration {


    @Bean
    @ConditionalOnProperty(name = "bot.im",havingValue = "dingTalk")
    public DingTalkSendService dingTalkSendService(DingTalkProperties dingTalkProperties){
        return new DingTalkSendService(dingTalkProperties);
    }
    @Bean
    @ConditionalOnProperty(name = "bot.im",havingValue = "feishu", matchIfMissing = true)
    public FeishuTalkSendService feishuTalkSendService(FeishuTalkProperties feishuTalkProperties){
        return new FeishuTalkSendService(feishuTalkProperties);
    }
}
