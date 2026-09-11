package com.oa_server.module.emp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa_server.module.auth.dto.CompleteProfileDTO;
import com.oa_server.module.emp.entity.Emp;
import com.oa_server.module.emp.vo.EmpVO;

/**
 * 员工服务接口
 *
 * @author Alu
 * @date 2026-09-09
 */
public interface EmpService extends IService<Emp> {

    /**
     * 实体转 VO
     *
     * @param emp 员工实体
     * @return 员工 VO
     */
    EmpVO empToEmpVO(Emp emp);

    /**
     * 完善资料
     *
     * @param completeProfileDTO 完善资料DTO
     */
    void completeProfile(CompleteProfileDTO completeProfileDTO);

    /**
     * 根据ID获取员工资料
     *
     * @param empId 员工ID
     * @return 员工资料
     */
    EmpVO getEmpInfo(Long empId);
}
