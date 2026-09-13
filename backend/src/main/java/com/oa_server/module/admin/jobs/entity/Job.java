package com.oa_server.module.admin.jobs.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa_server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 职位实体
 *
 * @author Alu
 * @date 2026-09-13
 */
@Data
@EqualsAndHashCode(callSuper = true) // 包含父类字段
@TableName("job")
public class Job extends BaseEntity {
    /**
     * 职位名称
     */
    private String jobName;

    /**
     * 排序（越小越靠前）
     */
     private Integer sort;

}
