<script setup lang="ts">
import { computed } from 'vue';
import { createReusableTemplate } from '@vueuse/core';
import { useThemeStore } from '@/store/modules/theme';
import { $t } from '@/locales';

defineOptions({
  name: 'CardData'
});

const props = defineProps<{
  overview: Api.Cch.DashboardOverview;
}>();

interface CardData {
  key: string;
  title: string;
  value: number;
  unit: string;
  color: {
    start: string;
    end: string;
  };
  icon: string;
}

const cardData = computed<CardData[]>(() => [
  {
    key: 'projectCount',
    title: $t('page.home.projectCount'),
    value: props.overview.projectCount,
    unit: '',
    color: {
      start: '#ec4786',
      end: '#b955a4'
    },
    icon: 'ant-design:project-outlined'
  },
  {
    key: 'challengeCount',
    title: $t('page.home.challengeCount'),
    value: props.overview.challengeCount,
    unit: '',
    color: {
      start: '#865ec0',
      end: '#5144b4'
    },
    icon: 'ant-design:code-outlined'
  },
  {
    key: 'versionCount',
    title: $t('page.home.versionCount'),
    value: props.overview.versionCount,
    unit: '',
    color: {
      start: '#56cdf3',
      end: '#719de3'
    },
    icon: 'ant-design:branches-outlined'
  },
  {
    key: 'draftCount',
    title: $t('page.home.draftCount'),
    value: props.overview.draftCount,
    unit: '',
    color: {
      start: '#fcbc25',
      end: '#f68057'
    },
    icon: 'ant-design:file-text-outlined'
  },
  {
    key: 'fileCount',
    title: $t('page.home.fileCount'),
    value: props.overview.fileCount,
    unit: '',
    color: {
      start: '#5da8ff',
      end: '#8e9dff'
    },
    icon: 'ant-design:paper-clip-outlined'
  },
  {
    key: 'imageCount',
    title: $t('page.home.imageCount'),
    value: props.overview.imageCount,
    unit: '',
    color: {
      start: '#26deca',
      end: '#1ba99b'
    },
    icon: 'ant-design:container-outlined'
  },
  {
    key: 'mockTestCount',
    title: $t('page.home.mockTestCount'),
    value: props.overview.mockTestCount,
    unit: '',
    color: {
      start: '#fedc69',
      end: '#f2a93b'
    },
    icon: 'ant-design:bug-outlined'
  },
  {
    key: 'exportTaskCount',
    title: $t('page.home.exportTaskCount'),
    value: props.overview.exportTaskCount,
    unit: '',
    color: {
      start: '#8e9dff',
      end: '#5b6cd9'
    },
    icon: 'ant-design:export-outlined'
  }
]);

interface GradientBgProps {
  gradientColor: string;
}

const [DefineGradientBg, GradientBg] = createReusableTemplate<GradientBgProps>();

const themeStore = useThemeStore();

function getGradientColor(color: CardData['color']) {
  return `linear-gradient(to bottom right, ${color.start}, ${color.end})`;
}
</script>

<template>
  <NCard :bordered="false" size="small" class="card-wrapper">
    <!-- define component start: GradientBg -->
    <DefineGradientBg v-slot="{ $slots, gradientColor }">
      <div
        class="px-16px pb-4px pt-8px text-white"
        :style="{ backgroundImage: gradientColor, borderRadius: themeStore.themeRadius + 'px' }"
      >
        <component :is="$slots.default" />
      </div>
    </DefineGradientBg>
    <!-- define component end: GradientBg -->

    <NGrid cols="s:1 m:2 l:4" responsive="screen" :x-gap="16" :y-gap="16">
      <NGi v-for="item in cardData" :key="item.key">
        <GradientBg :gradient-color="getGradientColor(item.color)" class="flex-1">
          <h3 class="text-16px">{{ item.title }}</h3>
          <div class="flex justify-between pt-12px">
            <SvgIcon :icon="item.icon" class="text-32px" />
            <CountTo
              :prefix="item.unit"
              :start-value="1"
              :end-value="item.value"
              class="text-30px text-white dark:text-dark"
            />
          </div>
        </GradientBg>
      </NGi>
    </NGrid>
  </NCard>
</template>

<style scoped></style>
