package com.lh.idlestore.chat.model.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessagePageVO {

    private List<ChatMessageVO> messages;
    private Long nextSequence;
    private Boolean hasMore;
}
