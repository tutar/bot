package org.tutar.bot.handler.gitlab;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tutar.bot.dto.DingTalkMarkdownMsg;
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
        DingTalkMarkdownMsg msg = new DingTalkMarkdownMsg();
        DingTalkMarkdownMsg.Markdown markdown = new DingTalkMarkdownMsg.Markdown();
        markdown.setTitle("GitLab Job build");
        // build stage/status/fail-reason
        StringBuilder sb = new StringBuilder();
        Map<String,String> commit = (Map<String, String>) event.get("commit");
        sb.append(" ### GitLab Job build ")
                .append("\n> ###### 阶段: **").append(event.get("build_stage")).append("** ")
                .append("\n> ###### 状态: **").append(event.get("build_status")).append("** ")
                .append("\n> ##### commit ")
                .append("\n> ###### author_name: ").append(commit.get("author_name"))
                .append("\n> ###### author_email: ").append(commit.get("author_email"));

        if("failed".equals(event.get("build_status"))){
            sb.append("\n> ###### 失败原因:").append(event.get("build_failure_reason"));
        }else if("success".equals(event.get("build_status"))){
            log.info("success");
        }else{
            // 非fail、success 不发消息
            return false;
        }
        sb.append("\n> ###### 查看 [详情](http://gitlab.vvupup.com/aupup/aupup-mall/-/jobs/").append(event.get("build_id")).append(")");

        markdown.setText(sb.toString());

        msg.setMarkdown(markdown);

        sendService.send(msg);

        return false;
    }
}
