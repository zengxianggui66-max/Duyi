/**
 * 教案结构化内容解析与展示
 */

export interface LessonPlanSection {
  key: string
  title: string
  icon: string
  items: string[]
}

export interface LessonPlanStructuredData {
  sections: LessonPlanSection[]
  hasStructured: boolean
  source: 'json' | 'text' | 'fallback'
}

const SECTION_DEFS: Array<{ key: string; title: string; icon: string; aliases: string[] }> = [
  { key: 'objectives', title: '教学目标', icon: '🎯', aliases: ['教学目标', 'objectives', '目标'] },
  { key: 'keyPoints', title: '教学重难点', icon: '⚡', aliases: ['教学重难点', '重难点', 'keyPoints', '重点'] },
  { key: 'preparation', title: '教学准备', icon: '📦', aliases: ['教学准备', '课前准备', 'preparation', '准备'] },
  { key: 'process', title: '教学过程', icon: '📋', aliases: ['教学过程', '教学流程', 'process', '课堂流程'] },
  { key: 'reflection', title: '教学反思', icon: '💭', aliases: ['教学反思', 'reflection', '反思'] },
]

function toLines(value: unknown): string[] {
  if (!value) return []
  if (Array.isArray(value)) {
    return value.map(String).map((s) => s.trim()).filter(Boolean)
  }
  const text = String(value).trim()
  if (!text) return []
  return text
    .split(/\n|[；;]/)
    .map((s) => s.trim())
    .filter(Boolean)
}

function parseJsonContent(raw: string): Record<string, unknown> | null {
  const trimmed = raw.trim()
  if (!trimmed.startsWith('{') && !trimmed.startsWith('[')) return null
  try {
    const parsed = JSON.parse(trimmed)
    return typeof parsed === 'object' && parsed !== null ? (parsed as Record<string, unknown>) : null
  } catch {
    return null
  }
}

/** 从 remark / description 等字段尝试解析 JSON 教案 */
export function parseLessonPlanFromText(raw?: string): LessonPlanStructuredData | null {
  if (!raw?.trim()) return null

  const json = parseJsonContent(raw)
  if (json) {
    const sections: LessonPlanSection[] = []
    for (const def of SECTION_DEFS) {
      const val = json[def.key] ?? json[def.title]
      if (def.key === 'process' && Array.isArray(val)) {
        const items = val
          .map((item) => {
            if (typeof item === 'string') return item.trim()
            if (item && typeof item === 'object') {
              const row = item as Record<string, unknown>
              const duration = String(row.duration || row.time || '').trim()
              const title = String(row.title || row.name || '').trim()
              const content = String(row.content || row.desc || '').trim()
              if (duration && title && content) return `${duration}|${title}|${content}`
              if (title && content) return `${title}：${content}`
              return title || content
            }
            return ''
          })
          .filter(Boolean)
        if (items.length) sections.push({ key: def.key, title: def.title, icon: def.icon, items })
        continue
      }
      const items = toLines(val)
      if (items.length) sections.push({ key: def.key, title: def.title, icon: def.icon, items })
    }
    if (sections.length) {
      return { sections, hasStructured: true, source: 'json' }
    }
  }

  const sections: LessonPlanSection[] = []
  for (const def of SECTION_DEFS) {
    const re = new RegExp(`【?${def.title}】?[:：]?([\\s\\S]*?)(?=【?(${SECTION_DEFS.map((d) => d.title).join('|')})】?[:：]|$)`, 'i')
    const m = raw.match(re)
    if (m?.[1]) {
      const items = toLines(m[1])
      if (items.length) sections.push({ key: def.key, title: def.title, icon: def.icon, items })
    }
  }
  if (sections.length) {
    return { sections, hasStructured: true, source: 'text' }
  }
  return null
}

/** 无结构化数据时，根据资源元信息生成可读的占位教案 */
export function buildFallbackLessonPlan(meta: {
  title?: string
  gradeName?: string
  unitName?: string
  subjectName?: string
  description?: string
}): LessonPlanStructuredData {
  const topic = meta.title || '本课'
  const unit = meta.unitName ? `（${meta.unitName}）` : ''
  const grade = meta.gradeName || ''
  const desc = meta.description?.trim()

  const sections: LessonPlanSection[] = [
    {
      key: 'objectives',
      title: '教学目标',
      icon: '🎯',
      items: desc
        ? [desc.length > 120 ? `${desc.slice(0, 120)}…` : desc]
        : [
            `知识与技能：理解并掌握《${topic}》的核心内容`,
            `过程与方法：通过朗读、讨论与合作探究理解课文`,
            `情感态度：激发学习兴趣，感受语言文字之美`,
          ],
    },
    {
      key: 'keyPoints',
      title: '教学重难点',
      icon: '⚡',
      items: [
        `重点：理解《${topic}》主要内容，正确朗读与识记生字词`,
        `难点：联系生活实际理解课文表达的情感与写法`,
      ],
    },
    {
      key: 'process',
      title: '教学过程',
      icon: '📋',
      items: [
        '一、情境导入（约 5 分钟）：联系学生经验，引出课题',
        '二、初读感知（约 10 分钟）：自由朗读，学习生字新词',
        '三、精读探究（约 15 分钟）：品读重点句段，体会表达方法',
        '四、拓展巩固（约 8 分钟）：课堂练习或小组交流',
        '五、总结布置（约 2 分钟）：回顾要点，布置课后作业',
      ],
    },
  ]

  if (grade || unit) {
    sections.unshift({
      key: 'preparation',
      title: '教学准备',
      icon: '📦',
      items: [
        `${grade}${unit}`.trim() || '配套教材',
        '多媒体课件、生字卡片（可选）',
      ],
    })
  }

  return { sections, hasStructured: false, source: 'fallback' }
}

export interface TeachingFlowStep {
  duration: string
  title: string
  content: string
  color: string
}

const FLOW_COLORS = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']

function parseProcessObject(value: unknown): TeachingFlowStep[] {
  if (!value) return []
  if (Array.isArray(value)) {
    const steps: TeachingFlowStep[] = []
    value.forEach((item, index) => {
      if (typeof item === 'string') {
        const parsed = parseProcessLine(item)
        if (parsed) steps.push({ ...parsed, color: FLOW_COLORS[index % FLOW_COLORS.length] })
        return
      }
      if (item && typeof item === 'object') {
        const row = item as Record<string, unknown>
        const title = String(row.title || row.name || row.step || '').trim()
        const content = String(row.content || row.desc || row.description || '').trim()
        const duration = String(row.duration || row.time || row.minutes || '').trim()
        if (title || content) {
          steps.push({
            duration: duration || '—',
            title: title || `环节 ${index + 1}`,
            content,
            color: FLOW_COLORS[index % FLOW_COLORS.length],
          })
        }
      }
    })
    return steps
  }
  return toLines(value).map((line, index) => ({
    ...parseProcessLine(line)!,
    color: FLOW_COLORS[index % FLOW_COLORS.length],
  })).filter(Boolean) as TeachingFlowStep[]
}

function parseProcessLine(line: string): Omit<TeachingFlowStep, 'color'> | null {
  const text = line.trim()
  if (!text) return null

  const structured = /^(\d+\s*分钟)\s*[|｜]\s*(.+?)\s*[|｜]\s*(.+)$/.exec(text)
  if (structured) {
    return { duration: structured[1], title: structured[2].trim(), content: structured[3].trim() }
  }

  const zhPattern =
    /^(?:[一二三四五六七八九十\d]+[、.．]?\s*)?(.+?)(?:[（(](?:约?\s*)?(\d+\s*分钟)[）)])?[:：]\s*(.+)$/.exec(text)
  if (zhPattern) {
    return {
      duration: zhPattern[2] ? zhPattern[2].replace(/\s+/g, '') : '—',
      title: zhPattern[1].trim(),
      content: zhPattern[3].trim(),
    }
  }

  const simple = /^(.+?)[:：]\s*(.+)$/.exec(text)
  if (simple) {
    return { duration: '—', title: simple[1].trim(), content: simple[2].trim() }
  }

  return { duration: '—', title: text, content: '' }
}

/** 从结构化教案中解析侧边栏「教学流程」时间轴 */
export function parseTeachingFlowSteps(data: LessonPlanStructuredData): TeachingFlowStep[] {
  const processSection = data.sections.find((s) => s.key === 'process')
  if (!processSection?.items.length) return []

  return processSection.items
    .map((line, index) => {
      const parsed = parseProcessLine(line)
      if (!parsed) return null
      return { ...parsed, color: FLOW_COLORS[index % FLOW_COLORS.length] }
    })
    .filter(Boolean) as TeachingFlowStep[]
}

/** 从 JSON 字符串直接解析教学流程（上传入库用） */
export function parseTeachingFlowFromJson(raw?: string): TeachingFlowStep[] {
  if (!raw?.trim()) return []
  const json = parseJsonContent(raw)
  if (!json) return []

  const flow = json.teachingFlow ?? json.process ?? json['教学流程']
  const steps = parseProcessObject(flow)
  if (steps.length) return steps

  const structured = resolveLessonPlanContent({ lessonPlanJson: raw })
  return parseTeachingFlowSteps(structured)
}

/** 上传表单文本 → lesson_plan_json */
export function buildLessonPlanJsonFromFlowText(text: string): string | undefined {
  const trimmed = text.trim()
  if (!trimmed) return undefined

  const lines = trimmed.split('\n').map((s) => s.trim()).filter(Boolean)
  const process = lines.map((line) => {
    const parsed = parseProcessLine(line)
    if (!parsed) return { title: line, content: '', duration: '—' }
    return parsed
  })

  return JSON.stringify({ process })
}

export function resolveLessonPlanContent(resource: {
  title?: string
  description?: string
  remark?: string
  lessonPlanJson?: string
  grade?: string
  gradeName?: string
  unitName?: string
  subject?: string
  subjectName?: string
}): LessonPlanStructuredData {
  const raw =
    resource.lessonPlanJson ||
    resource.remark ||
    resource.description

  const parsed = parseLessonPlanFromText(raw)
  if (parsed) return parsed

  return buildFallbackLessonPlan({
    title: resource.title,
    description: resource.description,
    gradeName: resource.gradeName || resource.grade,
    unitName: resource.unitName,
    subjectName: resource.subjectName || resource.subject,
  })
}
