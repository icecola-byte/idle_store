package com.lh.idlestore.commodity.enums;

import com.lh.framework.common.exception.BaseExceptionInterface;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommodityResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 业务异常状态码 -----------
    CATEGORY_NOT_FOUND("COMMODITY-20001", "商品分类不存在"),
    ROOT_CATEGORY_CANNOT_DELETE("COMMODITY-20002", "根分类不允许删除"),
    CATEGORY_CONTAINS_COMMODITY("COMMODITY-20003", "分类或子分类下存在商品，无法删除"),
    GENERATE_ID_FAILED("COMMODITY-20004", "生成消息ID失败"),
    CATEGORY_NAME_ALREADY_EXISTS("COMMODITY-20005", "同级分类名称已存在"),

    // ----------- 远程调用异常状态码 --------
    OSS_SERVICE_CALL_FAILED("COMMODITY-30001", "文件服务调用失败")
    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;
}
