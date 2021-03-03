package org.tutar.bot;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.tutar.bot.handler.Manager;
import org.tutar.bot.handler.Request;

import java.util.Map;

/**
 * CI WebHook请求
 */
@Slf4j
@RestController
public class RobotController {

    @Autowired
    private Manager manager;

    /**
     * receive webhook invoke
     * @return
     */
    @PostMapping("/robot/send")
    public Object send(@RequestBody Map<String,Object> param,
                       @RequestHeader HttpHeaders headers){
        try{
            Request request = new Request();
            request.setHeader(headers);
            request.setMessage(param);
            manager.doFilter(request);

        } catch (Exception e){
            log.error(Throwables.getStackTraceAsString(e));
            return "{\"errcode\":300001,\"errmsg\":\"not support event\"}";
        }


        log.debug(param.toString());
        return "{\"errcode\":300001,\"errmsg\":\"not support event\"}";
    }
}
