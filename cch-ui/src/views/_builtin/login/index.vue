<script setup lang="ts">
import { computed } from 'vue';
import type { Component } from 'vue';
import { loginModuleRecord } from '@/constants/app';
import { useAppStore } from '@/store/modules/app';
import { useThemeStore } from '@/store/modules/theme';
import { $t } from '@/locales';
import PwdLogin from './modules/pwd-login.vue';
import CodeLogin from './modules/code-login.vue';
import Register from './modules/register.vue';
import ResetPwd from './modules/reset-pwd.vue';
import BindWechat from './modules/bind-wechat.vue';

interface Props {
  /** The login module */
  module?: UnionKey.LoginModule;
}

const props = defineProps<Props>();

const appStore = useAppStore();
const themeStore = useThemeStore();

interface LoginModule {
  label: App.I18n.I18nKey;
  component: Component;
}

const moduleMap: Record<UnionKey.LoginModule, LoginModule> = {
  'pwd-login': { label: loginModuleRecord['pwd-login'], component: PwdLogin },
  'code-login': { label: loginModuleRecord['code-login'], component: CodeLogin },
  register: { label: loginModuleRecord.register, component: Register },
  'reset-pwd': { label: loginModuleRecord['reset-pwd'], component: ResetPwd },
  'bind-wechat': { label: loginModuleRecord['bind-wechat'], component: BindWechat }
};

const activeModule = computed(() => moduleMap[props.module || 'pwd-login']);
</script>

<template>
  <div class="scroll box-border size-full flex">
    <div class="cyber-panel relative box-border hidden h-full w-65vw overflow-hidden xl:block">
      <div class="cyber-panel__grid"></div>
      <div class="cyber-panel__scanlines"></div>
      <div class="cyber-panel__glow"></div>

      <div class="relative z-10 flex items-center pl-30px pt-30px">
        <span class="cyber-mark">>_</span>
        <h3 class="ml-12px text-20px text-#E6F7FF font-500 tracking-[0.06em]">{{ $t('system.title') }}</h3>
      </div>

      <div class="cyber-terminal">
        <div class="cyber-terminal__bar">
          <span class="cyber-terminal__dot is-red"></span>
          <span class="cyber-terminal__dot is-amber"></span>
          <span class="cyber-terminal__dot is-green"></span>
          <span class="cyber-terminal__title">cyber-contest-hub — login</span>
        </div>
        <div class="cyber-terminal__body">
          <p class="cyber-terminal__line">
            <span class="prompt">$</span>
            cyber-contest-hub login
          </p>
          <p class="cyber-terminal__line is-dim">authenticating ...</p>
          <p class="cyber-terminal__line">
            <span class="ok">✓</span>
            <span class="field">challenges</span>
            loaded
          </p>
          <p class="cyber-terminal__line">
            <span class="ok">✓</span>
            <span class="field">images</span>
            ready
          </p>
          <p class="cyber-terminal__line">
            <span class="ok">✓</span>
            <span class="field">flags</span>
            encrypted
          </p>
          <p class="cyber-terminal__line">
            <span class="ok">✓</span>
            <span class="field">contests</span>
            synced
          </p>
          <p class="cyber-terminal__line">
            <span class="prompt">&gt;</span>
            access granted
            <span class="cursor"></span>
          </p>
        </div>
      </div>

      <div class="absolute bottom-80px w-full px-40px">
        <h1 class="text-34px text-white font-600">{{ $t('page.login.common.title') }}</h1>
        <p class="mt-10px text-15px text-#7C8DA6">{{ $t('page.login.common.subTitle') }}</p>
      </div>
    </div>
    <div class="relative h-full flex-1 xl:m-auto sm:!w-full">
      <header class="flex-y-center justify-between px-30px pt-30px xl:justify-end">
        <div class="relative z-100 flex items-center xl:hidden">
          <span class="cyber-mark cyber-mark--sm">>_</span>
          <h3 class="ml-10px text-20px font-400">{{ $t('system.title') }}</h3>
        </div>
        <div class="flex items-center justify-end">
          <ThemeSchemaSwitch
            :theme-schema="themeStore.themeScheme"
            :show-tooltip="false"
            class="text-20px lt-sm:text-18px"
            @switch="themeStore.toggleThemeScheme"
          />
          <LangSwitch
            v-if="themeStore.header.multilingual.visible"
            :lang="appStore.locale"
            :lang-options="appStore.localeOptions"
            :show-tooltip="false"
            class="text-20px lt-sm:text-18px"
            @change-lang="appStore.changeLocale"
          />
        </div>
      </header>
      <main
        class="m-auto mt-10% h-630px max-w-450px w-full rounded-5px bg-cover px-24px xl:absolute xl:inset-0 lg:mt-15% xl:mt-auto"
      >
        <Transition :name="themeStore.page.animateMode" mode="out-in" appear>
          <component :is="activeModule.component" />
        </Transition>
      </main>
    </div>
  </div>
</template>

<style scoped>
.scroll {
  overflow: auto;
}

.scroll::-webkit-scrollbar {
  display: none;
}

.scroll {
  -ms-overflow-style: none;
}

.scroll {
  scrollbar-width: none;
}

/* 深色终端指挥台：左侧品牌面板 */
.cyber-panel {
  background: radial-gradient(1200px 800px at 72% 18%, #0e1f3d 0%, #0a1428 46%, #060c18 100%);
  color: #e6f7ff;
}

.cyber-panel__grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(34, 211, 238, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(34, 211, 238, 0.06) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: radial-gradient(ellipse at 50% 40%, #000 28%, transparent 76%);
}

.cyber-panel__scanlines {
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    to bottom,
    transparent 0,
    transparent 3px,
    rgba(0, 0, 0, 0.14) 3px,
    rgba(0, 0, 0, 0.14) 4px
  );
  opacity: 0.5;
  pointer-events: none;
}

.cyber-panel__glow {
  position: absolute;
  top: -12%;
  left: -10%;
  width: 58%;
  height: 58%;
  background: radial-gradient(circle, rgba(34, 211, 238, 0.16), transparent 70%);
  filter: blur(24px);
}

.cyber-mark {
  color: #22d3ee;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  text-shadow: 0 0 14px rgba(34, 211, 238, 0.55);
}

.cyber-mark--sm {
  font-size: 22px;
}

.cyber-terminal {
  position: absolute;
  top: 50%;
  left: 50%;
  width: min(560px, 76%);
  overflow: hidden;
  border: 1px solid rgba(34, 211, 238, 0.25);
  border-radius: 10px;
  background: rgba(6, 14, 26, 0.74);
  box-shadow:
    0 24px 60px rgba(0, 0, 0, 0.45),
    inset 0 0 0 1px rgba(34, 211, 238, 0.06);
  backdrop-filter: blur(6px);
  transform: translate(-50%, -58%);
}

.cyber-terminal__bar {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.04);
  border-bottom: 1px solid rgba(34, 211, 238, 0.12);
}

.cyber-terminal__dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.cyber-terminal__dot.is-red {
  background: #ff5f57;
}

.cyber-terminal__dot.is-amber {
  background: #febc2e;
}

.cyber-terminal__dot.is-green {
  background: #28c840;
}

.cyber-terminal__title {
  margin-left: 8px;
  color: #7c8da6;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
}

.cyber-terminal__body {
  padding: 20px 22px;
  color: #c6e6f5;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 14px;
  line-height: 2;
}

.cyber-terminal__line {
  margin: 0;
  white-space: nowrap;
}

.cyber-terminal__line.is-dim {
  color: #5f7692;
}

.prompt {
  color: #22d3ee;
  font-weight: 700;
}

.ok {
  color: #2dd4bf;
}

.field {
  display: inline-block;
  width: 112px;
}

.cursor {
  display: inline-block;
  width: 8px;
  height: 16px;
  margin-left: 4px;
  background: #22d3ee;
  vertical-align: -2px;
  animation: cyber-blink 1s step-end infinite;
}

@keyframes cyber-blink {
  50% {
    opacity: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .cursor {
    animation: none;
  }
}
</style>
