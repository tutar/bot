package org.tutar.bot.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tutar.bot.configure.FeishuTalkProperties;
import org.tutar.bot.dto.DingTalkMarkdownMsg;
import org.tutar.bot.dto.JobMessage;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = BotTestConfiguration.class)
@ActiveProfiles("test")
public class FeishuTalkSendServiceTest {

    @Autowired
    private FeishuTalkSendService feishuTalkSendService;

    @Autowired
    private FeishuTalkProperties feishuTalkProperties;

    @Test
    public void sendMessageTest(){
        JobMessage jobMessage = new JobMessage();
        jobMessage.setTitle("GitLab Job build");
        jobMessage.setStage("setTitle");
        jobMessage.setStatus("setTitle");
        jobMessage.setCommitterName("c-huf@vanke.com");
        jobMessage.setCommitterEmail("setTitle");
        jobMessage.setCommitMessage(null);
        jobMessage.setUrl("http://gitlab.vvupup.com/aupup/aupup-mall/-/jobs/2349");
        jobMessage.setFailReason("setTitle");

        feishuTalkSendService.send(jobMessage);
    }

    @Test
    public void signTest(){

        Assert.assertEquals("WuhPKkWXn49RZpstecAhgiBom2x5JjhD+mDglivI6WA=",feishuTalkSendService.getSign(1614670146920L,feishuTalkProperties.getSecret()));
    }
}
