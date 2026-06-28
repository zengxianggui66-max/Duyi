/**
 * 册别（年级）配置
 * 集中管理各学段的册别/年级选项
 */

export interface VolumeItem {
  id: string
  name: string
  isNew?: boolean
}

export const volumeDataMap: Record<string, VolumeItem[]> = {
  preschool: [
    { id: 'k2s1', name: '中班上学期', isNew: false },
    { id: 'k2s2', name: '中班下学期', isNew: false },
    { id: 'k3s1', name: '大班上学期', isNew: true },
    { id: 'k3s2', name: '大班下学期', isNew: true },
    { id: 'bridge_summer', name: '暑假衔接', isNew: true },
  ],
  primary: [
    { id: 'y1s1', name: '一年级上册', isNew: true },
    { id: 'y1s2', name: '一年级下册', isNew: true },
    { id: 'y2s1', name: '二年级上册', isNew: true },
    { id: 'y2s2', name: '二年级下册', isNew: true },
    { id: 'y3s1', name: '三年级上册', isNew: true },
    { id: 'y3s2', name: '三年级下册', isNew: true },
    { id: 'y4s1', name: '四年级上册', isNew: false },
    { id: 'y4s2', name: '四年级下册', isNew: false },
    { id: 'y5s1', name: '五年级上册', isNew: false },
    { id: 'y5s2', name: '五年级下册', isNew: false },
    { id: 'y6s1', name: '六年级上册', isNew: false },
    { id: 'y6s2', name: '六年级下册', isNew: false },
  ],
  junior: [
    { id: 'j7s1', name: '七年级上册', isNew: true },
    { id: 'j7s2', name: '七年级下册', isNew: true },
    { id: 'j8s1', name: '八年级上册', isNew: true },
    { id: 'j8s2', name: '八年级下册', isNew: true },
    { id: 'j9s1', name: '九年级上册', isNew: true },
    { id: 'j9s2', name: '九年级下册', isNew: true },
  ],
  senior: [
    { id: 's10s1', name: '必修一', isNew: true },
    { id: 's10s2', name: '必修二', isNew: true },
    { id: 's11s1', name: '选择性必修一', isNew: false },
    { id: 's11s2', name: '选择性必修二', isNew: false },
  ],
  art: [
    { id: 'a1s1', name: '一年级上册', isNew: true },
    { id: 'a1s2', name: '一年级下册', isNew: true },
    { id: 'a2s1', name: '二年级上册', isNew: true },
    { id: 'a2s2', name: '二年级下册', isNew: true },
    { id: 'a3s1', name: '三年级上册', isNew: false },
    { id: 'a3s2', name: '三年级下册', isNew: false },
  ],
  dance: [
    { id: 'd1s1', name: '一年级上册', isNew: true },
    { id: 'd1s2', name: '一年级下册', isNew: true },
    { id: 'd2s1', name: '二年级上册', isNew: true },
    { id: 'd2s2', name: '二年级下册', isNew: true },
  ],
}

/** 学段对应的年级/册别筛选项（扩展筛选条「年级」下拉） */
export function getGradeListForStage(stage: string): string[] {
  const volumes = volumeDataMap[stage] || volumeDataMap.primary
  if (stage === 'senior') {
    return volumes.map((v) => v.name)
  }
  const seen = new Set<string>()
  const grades: string[] = []
  for (const v of volumes) {
    const m = v.name.match(/^(.+?)(上册|下册)$/)
    const grade = m ? m[1] : v.name
    if (!seen.has(grade)) {
      seen.add(grade)
      grades.push(grade)
    }
  }
  return grades
}

export function getDefaultGradeForStage(stage: string): string {
  const list = getGradeListForStage(stage)
  return list[0] || '一年级'
}

/** 册别展示名 → 年级 + 学期 */
export function parseGradeSemesterFromVolumeName(
  volumeName: string,
): { grade: string; semester: string } | null {
  if (!volumeName) return null
  const preschoolMatch = volumeName.match(/^(.+?)(上学期|下学期)$/)
  if (preschoolMatch) {
    return {
      grade: preschoolMatch[1],
      semester: preschoolMatch[2],
    }
  }
  const m = volumeName.match(/^(.+?)(上册|下册)$/)
  if (m) {
    return {
      grade: m[1],
      semester: m[2] === '上册' ? '上学期' : '下学期',
    }
  }
  return { grade: volumeName, semester: '全部' }
}

/** 年级 + 学期 → volume id */
export function resolveVolumeIdByGradeSemester(
  stage: string,
  grade: string,
  semester: string,
): string | undefined {
  const list = volumeDataMap[stage] || []
  if (!grade) return undefined

  if (stage === 'senior') {
    return list.find((v) => v.name === grade)?.id
  }

  if (semester === '上学期') {
    const target = `${grade}上册`
    const hit = list.find((v) => v.name === target)
    if (hit) return hit.id
  }
  if (semester === '下学期') {
    const target = `${grade}下册`
    const hit = list.find((v) => v.name === target)
    if (hit) return hit.id
  }

  return list.find((v) => v.name.startsWith(grade))?.id
}
