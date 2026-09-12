package com.oa_server.module.emp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa_server.module.auth.dto.CompleteProfileDTO;
import com.oa_server.module.emp.dto.ChangePasswordDTO;
import com.oa_server.module.emp.dto.UpdateProfileDTO;
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

    /**
     * 更新当前员工资料
     *
     * @param updateProfileDTO  更新员工资料 DTO
     * @return 更新后的员工资料
     */
    EmpVO updateProfile(UpdateProfileDTO updateProfileDTO);

    /**
     * 头像上传
     *
     * @param base64 头像图片的 base64 编码
     * @return 头像图片的 URL
     */
    String uploadAvatar(String base64);

    /**
     * 修改密码
     *
     * @param changePasswordDTO 修改密码 DTO
     */
    void changePassword(ChangePasswordDTO changePasswordDTO);
}
