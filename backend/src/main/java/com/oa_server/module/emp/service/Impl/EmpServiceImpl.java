package com.oa_server.module.emp.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa_server.module.emp.entity.Emp;
import com.oa_server.module.emp.mapper.EmpMapper;
import com.oa_server.module.emp.service.EmpService;
import org.springframework.stereotype.Service;

/**
 * 员工服务实现
 *
 * @author Alu
 * @date 2026-09-09
 */
@Service
public class EmpServiceImpl extends ServiceImpl<EmpMapper, Emp> implements EmpService {
}
