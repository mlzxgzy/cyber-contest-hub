<script setup lang="ts">
import {ref, watch} from 'vue';

type PortRow = {
  name: string;
  protocol: string;
  internalPort: number | null;
  remark: string;
};

interface Props {
  modelValue?: Record<string, Api.Cch.ChallengeDraftContainerTargetPort>;
}

interface Emits {
  (e: 'update:modelValue', value: Record<string, Api.Cch.ChallengeDraftContainerTargetPort>): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const rows = ref<PortRow[]>([]);
let syncingFromParent = false;

function portsSig(m?: Record<string, Api.Cch.ChallengeDraftContainerTargetPort>) {
  const src = m ?? {};
  const keys = Object.keys(src).sort();
  return JSON.stringify(
    keys.map(k => {
      const cfg = src[k];
      return [
        k,
        {
          protocol: (cfg?.protocol ?? 'tcp') as string,
          internalPort: (cfg?.internalPort ?? null) as number | null,
          remark: (cfg?.remark ?? '') as string
        }
      ];
    })
  );
}

function fromMap(m?: Record<string, Api.Cch.ChallengeDraftContainerTargetPort>) {
  const src = m ?? {};
  rows.value = Object.entries(src).map(([name, cfg]) => ({
    name,
    protocol: (cfg?.protocol ?? 'tcp') as string,
    internalPort: (cfg?.internalPort ?? null) as number | null,
    remark: (cfg?.remark ?? '') as string
  }));
}

function toMap(r: PortRow[]) {
  const out: Record<string, Api.Cch.ChallengeDraftContainerTargetPort> = {};
  for (const x of r) {
    const name = (x.name || '').trim();
    if (!name) continue;
    out[name] = {
      protocol: (x.protocol || '').trim() || 'tcp',
      internalPort: x.internalPort ?? null,
      remark: x.remark ?? ''
    };
  }
  return out;
}

watch(
  () => props.modelValue,
  v => {
    const incomingSig = portsSig(v);
    const currentSig = portsSig(toMap(rows.value));
    // Do NOT overwrite local rows when parent pushes equivalent map;
    // keep blank "new row" being edited.
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
    // Only emit when map meaningfully changed; blank-name rows shouldn't cause a parent update.
    if (portsSig(next) === portsSig(props.modelValue)) return;
    emit('update:modelValue', next);
  },
  {deep: true, flush: 'sync'}
);

function addRow() {
  rows.value.push({
    name: '',
    protocol: 'tcp',
    internalPort: null,
    remark: ''
  });
}

function removeRow(i: number) {
  rows.value.splice(i, 1);
}

const protocolOptions = [
  {label: 'http', value: 'http'},
  {label: 'tcp', value: 'tcp'},
  {label: 'udp', value: 'udp'}
];
</script>

<template>
  <NSpace vertical class="w-full">
    <template v-if="rows.length">
      <NGrid v-for="(r, idx) in rows" :key="idx" cols="1 700:4" :x-gap="12" :y-gap="8">
        <NGi>
          <NInput v-model:value="r.name" placeholder="端口名称（key）"/>
        </NGi>
        <NGi>
          <NSelect v-model:value="r.protocol" :options="protocolOptions"/>
        </NGi>
        <NGi>
          <NInputNumber
            v-model:value="r.internalPort"
            :min="1"
            :precision="0"
            placeholder="内部端口"
            class="w-full"
          />
        </NGi>
        <NGi class="flex items-center gap-8px">
          <NInput v-model:value="r.remark" placeholder="备注"/>
          <NButton type="error" secondary @click="removeRow(idx)">删除</NButton>
        </NGi>
      </NGrid>
    </template>
    <NEmpty v-else description="暂无端口映射"/>

    <NButton type="primary" secondary @click="addRow">新增端口映射</NButton>
  </NSpace>
</template>

