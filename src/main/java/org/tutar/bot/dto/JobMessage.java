package org.tutar.bot.dto;

import lombok.Data;

@Data
public class JobMessage implements Message{

    private String title;
    private String stage;
    private String status;
    private String committerName;
    private String committerEmail;
    private String commitMessage;
    private String url;
    private String failReason;

}
