CREATE DATABASE IF NOT EXISTS `oa_per`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
USE `oa_per`;

-- 1. 部门表
CREATE TABLE dept (
                      id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '部门ID',
                      dept_name   VARCHAR(50)  NOT NULL                COMMENT '部门名称',
                      description VARCHAR(255)                         COMMENT '部门描述',
                      created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      is_deleted  TINYINT(1)   DEFAULT 0               COMMENT '逻辑删除（0-未删除，1-已删除）',
                      PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 2. 职位表
CREATE TABLE job (
                     id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '职位ID',
                     job_name    VARCHAR(50)  NOT NULL                COMMENT '职位名称',
                     sort        INT          DEFAULT 0               COMMENT '排序（数值越小越靠前）',
                     created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                     updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                     is_deleted  TINYINT(1)   DEFAULT 0               COMMENT '逻辑删除（0-未删除，1-已删除）',
                     PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位表';

-- 3. 员工表
CREATE TABLE emp (
                     id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '员工ID',
                     emp_no         VARCHAR(20)  NOT NULL                COMMENT '员工编号（唯一）',
                     name           VARCHAR(50)                          COMMENT '姓名',
                     gender         TINYINT(1)                           COMMENT '性别（0-女，1-男）',
                     phone          VARCHAR(20)                          COMMENT '手机号',
                     email          VARCHAR(50)                          COMMENT '邮箱',
                     avatar         VARCHAR(255)                         COMMENT '头像URL',
                     password       VARCHAR(255) NOT NULL                COMMENT '密码（加密存储）',
                     dept_id        BIGINT                               COMMENT '部门ID',
                     job_id         BIGINT                               COMMENT '职位ID',
                     hire_date      DATE                                 COMMENT '入职时间',
                     role_type      TINYINT                              COMMENT '角色类型（0-普通员工、1-管理员）',
                     account_status TINYINT      DEFAULT 1               COMMENT '账号状态（2-待完善，1-正常，0-禁用）',
                     created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                     updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                     is_deleted     TINYINT(1)   DEFAULT 0               COMMENT '逻辑删除（0-未删除，1-已删除）',
                     PRIMARY KEY (id),
                     UNIQUE KEY uk_emp_no (emp_no),
                     KEY idx_dept_id (dept_id),
                     KEY idx_job_id (job_id),
                     CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id) REFERENCES dept (id),
                     CONSTRAINT fk_emp_job  FOREIGN KEY (job_id)  REFERENCES job  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';