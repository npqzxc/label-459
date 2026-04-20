import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/text-convert',
    name: 'TextConvert',
    component: () => import('../views/TextConvert.vue')
  },
  {
    path: '/word-to-pdf',
    name: 'WordToPdf',
    component: () => import('../views/WordToPdf.vue')
  },
  {
    path: '/crypto',
    name: 'Crypto',
    component: () => import('../views/Crypto.vue')
  },
  {
    path: '/json-format',
    name: 'JsonFormat',
    component: () => import('../views/JsonFormat.vue')
  }
]

const router = new VueRouter({
  mode: 'hash',
  base: process.env.BASE_URL,
  routes
})

export default router
