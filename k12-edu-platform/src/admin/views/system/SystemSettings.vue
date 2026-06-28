<template>

  <AdminPageShell title="系统设置" desc="角色权限、操作日志、系统信息">

    <el-tabs v-model="activeTab">

      <el-tab-pane v-if="hasRoleManage" label="角色权限" name="roles">

        <RoleManage v-if="activeTab === 'roles'" />

      </el-tab-pane>

      <el-tab-pane v-if="hasLogView" label="操作日志" name="logs">

        <OperationLogList v-if="activeTab === 'logs'" />

      </el-tab-pane>

      <el-tab-pane label="系统信息" name="info">

        <el-descriptions :column="1" border>

          <el-descriptions-item label="当前用户">{{ adminStore.user?.username }}</el-descriptions-item>

          <el-descriptions-item label="后台角色">

            {{ adminStore.user?.roles?.join(', ') || '-' }}

          </el-descriptions-item>

          <el-descriptions-item label="权限数量">{{ adminStore.permissions.length }}</el-descriptions-item>

        </el-descriptions>

      </el-tab-pane>

    </el-tabs>

  </AdminPageShell>

</template>



<script setup lang="ts">

import { ref, computed } from 'vue'

import AdminPageShell from '@/admin/components/AdminPageShell.vue'

import RoleManage from '@/admin/components/RoleManage.vue'

import OperationLogList from '@/admin/components/OperationLogList.vue'

import { useAdminAuthStore } from '@/admin/store/adminAuth'



const activeTab = ref('roles')

const adminStore = useAdminAuthStore()

const hasRoleManage = computed(
  () => adminStore.hasPermission('admin:system:role_manage') || adminStore.hasPermission('admin:role:view')
)

const hasLogView = computed(
  () =>
    adminStore.hasPermission('admin:system:log_view') ||
    adminStore.hasPermission('admin:system:config_edit')
)

</script>

