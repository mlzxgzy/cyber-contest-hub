<script setup lang="ts">
import {computed} from 'vue';
import {NCard, NForm, NFormItem, NGrid, NGi, NInput, NInputNumber, NSelect, NSwitch, NTabs, NTabPane} from 'naive-ui';

/**
 * 注意：当前 Api.Cch.ChallengeDraftConfig typings 中尚未定义 VM 配置结构。
 * 为了不阻塞前端交互，这里把 VM 配置暂存到 config.vmConfig（对象）中。
 * 后端字段确定后再做强类型与字段映射。
 */
interface Props {
  /** 直接传 draftData.config 进来即可 */
  modelValue: any;
}

interface Emits {
  (e: 'update:modelValue', value: any): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const cfg = computed<any>({
  get() {
    return props.modelValue ?? {};
  },
  set(v) {
    emit('update:modelValue', v);
  }
});

const vmConfig = computed<any>({
  get() {
    cfg.value.vmConfig ??= {};
    const c = cfg.value.vmConfig;
    c.basic ??= {};
    c.access ??= {};
    c.network ??= {};
    c.resources ??= {};
    c.storage ??= {};
    c.lifecycle ??= {};
    c.advanced ??= {};
    return c;
  },
  set(v) {
    cfg.value.vmConfig = v;
  }
});

const accessTypeOptions = [
  {label: 'SSH', value: 'ssh'},
  {label: 'RDP', value: 'rdp'},
  {label: 'WEB', value: 'web'}
];
</script>

<template>
  <div class="vm-config">
    <NCard size="small" :content-style="{ padding: '12px' }">
      <NTabs type="line" animated>
        <!-- 1) 基础 -->
        <NTabPane name="basic" tab="基础">
          <NForm size="small" label-placement="left" label-width="110">
            <NGrid cols="1 900:2" x-gap="8" y-gap="8">
              <NGi>
                <NFormItem label="靶机名称">
                  <NInput v-model:value="vmConfig.basic.name" placeholder="例如：vm-1 / web / pwn"/>
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="系统类型">
                  <NSelect
                    v-model:value="vmConfig.basic.osType"
                    :options="[
                      { label: 'Linux', value: 'linux' },
                      { label: 'Windows', value: 'windows' },
                      { label: 'Other', value: 'other' }
                    ]"
                    clearable
                    placeholder="可选"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="镜像/模板">
                  <NInput v-model:value="vmConfig.basic.template" placeholder="镜像ID / 模板名 / 快照标识（可选）"/>
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="实例数量">
                  <NInputNumber
                    v-model:value="vmConfig.basic.replicas"
                    :min="1"
                    :precision="0"
                    placeholder="默认 1"
                    class="w-full"
                  />
                </NFormItem>
              </NGi>
            </NGrid>
          </NForm>
        </NTabPane>

        <!-- 2) 访问 -->
        <NTabPane name="access" tab="访问">
          <NForm size="small" label-placement="left" label-width="110">
            <NGrid cols="1 900:2" x-gap="8" y-gap="8">
              <NGi>
                <NFormItem label="访问方式">
                  <NSelect
                    v-model:value="vmConfig.access.type"
                    :options="accessTypeOptions"
                    clearable
                    placeholder="例如：SSH"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="对外暴露">
                  <NSwitch v-model:value="vmConfig.access.expose"/>
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="用户名">
                  <NInput v-model:value="vmConfig.access.username" placeholder="例如：root / administrator"/>
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="密码/凭据">
                  <NInput v-model:value="vmConfig.access.credential" type="password" show-password-on="click" placeholder="可选"/>
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="SSH端口">
                  <NInputNumber v-model:value="vmConfig.access.sshPort" :min="1" :max="65535" :precision="0" class="w-full"/>
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="RDP端口">
                  <NInputNumber v-model:value="vmConfig.access.rdpPort" :min="1" :max="65535" :precision="0" class="w-full"/>
                </NFormItem>
              </NGi>
            </NGrid>
          </NForm>
        </NTabPane>

        <!-- 3) 网络 -->
        <NTabPane name="network" tab="网络">
          <NForm size="small" label-placement="left" label-width="110">
            <NGrid cols="1 900:2" x-gap="8" y-gap="8">
              <NGi>
                <NFormItem label="网络/交换机">
                  <NInput v-model:value="vmConfig.network.networkId" placeholder="网络ID/名称（可选）"/>
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="分配方式">
                  <NSelect
                    v-model:value="vmConfig.network.ipMode"
                    :options="[
                      { label: 'DHCP', value: 'dhcp' },
                      { label: '静态IP', value: 'static' }
                    ]"
                    clearable
                    placeholder="可选"
                  />
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="静态IP">
                  <NInput v-model:value="vmConfig.network.staticIp" placeholder="例如：10.0.0.10（ipMode=静态IP时）"/>
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="DNS">
                  <NInput v-model:value="vmConfig.network.dns" placeholder="例如：8.8.8.8,1.1.1.1（可选）"/>
                </NFormItem>
              </NGi>
            </NGrid>
          </NForm>
        </NTabPane>

        <!-- 4) 资源 -->
        <NTabPane name="resources" tab="资源">
          <NForm size="small" label-placement="left" label-width="110">
            <NGrid cols="1 900:2" x-gap="8" y-gap="8">
              <NGi>
                <NFormItem label="CPU(核)">
                  <NInputNumber v-model:value="vmConfig.resources.cpu" :min="1" :precision="0" class="w-full"/>
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="内存(MB)">
                  <NInputNumber v-model:value="vmConfig.resources.memoryMb" :min="128" :precision="0" class="w-full"/>
                </NFormItem>
              </NGi>
            </NGrid>
          </NForm>
        </NTabPane>

        <!-- 5) 存储 -->
        <NTabPane name="storage" tab="存储">
          <NForm size="small" label-placement="left" label-width="110">
            <NGrid cols="1 900:2" x-gap="8" y-gap="8">
              <NGi>
                <NFormItem label="系统盘(GB)">
                  <NInputNumber v-model:value="vmConfig.storage.systemDiskGb" :min="1" :precision="0" class="w-full"/>
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="数据盘(GB)">
                  <NInputNumber v-model:value="vmConfig.storage.dataDiskGb" :min="0" :precision="0" class="w-full"/>
                </NFormItem>
              </NGi>
            </NGrid>
          </NForm>
        </NTabPane>

        <!-- 6) 启动与健康 -->
        <NTabPane name="lifecycle" tab="启动与健康">
          <NForm size="small" label-placement="left" label-width="110">
            <NGrid cols="1 900:2" x-gap="8" y-gap="8">
              <NGi>
                <NFormItem label="启动脚本">
                  <NInput v-model:value="vmConfig.lifecycle.startupScript" type="textarea" :rows="3" placeholder="可选"/>
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="健康检查">
                  <NInput v-model:value="vmConfig.lifecycle.healthcheck" type="textarea" :rows="3" placeholder="例如：curl http://127.0.0.1:8080（可选）"/>
                </NFormItem>
              </NGi>
            </NGrid>
          </NForm>
        </NTabPane>

        <!-- 7) 高级 -->
        <NTabPane name="advanced" tab="高级">
          <NForm size="small" label-placement="left" label-width="110">
            <NGrid cols="1 900:2" x-gap="8" y-gap="8">
              <NGi>
                <NFormItem label="备注">
                  <NInput v-model:value="vmConfig.advanced.remark" placeholder="仅后台可见（可选）"/>
                </NFormItem>
              </NGi>
              <NGi>
                <NFormItem label="标签">
                  <NInput v-model:value="vmConfig.advanced.tags" placeholder="逗号分隔（可选）"/>
                </NFormItem>
              </NGi>
            </NGrid>
          </NForm>
        </NTabPane>
      </NTabs>
    </NCard>
  </div>
</template>

<style scoped lang="scss">
.vm-config {
  :deep(.n-form-item) {
    margin-bottom: 8px;
  }
}
</style>

