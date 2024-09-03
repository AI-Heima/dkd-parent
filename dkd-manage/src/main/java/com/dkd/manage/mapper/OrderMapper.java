package com.dkd.manage.mapper;

import java.util.Date;
import java.util.List;
import com.dkd.manage.domain.Order;
import com.dkd.manage.domain.vo.SkuVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单管理Mapper接口
 * 
 * @author itheima
 * @date 2024-09-02
 */
public interface OrderMapper 
{
    /**
     * 查询订单管理
     * 
     * @param id 订单管理主键
     * @return 订单管理
     */
    public Order selectOrderById(Long id);

    /**
     * 查询订单管理列表
     * 
     * @param order 订单管理
     * @return 订单管理集合
     */
    public List<Order> selectOrderList(Order order);

    /**
     * 新增订单管理
     * 
     * @param order 订单管理
     * @return 结果
     */
    public int insertOrder(Order order);

    /**
     * 修改订单管理
     * 
     * @param order 订单管理
     * @return 结果
     */
    public int updateOrder(Order order);

    /**
     * 删除订单管理
     * 
     * @param id 订单管理主键
     * @return 结果
     */
    public int deleteOrderById(Long id);

    /**
     * 批量删除订单管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOrderByIds(Long[] ids);

    @Select("select sku_name, count(*) sale_num from tb_order where inner_code = #{innerCode} and status in (1, 2) and update_time between #{startTime} and #{endTime} group by sku_name order by count(*) desc")
    List<SkuVo> selectOrderListByInnercode(@Param("innerCode") String innerCode, @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("select sum(amount) from tb_order where inner_code = #{innerCode} and status in (1, 2) and update_time between #{startTime} and #{endTime}")
    int sumOrderAmountByInnerCode(@Param("innerCode") String innerCode, @Param("startTime") String startTime, @Param("endTime") String endTime);
}
