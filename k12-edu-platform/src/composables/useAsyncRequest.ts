/**
 * 异步请求 composable
 * 统一管理 loading、error、success 状态
 */
import { ref, shallowRef, isRef } from 'vue'

export interface AsyncRequestOptions<T> {
  immediate?: boolean
  defaultValue?: T
  onSuccess?: (data: T) => void
  onError?: (error: any) => void
}

export function useAsyncRequest<T>(
  asyncFn: () => Promise<T>,
  options: AsyncRequestOptions<T> = {}
) {
  const { immediate = false, defaultValue, onSuccess, onError } = options

  const loading = ref(false)
  const error = shallowRef<Error | null>(null)
  const data = shallowRef<T | undefined>(defaultValue)
  const status = ref<'idle' | 'loading' | 'success' | 'error'>('idle')

  async function execute(_args?: unknown) {
    loading.value = true
    error.value = null
    status.value = 'loading'

    try {
      const fn = isRef(asyncFn) ? asyncFn.value : asyncFn
      const result = await (fn as () => Promise<T>)()
      data.value = result
      status.value = 'success'
      onSuccess?.(result)
      return result
    } catch (e) {
      error.value = e instanceof Error ? e : new Error(String(e))
      status.value = 'error'
      onError?.(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  function reset() {
    loading.value = false
    error.value = null
    data.value = defaultValue
    status.value = 'idle'
  }

  // 立即执行
  if (immediate) {
    execute()
  }

  return {
    loading,
    error,
    data,
    status,
    execute,
    reset,
    // 便捷属性
    isSuccess: () => status.value === 'success',
    isError: () => status.value === 'error',
    isIdle: () => status.value === 'idle',
  }
}

/**
 * 使用带参数的异步请求
 */
export function useAsyncRequestWithParams<T, P = unknown>(
  asyncFn: (params: P) => Promise<T>,
  options: AsyncRequestOptions<T> & { params?: P } = {}
) {
  const { immediate = false, params: initialParams, ...rest } = options

  const params = ref<P | undefined>(initialParams)
  const request = useAsyncRequest<T>(
    () => asyncFn(params.value as P),
    { immediate, ...rest }
  )

  async function execute(newParams?: P) {
    if (newParams !== undefined) {
      params.value = newParams
    }
    return request.execute()
  }

  return {
    ...request,
    params,
    execute,
  }
}
