package com.lh.idlestore.chat.service;

import com.lh.idlestore.chat.model.command.SendSystemNotificationCommand;
import com.lh.idlestore.chat.model.vo.response.SendMessageRespVO;

public interface SystemNotificationService {

    SendMessageRespVO sendNotification(
            SendSystemNotificationCommand command);
}
