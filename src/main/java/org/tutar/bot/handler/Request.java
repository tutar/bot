package org.tutar.bot.handler;

import lombok.Data;
import org.springframework.http.HttpHeaders;

import java.util.Map;

/**
 * CI WebHook请求参数
 */
@Data
public class Request {

    private HttpHeaders header;
    private Map<String,Object> message;
}
