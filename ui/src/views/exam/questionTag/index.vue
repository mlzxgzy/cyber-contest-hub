<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="题目ID" prop="questionId">
        <el-input
          v-model="queryParams.questionId"
          placeholder="请输入题目ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="题目名称" prop="questionName">
        <el-input
          v-model="queryParams.questionName"
          placeholder="请输入题目名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Tag" prop="tag">
        <el-input
          v-model="queryParams.tag"
          placeholder="请输入Tag"
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
          v-hasPermi="['exam:questionTag:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['exam:questionTag:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['exam:questionTag:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['exam:questionTag:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="questionTagList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="题目ID" align="center" prop="questionId" />
      <el-table-column label="题目名称" align="center" prop="questionName" />
      <el-table-column label="Tag" align="center" prop="tag" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['exam:questionTag:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['exam:questionTag:remove']">删除</el-button>
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

    <!-- 添加或修改题目Tag对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="questionTagRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="题目ID" prop="questionId">
          <el-input v-model="form.questionId" placeholder="请输入题目ID" />
        </el-form-item>
        <el-form-item label="题目名称" prop="questionName">
          <el-input v-model="form.questionName" placeholder="请输入题目名称" />
        </el-form-item>
        <el-form-item label="Tag" prop="tag">
          <el-input v-model="form.tag" placeholder="请输入Tag" />
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

<script setup name="QuestionTag">
import {
  addQuestionTag,
  delQuestionTag,
  getQuestionTag,
  listQuestionTag,
  updateQuestionTag
} from "@/api/exam/questionTag";

const { proxy } = getCurrentInstance();

const questionTagList = ref([]);
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
    questionId: null,
    questionName: null,
    tag: null
  },
  rules: {
    questionId: [
      { required: true, message: "题目ID不能为空", trigger: "blur" }
    ],
    questionName: [
      { required: true, message: "题目名称不能为空", trigger: "blur" }
    ],
    tag: [
      { required: true, message: "Tag不能为空", trigger: "blur" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询题目Tag列表 */
function getList() {
  loading.value = true;
  listQuestionTag(queryParams.value).then(response => {
    questionTagList.value = response.rows;
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
    questionId: null,
    questionName: null,
    tag: null
  };
  proxy.resetForm("questionTagRef");
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
  title.value = "添加题目Tag";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getQuestionTag(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改题目Tag";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["questionTagRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateQuestionTag(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addQuestionTag(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认删除题目Tag编号为"' + _ids + '"的数据项？').then(function() {
    return delQuestionTag(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('exam/questionTag/export', {
    ...queryParams.value
  }, `questionTag_${new Date().getTime()}.xlsx`)
}

getList();
</script>
