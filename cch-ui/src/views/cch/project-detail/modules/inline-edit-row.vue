<script lang="ts" setup>
import {computed, nextTick, ref} from 'vue';
import {onClickOutside} from '@vueuse/core';

defineOptions({
  name: 'InlineEditRow'
});

interface Props {
  /** 行标签 */
  label: string;
  /** 当前值 */
  value?: string | null;
  /** 编辑控件类型 */
  type?: 'text' | 'textarea' | 'date';
  /** 是否可编辑 */
  editable?: boolean;
  /** 空值占位提示 */
  placeholder?: string;
  /** 保存中（禁用编辑控件防止重复提交） */
  saving?: boolean;
  /** 历史输入推荐（传入后文本编辑控件使用自动补全） */
  suggestions?: string[];
}

const props = withDefaults(defineProps<Props>(), {
  value: '',
  type: 'text',
  editable: false,
  placeholder: '点击填写',
  saving: false,
  suggestions: undefined
});

interface Emits {
  (e: 'save', value: string): void;
}

const emit = defineEmits<Emits>();

const editing = ref(false);
const draft = ref('');
const inputRef = ref<{ focus: () => void; select?: () => void } | null>(null);
const datePickerRef = ref<{ focus: () => void } | null>(null);
const rowRef = ref<HTMLElement | null>(null);

const isTextarea = computed(() => props.type === 'textarea');
const isDate = computed(() => props.type === 'date');
const useAutocomplete = computed(() => props.suggestions !== undefined);
const displayValue = computed(() => props.value || '');

/** 根据输入内容过滤历史推荐 */
const autocompleteOptions = computed(() => {
  const keyword = (draft.value || '').trim();
  const list = props.suggestions || [];
  const matched = keyword ? list.filter(item => item.includes(keyword) && item !== keyword) : list;
  return matched.map(item => ({label: item, value: item}));
});

onClickOutside(rowRef, () => {
  if (editing.value) {
    handleCancel();
  }
});

function startEdit() {
  if (!props.editable || editing.value || props.saving) return;
  draft.value = props.value || '';
  editing.value = true;
  nextTick(() => {
    if (isDate.value) {
      datePickerRef.value?.focus?.();
    } else {
      inputRef.value?.focus();
      inputRef.value?.select();
    }
  });
}

function handleBlur() {
  if (!editing.value) return;
  commit();
}

function handleEnter() {
  inputRef.value?.blur();
}

function handleCancel() {
  if (!editing.value) return;
  editing.value = false;
}

function commit() {
  editing.value = false;
  const next = draft.value ?? '';
  if (next !== (props.value || '')) {
    emit('save', next);
  }
}

function handleDateChange(val: string | null) {
  draft.value = val || '';
  commit();
}
</script>

<template>
  <div
    ref="rowRef"
    class="inline-edit-row"
    :class="{'inline-edit-row--editable': editable && !editing, 'inline-edit-row--editing': editing}"
    @click="startEdit"
  >
    <div class="inline-edit-row__label">{{ label }}</div>
    <div class="inline-edit-row__content">
      <template v-if="editing">
        <NAutoComplete
          v-if="type === 'text' && useAutocomplete"
          ref="inputRef"
          v-model:value="draft"
          size="small"
          :options="autocompleteOptions"
          :placeholder="placeholder"
          :disabled="saving"
          :input-props="{autocomplete: 'off'}"
          @blur="handleBlur"
          @keydown.enter.prevent="handleEnter"
          @keydown.esc.prevent="handleCancel"
        />
        <NInput
          v-else-if="type === 'text'"
          ref="inputRef"
          v-model:value="draft"
          size="small"
          :placeholder="placeholder"
          :disabled="saving"
          @blur="handleBlur"
          @keydown.enter.prevent="handleEnter"
          @keydown.esc.prevent="handleCancel"
        />
        <NInput
          v-else-if="type === 'textarea'"
          ref="inputRef"
          v-model:value="draft"
          type="textarea"
          size="small"
          :rows="4"
          :placeholder="placeholder"
          :disabled="saving"
          @blur="handleBlur"
          @keydown.esc.prevent="handleCancel"
        />
        <NDatePicker
          v-else
          ref="datePickerRef"
          :formatted-value="draft || null"
          type="date"
          value-format="yyyy-MM-dd"
          size="small"
          clearable
          :placeholder="placeholder"
          :disabled="saving"
          @update:formatted-value="handleDateChange"
          @blur="handleBlur"
        />
      </template>
      <template v-else>
        <div class="inline-edit-row__value" :class="{'inline-edit-row__value--empty': !displayValue}">
          <span class="whitespace-pre-wrap">{{ displayValue || placeholder }}</span>
        </div>
        <ButtonIcon v-if="editable" class="inline-edit-row__edit-icon" icon="ic:round-edit" />
      </template>
    </div>
  </div>
</template>

<style scoped>
.inline-edit-row {
  display: flex;
  align-items: flex-start;
  padding: 8px 12px;
  border-bottom: 1px dashed var(--n-border-color, rgba(128, 128, 128, 0.2));
}

.inline-edit-row:last-child {
  border-bottom: none;
}

.inline-edit-row--editable {
  cursor: pointer;
  transition: background-color 0.2s;
}

.inline-edit-row--editable:hover {
  background-color: var(--n-color-target, rgba(128, 128, 128, 0.08));
}

.inline-edit-row--editing {
  cursor: default;
}

.inline-edit-row__label {
  flex-shrink: 0;
  width: 96px;
  padding-top: 2px;
  overflow: hidden;
  color: var(--n-text-color-disabled, rgba(128, 128, 128, 0.8));
  font-size: 13px;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.inline-edit-row__content {
  display: flex;
  flex: 1;
  gap: 8px;
  align-items: flex-start;
  min-width: 0;
}

.inline-edit-row__value {
  flex: 1;
  padding-top: 2px;
  min-width: 0;
  font-size: 14px;
  line-height: 20px;
  word-break: break-word;
}

.inline-edit-row__value--empty {
  color: var(--n-text-color-disabled, rgba(128, 128, 128, 0.5));
}

.inline-edit-row__edit-icon {
  flex-shrink: 0;
  visibility: hidden;
  margin-top: 2px;
}

.inline-edit-row--editable:hover .inline-edit-row__edit-icon {
  visibility: visible;
}
</style>
