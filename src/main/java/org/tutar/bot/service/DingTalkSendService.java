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
import org.tutar.bot.util.HttpUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Service
public class DingTalkSendService implements SendService<DingTalkMarkdownMsg>{

    private DingTalkProperties dingTalkProperties;

    public DingTalkSendService(DingTalkProperties dingTalkProperties){
        this.dingTalkProperties = dingTalkProperties;
    }

    @Override
    public void send(DingTalkMarkdownMsg msg){

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
