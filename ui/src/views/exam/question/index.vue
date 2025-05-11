<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="类型" prop="category">
        <el-input
          v-model="queryParams.category"
          placeholder="请输入类型"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="难度" prop="difficult">
        <el-input
          v-model="queryParams.difficult"
          placeholder="请输入难度"
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
          v-hasPermi="['exam:question:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['exam:question:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['exam:question:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['exam:question:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="questionList" @selection-change="handleSelectionChange"
              :row-key="row => row.id"
              @expand-change="handleExpandChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="名称" align="center" prop="name" />
      <el-table-column label="描述" align="center" prop="description" />
      <el-table-column label="类型" align="center" prop="category" />
      <el-table-column label="难度" align="center" prop="difficult" />
      <el-table-column label="Flag" align="center" prop="flag" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Discount" @click="handleAddTag(scope.row)" v-hasPermi="['exam:question:edit', 'exam:questionTag:add']">添加Tag</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['exam:question:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['exam:question:remove']">删除</el-button>
        </template>
      </el-table-column>
      <el-table-column type="expand">
        <template #default="{row}">
          <div v-if="row.expandLoading" class="expand-loading">
            <el-icon class="is-loading">
              <Loading/>
            </el-icon>
            加载中...
          </div>
          <div v-else class="expand-content">
            <!-- Tag展示区域 -->
            <div class="section">
              <h4>题目标签</h4>
              <div class="tag-container">
                <el-tag
                    v-for="tag in tagsMap[row.id]"
                    :key="tag.id"
                    class="tag-item"
                    closable
                    @close="handleDeleteTag(row.id, tag.id)"
                    v-hasPermi="['exam:questionTag:remove']"
                >
                  {{ tag.tag }}
                </el-tag>
                <el-button
                    v-hasPermi="['exam:questionTag:add']"
                    type="primary"
                    size="small"
                    @click="handleAddTag(row)"
                >
                  + 添加标签
                </el-button>
                <div v-if="!tagsMap[row.id]?.length" class="empty-tip">
                  暂无标签
                </div>
              </div>
            </div>

            <!-- 附件展示区域 -->
            <div class="section">
              <h4>题目附件</h4>
              <div class="attachment-container">
                <div
                    v-for="file in attachmentsMap[row.id]"
                    :key="file.id"
                    class="attachment-item"
                >
                  <div class="file-info">
                    <el-link
                        type="primary"
                        :underline="false"
                        @click="handleDownload(file)"
                    >
                      <el-icon>
                        <Document/>
                      </el-icon>
                      {{ file.name }}
                    </el-link>
                    <div class="file-desc" v-if="file.description">
                      {{ file.description }}
                    </div>
                  </div>
                  <div class="file-actions">
                    <el-button
                        link
                        type="danger"
                        size="small"
                        @click="handleDeleteAttachment(row.id, file.id)"
                        v-hasPermi="['exam:questionAttachment:remove']"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                <div v-if="!attachmentsMap[row.id]?.length" class="empty-tip">
                  暂无附件
                </div>
              </div>
            </div>
          </div>
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

    <!-- 添加或修改题目对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="questionRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="类型" prop="category">
          <el-input v-model="form.category" placeholder="请输入类型" />
        </el-form-item>
        <el-form-item label="难度" prop="difficult">
          <el-input v-model="form.difficult" placeholder="请输入难度" />
        </el-form-item>
        <el-form-item label="Flag" prop="flag">
          <el-input v-model="form.flag" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加Tag对话框 -->
    <el-dialog title="添加题目Tag" v-model="addTagOpen" width="400px" append-to-body>
      <el-form ref="tagFormRef" :model="tagForm" :rules="tagRules" label-width="80px">
        <el-form-item label="题目ID" prop="questionId" v-show="false">
          <el-input v-model="tagForm.questionId" placeholder="请输入题目ID" />
        </el-form-item>
        <el-form-item label="题目名称" prop="questionName" v-show="false">
          <el-input v-model="tagForm.questionName" placeholder="请输入题目名称" />
        </el-form-item>
        <el-form-item label="Tag名称" prop="tag">
          <el-input
              v-model="tagForm.tag"
              placeholder="请输入Tag名称"
              clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitTagForm">确 定</el-button>
          <el-button @click="cancelTagForm">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Question">
import { listQuestion, getQuestion, delQuestion, addQuestion, updateQuestion } from "@/api/exam/question";
import { listQuestionTag, addQuestionTag } from "@/api/exam/questionTag";
import { listQuestionAttachment } from "@/api/exam/questionAttachment";

const { proxy } = getCurrentInstance();

const questionList = ref([]);
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
    name: null,
    description: null,
    category: null,
    difficult: null,
    flag: null,
  },
  rules: {
    name: [
      { required: true, message: "名称不能为空", trigger: "blur" }
    ],
    category: [
      { required: true, message: "类型不能为空", trigger: "blur" }
    ],
    difficult: [
      { required: true, message: "难度不能为空", trigger: "blur" }
    ],
    flag: [
      { required: true, message: "Flag不能为空", trigger: "blur" }
    ],
    createTime: [
      { required: true, message: "创建时间不能为空", trigger: "blur" }
    ],
    updateTime: [
      { required: true, message: "修改时间不能为空", trigger: "blur" }
    ],
  }
});

// 新增状态
const addTagOpen = ref(false);
const tagForm = ref({
  questionId: null,
  questionName: null,
  tag: null
});

// 新增状态
const tagsMap = ref({});         // {题目ID: Tag列表}
const attachmentsMap = ref({});  // {题目ID: 附件列表}
const expandedRows = ref(new Set());// 已展开的行

// 验证规则
const tagRules = reactive({
  tag: [
    { required: true, message: "Tag名称不能为空", trigger: "blur" }
  ]
});

// 新增方法 - 处理展开行
const handleExpandChange = async (row) => {
  if (!expandedRows.value.has(row.id)) {
    try {
      row.expandLoading = true;
      // 并行获取Tag和附件
      const [tagsRes, attachmentsRes] = await Promise.all([
        listQuestionTag({ questionId: row.id }),
        listQuestionAttachment({ questionId: row.id })
      ]);

      tagsMap.value[row.id] = tagsRes.rows;
      attachmentsMap.value[row.id] = attachmentsRes.rows;
      expandedRows.value.add(row.id);
    } finally {
      row.expandLoading = false;
    }
  }
};

// 新增方法 - 删除Tag
const handleDeleteTag = async (questionId, tagId) => {
  await proxy.$modal.confirm('确认删除该Tag?');
  await delQuestionTag(tagId);
  tagsMap.value[questionId] = tagsMap.value[questionId].filter(t => t.id !== tagId);
  proxy.$modal.msgSuccess("删除成功");
};

// 新增方法 - 删除附件
const handleDeleteAttachment = async (questionId, attachmentId) => {
  await proxy.$modal.confirm('确认删除该附件?');
  await delQuestionAttachment(attachmentId);
  attachmentsMap.value[questionId] = attachmentsMap.value[questionId].filter(a => a.id !== attachmentId);
  proxy.$modal.msgSuccess("删除成功");
};

// 新增方法 - 下载附件
const handleDownload = (row) => {
  window.location.href = row.path;
};

const { queryParams, form, rules } = toRefs(data);

/** 查询题目列表 */
function getList() {
  loading.value = true;
  listQuestion(queryParams.value).then(response => {
    questionList.value = response.rows;
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
    name: null,
    description: null,
    category: null,
    difficult: null,
    flag: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  };
  proxy.resetForm("questionRef");
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
  title.value = "添加题目";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getQuestion(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改题目";
  });
}

/** 添加Tag按钮操作 */
function handleAddTag(row) {
  // 重置表单
  tagForm.value = {
    questionId: row.id,  // 使用当前题目的ID
    questionName: row.name,  // 使用当前题目的ID
    tag: null
  };
  addTagOpen.value = true;
}

/** 提交Tag表单 */
function submitTagForm() {
  proxy.$refs["tagFormRef"].validate(async (valid) => {
    if (valid) {
      try {
        await addQuestionTag(tagForm.value);
        proxy.$modal.msgSuccess("Tag添加成功");
        addTagOpen.value = false;
        // 更新本地缓存
        if (tagsMap.value[tagForm.value.questionId]) {
          tagsMap.value[tagForm.value.questionId].push({
            id: Date.now(), // 临时ID，实际应使用接口返回数据
            ...tagForm.value
          });
        }
      } catch {
        proxy.$modal.msgError("Tag添加失败");
      }
    }
  });
}

/** 取消Tag添加 */
function cancelTagForm() {
  addTagOpen.value = false;
  proxy.resetForm("tagFormRef");
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["questionRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateQuestion(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addQuestion(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认删除题目编号为"' + _ids + '"的数据项？').then(function() {
    return delQuestion(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('exam/question/export', {
    ...queryParams.value
  }, `question_${new Date().getTime()}.xlsx`)
}

getList();
</script>

<style scoped>
.expand-content {
  padding: 0 40px;
}

.section {
  margin: 20px 0;
}

.section h4 {
  color: #606266;
  font-size: 14px;
  margin: 0 0 12px 0;
}

.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 16px;
}

.tag-item {
  margin-right: 8px;
  transition: all 0.3s;
}

.tag-item:hover {
  transform: translateY(-2px);
}

.attachment-container {
  border-radius: 4px;
  background: #f8f9fa;
  padding: 12px;
}

.attachment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  margin: 4px 0;
  background: white;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

.attachment-item:hover {
  transform: translateX(4px);
}

.file-info {
  flex: 1;
}

.file-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.empty-tip {
  color: #909399;
  font-size: 13px;
  padding: 8px 0;
}
</style>
