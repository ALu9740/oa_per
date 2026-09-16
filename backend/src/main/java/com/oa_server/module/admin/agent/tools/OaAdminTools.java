package com.oa_server.module.admin.agent.tools;

import com.oa_server.module.admin.depts.dto.AdminEditDeptDTO;
import com.oa_server.module.admin.depts.entity.Dept;
import com.oa_server.module.admin.depts.mapper.DeptSMapper;
import com.oa_server.module.admin.depts.service.AdminDeptSService;
import com.oa_server.module.admin.emps.dto.AdminEditEmpDTO;
import com.oa_server.module.admin.emps.dto.AdminEmpQueryDTO;
import com.oa_server.module.admin.emps.dto.AdminUpdateAccountStatusDTO;
import com.oa_server.module.admin.emps.service.AdminEmpSService;
import com.oa_server.module.admin.emps.vo.AdminEmpVO;
import com.oa_server.module.admin.emps.vo.OptionVO;
import com.oa_server.module.admin.jobs.dto.AdminEditJobDTO;
import com.oa_server.module.admin.jobs.entity.Job;
import com.oa_server.module.admin.jobs.mapper.JobSMapper;
import com.oa_server.module.admin.jobs.service.AdminJobSService;
import com.oa_server.module.emp.entity.Emp;
import com.oa_server.module.emp.mapper.EmpMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private final AdminDeptSService adminDeptSService;
    private final AdminJobSService adminJobSService;
    private final EmpMapper empMapper;
    private final DeptSMapper deptSMapper;
    private final JobSMapper jobSMapper;

    @Tool(description = "根据员工姓名查询员工（支持模糊匹配），返回员工的ID、工号、姓名、部门、职位、手机号、入职时间、账号状态。当需要找到某个员工但不知道其ID时，先调用本工具。")
    public List<AdminEmpVO> searchEmpByName(@ToolParam(description = "员工姓名或姓名片段") String name) {
        AdminEmpQueryDTO query = new AdminEmpQueryDTO();
        query.setName(name);
        query.setPage(1L);
        query.setSize(10L);
        return adminEmpSService.getEmpList(query).getRecords();
    }

    @Tool(description = "根据部门完整名称查询部门信息（ID、名称、描述、创建时间、更新时间）")
    public Dept searchDeptByName(@ToolParam(description = "部门完整名称，例如：技术部") String deptName) {
        return deptSMapper.selectDeptByName(deptName);
    }

    @Tool(description = "根据职位完整名称查询职位信息（ID、名称、排序、创建时间、更新时间）")
    public Job searchJobByName(@ToolParam(description = "职位完整名称，例如：后端工程师") String jobName) {
        return jobSMapper.selectJobByName(jobName);
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

    @Tool(description = "修改员工个人信息（姓名/性别/手机号/邮箱/入职时间）。" +
            "部门和职位调动请使用 transferEmp 工具。" +
            "不需要修改的字段传 null。")
    public String editEmpInfo(
            @ToolParam(description = "员工ID") Long empId,
            @ToolParam(description = "姓名，不需要修改时传 null", required = false) String name,
            @ToolParam(description = "性别 0-女 1-男，不需要修改时传 null", required = false) Integer gender,
            @ToolParam(description = "手机号，不需要修改时传 null", required = false) String phone,
            @ToolParam(description = "邮箱，不需要修改时传 null", required = false) String email,
            @ToolParam(description = "入职时间 格式yyyy-MM-dd，不需要修改时传 null", required = false) LocalDate hireDate) {
        try {
            Emp emp = empMapper.findById(empId);
            if (emp == null) {
                return "操作失败：员工不存在，empId=" + empId;
            }
            AdminEditEmpDTO dto = new AdminEditEmpDTO();
            dto.setId(empId);
            dto.setName(name != null ? name : emp.getName());
            dto.setGender(gender != null ? gender : emp.getGender());
            dto.setPhone(phone != null ? phone : emp.getPhone());
            dto.setEmail(email != null ? email : emp.getEmail());
            dto.setDeptId(emp.getDeptId());
            dto.setJobId(emp.getJobId());
            dto.setHireDate(hireDate != null ? hireDate : emp.getHireDate());

            adminEmpSService.editEmp(dto);

            // 日志 & 返回
            List<String> changes = new ArrayList<>();
            if (name != null) changes.add("姓名=" + name);
            if (gender != null) changes.add("性别=" + gender);
            if (phone != null) changes.add("手机号=" + phone);
            if (email != null) changes.add("邮箱=" + email);
            if (hireDate != null) changes.add("入职时间=" + hireDate);
            log.info("[AI-Agent] 员工信息更新：empId={}, 变更={}", empId, changes);
            return "修改成功：员工[" + emp.getName() + "]已更新 " + String.join(", ", changes);
        } catch (Exception e) {
            log.warn("[AI-Agent] 员工信息更新失败：empId={}", empId, e);
            return "操作失败：" + e.getMessage();
        }
    }


    @Tool(description = "更新部门信息（名称、描述）。不需要修改的字段传 null。必须先通过查询工具拿到部门ID。")
    public String updateDept(
            @ToolParam(description = "部门ID") Long deptId,
            @ToolParam(description = "部门名称，不需要修改时传 null", required = false) String deptName,
            @ToolParam(description = "部门描述，不需要修改时传 null", required = false) String description) {
        try {
            Dept dept = deptSMapper.findByIdDept(deptId);
            if (dept == null) {
                return "操作失败：部门不存在，deptId=" + deptId;
            }
            AdminEditDeptDTO dto = new AdminEditDeptDTO();
            dto.setId(deptId);
            dto.setName(deptName != null ? deptName : dept.getDeptName());
            dto.setDescription(description != null ? description : dept.getDescription());

            adminDeptSService.editDept(dto);

            List<String> changes = new ArrayList<>();
            if (deptName != null) changes.add("名称=" + deptName);
            if (description != null) changes.add("描述=" + description);
            log.info("[AI-Agent] 更新部门：deptId={}, 变更={}", deptId, changes);
            return "修改成功：部门[" + dept.getDeptName() + "]已更新 " + String.join(", ", changes);
        } catch (Exception e) {
            log.warn("[AI-Agent] 更新部门失败：deptId={}", deptId, e);
            return "操作失败：" + e.getMessage();
        }
    }

    @Tool(description = "更新职位信息（名称、排序）。不需要修改的字段传 null。必须先通过查询工具拿到职位ID。")
    public String updateJob(
            @ToolParam(description = "职位ID") Long jobId,
            @ToolParam(description = "职位名称，不需要修改时传 null", required = false) String jobName,
            @ToolParam(description = "排序数字，不需要修改时传 null", required = false) Integer sort) {
        try {
            Job job = jobSMapper.findByIdJob(jobId);
            if (job == null) {
                return "操作失败：职位不存在，jobId=" + jobId;
            }
            AdminEditJobDTO dto = new AdminEditJobDTO();
            dto.setId(jobId);
            dto.setName(jobName != null ? jobName : job.getJobName());
            dto.setSort(sort != null ? sort : job.getSort());

            adminJobSService.editJob(dto);

            List<String> changes = new ArrayList<>();
            if (jobName != null) changes.add("名称=" + jobName);
            if (sort != null) changes.add("排序=" + sort);
            log.info("[AI-Agent] 更新职位：jobId={}, 变更={}", jobId, changes);
            return "修改成功：职位[" + job.getJobName() + "]已更新 " + String.join(", ", changes);
        } catch (Exception e) {
            log.warn("[AI-Agent] 更新职位失败：jobId={}", jobId, e);
            return "操作失败：" + e.getMessage();
        }
    }
}
