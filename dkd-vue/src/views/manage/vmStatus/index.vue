<template>
  <div class="app-container">
    <el-form @submit.native.prevent :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="设备编号" prop="innerCode">
        <el-input
          v-model="queryParams.innerCode"
          placeholder="请输入设备编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="vmList">
      <el-table-column label="序号" type="index" width="50" align="center"
      :index="item => (queryParams.pageNum - 1) * queryParams.pageSize + item + 1" />
      <el-table-column label="设备编号" align="center" prop="innerCode" />
      <el-table-column label="设备型号" align="center">
        <template #default="scope">
          {{ vmTypeList.find(item => item.id === scope.row.vmTypeId).name }}
        </template>
      </el-table-column>
      <el-table-column label="详细地址" align="left" prop="addr" show-overflow-tooltip />
      <el-table-column label="运营状态" align="center">
        <template #default="scope">
          <dict-tag :options="vm_status" :value="scope.row.vmStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="设备状态" align="center">
        <template #default="scope">
          {{ (scope.row.runningStatus !== null && JSON.parse(scope.row.runningStatus).status) ? '正常' : '异常' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" @click="handleRead(scope.row)" v-hasPermi="['manage:vm:query']">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
      :page-sizes="[10, 20, 50]"
    />

    <!-- 添加或修改设备管理对话框 -->
    <el-dialog style="padding-bottom: 24px;" title="设备详情" v-model="open" width="600px" append-to-body>
      <el-row style="padding-left: 16px;">
        <el-col :span="6">
          <el-form-item label="销售量：">
            <div style="margin-left: -10px;">{{ form.salesCount }}</div>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="销售额：">
            <div style="margin-left: -10px;">{{ form.salesAmount }}</div>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="补货次数：">
            <div style="margin-left: -10px;">{{ form.replenishCount }}</div>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="维修次数：">
            <div style="margin-left: -10px;">{{ form.repairCount }}</div>
          </el-form-item>
        </el-col>
      </el-row>
      <label style="padding-left: 16px;">商品销量（月）</label> 
      <table style="margin: 20px 0 0 26px; border-collapse: collapse;" border="1" width="500">
        <tbody>
          <tr v-for="item1 in rowNum - 1">
            <td style="padding-left: 5px;" v-for="item2 in 5" width="100" height="30">{{ form.skuList[(item1-1)*5+item2-1].skuName }}：{{ form.skuList[(item1-1)*5+item2-1].saleNum }}</td>
            <!-- <td style="padding-left: 5px;" v-for="item2 in 5" width="100" height="30">111</td> -->
          </tr>
          <tr>
            <td style="padding-left: 5px;" v-for="item2 in lastColNum" width="100" height="30">{{ form.skuList[(rowNum-1)*5+item2-1].skuName }}：{{ form.skuList[(rowNum-1)*5+item2-1].saleNum }}</td>
            <!-- <td style="padding-left: 5px;" v-for="item2 in lastColNum" width="100" height="30">111</td> -->
            <td style="padding-left: 5px;" v-for="item2 in 5 - lastColNum" width="100" height="30"></td>
          </tr>
        </tbody>
      </table>
      <el-button style="float: right; margin: 10px 30px 0 0;" type="primary" link @click="skipDeviceManage">调整货道</el-button>
    </el-dialog>
  </div>
</template>

<script setup name="Vm">
import { listVm } from "@/api/manage/vm";
import { listVmType } from "@/api/manage/vmType";
import { loadAllParams } from "@/api/page";
import { getOrderByInnerCode } from '@/api/manage/order'
import router from '@/router'

const { proxy } = getCurrentInstance();
const { vm_status } = proxy.useDict('vm_status');

const vmList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    innerCode: null,
    nodeId: null,
    businessType: null,
    regionId: null,
    partnerId: null,
    vmTypeId: null,
    vmStatus: null,
    runningStatus: null,
    policyId: null,
  },
  form: {},
  skipDate: {}
});

const { queryParams, form, skipDate } = toRefs(data);

/** 查询设备管理列表 */
function getList() {
  loading.value = true;
  listVm(queryParams.value).then(response => {
    vmList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 查看详情按钮操作 */
const rowNum = ref(1);
const lastColNum = ref(0);
function handleRead(row) {
  form.value = {}
  skipDate.value = {...row}
  rowNum.value = 1
  lastColNum.value = 0
  getOrderByInnerCode(row.innerCode).then(response => {
    form.value = response.data;
    form.value.salesCount = response.data.skuList.reduce((acc, cur) => acc + cur.saleNum, 0)
    if(form.value.skuList.length > 0) {
      rowNum.value = Math.ceil(form.value.skuList.length / 5)
      lastColNum.value = form.value.skuList.length % 5 === 0 ? 5 : form.value.skuList.length % 5
    }
    open.value = true;
  });
}

/** 查询设备类型列表 */
const vmTypeList = ref([]);
function getVmTypeList() {
  listVmType(loadAllParams).then(response => {
    vmTypeList.value = response.rows;
  });
}

// 跳转到设备管理货道对话框
function skipDeviceManage() {
  router.push({
    path: '/vm/vm',
    query: {
      innerCode: skipDate.value.innerCode,
      vmTypeId: skipDate.value.vmTypeId
    }
  })
}

getVmTypeList();
getList();
</script>
