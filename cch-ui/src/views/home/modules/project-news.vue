<script setup lang="ts">
import { computed } from 'vue';
import { $t } from '@/locales';

defineOptions({
  name: 'ProjectNews'
});

const props = defineProps<{
  projects: Api.Cch.Project[];
}>();

const projectTypeLabelMap: Record<string, string> = {
  normal: '普通项目',
  contest: '竞赛项目'
};

const newses = computed(() =>
  props.projects.map(project => ({
    id: project.id,
    content: project.name,
    time: `${project.createTime || ''} · ${projectTypeLabelMap[project.projectType] || project.projectType}`
  }))
);
</script>

<template>
  <NCard :title="$t('page.home.projectNews.title')" :bordered="false" size="small" segmented class="card-wrapper">
    <NList>
      <NListItem v-for="item in newses" :key="item.id">
        <template #prefix>
          <SoybeanAvatar class="size-48px!" />
        </template>
        <NThing :title="item.content" :description="item.time" />
      </NListItem>
    </NList>
  </NCard>
</template>

<style scoped></style>
