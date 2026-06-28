import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { registerAdminPermissions } from '@/admin/permissions'
import './assets/styles/global.css'
import './assets/styles/filter-option.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
registerAdminPermissions(app)

app.mount('#app')
