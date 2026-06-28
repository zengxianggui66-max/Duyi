from pathlib import Path

p = Path(__file__).resolve().parents[1] / 'src/views/home/ResourceDetail.vue'
text = p.read_text(encoding='utf-8')

start = text.index('          <!-- 3.1 预览区 -->')
end = text.index('          <!-- 3.2 资源详情区 -->', start)

replacement = """          <ResourcePreviewPanel
            ref="previewPanelRef"
            :resource="resource"
            :preview-type="previewType"
            :is-full-preview="isFullPreview"
            :current-slide="currentSlide"
            :slide-count="slideCount"
            :is-playing="isPlaying"
            :audio-progress="audioProgress"
            :current-time="currentTime"
            :audio-duration="audioDuration"
            :pdf-url="pdfUrl"
            @toggle-fullscreen="toggleFullPreview"
            @prev-slide="prevSlide"
            @next-slide="nextSlide"
            @goto-slide="(i) => (currentSlide = i)"
            @toggle-audio="toggleAudio"
            @audio-loaded="onAudioLoaded"
            @audio-timeupdate="onAudioTimeUpdate"
            @seek-audio="seekAudio"
          />

          <ResourceLessonPlanCard
            v-if="previewType === 'ppt'"
            :expanded="showLessonPlan"
            @toggle="showLessonPlan = !showLessonPlan"
          />

"""

text = text[:start] + replacement + text[end:]

s0 = text.index('<script setup lang="ts">')
s1 = text.index('</script>', s0)
new_script = """<script setup lang="ts">
import { ref, watchEffect } from 'vue'
import ResourceDetailMeta from '@/components/resource-detail/ResourceDetailMeta.vue'
import ResourceDetailActions from '@/components/resource-detail/ResourceDetailActions.vue'
import ResourcePreviewPanel from '@/components/resource-detail/ResourcePreviewPanel.vue'
import ResourceLessonPlanCard from '@/components/resource-detail/ResourceLessonPlanCard.vue'
import { useResourceDetail } from '@/composables/useResourceDetail'

const previewPanelRef = ref<InstanceType<typeof ResourcePreviewPanel> | null>(null)

const {
  resource, loading, isCollected, downloading, showShareDialog, shareUrl,
  showLessonPlan, isFullPreview, previewType, currentSlide, slideCount,
  audioPlayer, isPlaying, audioProgress, currentTime, audioDuration,
  ratingCount, userRating, commentText, dimensionScores, comments,
  attachFiles, relatedResources, recommendResources, resourceHighlights,
  currentChannel, gradeLevelMap, pdfUrl,
  formatResourceType, formatMediaType, getRecommendColor,
  handleDownload, handleCollect, handleShare, handleAddToFolder,
  copyShareUrl, shareToWechat, shareToQQ, shareToWorkWechat,
  prevSlide, nextSlide, toggleFullPreview, toggleAudio, onAudioLoaded,
  onAudioTimeUpdate, seekAudio, downloadFile, goToResource,
  submitComment, likeComment, replyComment,
} = useResourceDetail()

watchEffect(() => {
  const el = previewPanelRef.value?.audioPlayer
  if (el) audioPlayer.value = el
})
</script>"""

text = text[:s0] + new_script + text[s1 + len('</script>'):]

# Remove preview CSS block
m0 = '/* ==================== 预览区 ==================== */'
m1 = '/* ==================== 资源详情 ==================== */'
if m0 in text and m1 in text:
    i0 = text.index(m0)
    i1 = text.index(m1, i0)
    text = text[:i0] + text[i1:]

# Remove lesson plan CSS if still there
m2 = '/* ==================== 教案预览 ==================== */'
m3 = '/* ==================== 资源详情 ==================== */'
if m2 in text:
    i0 = text.index(m2)
    i1 = text.index(m3, i0) if m3 in text[i0:] else len(text)
    # only remove if duplicate m3
    if text[i0:i1].count(m3) == 0:
        pass
    else:
        text = text[:i0] + text[i1:]

p.write_text(text, encoding='utf-8')
print('ok', len(text.splitlines()), 'lines')
