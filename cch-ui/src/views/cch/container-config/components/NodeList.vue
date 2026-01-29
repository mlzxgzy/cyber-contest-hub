<script setup lang="tsx">
import {ref, computed, watch, h} from 'vue';
import {NTag, NInput, NButton} from 'naive-ui';
import {fetchGetNodeList, fetchUpdateNodeExternalAddress} from '@/service/api/cch/container-config';

defineOptions({
  name: 'NodeList'
});

// Props
const props = defineProps<{
  /** 是否已连接 */
  isConnected: boolean;
}>();

// 节点数据
const nodes = ref<Api.Cch.ClusterNode[]>([]);
const nodesLoading = ref(false);
// 正在编辑的节点外部访问地址
const editingNodeAddress = ref<Record<string, string>>({});

// 加载节点列表
async function loadNodeList() {
  if (!nodes.value.length) {
    // 只有在节点数据为空时才加载，避免重复加载
    await fetchData();
  }
}

async function fetchData() {
  nodesLoading.value = true;
  try {
    const {data, error} = await fetchGetNodeList();
    if (!error && data) {
      nodes.value = data.map((node: Api.Cch.ClusterNode) => {
        // 从labels中提取外部访问地址
        const externalAddress = node.labels?.['external.access.address'] ||
          node.labels?.['cch.external.access.address'] ||
          '';
        return {
          ...node,
          externalAccessAddress: externalAddress
        };
      });
      // 初始化编辑状态
      editingNodeAddress.value = {};
      nodes.value.forEach(node => {
        editingNodeAddress.value[node.id] = node.externalAccessAddress || '';
      });
    } else {
      nodes.value = [];
    }
  } catch (err) {
    console.error('加载节点列表失败:', err);
    nodes.value = [];
  } finally {
    nodesLoading.value = false;
  }
}

// 保存节点外部访问地址
async function saveNodeExternalAddress(node: Api.Cch.ClusterNode) {
  const address = editingNodeAddress.value[node.id]?.trim() || '';

  try {
    const {error} = await fetchUpdateNodeExternalAddress(node.id, address);
    if (error) {
      window.$message?.error('保存外部访问地址失败');
      return;
    }

    // 更新本地数据
    const nodeIndex = nodes.value.findIndex(n => n.id === node.id);
    if (nodeIndex !== -1) {
      nodes.value[nodeIndex] = {
        ...nodes.value[nodeIndex],
        labels: {
          ...(nodes.value[nodeIndex].labels || {}),
          'cch.external.access.address': address
        },
        externalAccessAddress: address
      };
    }

    window.$message?.success('保存成功');
  } catch (err) {
    console.error('保存节点外部访问地址失败:', err);
    window.$message?.error('保存失败');
  }
}

// 节点表格列定义
const nodeColumns = computed(() => [
  {
    title: '节点名称',
    key: 'name',
    width: 120
  },
  {
    title: '角色',
    key: 'role',
    width: 80,
    render: (row: Api.Cch.ClusterNode) => {
      const roleMap: Record<string, string> = {
        'manager': '管理节点',
        'worker': '工作节点',
        'master': '主节点',
        'node': '工作节点'
      };
      return roleMap[row.role || ''] || row.role || '-';
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: Api.Cch.ClusterNode) => {
      const status = row.status || '';
      const statusLower = status.toLowerCase();
      const type = statusLower.includes('ready') || statusLower.includes('active')
        ? 'success'
        : statusLower.includes('down') || statusLower.includes('error')
          ? 'error'
          : 'warning';
      return h(NTag, {type, size: 'small'}, {default: () => status || '-'});
    }
  },
  {
    title: '外部访问地址',
    key: 'externalAccessAddress',
    minWidth: 200,
    render: (row: Api.Cch.ClusterNode) => {
      return h('div', {class: 'flex items-center gap-8px'}, [
        h(NInput, {
          value: editingNodeAddress.value[row.id] || '',
          placeholder: '请输入外部访问地址',
          size: 'small',
          'onUpdate:value': (val: string) => {
            editingNodeAddress.value[row.id] = val;
          },
          onKeydown: (e: KeyboardEvent) => {
            if (e.key === 'Enter') {
              const currentValue = editingNodeAddress.value[row.id] || '';
              const originalValue = row.externalAccessAddress || '';
              if (currentValue !== originalValue) {
                saveNodeExternalAddress(row);
              }
            }
          }
        }),
        h(NButton, {
          size: 'small',
          type: 'primary',
          quaternary: true,
          onClick: () => saveNodeExternalAddress(row)
        }, {default: () => '保存'})
      ]);
    }
  }
]);

// 监听连接状态变化
watch(() => props.isConnected, (connected) => {
  if (connected) {
    fetchData();
  } else {
    nodes.value = [];
    editingNodeAddress.value = {};
  }
}, {immediate: true});
</script>

<template>
  <NCard title="集群节点" :bordered="false" size="small" class="card-wrapper flex-1">
    <NScrollbar style="max-height: 400px;">
      <div v-if="nodesLoading" class="py-20px text-center">
        <NSpin size="small"/>
      </div>
      <div v-else-if="nodes.length === 0" class="py-20px text-center text-gray-500">
        暂无节点信息
      </div>
      <NDataTable
        v-else
        :columns="nodeColumns"
        :data="nodes"
        :bordered="false"
        size="small"
      />
    </NScrollbar>
  </NCard>
</template>

<style scoped></style>
