<template>
  <div class="app-container">
    <el-form @submit.native.prevent :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="合作商名称" prop="partnerName">
        <el-input
          v-model="queryParams.partnerName"
          placeholder="请输入合作商名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['manage:partner:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['manage:partner:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['manage:partner:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['manage:partner:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="partnerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="50" align="center"
      :index="item => (queryParams.pageNum - 1) * queryParams.pageSize + item + 1" />
      <el-table-column label="合作商名称" align="center" prop="partnerName" />
      <el-table-column label="账号" align="center" prop="username" />
      <el-table-column label="点位数" align="center" prop="nodeCount" />
      <el-table-column label="分成比例" align="center">
        <template #default="scope">
          {{ scope.row.profitShareRatio }}%
        </template>
      </el-table-column>
      <el-table-column label="联系人" align="center" prop="contactPerson" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" />
      <el-table-column label="操作" width="300px" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" @click="handleReset(scope.row)" v-hasPermi="['manage:partner:edit']">重置密码</el-button>
          <el-button link type="primary" @click="handleRead(scope.row)" v-hasPermi="['manage:partner:query']">查看详情</el-button>
          <el-button link type="primary" @click="handleUpdate(scope.row)" v-hasPermi="['manage:partner:edit']">修改</el-button>
          <el-button link type="primary" @click="handleDelete(scope.row)" v-hasPermi="['manage:partner:remove']">删除</el-button>
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

    <!-- 添加或修改合作商管理对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="partnerRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="合作商名称" prop="partnerName">
          <el-input v-model="form.partnerName" placeholder="请输入合作商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item v-if="form.id !== null" label="创建时间">
          {{ form.createTime }}
        </el-form-item>
        <el-form-item label="分成比例(%)" prop="profitShareRatio">
          <el-input @input="e => form.profitShareRatio = e.replace(/[^\d.]/g, '')" v-model="form.profitShareRatio" placeholder="请输入分成比例(%)" />
        </el-form-item>
        <el-form-item v-if="form.id === null" label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item v-if="form.id === null" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看合作商详情对话框 -->
    <el-dialog title="合作商详情" v-model="readOpen" width="500px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="合作商名称">{{ form.partnerName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ form.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ form.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="分成比例">{{ form.profitShareRatio }}%</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="Partner">
import { listPartner, getPartner, delPartner, addPartner, updatePartner, resetPartnerPwd } from "@/api/manage/partner";
import { ElMessageBox } from 'element-plus'

const { proxy } = getCurrentInstance();

const partnerList = ref([]);
const readOpen = ref(false);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    partnerName: null,
  },
  rules: {
    partnerName: [
      { required: true, message: "合作商名称不能为空", trigger: "blur" }
    ],
    contactPerson: [
      { required: true, message: "联系人不能为空", trigger: "blur" }
    ],
    contactPhone: [
      { required: true, message: "联系电话不能为空", trigger: "blur" },
      { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号码", trigger: "blur" }
    ],
    profitShareRatio: [
      { required: true, message: "分成比例(%)不能为空", trigger: "blur" },
      { pattern: /^([0-9]{1,2}(\.[0-9]{1,2})?|100(\.00?)?)$/, message: "分成比例(%)必须在0-100之间且不能超过两位小数", trigger: "blur" }
    ],
    username: [
      { required: true, message: "账号不能为空", trigger: "blur" },
      { pattern: /^[a-zA-Z0-9]{6,16}$/, message: "账号必须为6-16位数字或字母", trigger: "blur" }
    ],
    password: [
      { required: true, message: "密码不能为空", trigger: "blur" },
      { pattern: /^[a-zA-Z0-9]{6,16}$/, message: "密码必须为6-16位数字或字母", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询合作商管理列表 */
function getList() {
  loading.value = true;
  listPartner(queryParams.value).then(response => {
    partnerList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  setTimeout(() => {
    reset();
  }, 500);
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    partnerName: null,
    contactPerson: null,
    contactPhone: null,
    profitShareRatio: null,
    username: null,
    password: null,
    createTime: null,
    updateTime: null,
    createBy: null,
    updateBy: null,
    remark: null
  };
  proxy.resetForm("partnerRef");
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

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加合作商管理";
}

/** 查看详情按钮操作 */
function handleRead(row) {
  reset();
  getPartner(row.id).then(response => {
    form.value = response.data;
    readOpen.value = true;
  });
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getPartner(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改合作商管理";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["partnerRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updatePartner(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addPartner(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 重置密码按钮操作 */
function handleReset(row) {
  ElMessageBox.confirm('你确定要重置该合作商密码吗？', '', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    center: true,
    customStyle: {
      width: '270px'
    }
  }).then(function() {
    return resetPartnerPwd(row.id);
  }).then(() => {
    proxy.$modal.msgSuccess("重置密码成功");
  }).catch(() => {});
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value;
  proxy.$modal.delete('你确定要删除本条内容吗？').then(function() {
    return delPartner(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/partner/export', {
    ...queryParams.value
  }, `partner_${new Date().getTime()}.xlsx`)
}

getList();
</script>
