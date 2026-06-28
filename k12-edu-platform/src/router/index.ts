/**
 * Vue Router 路由配置
 * 所有页面路由定义，OAuth 回调路由（/login/oauth/callback）为关键页面
 */
import { createRouter, createWebHistory } from 'vue-router'
import { adminRoutes } from '@/admin/router/routes'
import { setupAdminGuards } from '@/admin/router/guards'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/home/HomePage.vue'),
    },
    {
      path: '/promotion',
      redirect: '/promotion/youxiao',
    },
    {
      path: '/promotion/:type',
      name: 'PromotionLanding',
      component: () => import('@/views/promotion/PromotionLandingPage.vue'),
      props: true,
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/user/Login.vue'),
      meta: { layout: 'auth' },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/user/Register.vue'),
      meta: { layout: 'auth' },
    },
    // ★ OAuth 回调 — 微信/QQ/企微扫码后跳转此页面
    {
      path: '/login/oauth/callback',
      name: 'OAuthCallback',
      component: () => import('@/views/user/OAuthCallback.vue'),
      meta: { layout: 'auth' },
    },
    // ★ OAuth 新用户（已改为默认教师，保留路由兼容旧链接）
    {
      path: '/oauth/select-role',
      name: 'OAuthSelectRole',
      component: () => import('@/views/user/OAuthSelectRole.vue'),
      meta: { layout: 'auth' },
    },
    // ★ 绑定手机号（OAuth 注册后强制引导）
    {
      path: '/bind-phone',
      name: 'BindPhone',
      component: () => import('@/views/user/BindPhone.vue'),
      meta: { layout: 'auth' },
    },
    // 用户中心
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/user/Profile.vue'),
    },
    {
      path: '/membership',
      name: 'Membership',
      component: () => import('@/views/user/Membership.vue'),
    },
    {
      path: '/my-resources',
      name: 'MyResources',
      component: () => import('@/views/user/MyResources.vue'),
    },
    {
      path: '/about',
      name: 'About',
      component: () => import('@/views/user/About.vue'),
    },
    // 教学资源
    {
      path: '/grade/:grade',
      name: 'Grade',
      component: () => import('@/views/grade/GradePage.vue'),
    },
    {
      path: '/resource/sync',
      redirect: '/lesson',
    },
    {
      path: '/resource/:id',
      name: 'ResourceDetail',
      component: () => import('@/views/home/ResourceDetail.vue'),
    },
    {
      path: '/search/result',
      name: 'SearchResult',
      component: () => import('@/views/search/SearchResultPage.vue'),
    },
    {
      path: '/resource/version/:id',
      name: 'VersionResource',
      component: () => import('@/views/resource/VersionResourcePage.vue'),
    },
    {
      path: '/subject/:stage/:subject/:version?',
      name: 'SubjectDetail',
      component: () => import('@/views/resource/SubjectDetailPage.vue'),
    },
    {
      path: '/subject/:id',
      name: 'SubjectLegacy',
      redirect: (to) => ({
        name: 'SubjectDetail',
        params: {
          stage: 'primary',
          subject: to.params.id,
          version: 'tongbian2024',
        },
        query: to.query,
      }),
    },
    {
      path: '/upload',
      name: 'Upload',
      component: () => import('@/views/resource/ResourceUpload.vue'),
    },
    // 资讯中心
    {
      path: '/news',
      name: 'NewsHub',
      component: () => import('@/views/news/NewsHub.vue'),
    },
    {
      path: '/news/list',
      name: 'NewsList',
      component: () => import('@/views/news/NewsList.vue'),
    },
    {
      path: '/news/publish',
      name: 'NewsPublish',
      component: () => import('@/views/news/NewsPublish.vue'),
    },
    {
      path: '/news/:id',
      name: 'NewsDetail',
      component: () => import('@/views/news/NewsDetail.vue'),
    },
    {
      path: '/news/channel/:key',
      name: 'NewsChannel',
      component: () => import('@/views/news/NewsChannel.vue'),
    },
    {
      path: '/help',
      name: 'HelpCenter',
      component: () => import('@/views/news/HelpCenter.vue'),
    },
    // 备课中心
    {
      path: '/lesson',
      name: 'LessonWorkbench',
      component: () => import('@/views/lesson/LessonWorkbench.vue'),
    },
    {
      path: '/lesson/smart',
      name: 'SmartLesson',
      component: () => import('@/views/lesson/SmartLesson.vue'),
    },
    {
      path: '/lesson/papers',
      name: 'PrepPaperList',
      component: () => import('@/views/lesson/PrepPaperList.vue'),
    },
    {
      path: '/lesson/basket',
      name: 'PrepBasket',
      component: () => import('@/views/lesson/PrepBasketPage.vue'),
    },
    {
      path: '/lesson/assemble',
      name: 'PrepAssemble',
      component: () => import('@/views/lesson/PrepBasketPage.vue'),
    },
    // 考试中心
    {
      path: '/exam',
      name: 'ExamHome',
      component: () => import('@/views/exam/ExamPaperList.vue'),
    },
    {
      path: '/exam/smart',
      name: 'SmartExam',
      component: () => import('@/views/exam/SmartExam.vue'),
    },
    // 特色专题
    {
      path: '/feature',
      name: 'FeaturePage',
      component: () => import('@/views/feature/FeaturePage.vue'),
    },
    {
      path: '/feature/shengya',
      redirect: '/topic',
    },
    {
      path: '/feature/banhui',
      redirect: '/theme-class-meeting',
    },
    {
      path: '/feature/zhuanti',
      redirect: '/topic',
    },
    {
      path: '/feature/:type',
      name: 'FeatureChannel',
      component: () => import('@/views/feature/FeatureChannel.vue'),
    },
    {
      path: '/culture',
      name: 'TraditionalCulture',
      component: () => import('@/views/feature/TraditionalCulture.vue'),
    },
    {
      path: '/traditional-culture',
      redirect: '/culture',
    },
    {
      path: '/competition',
      name: 'CompetitionZone',
      component: () => import('@/views/feature/CompetitionZone.vue'),
    },
    {
      path: '/competition-zone',
      redirect: '/competition',
    },
    {
      path: '/competition-zone/resource/:id',
      name: 'CompetitionResourceDetail',
      component: () => import('@/views/feature/CompetitionResourceDetail.vue'),
    },
    {
      path: '/topic',
      name: 'TopicZone',
      component: () => import('@/views/feature/TopicZone.vue'),
    },
    {
      path: '/topic-zone',
      redirect: '/topic',
    },
    {
      path: '/topic-zone/resource/:id',
      name: 'TopicResourceDetail',
      component: () => import('@/views/feature/TopicResourceDetail.vue'),
    },
    {
      path: '/theme-class-meeting',
      name: 'ThemeClassMeeting',
      component: () => import('@/views/feature/ThemeClassMeeting.vue'),
    },
    {
      path: '/theme-class-meeting/:category',
      name: 'ClassMeetingCategory',
      component: () => import('@/views/feature/ClassMeetingCategory.vue'),
    },
    ...adminRoutes,
    // 404
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/user/About.vue'),
    },
  ],
})

setupAdminGuards(router)

export default router
