# Compose 脚手架规模化指南

本脚手架使用同一套 Route、Screen、ViewModel、领域角色和数据边界覆盖小、中、大型项目。规模升级只替换装配或实现，不推翻 Feature API，也不为了“以后可能用到”提前引入框架。

## 规模判断

项目规模不能只按代码行数判断，应同时观察团队边界、发布方式、数据复杂度和非功能要求：

| 阶段 | 常见信号 | 保持不变 | 建议升级 |
| --- | --- | --- | --- |
| 小型 | 1–5 个 Feature、单团队、单应用、数据以在线读取为主 | 手动 `AppContainer`、窄 Repository、文件快照、单应用模块 | 不引入 UseCase、DI、数据库或动态化 |
| 中型 | 多业务域、多人并行、离线查询/事务、可靠后台任务、测试环境独立 | Feature 只依赖 domain；data 自己装配 Service/DataSource/Repository | 真实事务使用 Room；可延期工作使用 WorkManager；复用编排才增加 UseCase；CI 增加设备烟测与迁移测试 |
| 大型 | 多团队独立发布、白标/多租户、多进程、动态交付、严格性能与安全 SLO | `AppDependencies`、Route/Screen、领域角色和错误模型 | 用 Hilt/Dagger 实现 `AppDependencies`；按组织边界拆 `api/implementation`；需要时增加动态 Feature、Macrobenchmark/Baseline Profile、集中可观测性和供应链门禁 |

只有表中信号真实出现才升级。Feature 数量不是唯一开关；一个包含支付、离线事务或多租户认证的“小界面项目”，也应按对应风险升级。

## 可替换边界

- 依赖注入：Activity 永远只读取 `AppDependencies`。大型项目替换其实现，不修改 Feature 构造参数。
- 本地存储：Repository 契约保持不变。文件快照需要查询、增量更新、事务或迁移时，在 data 模块内部替换为 Room。
- 网络：每个业务服务在 `AppNetworkEndpoints` 注册独立 `NetworkEndpoint`。同认证策略可共享一个 `NetworkClientFactory`；认证、证书或 Converter 不同时创建另一工厂配置，不复制网络模块。
- 认证：短期令牌由 `NetworkHeaderProvider` 按 Host 注入；确需刷新令牌时注入 OkHttp `Authenticator`，并在产品代码中实现单飞刷新、失败熔断和退出登录，禁止在拦截器中无限重试。
- 环境：`APP_ENVIRONMENT` 只选择 `development/staging/production`，地址仍由代码注册表管理。不同环境必须经过相同 HTTPS、Host 白名单和 Release 检查。
- 发布：Release 构建无条件使用 production 环境。模板只验证未签名 AAB 的编译、混淆和打包；真实产品由 CI 使用受保护的上传密钥签名，再运行 `verify_signed_bundle.sh` 校验证书，禁止把 JKS 和密码提交到仓库。
- 可观测性：默认 `AppDiagnosticObserver` 已接收任务、网络和缓存故障。商业项目在这一处转发到自建平台或 Sentry，并确保脱敏、采样、离线队列和用户授权符合产品要求。

## 质量门禁升级

所有规模都必须通过仓库自带的架构检查、单元测试、Lint、单/多 Activity Debug 和 R8 Release。进一步能力按风险开启：

| 能力 | 启用条件 | 最小验收 |
| --- | --- | --- |
| 设备 UI 烟测 | 出现关键导航、权限、输入或系统组件流程 | CI 至少覆盖启动、主链路、返回栈和进程恢复 |
| Room 迁移测试 | 首次引入数据库或升级 Schema | 保存每个已发布 Schema，并验证逐版本迁移 |
| WorkManager 测试 | 有可靠后台任务 | 覆盖约束、重试、幂等和进程重启 |
| Macrobenchmark/Baseline Profile | 冷启动、滚动或页面切换有明确 SLO | 在固定真机/云设备记录基线，回归超过团队阈值阻断发布 |
| 证书固定/CT | 团队拥有后端证书轮换和事故恢复能力 | 主 Pin、备用 Pin、轮换演练和远程恢复方案齐全 |
| 依赖验证/SBOM | 进入受监管或高安全交付 | 锁定依赖来源与校验信息，发布产物可追溯 |

## 产品能力启用清单

以下能力需要真实产品输入，脚手架只保留边界，禁止用示例值制造“已经完成”的假象：

- 认证：确定 Token 生命周期、刷新接口和退出策略后，实现 `NetworkHeaderProvider` 与 OkHttp `Authenticator`；必须有单飞刷新、重试上限和失败熔断测试。
- 数据库：出现查询、事务、增量更新或已发布 Schema 后，以 `FeedLocalDataSource` 边界替换 Room，并保存逐版本 Schema 和迁移测试。
- 后台任务：只有必须跨进程可靠完成的同步、上传或清理任务才接入 WorkManager，并验证约束、幂等和退避。
- 性能：先确定目标设备和冷启动、滚动、页面切换 SLO，再建立 Macrobenchmark 与 Baseline Profile；没有阈值的跑分不作为门禁。
- 安全：证书 Pin、CT、依赖验证和 SBOM 必须绑定证书轮换、事故恢复、制品仓库和发布平台，不能提交演示密钥或虚假 Pin。
- 可观测性：在 `AppDiagnosticObserver` 替换平台转发时保留稳定码、Release 脱敏、采样、离线队列和用户授权，不上传原始请求正文。

设备测试已经通过可替换 `AppDependencies` 使用确定性仓库；接入项目应在自己的 CI 设备上运行同一套单、多 Activity 流程。CI 厂商、签名密钥和性能设备仍由产品仓库配置。

不要在模板里预置无法在所有产品上安全成立的 Pin、密钥、数据库、后台任务或性能阈值；这些必须由真实后端、设备基线和发布流程决定。

本仓库已经存在 Navigation 3、多 Activity 与系统播放器边界，因此保留设备烟测源码。普通 `verify_scaffold.sh` 负责编译测试 APK；有设备的本机或 CI 使用 `verify_device_tests.sh` 执行。CI 平台、设备型号和性能阈值仍由接入项目决定，避免模板绑定 GitHub Actions、GitLab CI 或某一家云真机。
