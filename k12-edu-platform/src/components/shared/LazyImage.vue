<template>
  <img
    :src="actualSrc"
    :alt="alt"
    :class="className"
    :style="style"
    loading="lazy"
    @error="onError"
    @load="onLoad"
  />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = withDefaults(
  defineProps<{
    src: string
    alt?: string
    className?: string
    style?: Record<string, string>
    /** CDN 图片处理参数（如 '?x-oss-process=image/resize,w_200/format,webp'） */
    cdnProcess?: string
    /** 加载失败时显示的 fallback URL */
    fallbackSrc?: string
  }>(),
  {
    alt: '',
    className: '',
    cdnProcess: '',
    fallbackSrc: '',
  },
)

const hasError = ref(false)
const loaded = ref(false)

const actualSrc = computed(() => {
  if (hasError.value && props.fallbackSrc) return props.fallbackSrc
  if (!props.src) return ''
  if (props.cdnProcess && props.src.startsWith('http')) {
    return `${props.src}${props.cdnProcess}`
  }
  return props.src
})

function onError() {
  hasError.value = true
}

function onLoad() {
  loaded.value = true
}
</script>
