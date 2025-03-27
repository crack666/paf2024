import { createPinia } from 'pinia';
import { useAuthStore } from './auth';
import { useTaskStore } from './task';
import { useQueueStore } from './queue';
import { useNotificationStore } from './notification';

const pinia = createPinia();

export {
  pinia,
  useAuthStore,
  useTaskStore,
  useQueueStore,
  useNotificationStore
};