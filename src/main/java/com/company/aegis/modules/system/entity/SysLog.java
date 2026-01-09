package com.company.aegis.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class SysLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * User Account
     */
    private String username;

    /**
     * Operation content
     */
    private String operation;

    /**
     * Request Method
     */
    private String method;

    /**
     * Request Params
     */
    private String params;

    /**
     * Execution Time (ms)
     */
    private Long time;

    /**
     * IP Address
     */
    private String ip;

    /**
     * Creation Time
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
