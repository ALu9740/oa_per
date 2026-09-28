package com.oa_server.module.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa_server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 协议签署记录
 *
 * @author Alu
 * @date 2026-09-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agreement_record")
public class AgreementRecord extends BaseEntity {

    /**
     * 员工ID
     */
    private Long empId;

    /**
     * 注册邮箱
     */
    private String email;

    /**
     * 协议版本号
     */
    private String agreementVersion;

    /**
     * 注册时IP
     */
    private String userIp;

    /**
     * 同意时间
     */
    private LocalDateTime agreedAt;
}