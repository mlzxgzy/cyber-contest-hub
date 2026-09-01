<script lang="ts" setup>
import {computed, ref, watch} from 'vue';
import dayjs from 'dayjs';
import {fetchCreateProject} from '@/service/api/cch/project';
import {useFormRules, useNaiveForm} from '@/hooks/common/form';
import {$t} from '@/locales';

defineOptions({
  name: 'ProjectOperateDrawer'
});

const visible = defineModel<boolean>('visible', {
  default: false
});

const emit = defineEmits<{
  (e: 'submitted'): void;
}>();

const {formRef, validate, restoreValidation} = useNaiveForm();
const {createRequiredRule} = useFormRules();

const projectTypeOptions = [
  {label: '普通项目', value: 'normal'},
  {label: '竞赛项目', value: 'contest'}
];

type Model = Api.Cch.ProjectOperateParams;

const model = ref<Model>(createDefaultModel());

function createDefaultModel(): Model {
  return {
    id: null,
    projectType: 'normal',
    name: '',
    remark: '',
    meta: undefined
  };
}

type RuleKey = Extract<
  keyof Model,
  | 'id'
  | 'projectType'
  | 'name'
  | 'remark'
>;

const isContestProject = computed(() => model.value.projectType === 'contest');

const rules = computed<Record<RuleKey, App.Global.FormRule>>(() => ({
  id: createRequiredRule('主键不能为空'),
  projectType: createRequiredRule('项目类型不能为空'),
  name: createRequiredRule(isContestProject.value ? '竞赛名称不能为空' : '项目名称不能为空'),
  remark: createRequiredRule(isContestProject.value ? '赛事备注不能为空' : '备注不能为空')
}));

const nameLabel = computed(() => {
  return isContestProject.value ? '竞赛名称' : '项目名称';
});

const namePlaceholder = computed(() => {
  return isContestProject.value ? '请输入竞赛名称' : '请输入项目名称';
});

const remarkLabel = computed(() => {
  return isContestProject.value ? '赛事备注' : '备注';
});

const remarkPlaceholder = computed(() => {
  return isContestProject.value ? '请输入赛事备注' : '请输入备注';
});

const contestMeta = ref<Api.Cch.ContestMeta>({
  contestName: '',
  contestRemark: '',
  startTime: undefined,
  endTime: undefined,
  challengeRequirement: '',
  stages: [],
  platforms: []
});

/** 新增一个阶段 */
function addStage() {
  if (!contestMeta.value.stages) {
    contestMeta.value.stages = [];
  }
  contestMeta.value.stages.push({
    stageName: '',
    startTime: null,
    duration: null,
    challengeRequirement: ''
  });
}

/** 删除指定阶段 */
function removeStage(index: number) {
  contestMeta.value.stages?.splice(index, 1);
}

/** 新增一个平台 */
function addPlatform() {
  if (!contestMeta.value.platforms) {
    contestMeta.value.platforms = [];
  }
  contestMeta.value.platforms.push({
    platformName: '',
    platformUrl: ''
  });
}

/** 删除指定平台 */
function removePlatform(index: number) {
  contestMeta.value.platforms?.splice(index, 1);
}

/** 根据开始时间与阶段时长（分钟）自动计算结束时间，未填时长时不显示 */
function getStageEndTime(stage: Api.Cch.ContestStage): string {
  if (!stage.startTime || !stage.duration || stage.duration <= 0) {
    return '';
  }
  const start = dayjs(stage.startTime);
  if (!start.isValid()) {
    return '';
  }
  return start.add(stage.duration, 'minute').format('YYYY-MM-DD HH:mm');
}

function closeDrawer() {
  visible.value = false;
}

function resetForm() {
  model.value = createDefaultModel();
  contestMeta.value = {
    contestName: '',
    contestRemark: '',
    startTime: undefined,
    endTime: undefined,
    challengeRequirement: '',
    stages: [],
    platforms: []
  };
}

async function handleSubmit() {
  await validate();

  const {projectType, name, remark} = model.value;

  const submitData: Api.Cch.ProjectOperateParams = {
    projectType,
    name,
    remark
  };

  // 如果是竞赛项目，添加meta信息
  if (projectType === 'contest') {
    submitData.meta = {
      contestName: name || '', // 使用项目名称作为竞赛名称
      contestRemark: remark || '', // 使用备注作为赛事备注
      startTime: contestMeta.value.startTime || '',
      endTime: contestMeta.value.endTime || '',
      challengeRequirement: contestMeta.value.challengeRequirement || '',
      stages: contestMeta.value.stages ?? [],
      platforms: contestMeta.value.platforms ?? []
    };
  }

  const {error} = await fetchCreateProject(submitData);
  if (error) return;

  window.$message?.success($t('common.addSuccess'));
  closeDrawer();
  emit('submitted');
}

watch(visible, () => {
  if (visible.value) {
    resetForm();
    restoreValidation();
  }
});
</script>

<template>
  <NDrawer v-model:show="visible" title="新增项目" :width="800" class="max-w-90%" display-directive="show">
    <NDrawerContent :native-scrollbar="false" title="新增项目" closable>
      <NForm ref="formRef" :model="model" :rules="rules">
        <NFormItem label="项目类型" path="projectType">
          <NSelect
            v-model:value="model.projectType"
            :options="projectTypeOptions"
            placeholder="请选择项目类型"
          />
        </NFormItem>
        <NFormItem :label="nameLabel" path="name">
          <NInput v-model:value="model.name" :placeholder="namePlaceholder"/>
        </NFormItem>
        <NFormItem :label="remarkLabel" path="remark">
          <NInput
            v-model:value="model.remark"
            :rows="3"
            :placeholder="remarkPlaceholder"
            type="textarea"
          />
        </NFormItem>
        <div v-if="isContestProject">
          <NDivider>竞赛信息</NDivider>
          <NFormItem label="开始时间">
            <NDatePicker
              v-model:formatted-value="contestMeta.startTime"
              type="date"
              value-format="yyyy-MM-dd"
              clearable
              placeholder="请选择开始时间"
            />
          </NFormItem>
          <NFormItem label="结束时间">
            <NDatePicker
              v-model:formatted-value="contestMeta.endTime"
              type="date"
              value-format="yyyy-MM-dd"
              clearable
              placeholder="请选择结束时间"
            />
          </NFormItem>
          <NFormItem label="题目需求">
            <NInput
              v-model:value="contestMeta.challengeRequirement"
              type="textarea"
              :rows="4"
              placeholder="请输入题目需求描述（支持多行）"
            />
          </NFormItem>

          <NDivider>竞赛阶段</NDivider>
          <div v-for="(stage, sIndex) in contestMeta.stages" :key="sIndex" class="stage-block">
            <NGrid :cols="3" :x-gap="12" responsive="screen">
              <NFormItem :label="`阶段${sIndex + 1}名称`">
                <NInput
                  v-model:value="stage.stageName"
                  placeholder="如：初赛 / 决赛 / 选拔赛"
                />
              </NFormItem>
              <NFormItem label="开始时间">
                <NDatePicker
                  v-model:formatted-value="stage.startTime"
                  type="datetime"
                  value-format="yyyy-MM-dd HH:mm"
                  clearable
                  placeholder="请选择开始时间"
                />
              </NFormItem>
              <NFormItem label="阶段时长（分钟）">
                <NInputNumber
                  v-model:value="stage.duration"
                  :min="0"
                  :step="10"
                  clearable
                  placeholder="如：120"
                />
              </NFormItem>
            </NGrid>
            <NFormItem label="结束时间">
              <div class="drawer-endtime">
                {{ getStageEndTime(stage) || '填写开始时间与时长后自动计算' }}
              </div>
            </NFormItem>
            <NFormItem label="本阶段赛题需求">
              <NInput
                v-model:value="stage.challengeRequirement"
                type="textarea"
                :rows="2"
                placeholder="请输入本阶段赛题需求"
              />
            </NFormItem>
            <div class="stage-remove">
              <NButton native-type="button" size="small" type="error" quaternary @click="removeStage(sIndex)">
                删除此阶段
              </NButton>
            </div>
          </div>
          <NButton native-type="button" dashed block @click="addStage">
            <template #icon><NIcon><i class="i-carbon-add" /></NIcon></template>
            添加阶段
          </NButton>

          <NDivider>竞赛平台</NDivider>
          <div v-for="(platform, pIndex) in contestMeta.platforms" :key="pIndex" class="platform-block">
            <NGrid :cols="2" :x-gap="12" responsive="screen">
              <NFormItem label="平台名称">
                <NInput
                  v-model:value="platform.platformName"
                  placeholder="请输入平台名称"
                />
              </NFormItem>
              <NFormItem label="平台地址">
                <NInput
                  v-model:value="platform.platformUrl"
                  placeholder="请输入平台地址（URL）"
                />
              </NFormItem>
            </NGrid>
            <div class="stage-remove">
              <NButton native-type="button" size="small" type="error" quaternary @click="removePlatform(pIndex)">
                删除此平台
              </NButton>
            </div>
          </div>
          <NButton native-type="button" dashed block @click="addPlatform">
            <template #icon><NIcon><i class="i-carbon-add" /></NIcon></template>
            添加平台
          </NButton>
        </div>
      </NForm>
      <template #footer>
        <NSpace :size="16">
          <NButton @click="closeDrawer">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>
.stage-block,
.platform-block {
  border: 1px dashed var(--n-border-color);
  border-radius: 8px;
  padding: 4px 12px 12px;
  margin-bottom: 12px;
}

.stage-remove {
  display: flex;
  justify-content: flex-end;
}

.drawer-endtime {
  width: 100%;
  padding: 6px 11px;
  border: 1px solid var(--n-border-color);
  border-radius: 4px;
  background: var(--n-color, transparent);
  color: var(--n-text-color-3, #666);
  min-height: 34px;
  display: flex;
  align-items: center;
}
</style>
