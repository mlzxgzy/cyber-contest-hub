<script setup lang="ts">
import { ref, watch } from 'vue';
import { NButton, NInput } from 'naive-ui';
import { type ParsedVersion, buildVersion, parseVersion } from '../version-tag';

defineOptions({
  name: 'VersionTagInput'
});

const modelValue = defineModel<string>({ default: '' });

const parsed = ref<ParsedVersion | null>(null);

// 内部编辑产生的新值不重新解析，避免编辑过程中分段结构被意外折叠
let internalEmit = false;

watch(
  modelValue,
  val => {
    if (internalEmit) return;
    parsed.value = parseVersion(val);
  },
  { immediate: true }
);

function emitChange() {
  const p = parsed.value;
  if (!p) return;
  const next = buildVersion(p);
  if (next !== modelValue.value) {
    internalEmit = true;
    modelValue.value = next;
    internalEmit = false;
  }
}

/** 对第 index 段加/减 delta（高位 +1 时低位清零，遵循语义化版本惯例） */
function bumpSegment(index: number, delta: number) {
  const p = parsed.value;
  if (!p) return;
  const current = Number.parseInt(p.segments[index], 10) || 0;
  p.segments[index] = String(Math.max(0, current + delta));
  if (delta > 0) {
    for (let i = index + 1; i < p.segments.length; i += 1) {
      p.segments[i] = '0';
    }
  }
  emitChange();
}

/** 段内直接输入：只保留数字，为空时暂不发值（等失焦兜底） */
function onSegmentInput(index: number, value: string) {
  const p = parsed.value;
  if (!p) return;
  const cleaned = value.replace(/\D/g, '');
  p.segments[index] = cleaned;
  if (cleaned !== '') {
    emitChange();
  }
}

/** 段内输入为空时，失焦兜底为 0 */
function onSegmentBlur(index: number) {
  const p = parsed.value;
  if (!p) return;
  if (p.segments[index] === '') {
    p.segments[index] = '0';
    emitChange();
  }
}

/** 非数字形态（无法解析）时退化为普通输入框 */
function onFallbackInput(value: string) {
  modelValue.value = value;
}
</script>

<template>
  <div v-if="parsed" class="version-tag-input">
    <span v-if="parsed.prefix" class="vt-prefix">{{ parsed.prefix }}</span>
    <template v-for="(seg, index) in parsed.segments" :key="index">
      <span v-if="index > 0" class="vt-dot">.</span>
      <div class="vt-segment">
        <NButton quaternary size="tiny" class="vt-btn" title="该段减一" @click="bumpSegment(index, -1)">-</NButton>
        <NInput
          :value="seg"
          size="small"
          class="vt-input"
          @update:value="v => onSegmentInput(index, v)"
          @blur="onSegmentBlur(index)"
        />
        <NButton quaternary size="tiny" class="vt-btn" title="该段加一" @click="bumpSegment(index, 1)">+</NButton>
      </div>
    </template>
    <span v-if="parsed.suffix" class="vt-suffix">{{ parsed.suffix }}</span>
  </div>
  <NInput
    v-else
    :value="modelValue"
    placeholder="请输入版本号，例如：v1.0.0"
    clearable
    @update:value="onFallbackInput"
  />
</template>

<style scoped lang="scss">
.version-tag-input {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  width: 100%;
}

.vt-prefix,
.vt-suffix {
  color: #6b7280;
  font-weight: 600;
}

.vt-dot {
  color: #9ca3af;
  margin: 0 2px;
}

.vt-segment {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.vt-btn {
  min-width: 22px;
  padding: 0 4px;
}

.vt-input {
  width: 64px;
}
</style>
