package com.oa_server.module.admin.depts.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa_server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体
 *
 * @author Alu
 * @date 2026-09-12
 */
@Data
@EqualsAndHashCode(callSuper = true) // 包含父类字段
@TableName("dept")
public class Dept extends BaseEntity {

    /**
     * 部门名称
     */
     private String deptName;

    /**
     * 部门描述
     */
     private String description;
}
