/**
 * 资源数据处理 Web Worker
 * 
 * 当资源数量超过阈值（默认 1000 条）时，将分组/排序/过滤操作移入 Worker，
 * 避免长时间计算阻塞主线程。
 * 
 * 使用方式：
 *   import { createResourceWorker } from './resourceWorker'
 *   const worker = createResourceWorker()
 *   const result = await worker.process(resources, activeType)
 *   worker.terminate()
 */

export interface ResourceWorkerInput {
  resources: any[]
  activeType: string
  typeOrder: string[]
}

export interface ResourceWorkerOutput {
  grouped: Array<{ type: string; items: any[] }>
  typeStats: Array<{ type: string; count: number }>
  total: number
}

const WORKER_THRESHOLD = 1000

let workerInstance: Worker | null = null

function createWorker(): Worker {
  const workerCode = `
    self.onmessage = function(e) {
      const { resources, activeType, typeOrder } = e.data
      
      // 过滤
      let filtered = resources
      if (activeType !== '全部') {
        filtered = resources.filter(i => (i.type || '其他') === activeType)
      }
      
      // 统计
      const map = new Map()
      for (const item of filtered) {
        const t = item.type || '其他'
        map.set(t, (map.get(t) || 0) + 1)
      }
      
      // 分组
      const groupedMap = new Map()
      for (const item of filtered) {
        const t = item.type || '其他'
        if (!groupedMap.has(t)) groupedMap.set(t, [])
        groupedMap.get(t).push(item)
      }
      
      // 按 typeOrder 排序
      const grouped = []
      const used = new Set()
      for (const type of typeOrder) {
        if (groupedMap.has(type)) {
          grouped.push({ type, items: groupedMap.get(type) })
          used.add(type)
        }
      }
      for (const [type, items] of groupedMap) {
        if (!used.has(type)) {
          grouped.push({ type, items })
        }
      }
      
      // 统计数据
      const typeStats = [
        { type: '全部', count: filtered.length },
        ...Array.from(map.entries()).map(([type, count]) => ({ type, count }))
      ]
      
      self.postMessage({ grouped, typeStats, total: filtered.length })
    }
  `
  
  const blob = new Blob([workerCode], { type: 'application/javascript' })
  return new Worker(URL.createObjectURL(blob))
}

export interface ResourceWorker {
  process(input: ResourceWorkerInput): Promise<ResourceWorkerOutput>
  terminate(): void
  isActive: boolean
}

export function createResourceWorker(): ResourceWorker {
  const worker = createWorker()
  workerInstance = worker

  return {
    isActive: true,
    process(input: ResourceWorkerInput): Promise<ResourceWorkerOutput> {
      return new Promise((resolve, reject) => {
        worker.onmessage = (e) => resolve(e.data)
        worker.onerror = (e) => reject(e)
        worker.postMessage(input)
      })
    },
    terminate() {
      worker.terminate()
      workerInstance = null
    },
  }
}

/**
 * 根据数据量决定是否使用 Worker 处理资源分组
 * 小数据量（< 1000 条）直接在主线程处理，避免 Worker 序列化开销
 */
export function shouldUseWorker(count: number): boolean {
  return count >= WORKER_THRESHOLD
}
