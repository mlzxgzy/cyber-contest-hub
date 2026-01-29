<script setup lang="ts">
import {ref, watch} from 'vue';

type EnvRow = { key: string; value: string };

interface Props {
  modelValue?: Record<string, string>;
}

interface Emits {
  (e: 'update:modelValue', value: Record<string, string>): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const rows = ref<EnvRow[]>([]);
let syncingFromParent = false;

function envSig(m?: Record<string, string>) {
  const src = m ?? {};
  const keys = Object.keys(src).sort();
  return JSON.stringify(keys.map(k => [k, src[k] ?? '']));
}

function fromMap(m?: Record<string, string>) {
  const src = m ?? {};
  rows.value = Object.entries(src).map(([k, v]) => ({key: k, value: v}));
}

function toMap(r: EnvRow[]) {
  const out: Record<string, string> = {};
  for (const x of r) {
    const k = (x.key || '').trim();
    if (!k) continue;
    out[k] = x.value ?? '';
  }
  return out;
}

watch(
  () => props.modelValue,
  v => {
    const incomingSig = envSig(v);
    const currentSig = envSig(toMap(rows.value));
    // If the parent pushes an equivalent map (possibly new reference),
    // do NOT overwrite local rows (e.g. keep blank "new row" being edited).
    if (incomingSig === currentSig) return;
    syncingFromParent = true;
    fromMap(v);
    syncingFromParent = false;
  },
  {immediate: true, deep: true}
);

watch(
  rows,
  v => {
    if (syncingFromParent) return;
    const next = toMap(v);
    // Only emit when the map meaningfully changed; this avoids wiping newly-added
    // blank rows (which are not representable in Map form until key is filled).
    if (envSig(next) === envSig(props.modelValue)) return;
    emit('update:modelValue', next);
  },
  {deep: true, flush: 'sync'}
);

function addRow() {
  rows.value.push({key: '', value: ''});
}

function removeRow(i: number) {
  rows.value.splice(i, 1);
}
</script>

<template>
  <NSpace vertical class="w-full">
    <template v-if="rows.length">
      <NGrid v-for="(r, idx) in rows" :key="idx" cols="1 800:3" :x-gap="12" :y-gap="8">
        <NGi>
          <NInput v-model:value="r.key" placeholder="变量名"/>
        </NGi>
        <NGi>
          <NInput v-model:value="r.value" placeholder="变量值"/>
        </NGi>
        <NGi class="flex justify-end">
          <NButton type="error" secondary @click="removeRow(idx)">删除</NButton>
        </NGi>
      </NGrid>
    </template>
    <NEmpty v-else description="暂无环境变量"/>

    <NButton type="primary" secondary @click="addRow">新增环境变量</NButton>
  </NSpace>
</template>

