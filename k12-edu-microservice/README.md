# K12 教育资源平台 - 微服务架构

## 架构总览

```
                    ┌──────────────┐
                    │   前端 (Vue) │
                    │ localhost:5173│
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │  API Gateway │
                    │ localhost:9000│
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
        ┌─────▼─────┐ ┌───▼────┐ ┌────▼────┐
        │  k12-auth  │ │k12-res │ │k12-art │  ...
        │  :8081     │ │:8082   │ │:8083    │
        └───────────┘ └────────┘ └─────────┘
                           │
                    ┌──────▼───────┐
                    │    Nacos     │
                    │  :8848       │
                    └──────────────┘
                           │
                    ┌──────▼───────┐
                    │    MySQL     │
                    │  xinketang   │
                    └──────────────┘
```

## 技术栈

| 组件 | 技术选型 | 版本 |
|------|----------|------|
| 基础框架 | Spring Boot | 3.3.5 |
| 微服务治理 | Spring Cloud | 2023.0.3 |
| 注册中心 | Nacos | 2.3.x |
| API 网关 | Spring Cloud Gateway | - |
| 服务调用 | OpenFeign + LoadBalancer | - |
| ORM | MyBatis-Plus | 3.5.9 |
| 数据库 | MySQL | 8.x |
| 认证 | JWT (jjwt) | 0.12.6 |
| 前端 | Vue 3 + TypeScript | - |

## 模块说明

| 模块 | 端口 | 说明 | 数据表 |
|------|------|------|--------|
| **k12-gateway** | 9000 | API 网关，统一入口 + JWT 鉴权 + 路由转发 | 无 |
| **k12-auth** | 8081 | 用户认证服务 | user |
| **k12-resource** | 8082 | 教学资源服务 | resource |
| **k12-article** | 8083 | 资讯文章服务 | article |
| **k12-lesson** | 8084 | 智能备课服务 | lesson_plan |
| **k12-exam** | 8085 | 智能组卷服务 | exam_paper |
| **k12-member** | 8086 | 会员服务 | member_order, user |
| **k12-common** | - | 公共模块（实体、DTO、工具类） | 无 |

## 部署步骤

### 1. 前置条件

- JDK 21+
- Maven 3.8+
- MySQL 8.x（库名 **xinketang**，见 `sql/README.md`）
- Nacos 2.3.x

### 2. 安装 Nacos

```bash
# 下载 Nacos
curl -O https://github.com/alibaba/nacos/releases/download/2.3.2/nacos-server-2.3.2.zip
unzip nacos-server-2.3.2.zip
cd nacos/bin

# Windows 单机启动
startup.cmd -m standalone
```

访问 http://localhost:8848/nacos 创建命名空间 `k12-edu`。

### 3. 编译打包

```bash
cd k12-edu-microservice
mvn clean package -DskipTests
```

### 4. 启动服务（按顺序）

```bash
# 1. 启动认证服务
java -jar k12-auth/target/k12-auth-1.0.0.jar

# 2. 启动资源服务
java -jar k12-resource/target/k12-resource-1.0.0.jar

# 3. 启动资讯服务
java -jar k12-article/target/k12-article-1.0.0.jar

# 4. 启动备课服务
java -jar k12-lesson/target/k12-lesson-1.0.0.jar

# 5. 启动组卷服务
java -jar k12-exam/target/k12-exam-1.0.0.jar

# 6. 启动会员服务
java -jar k12-member/target/k12-member-1.0.0.jar

# 7. 启动网关（最后启动）
java -jar k12-gateway/target/k12-gateway-1.0.0.jar
```

### 5. 前端配置

修改 `k12-edu-platform/vite.config.ts` 的 proxy target：

```ts
proxy: {
  '/api': {
    target: 'http://localhost:9000',  // 原来是 8080，改为网关 9000
    changeOrigin: true
  }
}
```

然后 `npm run dev` 启动前端。

## 网关路由规则

| 请求路径 | 转发目标 | 说明 |
|----------|----------|------|
| `/api/auth/**` | k12-auth:8081 | 登录/注册 |
| `/api/resource/**` | k12-resource:8082 | 资源管理 |
| `/api/news/**` | k12-article:8083 | 资讯文章 |
| `/api/lesson/**` | k12-lesson:8084 | 智能备课 |
| `/api/exam/**` | k12-exam:8085 | 智能组卷 |
| `/api/member/**` | k12-member:8086 | 会员服务 |

## 从单体迁移的关键变化

1. **认证方式**：从 Spring Security 拦截器 → 网关全局过滤器统一鉴权
2. **用户信息传递**：从 `SecurityContextHolder` → 请求头 `X-User-Id` / `X-Username` / `X-User-Role`
3. **服务间通信**：同模块直接数据库访问 → 跨模块通过 OpenFeign 远程调用
4. **前端无感知**：API 路径完全不变，只需改 proxy target 从 8080 → 9000

## 扩展方向

- [ ] Nacos Config 配置中心
- [ ] Sentinel 熔断限流
- [ ] Seata 分布式事务
- [ ] Skywalking / Zipkin 链路追踪
- [ ] Docker + Docker Compose 容器化部署
- [ ] Kubernetes 编排
