package com.lh.idlestore.commodity.model.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsertCommodityCategoryReqVO {

    @NotNull
    @PositiveOrZero
    private Long parentId;

    @NotBlank
    @Size(max = 64)
    private String categoryName;

    private MultipartFile iconFile;

    @PositiveOrZero
    private Integer sortOrder;
}
