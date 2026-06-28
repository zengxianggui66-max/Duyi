<template>
  <ContentPackageCrud
    entity-label="精品专辑"
    resource-link="/topic-zone"
    resource-source-type="topic_resource"
    :list-fn="adminContentApi.listTopicAlbums"
    :create-fn="adminContentApi.createTopicAlbum"
    :update-fn="adminContentApi.updateTopicAlbum"
    :status-fn="adminContentApi.setTopicAlbumStatus"
    :delete-fn="adminContentApi.deleteTopicAlbum"
    :show-category="true"
    :show-region="true"
    :show-grade-stage="true"
  />
</template>

<script setup lang="ts">
import ContentPackageCrud from '@/admin/components/content/ContentPackageCrud.vue'
import { adminContentApi } from '@/admin/api/content'
</script>
