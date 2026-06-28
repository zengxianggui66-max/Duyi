from pathlib import Path

src = Path(__file__).resolve().parents[1] / 'src/views/home/ResourceDetail.vue'
lines = src.read_text(encoding='utf-8').splitlines()
body = '\n'.join(lines[523:986])  # script content without import lines handled in header

header = """import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export function useResourceDetail() {
  const route = useRoute()
  const router = useRouter()
"""

# strip leading imports and route/router from body
filtered = []
skip = True
for line in body.splitlines():
    if skip:
        if line.startswith('import '):
            continue
        if 'const route = useRoute()' in line or 'const router = useRouter()' in line:
            continue
        if line.strip() == '':
            continue
        skip = False
    filtered.append('  ' + line if line and not line.startswith('  ') else ('  ' + line.lstrip() if line else line))

body_indented = '\n'.join(filtered)

footer = """
  return {
    resource, loading, isCollected, downloading, showShareDialog, shareUrl,
    showLessonPlan, isFullPreview, previewType, currentSlide, slideCount,
    audioPlayer, isPlaying, audioProgress, currentTime, audioDuration,
    ratingCount, userRating, commentText, dimensionScores, comments,
    attachFiles, relatedResources, recommendResources, resourceHighlights,
    currentChannel, gradeLevelMap, pdfUrl,
    formatResourceType, formatMediaType, getSlideStyle, getRecommendColor,
    handleDownload, handleCollect, handleShare, handleAddToFolder,
    copyShareUrl, shareToWechat, shareToQQ, shareToWorkWechat,
    prevSlide, nextSlide, toggleFullPreview, toggleAudio, onAudioLoaded,
    onAudioTimeUpdate, seekAudio, formatTime, downloadFile, goToResource,
    submitComment, likeComment, replyComment,
  }
}
"""

out = Path(__file__).resolve().parents[1] / 'src/composables/useResourceDetail.ts'
out.write_text(header + body_indented + footer, encoding='utf-8')
print('lines', len(out.read_text(encoding='utf-8').splitlines()))
