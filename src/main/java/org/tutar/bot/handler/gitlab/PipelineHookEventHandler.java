package org.tutar.bot.handler.gitlab;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tutar.bot.dto.DingTalkMarkdownMsg;
import org.tutar.bot.dto.Message;
import org.tutar.bot.handler.Handler;
import org.tutar.bot.handler.Request;
import org.tutar.bot.service.SendService;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PipelineHookEventHandler  implements Handler {

    private SendService<Message> sendService;

    public PipelineHookEventHandler(SendService sendService){
        this.sendService = sendService;
    }

    @Override
    public Boolean isHandle(Request request) {
        List<String> eventTypes = request.getHeader().get("X-Gitlab-Event");
        if(eventTypes!=null && "Pipeline Hook".equals(eventTypes.get(0))){
            return "pipeline".equals(request.getMessage().get("object_kind"));
        }
        return false;
    }

    @Override
    public Boolean process(Request request) throws Exception {
        // 组装消息并发送
        Map<String,Object> event = request.getMessage();
        DingTalkMarkdownMsg msg = new DingTalkMarkdownMsg();
        DingTalkMarkdownMsg.Markdown markdown = new DingTalkMarkdownMsg.Markdown();
        markdown.setTitle("GitLab Job build");
        // build stage/status/fail-reason
        StringBuilder sb = new StringBuilder();
        Map<String,Object> commit = (Map<String, Object>) event.get("commit");
        Map<String,String> author = (Map<String, String>) commit.get("author");
        sb.append(" ### GitLab Job build ")
                .append("\n> ###### build_stage: **").append(event.get("build_stage")).append("** ")
                .append("\n> ###### build_status: **").append(event.get("build_status")).append("** ")
                .append("\n> ##### commit ")
                .append("\n> ###### id: ").append(commit.get("id"))
                .append("\n> ###### message: ").append(commit.get("message"))
                .append("\n> ###### author_name: ").append(author.get("name"))
                .append("\n> ###### author_email: ").append(author.get("email"));

        if("failed".equals(event.get("build_status"))){
            sb.append("\n> ###### failure_reason:").append(event.get("build_failure_reason"));
        }
        markdown.setText(sb.toString());

        msg.setMarkdown(markdown);
        // 发送消息
        sendService.send(msg);

        return false;
    }
}
