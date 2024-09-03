package com.dkd.manage.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.dkd.common.constant.DkdContants;
import com.dkd.common.utils.DateUtils;
import com.dkd.manage.domain.vo.SkuDetailVo;
import com.dkd.manage.domain.vo.SkuVo;
import com.dkd.manage.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dkd.manage.mapper.OrderMapper;
import com.dkd.manage.domain.Order;
import com.dkd.manage.service.IOrderService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单管理Service业务层处理
 * 
 * @author itheima
 * @date 2024-09-02
 */
@Service
public class OrderServiceImpl implements IOrderService 
{
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TaskMapper taskMapper;

    /**
     * 查询订单管理
     * 
     * @param id 订单管理主键
     * @return 订单管理
     */
    @Override
    public Order selectOrderById(Long id)
    {
        return orderMapper.selectOrderById(id);
    }

    /**
     * 查询订单管理列表
     * 
     * @param order 订单管理
     * @return 订单管理
     */
    @Override
    public List<Order> selectOrderList(Order order)
    {
        return orderMapper.selectOrderList(order);
    }

    /**
     * 新增订单管理
     * 
     * @param order 订单管理
     * @return 结果
     */
    @Override
    public int insertOrder(Order order)
    {
        order.setCreateTime(DateUtils.getNowDate());
        return orderMapper.insertOrder(order);
    }

    /**
     * 修改订单管理
     * 
     * @param order 订单管理
     * @return 结果
     */
    @Override
    public int updateOrder(Order order)
    {
        order.setUpdateTime(DateUtils.getNowDate());
        return orderMapper.updateOrder(order);
    }

    /**
     * 批量删除订单管理
     * 
     * @param ids 需要删除的订单管理主键
     * @return 结果
     */
    @Override
    public int deleteOrderByIds(Long[] ids)
    {
        return orderMapper.deleteOrderByIds(ids);
    }

    /**
     * 删除订单管理信息
     * 
     * @param id 订单管理主键
     * @return 结果
     */
    @Override
    public int deleteOrderById(Long id)
    {
        return orderMapper.deleteOrderById(id);
    }

    /**
     * 根据设备编码查询商品销售详情和补货次数以及维修次数
     * @param innerCode
     * @return
     */
    @Transactional
    @Override
    public SkuDetailVo salesList(String innerCode) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String endTime = sdf.format(ca.getTime());
        ca.add(Calendar.MONTH, -1);
        String startTime = sdf.format(ca.getTime());

        SkuDetailVo skuDetailVo = new SkuDetailVo();
        // 1.查询补货次数和维修次数
        int replenishCount = taskMapper.countTaskByInnerCodeAndTypeAndStatus(innerCode, DkdContants.TASK_TYPE_SUPPLY, DkdContants.TASK_STATUS_FINISH, startTime, endTime);
        int repairCount = taskMapper.countTaskByInnerCodeAndTypeAndStatus(innerCode, DkdContants.TASK_TYPE_REPAIR, DkdContants.TASK_STATUS_FINISH, startTime, endTime);
        skuDetailVo.setReplenishCount(replenishCount);
        skuDetailVo.setRepairCount(repairCount);

        // 2.查询最近一个月商品销售列表
        List<SkuVo> skuVoList = orderMapper.selectOrderListByInnercode(innerCode, startTime, endTime);
        skuDetailVo.setSkuList(skuVoList);
        if(skuVoList == null || skuVoList.size() == 0) {
            skuDetailVo.setSalesAmount(0);
        } else {
            // 3.查询商品销售额
            int salesAmount = orderMapper.sumOrderAmountByInnerCode(innerCode, startTime, endTime);
            skuDetailVo.setSalesAmount(salesAmount);
        }

        return skuDetailVo;
    }
}
