<template>
  <ContentPackageCrud
    entity-label="研学包"
    resource-link="/traditional-culture"
    resource-source-type="culture_resource"
    :list-fn="adminContentApi.listCulturePackages"
    :create-fn="adminContentApi.createCulturePackage"
    :update-fn="adminContentApi.updateCulturePackage"
    :status-fn="adminContentApi.setCulturePackageStatus"
    :delete-fn="adminContentApi.deleteCulturePackage"
    :show-region="true"
    :show-culture-fields="true"
  />
</template>

<script setup lang="ts">
import ContentPackageCrud from '@/admin/components/content/ContentPackageCrud.vue'
import { adminContentApi } from '@/admin/api/content'
</script>
