package org.tutar.bot.dto;

import java.io.Serializable;

public interface Message extends Serializable {
    String getTitle();
    String getStage();
    String getStatus();
    String getCommitterName();
    String getCommitterEmail();
    default String getCommitMessage(){return "";}
    String getUrl();
    String getFailReason();
}
