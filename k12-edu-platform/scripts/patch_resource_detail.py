from pathlib import Path

p = Path(__file__).resolve().parents[1] / 'src/views/home/ResourceDetail.vue'
text = p.read_text(encoding='utf-8')

start = text.index('      <!-- 2. 资源头部信息区 -->')
end = text.index('      <!-- 加载状态 -->', start)
new_header = """      <ResourceDetailMeta
        v-if="resource"
        :resource="resource"
        :grade-level-map="gradeLevelMap"
        :format-resource-type="formatResourceType"
        :format-media-type="formatMediaType"
      >
        <template #actions>
          <ResourceDetailActions
            :is-collected="isCollected"
            :downloading="downloading"
            @download="handleDownload"
            @collect="handleCollect"
            @share="handleShare"
            @add-to-folder="handleAddToFolder"
          />
        </template>
      </ResourceDetailMeta>

"""
text = text[:start] + new_header + text[end:]

s0 = text.index('<script setup lang="ts">')
s1 = text.index('</script>', s0)
new_script = """<script setup lang="ts">
import ResourceDetailMeta from '@/components/resource-detail/ResourceDetailMeta.vue'
import ResourceDetailActions from '@/components/resource-detail/ResourceDetailActions.vue'
import { useResourceDetail } from '@/composables/useResourceDetail'

const {
  resource, loading, isCollected, downloading, showShareDialog, shareUrl,
  showLessonPlan, isFullPreview, previewType, currentSlide, slideCount,
  audioPlayer, isPlaying, audioProgress, currentTime, audioDuration,
  ratingCount, userRating, commentText, dimensionScores, comments,
  attachFiles, relatedResources, recommendResources, resourceHighlights,
  currentChannel, gradeLevelMap,
  formatResourceType, formatMediaType, getSlideStyle, getRecommendColor,
  handleDownload, handleCollect, handleShare, handleAddToFolder,
  copyShareUrl, shareToWechat, shareToQQ, shareToWorkWechat,
  prevSlide, nextSlide, toggleFullPreview, toggleAudio, onAudioLoaded,
  onAudioTimeUpdate, seekAudio, downloadFile, goToResource,
  submitComment, likeComment, replyComment,
} = useResourceDetail()
</script>"""
text = text[:s0] + new_script + text[s1 + len('</script>'):]
p.write_text(text, encoding='utf-8')
print('patched resource detail')
