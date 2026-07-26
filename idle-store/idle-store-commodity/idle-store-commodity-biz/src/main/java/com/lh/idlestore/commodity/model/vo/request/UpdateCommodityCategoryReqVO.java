package com.lh.idlestore.commodity.model.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommodityCategoryReqVO {

    @NotNull
    private Long categoryId;

    private String categoryName;

    private MultipartFile iconFile;

    private Integer sortOrder;

    private Integer status;
}
