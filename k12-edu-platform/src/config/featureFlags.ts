/** Phase 5-E：API 切源开关（默认开启，设 VITE_USE_TAXONOMY_API=false 可回退常量） */
export const USE_TAXONOMY_API = import.meta.env.VITE_USE_TAXONOMY_API !== 'false'

/** Phase 5-E：字典/标签 API 切源 */
export const USE_DICTIONARY_API = import.meta.env.VITE_USE_DICTIONARY_API !== 'false'

/** M2：catalog 目录树浏览，默认启用；设 VITE_USE_CATALOG_BROWSE=false 可临时回退。 */
export const USE_CATALOG_BROWSE = import.meta.env.VITE_USE_CATALOG_BROWSE !== 'false'

/** M5：上传走主表 API（设 VITE_USE_MASTER_WRITE=true 启用） */
export const USE_MASTER_WRITE = import.meta.env.VITE_USE_MASTER_WRITE === 'true'

/** Phase 7-A：首页运营 API 切源（默认开启，设 VITE_USE_HOME_OPS_API=false 回退静态常量） */
export const USE_HOME_OPS_API = import.meta.env.VITE_USE_HOME_OPS_API !== 'false'
