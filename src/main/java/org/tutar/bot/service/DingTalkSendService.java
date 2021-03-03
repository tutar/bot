package org.tutar.bot.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.ResponseBody;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;
import org.tutar.bot.configure.DingTalkProperties;
import org.tutar.bot.dto.DingTalkMarkdownMsg;
import org.tutar.bot.dto.JobMessage;
import org.tutar.bot.dto.Message;
import org.tutar.bot.util.HttpUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

@Slf4j
public class DingTalkSendService implements SendService<Message>{

    private DingTalkProperties dingTalkProperties;

    public DingTalkSendService(DingTalkProperties dingTalkProperties){
        this.dingTalkProperties = dingTalkProperties;
    }

    @Override
    public void send(Message jobMessage){

        DingTalkMarkdownMsg msg = new DingTalkMarkdownMsg();
        DingTalkMarkdownMsg.Markdown markdown = new DingTalkMarkdownMsg.Markdown();
        markdown.setTitle(jobMessage.getTitle());
        // build stage/status/fail-reason
        StringBuilder sb = new StringBuilder();

        sb.append(" ### GitLab Job build ")
                .append("\n> ###### 阶段: **").append(jobMessage.getStage()).append("** ")
                .append("\n> ###### 状态: **").append(jobMessage.getStatus()).append("** ")
                .append("\n> ##### commit ")
                .append("\n> ###### message: ").append(jobMessage.getCommitMessage())
                .append("\n> ###### author_name: ").append(jobMessage.getCommitterName())
                .append("\n> ###### author_email: ").append(jobMessage.getCommitterEmail());

        if("failed".equals(jobMessage.getStatus())){
            sb.append("\n> ###### 失败原因:").append(jobMessage.getFailReason());
        }else if("success".equals(jobMessage.getStatus())){
            log.info("success");
        }else{
            // 非fail、success 不发消息
            return ;
        }

        sb.append("\n> ###### 查看 [详情](").append(jobMessage.getUrl()).append(")");

        markdown.setText(sb.toString());

        msg.setMarkdown(markdown);


        Long timestamp = System.currentTimeMillis();
        String sign = getSign(timestamp,dingTalkProperties.getSecret());

        // 发送消息
        String url = dingTalkProperties.getWebHookUrl();
        url = url+"&"+"timestamp="+timestamp+"&sign="+sign;
        OkHttpClient client = new OkHttpClient.Builder().protocols(Arrays.asList(Protocol.HTTP_2,Protocol.HTTP_1_1)).build();
        Map<String,String> header = Maps.newHashMap();
        header.put("Content-Type", "application/json");
        ResponseBody body = HttpUtils.doPost(client, url, msg,header).body();

        try {
            log.debug(body.string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getSign(Long timestamp,String secret) {
        try {
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        } catch (Exception e){
            log.error("get sign error: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e);
        }
    }

}
