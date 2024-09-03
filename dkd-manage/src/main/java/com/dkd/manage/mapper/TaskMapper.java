package com.dkd.manage.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dkd.manage.domain.Task;
import com.dkd.manage.domain.dto.TaskDto;
import com.dkd.manage.domain.vo.TaskVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 工单Mapper接口
 * 
 * @author itheima
 * @date 2024-09-01
 */
public interface TaskMapper 
{
    /**
     * 查询工单
     * 
     * @param taskId 工单主键
     * @return 工单
     */
    public Task selectTaskByTaskId(Long taskId);

    /**
     * 查询工单列表
     * 
     * @param task 工单
     * @return 工单集合
     */
    public List<Task> selectTaskList(Task task);

    /**
     * 新增工单
     * 
     * @param task 工单
     * @return 结果
     */
    public int insertTask(Task task);

    /**
     * 修改工单
     * 
     * @param task 工单
     * @return 结果
     */
    public int updateTask(Task task);

    /**
     * 删除工单
     * 
     * @param taskId 工单主键
     * @return 结果
     */
    public int deleteTaskByTaskId(Long taskId);

    /**
     * 批量删除工单
     * 
     * @param taskIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTaskByTaskIds(Long[] taskIds);

    /**
     * 查询工单列表
     *
     * @param task 工单
     * @return TaskVo集合
     */
    public List<TaskVo> selectTaskvOList(Task task);

    @Select("select user_name from sys_user where user_id=#{assignorId}")
    Map<String, Object> getAssignorById(Long assignorId);

    @Select("select count(*) count from tb_task where inner_code = #{innerCode} and product_type_id = #{productTypeId} and task_status = #{taskStatus} and update_time between #{startTime} and #{endTime}")
    int countTaskByInnerCodeAndTypeAndStatus(@Param("innerCode") String innerCode, @Param("productTypeId") Long product_type_id, @Param("taskStatus") Long task_status, @Param("startTime") String startTime, @Param("endTime") String endTime);
}
