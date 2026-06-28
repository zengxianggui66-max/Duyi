/**
 * 学段/学科 Composable
 * Phase 5-F：学科列表优先从 taxonomy API 加载
 */
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { stages, stageNames, subjectDataMap, type StageKey, type Stage, type SubjectItem } from '../config/subjectConfig'
import { loadSubjects as loadSubjectsFromApi } from './taxonomySource'

export function useStageSubject() {
  const router = useRouter()

  const currentStage = ref<StageKey>('primary')
  const subjectList = ref<SubjectItem[]>([])
  const subjectsLoaded = ref(false)
  const currentSubjects = computed(() =>
    subjectList.value.length ? subjectList.value : subjectDataMap[currentStage.value] || [],
  )
  const currentStageName = computed(() => stageNames[currentStage.value] || '小学')
  const currentSubject = ref<SubjectItem | null>(null)

  async function refreshSubjects(stageKey?: StageKey) {
    const key = stageKey ?? currentStage.value
    try {
      subjectList.value = await loadSubjectsFromApi(key)
    } catch {
      subjectList.value = subjectDataMap[key] || []
    } finally {
      subjectsLoaded.value = true
    }
  }

  function switchStage(key: string) {
    currentStage.value = key as StageKey
    void refreshSubjects(currentStage.value)
    const subjects = currentSubjects.value
    if (subjects.length) {
      currentSubject.value = subjects[0]
    }
  }

  function selectSubject(subject: SubjectItem) {
    if (currentSubject.value?.key === subject.key) return
    currentSubject.value = subject
  }

  /** 路由守卫：无效学科 param 时重定向首页 */
  async function ensureSubjectRouteValid(
    route: RouteLocationNormalizedLoaded,
    opts?: { redirectName?: string },
  ): Promise<boolean> {
    const stageParam = route.params.stage as string | undefined
    const subjectParam = route.params.subject as string | undefined
    if (stageParam) {
      currentStage.value = stageParam as StageKey
    }
    await refreshSubjects(currentStage.value)
    if (!subjectParam) return true
    const found = currentSubjects.value.find((s) => s.key === subjectParam)
    if (found) {
      currentSubject.value = found
      return true
    }
    await router.replace({ name: opts?.redirectName ?? 'Home' })
    return false
  }

  watch(currentStage, (key) => {
    void refreshSubjects(key)
  })

  void refreshSubjects(currentStage.value)

  return {
    currentStage,
    currentStageName,
    currentSubjects,
    currentSubject,
    subjectsLoaded,
    stages: stages as Stage[],
    switchStage,
    selectSubject,
    refreshSubjects,
    ensureSubjectRouteValid,
  }
}
