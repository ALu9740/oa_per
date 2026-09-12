package com.oa_server.module.admin.emps.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa_server.common.result.PageResult;
import com.oa_server.module.admin.emps.dto.AdminEmpQueryDTO;
import com.oa_server.module.admin.emps.service.AdminEmpSService;
import com.oa_server.module.admin.emps.vo.AdminEmpVO;
import com.oa_server.module.emp.mapper.EmpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 员工管理 服务实现
 *
 * @author Alu
 * @date 2026-09-12
 */
@Service
@RequiredArgsConstructor
public class AdminEmpSServiceImpl implements AdminEmpSService {

    private final EmpMapper empMapper;

    @Override
    public PageResult<AdminEmpVO> getEmpList(AdminEmpQueryDTO adminEmpQueryDTO) {
        //空值兜底
        long pageNum = adminEmpQueryDTO.getPage() != null ? adminEmpQueryDTO.getPage() : 1L;
        long pageSize = adminEmpQueryDTO.getSize() != null ? adminEmpQueryDTO.getSize() : 10L;

        pageSize = Math.min(pageSize, 100L);
        //构造 MyBatis-Plus 分页对象
        Page<AdminEmpVO> page = new Page<>(pageNum, pageSize);

        //分页查询员工列表
        Page<AdminEmpVO> result = empMapper.selectEmpPage(page, adminEmpQueryDTO);

        //转成 PageResult 返回
        return PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );
    }
}
