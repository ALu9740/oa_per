package com.oa_server.module.admin.emps.vo;

import lombok.Data;

/**
 * 下拉选项 VO（部门/职位）
 *
 * @author Alu
 * @date 2026-09-12
 */
@Data
public class OptionVO {

    /**
     * 选项ID
     */
    private Long id;

    /**
     * 选项名称
     */
    private String name;
}
