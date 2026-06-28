/**
 * 版本/册别 Composable
 * 管理版本选择、册别选择、相关状态
 * 配置数据统一从 config 导入
 */
import { ref, computed, type Ref } from 'vue'
import { useRoute } from 'vue-router'
import { subjectVersionsMap, type VersionItem, type SubjectItem } from '../config/subjectConfig'
import { volumeDataMap, type VolumeItem } from '../config/volumeData'

export function useVersionVolume(currentStage: Ref<string>, currentSubject: Ref<SubjectItem | null>) {
  const route = useRoute()

  const currentSubjectVersions = computed(() => {
    if (!currentSubject.value) return []
    return subjectVersionsMap[currentSubject.value.key] || subjectVersionsMap.default
  })

  const selectedVersionKey = ref('renjiao')
  const selectedVersionId = ref(1)
  const isNewTextbook = ref(true)
  const currentPublisher = ref('人民教育出版社')

  const selectedVersionName = computed(() => {
    const editionFromQuery = route.query.edition
    if (typeof editionFromQuery === 'string' && editionFromQuery) {
      return editionFromQuery
    }
    const ver = currentSubjectVersions.value.find((v: VersionItem) => v.key === selectedVersionKey.value)
    return ver?.name || '人教版'
  })

  const volumeList = computed(() => volumeDataMap[currentStage.value] || [])
  const selectedVolumeId = ref('')

  const currentGradeLevelName = computed(() => {
    const vol = volumeList.value.find((v: VolumeItem) => v.id === selectedVolumeId.value)
    return vol?.name || ''
  })

  function setDefaultVolumeByStage() {
    const list = volumeList.value
    if (list.length > 0) {
      selectedVolumeId.value = list[0].id
    }
  }

  function handleConfirmVersion(versionKey: string, isNew: boolean, volumeId: string) {
    selectedVersionKey.value = versionKey
    isNewTextbook.value = isNew
    selectedVolumeId.value = volumeId
  }

  return {
    currentSubjectVersions,
    selectedVersionKey,
    selectedVersionId,
    selectedVersionName,
    isNewTextbook,
    currentPublisher,
    volumeList,
    selectedVolumeId,
    currentGradeLevelName,
    setDefaultVolumeByStage,
    handleConfirmVersion,
  }
}
