<template>
  <div class="edit-user-profile-container" v-loading="loading">
    <el-card>
      <template #header>
        <h3>编辑我的资料</h3>
      </template>
      <el-form v-if="user" :model="userForm" label-position="top" style="max-width: 600px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleUpdateProfile" style="width: 100%;">保存更改</el-button>
        </el-form-item>
      </el-form>
      <div v-else>正在加载用户数据...</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useAuthStore } from '../../../store/auth';
import { updateUser, getUserById } from '../../../api/user';
import { ElMessage } from 'element-plus';
import type { Person } from '../../../api/types';
import { useRouter } from 'vue-router';

const loading = ref(false);
const authStore = useAuthStore();
const user = authStore.user;
const router = useRouter();

const userForm = ref<Partial<Person>>({});

watch(
  () => authStore.user,
  (newUser) => {
    if (newUser?.id) {
      loading.value = true;
      getUserById(newUser.id).then((res) => {
        if (res.success) {
          userForm.value = res.data;
        } else {
          ElMessage.error('获取用户信息失败: ' + res.message);
        }
      }).finally(() => {
        loading.value = false;
      });
    }
  },
  { immediate: true }
);

const handleUpdateProfile = async () => {
  if (!user || !user.id) {
    ElMessage.error('用户数据不可用。');
    return;
  }
  loading.value = true;
  try {
    const payload: Person = {
      ...user,
      ...userForm.value,
      username: user.username,
    };

    const response = await updateUser(user.id, payload);
    if (response.success) {
      authStore.setUser(response.data);
      ElMessage.success('个人资料更新成功！');
      router.back();
    } else {
      throw new Error(response.message || '更新个人资料失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message || '发生错误');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.edit-user-profile-container {
  padding: 20px;
}
</style>