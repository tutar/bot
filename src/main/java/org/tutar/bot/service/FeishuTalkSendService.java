package org.tutar.bot.service;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.ResponseBody;
import org.apache.tomcat.util.codec.binary.Base64;
import org.tutar.bot.configure.FeishuTalkProperties;
import org.tutar.bot.dto.DingTalkMarkdownMsg;
import org.tutar.bot.dto.Message;
import org.tutar.bot.util.HttpUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 飞书消息发送
 * @author tutar
 */
@Slf4j
public class FeishuTalkSendService implements SendService<Message>{

    private FeishuTalkProperties feishuTalkProperties;

    public FeishuTalkSendService(FeishuTalkProperties feishuTalkProperties){
        this.feishuTalkProperties = feishuTalkProperties;
    }

    @Override
    public void send(Message message){

        // TODO 根据消息内容 构建飞书特定格式消息
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp,feishuTalkProperties.getSecret());

        // 发送消息
        OkHttpClient client = new OkHttpClient.Builder().protocols(Arrays.asList(Protocol.HTTP_2,Protocol.HTTP_1_1)).build();
        Map<String,String> header = Maps.newHashMap();
        header.put("Content-Type", "application/json");

        // 消息体
        Map<String,Object> msgBody = Maps.newHashMap();
        msgBody.put("title",message.getTitle());
        List<Object> contents = Lists.newArrayList();

        List<Object> content = Collections.singletonList(
                ImmutableMap.of("tag", "text", "text", "阶段：" + message.getStage())
        );
        contents.add(content);
        content = Arrays.asList(
                ImmutableMap.of("tag", "text","text","状态："+message.getStatus()),
                ImmutableMap.of("tag", "a","text","  请查看","href",message.getUrl())
        );
        contents.add(content);

        content = Arrays.asList(
                ImmutableMap.of("tag", "text","text","贡献者："+message.getStatus()),
                ImmutableMap.of("tag", "text","text","贡献者邮箱："+message.getStatus())
        );
        contents.add(content);


        msgBody.put("content",contents);

        // 消息头
        Map<String,Object> param = Maps.newHashMap();
        param.put("timestamp",timestamp.toString());
        param.put("sign",sign);
        param.put("msg_type","post");
        param.put("content",ImmutableMap.of("post", ImmutableMap.of("zh_cn",msgBody)));


        ResponseBody body = HttpUtils.doPost(client, feishuTalkProperties.getWebHookUrl(), param,header).body();
        try {
            log.debug("timestamp:{}, body:{}",timestamp,body.string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSign(Long timestamp,String secret) {
        try {
            String key = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal("".getBytes(StandardCharsets.UTF_8));
            return new String(Base64.encodeBase64(signData), StandardCharsets.UTF_8);
        } catch (Exception e){
            log.error("get sign error: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e);
        }
    }


}
