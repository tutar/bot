package org.tutar.bot.handler.gitlab;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tutar.bot.dto.JobMessage;
import org.tutar.bot.dto.Message;
import org.tutar.bot.handler.Handler;
import org.tutar.bot.handler.Request;
import org.tutar.bot.service.SendService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class JobHookEventHandler implements Handler {

    private SendService<Message> sendService;

    public JobHookEventHandler(SendService sendService){
        this.sendService = sendService;
    }

    @Override
    public Boolean isHandle(Request request) {

        List<String> eventTypes = request.getHeader().get("X-Gitlab-Event");
        if(eventTypes!=null && "Job Hook".equals(eventTypes.get(0))){
            return "build".equals(request.getMessage().get("object_kind"));
        }
        return false;
    }

    /**
     * stage
     * status
     * reason
     * last-committer:id/message/author
     * @param request
     * @return
     */
    @Override
    public Boolean process(Request request) throws IOException {
        // 组装消息并发送
        Map<String,Object> event = request.getMessage();
        Map<String,String> commit = (Map<String, String>) event.get("commit");

        String url = "http://gitlab.vvupup.com/aupup/aupup-mall/-/jobs/"+event.get("build_id");

        JobMessage jobMessage = new JobMessage();
        jobMessage.setTitle("GitLab Job build");
        jobMessage.setCommitterEmail(commit.get("author_email"));
        jobMessage.setCommitterName(commit.get("author_name"));
        jobMessage.setStage(String.valueOf(event.get("build_stage")));
        jobMessage.setStatus(String.valueOf(event.get("build_status")));
        jobMessage.setUrl(url);
        jobMessage.setFailReason(String.valueOf(event.get("build_failure_reason")));

        sendService.send(jobMessage);

        return false;
    }
}
