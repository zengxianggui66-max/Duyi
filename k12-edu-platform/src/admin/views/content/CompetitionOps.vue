<template>
  <ContentPackageCrud
    entity-label="竞赛套卷"
    resource-link="/competition-zone"
    resource-source-type="competition_resource"
    :list-fn="adminContentApi.listCompetitionPackages"
    :create-fn="adminContentApi.createCompetitionPackage"
    :update-fn="adminContentApi.updateCompetitionPackage"
    :status-fn="adminContentApi.setCompetitionPackageStatus"
    :delete-fn="adminContentApi.deleteCompetitionPackage"
    :show-category="true"
    :show-grade-stage="true"
  />
</template>

<script setup lang="ts">
import ContentPackageCrud from '@/admin/components/content/ContentPackageCrud.vue'
import { adminContentApi } from '@/admin/api/content'
</script>
