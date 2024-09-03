<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="订单编号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="选择日期" style="width: 308px">
        <el-date-picker
          v-model="daterangeCreateTime"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="orderList">
      <el-table-column label="序号" type="index" width="50" align="center"
      :index="item => (queryParams.pageNum - 1) * queryParams.pageSize + item + 1" />
      <el-table-column width="250px" label="订单编号" align="center" prop="orderNo" />
      <el-table-column label="商品名称" align="center" prop="skuName" />
      <el-table-column label="订单金额" align="center">
        <template #default="scope">
          <el-tag>{{ scope.row.amount }}元</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="设备编号" align="center" prop="innerCode" />
      <el-table-column label="订单状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="order_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="订单日期" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" @click="handleRead(scope.row)" v-hasPermi="['manage:order:query']">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 订单详情对话框 -->
    <el-dialog title="订单详情" v-model="open" width="500px" append-to-body>
      <el-form label-width="80">
        <el-row>
          <el-col :span="24">
            <el-form-item label="订单编号">
              {{ form.orderNo }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品名称">
              {{ form.skuName }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单金额">
              {{ form.amount }} 元
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单状态">
              {{ order_status[form.status].label }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备编号">
              {{ form.innerCode }}
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="设备地址">
              {{ form.addr }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="创建时间">
              {{ form.createTime }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="完成时间">
              {{ form.updateTime }}
            </el-form-item>
          </el-col>
          <el-col v-if="form.status === 1" :span="12">
            <el-form-item label="支付方式">
              {{ form.payType === 'wxpay' ? '微信支付' : '支付宝支付' }}
            </el-form-item>
          </el-col>
          <el-col v-if="form.status === 1" :span="12">
            <el-form-item label="交易单号">
              {{ form.thirdNo }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作">
              {{ pay_status[form.payStatus].label }}
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
    </el-dialog>
  </div>
</template>

<script setup name="Order">
import { listOrder, getOrder } from "@/api/manage/order";

const { proxy } = getCurrentInstance();
const { pay_status, order_status } = proxy.useDict('pay_status', 'order_status');

const orderList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const total = ref(0);
const title = ref("");
const daterangeCreateTime = ref([]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderNo: null,
    innerCode: null,
    skuId: null,
    skuName: null,
    classId: null,
    status: null,
    regionId: null,
    partnerId: null,
    nodeId: null,
    createTime: null,
  },
  rules: {
    orderNo: [
      { required: true, message: "订单编号不能为空", trigger: "blur" }
    ],
    amount: [
      { required: true, message: "支付金额不能为空", trigger: "blur" }
    ],
    price: [
      { required: true, message: "商品金额不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询订单管理列表 */
function getList() {
  loading.value = true;
  queryParams.value.params = {};
  if (null != daterangeCreateTime && '' != daterangeCreateTime) {
    queryParams.value.params["beginCreateTime"] = daterangeCreateTime.value[0];
    queryParams.value.params["endCreateTime"] = daterangeCreateTime.value[1];
  }
  if (queryParams.value.params.beginCreateTime !== undefined) queryParams.value.params.beginCreateTime += ' 00:00:00';
  if (queryParams.value.params.endCreateTime !== undefined) queryParams.value.params.endCreateTime += ' 23:59:59'; 
  
  listOrder(queryParams.value).then(response => {
    orderList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    if (total.value === 0) {
      // 提示无相关搜索结果
      proxy.$modal.msg("无相关搜索结果");
    }
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    orderNo: null,
    thirdNo: null,
    innerCode: null,
    channelCode: null,
    skuId: null,
    skuName: null,
    classId: null,
    status: null,
    amount: null,
    price: null,
    payType: null,
    payStatus: null,
    bill: null,
    addr: null,
    regionId: null,
    regionName: null,
    businessType: null,
    partnerId: null,
    openId: null,
    nodeId: null,
    nodeName: null,
    cancelDesc: null,
    createTime: null,
    updateTime: null
  };
  proxy.resetForm("orderRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  daterangeCreateTime.value = [];
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 查看详情按钮操作 */
function handleRead(row) {
  console.log(order_status.value);
  
  form.value = {...row}
  open.value = true;
}

getList();
</script>
