<template>
  <div class="user-profile-container" v-loading="loading">
    <!-- User Info Header -->
    <div class="user-info-header">
      <el-avatar :size="80" :src="userForm.photo" icon="el-icon-user-solid" />
      <div class="user-details">
        <h2>{{ user?.username }}</h2>
        <p>ID: {{ user?.id }}</p>
      </div>
    </div>

    <!-- Action List -->
    <div class="action-list">
      <el-menu>
        <el-menu-item index="1" @click="navigateTo('/mobile/profile/user-profile')">
          <el-icon><User /></el-icon>
          <span>我的资料</span>
        </el-menu-item>
        <el-menu-item index="2" @click="showPasswordDialog = true">
          <el-icon><Lock /></el-icon>
          <span>修改密码</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- Role Switcher -->
    <div class="role-switcher" v-if="availableRoles.length > 0">
      <h3 class="role-switcher-title">切换身份</h3>
      <el-menu>
        <el-menu-item
          v-for="role in availableRoles"
          :key="role.name"
          :index="role.name"
          @click="switchRole(role.path)"
        >
          <el-icon><component :is="role.icon" /></el-icon>
          <span>{{ role.title }}</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- Logout Button -->
    <div class="logout-section">
      <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
    </div>

    <!-- Password Update Dialog -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="90%">
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-position="top"
        @submit.prevent="handleUpdatePassword"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdatePassword">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, reactive, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../store/auth';
import { getUserById, updateUserPassword } from '../api/user';
import { ElMessage, type FormInstance } from 'element-plus';
import { User, Lock, ShoppingCart, UserCog } from 'lucide-vue-next';
import type { Person } from '../api/types';

const loading = ref(false);
const showPasswordDialog = ref(false);
const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const user = authStore.user;

const passwordFormRef = ref<FormInstance>();
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: '',
});

const passwordRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: any, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
};

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

const allRoles = [
  { name: 'CUSTOMER', title: '顾客', path: '/mobile/home', icon: ShoppingCart },
  { name: 'MERCHANT', title: '商家', path: '/mobile/merchant/dashboard', icon: UserCog },
  { name: 'ADMIN', title: '管理', path: '/mobile/admin/dashboard', icon: UserCog },
];

const getCurrentRole = () => {
  const currentPath = route.path;
  if (currentPath.startsWith('/mobile/admin')) return 'ADMIN';
  if (currentPath.startsWith('/mobile/merchant')) return 'MERCHANT';
  return 'CUSTOMER';
};

const availableRoles = computed(() => {
  const userRoles = authStore.userRoles;
  const currentRole = getCurrentRole();
  return allRoles.filter(role => userRoles.includes(role.name) && role.name !== currentRole);
});

const switchRole = (path: string) => {
  router.push(path);
};

const navigateTo = (path: string) => {
  router.push(path);
};

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value || !user) return;

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const res = await updateUserPassword({
          username: user.username,
          password: passwordForm.newPassword,
        });
        if (res.success) {
          ElMessage.success('密码更新成功');
          showPasswordDialog.value = false;
          passwordForm.newPassword = '';
          passwordForm.confirmPassword = '';
          passwordFormRef.value?.resetFields();
        } else {
          ElMessage.error(res.message || '密码更新失败');
        }
      } catch (error) {
        ElMessage.error('密码更新失败');
      } finally {
        loading.value = false;
      }
    }
  });
};

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.user-profile-container {
  background-color: #f4f4f4;
  min-height: 100vh;
  padding-bottom: 20px;
}

.user-info-header {
  display: flex;
  align-items: center;
  gap: 20px;
  background-color: var(--el-color-primary);
  color: white;
  padding: 40px 20px;
}

.user-details h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
}

.user-details p {
  margin: 5px 0 0;
  font-size: 0.9rem;
  opacity: 0.9;
}

.action-list, .role-switcher {
  margin: 20px;
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.role-switcher-title {
  font-size: 1rem;
  color: #666;
  padding: 15px 20px;
  margin: 0;
  border-bottom: 1px solid #f0f0f0;
}

.el-menu {
  border-right: none;
}

.el-menu-item {
  height: 50px;
  line-height: 50px;
  font-size: 1rem;
}

.el-menu-item .el-icon {
  margin-right: 10px;
}

.logout-section {
  margin: 30px 20px;
}

.logout-section .el-button {
  width: 100%;
  height: 45px;
  font-size: 1rem;
}
</style>