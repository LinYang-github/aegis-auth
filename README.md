# 🛡️ Aegis-Auth (统一认证授权服务)

> **定位**：轻量、标准、安全、可演进的企业级统一身份认证系统。
>
> **愿景**：构建一个不臃肿、不锁死未来、能够支撑公司未来 5 年发展的 Auth 基础服务。

## 📖 项目简介

**Aegis-Auth** 是基于 **Spring Boot 3** 和 **Spring Authorization Server (SAS)** 构建的模块化单体应用。它严格遵循 OAuth2.1 和 OpenID Connect 1.0 标准，旨在为公司内部及第三方应用提供统一的身份管理（Identity）、认证（Authentication）和授权（Authorization）服务。

### 核心设计原则
- **标准优先**：全面拥抱 OAuth2 / OIDC / JWT 标准，拒绝私有协议。
- **无状态设计**：以 Access Token 为核心，适应云原生环境。
- **模块化单体**：物理单体部署（运维简单），逻辑模块隔离（随时可拆分微服务）。
- **严格隔离**：管理端 API (Admin) 与 认证协议端点 (Protocol) 在安全链上完全解耦。

---

## 🏗️ 技术栈 (Tech Stack)

| 组件 | 版本 / 选型 | 说明 |
| :--- | :--- | :--- |
| **Language** | Java 17+ | LTS 版本 |
| **Framework** | Spring Boot 3.2+ | 核心容器 |
| **Auth Core** | Spring Authorization Server | OAuth2 协议标准实现 |
| **Security** | Spring Security 6 | 安全防护基石 |
| **ORM** | MyBatis Plus | 灵活高效的持久层 |
| **Database** | MySQL 8.0+ | 数据存储 |
| **Tools** | Hutool, Lombok | 效率工具 |

---

## 📂 目录结构

采用 **模块化单体 (Modular Monolith)** 架构设计：

```text
aegis-auth
├── src/main/java/com/company/aegis
│   ├── common              // 🔧 公共模块 (Result封装, 全局异常, 工具类)
│   ├── config              // ⚙️ 全局配置 (MyBatis, Redis, WebMvc)
│   ├── modules             // 📦 业务模块 (垂直拆分)
│   │   ├── system          // [核心] RBAC 身份管理 (User, Role, Permission)
│   │   ├── log             // [审计] 操作日志与登录日志
│   │   └── client          // [应用] OAuth2 客户端管理
│   ├── security            // 🛡️ 安全核心 (最为关键)
│   │   ├── config          // SecurityFilterChain 配置
│   │   │   ├── AuthorizationServerConfig.java  // OAuth2 协议端点配置
│   │   │   └── SecurityConfig.java             // 基础安全与 Admin API 保护
│   │   └── service         // UserDetailService 实现与 Token 增强
│   └── AegisAuthApplication.java
└── src/main/resources
    ├── mapper              // MyBatis XML
    ├── db                  // 数据库脚本
    └── application.yml     // 核心配置
```

---

## 🗺️ 演进路线图 (Roadmap)

### Phase 1: 核心 MVP (当前阶段)
- [x] 工程骨架搭建 (Spring Boot 3 + SAS)
- [x] 数据库设计 (RBAC + OAuth2 Clients)
- [ ] 接入真实数据库的 `UserDetailsService`
- [ ] 实现 OAuth2 `Authorization Code` 模式流程
- [ ] 基础用户管理 API (CRUD)

### Phase 2: 安全增强与审计
- [ ] 接入 Refresh Token 机制
- [ ] 完善登录防护 (错误次数限制、验证码)
- [ ] 实现操作审计日志 (AOP + DB)
- [ ] 密码加密策略升级

### Phase 3: 扩展与运维
- [ ] 完整支持 OIDC (UserInfo Endpoint)
- [ ] Docker 容器化部署方案
- [ ] 多租户架构支持 (预研)
- [ ] 密钥轮转 (Key Rotation)

---

## 🚀 快速开始 (Quick Start)

### 1. 环境准备
*   JDK 17 或更高版本
*   MySQL 8.0
*   Maven 3.8+

### 2. 数据库初始化
在 MySQL 中创建数据库 `aegis_auth`，并执行 `src/main/resources/db/schema.sql` 中的脚本：
1.  建立 SAS 标准表 (`oauth2_registered_client` 等)。
2.  建立业务表 (`sys_user`, `sys_role` 等)。
3.  初始化默认数据。

### 3. 修改配置
编辑 `src/main/resources/application.yml`，配置你的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aegis_auth?...
    username: root
    password: your_password
```

### 4. 编译与运行
```bash
# 编译及下载依赖
mvn clean install -DskipTests

# 启动服务
java -jar target/aegis-auth-1.0.0.jar
```
启动成功后，访问 `http://localhost:9000/login` 即可看到登录页面。

---

## ⚠️ 常见问题

**Q: 启动时提示 `SpringApplication cannot be resolved`？**
*   **A**: 这是 IDE 依赖缓存问题。请在 VS Code 中执行 `Java: Clean Java Language Server Workspace`，或在命令行运行 `mvn clean install` 确保依赖下载完成。

**Q: 如何获取 Token？**
*   **A**: 本项目严格遵循 OAuth2 标准。请使用 Postman 或 Web 客户端发起标准的 OAuth2 Authorization Code 流程。

---

## 📝 许可证

This project is licensed under the MIT License.