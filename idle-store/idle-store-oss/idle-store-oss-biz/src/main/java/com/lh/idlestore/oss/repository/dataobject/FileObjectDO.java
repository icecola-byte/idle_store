package com.lh.idlestore.oss.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_file_object")
public class FileObjectDO {

    @TableId(type = IdType.AUTO)
    private Long fileId;

    private String storageProvider;

    private String bucketName;

    private String objectKey;

    private String originalFilename;

    private String contentType;

    private Long fileSize;

    private Integer fileStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime deleteTime;
}
