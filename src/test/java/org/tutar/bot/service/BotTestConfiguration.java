package org.tutar.bot.service;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.tutar.bot.configure.BotConfiguration;

@Configuration
@EnableAutoConfiguration
@ComponentScan({
        "org.tutar.bot"
})
@Import(BotConfiguration.class)
public class BotTestConfiguration {
}
