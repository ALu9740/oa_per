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

-- 4. AI 会话表
CREATE TABLE ai_chat_session (
                                 id         BIGINT       NOT NULL                  COMMENT '会话ID（雪花算法生成）',
                                 emp_id     BIGINT       NOT NULL                  COMMENT '所属员工ID',
                                 chat_type  VARCHAR(10)  NOT NULL                  COMMENT '会话类型（AGENT-管理员智能体/RAG-员工问答）',
                                 title      VARCHAR(64)                            COMMENT '会话标题（取首条用户消息）',
                                 created_at DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 is_deleted TINYINT(1)   DEFAULT 0                 COMMENT '逻辑删除（0-未删除，1-已删除）',
                                 PRIMARY KEY (id),
                                 KEY idx_emp_type (emp_id, chat_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话表';

-- 5. AI 消息表
CREATE TABLE ai_chat_message (
                                 id         BIGINT       NOT NULL                  COMMENT '消息ID（雪花算法生成）',
                                 session_id BIGINT       NOT NULL                  COMMENT '所属会话ID',
                                 role       VARCHAR(12)  NOT NULL                  COMMENT '角色（user-用户/assistant-AI）',
                                 content    MEDIUMTEXT                             COMMENT '消息内容',
                                 created_at DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 is_deleted TINYINT(1)   DEFAULT 0                 COMMENT '逻辑删除（0-未删除，1-已删除）',
                                 PRIMARY KEY (id),
                                 KEY idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI消息表';

-- 6. 知识库文档表
CREATE TABLE kb_document (
                             id          BIGINT       NOT NULL                COMMENT '文档ID（雪花算法生成）',
                             file_name   VARCHAR(255) NOT NULL                COMMENT '原始文件名',
                             minio_path  VARCHAR(255) NOT NULL                COMMENT 'MinIO 对象路径（bucket内相对路径）',
                             chunk_count INT          DEFAULT 0               COMMENT '切分后的分块数量',
                             status      VARCHAR(16)  DEFAULT 'PROCESSING'    COMMENT '状态（PROCESSING-处理中/READY-已入库/FAILED-失败）',
                             error_msg   VARCHAR(255)                         COMMENT '失败原因',
                             created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             is_deleted  TINYINT(1)   DEFAULT 0               COMMENT '逻辑删除（0-未删除，1-已删除）',
                             PRIMARY KEY (id),
                             KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库文档表';