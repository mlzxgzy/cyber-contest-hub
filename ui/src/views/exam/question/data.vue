<template>
  <div class="app-container">
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
      <el-form-item>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup name="QuestionForm">
import { getQuestion, addQuestion, updateQuestion } from "@/api/exam/question";
import { useRoute, useRouter } from 'vue-router';

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const data = reactive({
  form: {},
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
  }
});

const { form, rules } = toRefs(data);

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

// 取消按钮
function cancel() {
  proxy.$tab.closePage();
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["questionRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateQuestion(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          proxy.$tab.closePage();
        });
      } else {
        addQuestion(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          proxy.$tab.closePage();
        });
      }
    }
  });
}

// 初始化
(() => {
  reset();
  const id = route.query.id;
  if (id) {
    // 修改模式，获取题目详情
    getQuestion(id).then(response => {
      form.value = response.data;
    });
  }
})();
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
