<template>
  <ProfileMenu>
    <template #menu-items>
      <router-link to="/mobile/merchant/applications" class="menu-item">
        <span>开店申请</span>
        <ChevronRight :size="20" color="#999" />
      </router-link>
    </template>
  </ProfileMenu>
</template>

<script setup lang="ts">
import ProfileMenu from '../../../components/ProfileMenu.vue';
import { ChevronRight } from 'lucide-vue-next';
</script>

<style scoped>
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  cursor: pointer;
  text-decoration: none;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}
</style>