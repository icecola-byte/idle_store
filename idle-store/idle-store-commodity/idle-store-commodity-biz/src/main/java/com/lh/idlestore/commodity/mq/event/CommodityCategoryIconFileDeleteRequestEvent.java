package com.lh.idlestore.commodity.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommodityCategoryIconFileDeleteRequestEvent {

    private Long eventId;

    private Long fileId;
}
