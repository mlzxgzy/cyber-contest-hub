<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="竞赛ID" prop="competitionId">
        <el-input
          v-model="queryParams.competitionId"
          placeholder="请输入竞赛ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="竞赛名称" prop="competitionName">
        <el-input
          v-model="queryParams.competitionName"
          placeholder="请输入竞赛名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="选手ID" prop="competitorId">
        <el-input
          v-model="queryParams.competitorId"
          placeholder="请输入选手ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="选手姓名" prop="competitorName">
        <el-input
          v-model="queryParams.competitorName"
          placeholder="请输入选手姓名"
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
          v-hasPermi="['exam:competiitonCompetitor:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['exam:competiitonCompetitor:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['exam:competiitonCompetitor:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['exam:competiitonCompetitor:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="competiitonCompetitorList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="竞赛ID" align="center" prop="competitionId" />
      <el-table-column label="竞赛名称" align="center" prop="competitionName" />
      <el-table-column label="选手ID" align="center" prop="competitorId" />
      <el-table-column label="选手姓名" align="center" prop="competitorName" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['exam:competiitonCompetitor:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['exam:competiitonCompetitor:remove']">删除</el-button>
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

    <!-- 添加或修改竞赛选手对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="competiitonCompetitorRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="竞赛ID" prop="competitionId">
          <el-input v-model="form.competitionId" placeholder="请输入竞赛ID" />
        </el-form-item>
        <el-form-item label="竞赛名称" prop="competitionName">
          <el-input v-model="form.competitionName" placeholder="请输入竞赛名称" />
        </el-form-item>
        <el-form-item label="选手ID" prop="competitorId">
          <el-input v-model="form.competitorId" placeholder="请输入选手ID" />
        </el-form-item>
        <el-form-item label="选手姓名" prop="competitorName">
          <el-input v-model="form.competitorName" placeholder="请输入选手姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CompetiitonCompetitor">
import {
  addCompetiitonCompetitor,
  delCompetiitonCompetitor,
  getCompetiitonCompetitor,
  listCompetiitonCompetitor,
  updateCompetiitonCompetitor
} from "@/api/exam/competiitonCompetitor";

const { proxy } = getCurrentInstance();

const competiitonCompetitorList = ref([]);
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
    competitionId: null,
    competitionName: null,
    competitorId: null,
    competitorName: null
  },
  rules: {
    competitionId: [
      { required: true, message: "竞赛ID不能为空", trigger: "blur" }
    ],
    competitionName: [
      { required: true, message: "竞赛名称不能为空", trigger: "blur" }
    ],
    competitorId: [
      { required: true, message: "选手ID不能为空", trigger: "blur" }
    ],
    competitorName: [
      { required: true, message: "选手姓名不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询竞赛选手列表 */
function getList() {
  loading.value = true;
  listCompetiitonCompetitor(queryParams.value).then(response => {
    competiitonCompetitorList.value = response.rows;
    total.value = response.total;
    loading.value = false;
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
    competitionId: null,
    competitionName: null,
    competitorId: null,
    competitorName: null
  };
  proxy.resetForm("competiitonCompetitorRef");
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
  title.value = "添加竞赛选手";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getCompetiitonCompetitor(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改竞赛选手";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["competiitonCompetitorRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateCompetiitonCompetitor(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addCompetiitonCompetitor(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除竞赛选手编号为"' + _ids + '"的数据项？').then(function() {
    return delCompetiitonCompetitor(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('exam/competiitonCompetitor/export', {
    ...queryParams.value
  }, `competiitonCompetitor_${new Date().getTime()}.xlsx`)
}

getList();
</script>
