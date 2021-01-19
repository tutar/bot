package org.tutar.bot.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public final class Manager {

    private List<Handler> filters;

    public Manager(List<Handler> optionalFilters){
        this.filters = optionalFilters;
    }

    /**
     * 责任链调用入口
     * @param request 待处理对象
     * @return
     */
    public void doFilter(Request request) throws Exception {
        for(Handler filter: filters){
            if(filter.isHandle(request) && !filter.process(request) ){
                log.info("filters process break at:{}",filter.getClass().getSimpleName());
                break;
            }
        }
        log.info("filter process finish...");
    }

}