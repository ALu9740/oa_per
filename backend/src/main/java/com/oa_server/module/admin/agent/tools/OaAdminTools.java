package com.oa_server.module.admin.agent.tools;

import com.oa_server.module.admin.depts.entity.Dept;
import com.oa_server.module.admin.depts.mapper.DeptSMapper;
import com.oa_server.module.admin.emps.dto.AdminEditEmpDTO;
import com.oa_server.module.admin.emps.dto.AdminEmpQueryDTO;
import com.oa_server.module.admin.emps.dto.AdminUpdateAccountStatusDTO;
import com.oa_server.module.admin.emps.service.AdminEmpSService;
import com.oa_server.module.admin.emps.vo.AdminEmpVO;
import com.oa_server.module.admin.emps.vo.OptionVO;
import com.oa_server.module.emp.entity.Emp;
import com.oa_server.module.emp.mapper.EmpMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
/**
 * 管理员智能体工具集
 *
 * @author Alu
 * @date 2026-09-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OaAdminTools {

    private final AdminEmpSService adminEmpSService;
    private final EmpMapper empMapper;
    private final DeptSMapper deptSMapper;

    @Tool(description = "根据员工姓名查询员工（支持模糊匹配），返回员工的ID、工号、姓名、部门、职位、手机号、账号状态。当需要找到某个员工但不知道其ID时，先调用本工具。")
    public List<AdminEmpVO> searchEmpByName(@ToolParam(description = "员工姓名或姓名片段") String name) {
        AdminEmpQueryDTO query = new AdminEmpQueryDTO();
        query.setName(name);
        query.setPage(1L);
        query.setSize(10L);
        return adminEmpSService.getEmpList(query).getRecords();
    }

    @Tool(description = "根据部门完整名称查询部门信息（ID、名称、描述）")
    public Dept searchDeptByName(@ToolParam(description = "部门完整名称，例如：技术部") String deptName) {
        return deptSMapper.selectDeptByName(deptName);
    }

    @Tool(description = "查询系统中所有部门列表（ID和名称），用于不确定部门名称时让管理员选择")
    public List<OptionVO> listAllDept() {
        return empMapper.selectDeptOptions();
    }

    @Tool(description = "查询系统中所有职位列表（ID和名称），用于不确定职位名称时让管理员选择")
    public List<OptionVO> listAllJob() {
        return empMapper.selectJobOptions();
    }

    @Tool(description = "将员工调动到指定部门，可选择同时调整职位。必须先通过查询工具拿到员工ID和目标部门ID，再调用本工具。")
    public String transferEmp(@ToolParam(description = "员工ID") Long empId, @ToolParam(description = "目标部门ID") Long deptId, @ToolParam(description = "目标职位ID，不需要调整职位时传 null", required = false) Long jobId) {
        try {
            Emp emp = empMapper.findById(empId);
            if (emp == null) {
                return "操作失败：员工不存在，empId=" + empId;
            }
            String oldDeptName = deptSMapper.selectDeptName(emp.getDeptId());
            String newDeptName = deptSMapper.selectDeptName(deptId);
            if (newDeptName == null) {
                return "操作失败：目标部门不存在，deptId=" + deptId;
            }
            AdminEditEmpDTO dto = new AdminEditEmpDTO();
            dto.setId(empId);
            dto.setName(emp.getName());
            dto.setGender(emp.getGender());
            dto.setPhone(emp.getPhone());
            dto.setEmail(emp.getEmail());
            dto.setDeptId(deptId);
            dto.setJobId(jobId != null ? jobId : emp.getJobId());
            adminEmpSService.editEmp(dto);
            log.info("[AI-Agent] 员工调动：empId={}, [{}] -> [{}]", empId, oldDeptName, newDeptName);
            return "调动成功：员工[" + emp.getName() + "]已从[" + oldDeptName + "]调入[" + newDeptName + "]";
        } catch (Exception e) {
            log.warn("[AI-Agent] 员工调动失败：empId={}, deptId={}", empId, deptId, e);
            return "操作失败：" + e.getMessage();
        }
    }

    @Tool(description = "更新员工账号状态：0-禁用、1-正常。禁用后该员工将无法登录系统。")
    public String updateEmpAccountStatus(@ToolParam(description = "员工ID") Long empId, @ToolParam(description = "目标状态：0-禁用，1-正常") Integer accountStatus) {
        try {
            Emp emp = empMapper.findById(empId);
            if (emp == null) {
                return "操作失败：员工不存在，empId=" + empId;
            }
            AdminUpdateAccountStatusDTO dto = new AdminUpdateAccountStatusDTO();
            dto.setId(empId);
            dto.setAccountStatus(accountStatus);
            adminEmpSService.updateAccountStatus(dto);
            log.info("[AI-Agent] 更新账号状态：empId={}, status={}", empId, accountStatus);
            return "操作成功：员工[" + emp.getName() + "]的账号状态已更新为" + (accountStatus == 1 ? "正常" : "禁用");
        } catch (Exception e) {
            log.warn("[AI-Agent] 更新账号状态失败：empId={}", empId, e);
            return "操作失败：" + e.getMessage();
        }
    }
}
