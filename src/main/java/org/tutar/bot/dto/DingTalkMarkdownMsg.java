package org.tutar.bot.dto;

import lombok.Data;

import java.util.List;

@Data
public class DingTalkMarkdownMsg {

    private String msgtype="markdown";

    private Markdown markdown;

    private At at;

    @Data
    public static class Markdown{
        private String title;
        private String text;

    }
    @Data
    public static class At{

        private List<String> atMobiles;

        private boolean isAtAll=false;
    }
}
