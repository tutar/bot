package org.tutar.bot.service;

import org.tutar.bot.dto.Message;


public interface SendService<T extends Message> {

    /**
     * 消息发送
     * @param msg
     */
    void send(T msg);
}
