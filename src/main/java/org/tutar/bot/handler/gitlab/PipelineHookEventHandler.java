package org.tutar.bot.handler.gitlab;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tutar.bot.configure.BotProperties;
import org.tutar.bot.dto.JobMessage;
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

    private BotProperties botProperties;

    public PipelineHookEventHandler(SendService sendService,  BotProperties botProperties){
        this.sendService = sendService;
        this.botProperties = botProperties;
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
        Map<String,Object> commit = (Map<String, Object>) event.get("commit");
        Map<String,Object> objectAttributes = (Map<String, Object>) event.get("object_attributes");
        Map<String,String> author = (Map<String, String>) commit.get("author");
        String pipelineId = String.valueOf(objectAttributes.get("id"));

        JobMessage jobMessage = new JobMessage();
        jobMessage.setTitle("GitLab 流水线");
        jobMessage.setCommitterEmail(author.get("name"));
        jobMessage.setCommitterName(author.get("email"));
        jobMessage.setCommitMessage(String.valueOf(commit.get("message")));
        jobMessage.setStatus(String.valueOf(objectAttributes.get("status")));
        jobMessage.setFailReason(String.valueOf(event.get("build_failure_reason")));
        jobMessage.setUrl(botProperties.getGitlab().getPipelineBaseUrl()+pipelineId);

//        DingTalkMarkdownMsg msg = new DingTalkMarkdownMsg();
//        DingTalkMarkdownMsg.Markdown markdown = new DingTalkMarkdownMsg.Markdown();
//        markdown.setTitle(jobMessage.getTitle());
//        // build stage/status/fail-reason
//        StringBuilder sb = new StringBuilder();
//
//        sb.append(" ### GitLab Job build ")
//                .append("\n> ###### build_stage: **").append(jobMessage.getStage()).append("** ")
//                .append("\n> ###### build_status: **").append(jobMessage.getStatus()).append("** ")
//                .append("\n> ##### commit ")
//                .append("\n> ###### id: ").append(commit.get("id"))
//                .append("\n> ###### message: ").append(jobMessage.getCommitMessage())
//                .append("\n> ###### author_name: ").append(jobMessage.getCommitterName())
//                .append("\n> ###### author_email: ").append(jobMessage.getCommitterEmail());
//
//        if("failed".equals(jobMessage.getStatus())){
//            sb.append("\n> ###### failure_reason:").append(jobMessage.getFailReason());
//        }else if("success".equals(jobMessage.getStatus())){
//            log.info("success");
//        }else{
//            // 非fail、success 不发消息
//            return false;
//        }
//        markdown.setText(sb.toString());
//        msg.setMarkdown(markdown);
//
//
        // 发送消息
        sendService.send(jobMessage);

        return false;
    }
}
