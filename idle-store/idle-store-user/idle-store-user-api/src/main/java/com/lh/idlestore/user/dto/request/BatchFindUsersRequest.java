package com.lh.idlestore.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量查询用户公开信息请求。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchFindUsersRequest {

    @NotEmpty(message = "用户ID列表不能为空")
    @Size(max = 100, message = "单次最多查询100个用户")
    private List<@NotNull @Positive Long> userIds;
}
