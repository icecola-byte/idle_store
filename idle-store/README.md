# Idle Store 后端模块说明

本文只介绍 `idle-store` 后端工程，重点说明每个 Maven 模块负责什么、模块内部文件有什么作用，以及其他服务应该如何引入和使用。

## 1. 工程模块关系

```text
idle-store/
├── pom.xml
├── idle-store-framework/
│   ├── idle-store-common/
│   ├── idle-store-cache-contract/
│   ├── lh-spring-boot-starter-web/
│   ├── lh-spring-boot-starter-jackson/
│   ├── lh-spring-boot-starter-redis/
│   ├── lh-spring-boot-starter-logging/
│   ├── lh-spring-boot-starter-biz-context/
│   ├── lh-spring-boot-starter-biz-operationlog/
│   ├── lh-spring-boot-starter-thread-pool/
│   └── lh-spring-boot-starter-openfeign/
├── idle-store-gateway/
├── idle-store-auth/
├── idle-store-user/
│   ├── idle-store-user-api/
│   └── idle-store-user-biz/
├── idle-store-oss/
│   ├── idle-store-oss-api/
│   └── idle-store-oss-biz/
├── idle-store-distributed-id-generator/
│   ├── idle-store-distributed-id-generator-api/
│   └── idle-store-distributed-id-generator-biz/
├── idle-store-chat/
│   └── idle-store-chat-biz/
└── idle-store-data-align/
```

根 `pom.xml` 是聚合父工程，负责：

- 声明所有子模块；
- 统一 Java 17、Spring Boot 3.5.16、Spring Cloud 2025.0.0 等版本；
- 在 `dependencyManagement` 中管理公共依赖版本；
- 统一 Maven 编译和 Spring Boot 打包插件。

业务模块引用受父 POM 管理的依赖时通常不再写版本号。

> 如果某个子模块依赖比较老，和整体项目的依赖版本差距较大，可以单独在自己的模块里面声明自己的版本

## 2. Framework 公共模块

Framework 分成三类：

- `idle-store-common`：普通 Java 基础库，引入后不应该自动注册业务 Bean 或改变服务行为；
- `idle-store-cache-contract`：多个服务共同遵守的缓存 Key 和缓存数据契约；
- `lh-spring-boot-starter-*`：Spring Boot 功能模块，引入后通过自动配置提供 Bean 或统一行为。

当前业务模块的直接依赖关系如下，`-` 表示没有直接引入：

| 服务 | Cache Contract | Web | Jackson | Redis | Logging | Context | Operation Log | Thread Pool | OpenFeign |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Gateway | 是 | - | 是 | 是 | 是 | - | - | - | - |
| Auth | - | 是 | 是 | 是 | 是 | 是 | 是 | 是 | 是 |
| User Biz | 是 | 是 | 是 | 是 | 是 | 是 | 是 | - | 是 |
| OSS Biz | - | 是 | 是 | - | 是 | - | 是 | - | - |
| Chat Biz | - | 是 | 是 | 是 | 是 | 是 | 是 | - | 是 |
| Distributed ID Biz | - | - | - | - | 是 | - | - | - | - |
| Data Align | - | 是 | - | 是 | 是 | - | - | - | - |

这张表描述的是当前 POM，不代表新增服务必须照搬。是否引入某个 starter，应由服务是否使用该能力决定。

### 2.1 idle-store-common

Common 只放跨服务都能使用、与具体业务和运行环境无关的基础契约。

```text
idle-store-common/src/main/java/com/lh/framework/common/
├── constant/
│   ├── DateConstants.java
│   └── GlobalConstants.java
├── enums/
│   ├── DeletedEnum.java
│   └── StatusEnum.java
├── exception/
│   ├── BaseExceptionInterface.java
│   └── BizException.java
├── response/
│   └── Response.java
├── util/
│   ├── JsonUtils.java
│   └── ParamUtils.java
└── validation/
    ├── PhoneNumber.java
    └── PhoneNumberValidator.java
```

| 文件 | 作用 |
| --- | --- |
| `DateConstants` | 保存统一日期格式，例如 `yyyy-MM-dd HH:mm:ss` |
| `GlobalConstants` | 保存跨模块协议常量，目前包含用户 ID Header 名 `userId` |
| `DeletedEnum` | 统一逻辑删除值 |
| `StatusEnum` | 通用启用、禁用状态 |
| `BaseExceptionInterface` | 约束错误码枚举必须提供 `errorCode` 和 `errorMessage` |
| `BizException` | 业务异常，接收实现了 `BaseExceptionInterface` 的错误码 |
| `Response<T>` | Controller 和 Feign API 共用的统一响应包装 |
| `JsonUtils` | 提供静态 JSON 序列化入口，由 Jackson starter 注入 Spring 的 ObjectMapper |
| `ParamUtils` | 无状态参数校验工具 |
| `PhoneNumber` | 自定义手机号校验注解 |
| `PhoneNumberValidator` | `PhoneNumber` 的 Jakarta Validation 实现 |

业务模块使用示例：

```xml
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>idle-store-common</artifactId>
</dependency>
```

```java
@GetMapping("/{id}")
public Response<UserProfileRespVO> get(@PathVariable Long id) {
    return Response.success(userService.get(id));
}
```

```java
throw new BizException(UserResponseCodeEnum.USER_NOT_FOUND);
```

Common 不应该放 Controller、数据库实体、Mapper、Feign 客户端、线程池、过滤器或自动配置。某项功能如果会自动创建 Bean，应放进独立 starter。

### 2.2 idle-store-cache-contract

该模块保存多个服务共同使用的缓存协议，目前不依赖 Spring、Redis 或 Feign。

```text
idle-store-cache-contract/
├── pom.xml
└── src/main/java/com/lh/idlestore/cache/contract/
    └── auth/AuthCacheKeys.java
```

`AuthCacheKeys` 统一以下权限缓存 Key：

```text
user:roles:{userId}
role:permissions:{roleKey}
```

User 负责写入权限缓存，Gateway 负责读取，因此双方必须依赖同一份 Key 构造规则：

```xml
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>idle-store-cache-contract</artifactId>
</dependency>
```

```java
String userRolesKey = AuthCacheKeys.userRoles(userId);
String permissionsKey = AuthCacheKeys.rolePermissions(roleKey);
```

修改共享 Key 的前缀属于跨服务缓存协议变更，需要同时升级读写双方，并处理 Redis 中的旧数据。

只有一个服务使用的 Key 不放在这里。例如验证码 Key 位于 Auth 的 `VerificationCodeCacheKeys`。如果以后 Order、Chat 等服务产生共享缓存协议，应按业务域增加独立类，不要把所有 Key 堆进一个巨大的类。

### 2.3 lh-spring-boot-starter-web

该 starter 为 Servlet 业务服务提供统一 Web 异常处理。

```text
lh-spring-boot-starter-web/
├── autoconfigure/WebAutoConfiguration.java
├── enums/CommonResponseCodeEnum.java
├── exception/GlobalExceptionHandler.java
└── META-INF/spring/...
```

| 文件 | 作用 |
| --- | --- |
| `WebAutoConfiguration` | 自动导入全局异常处理器 |
| `CommonResponseCodeEnum` | 定义 `COMMON-10000` 系统错误、`COMMON-10001` 参数错误 |
| `GlobalExceptionHandler` | 处理 `BizException`、参数校验异常、非法参数和未知异常 |
| `AutoConfiguration.imports` | 告诉 Spring Boot 加载 `WebAutoConfiguration` |

引入后不需要再写 `@Import`：

```xml
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>lh-spring-boot-starter-web</artifactId>
</dependency>
```

Controller 仍由业务模块自己编写。Controller 返回 `Response<T>`，Service 只返回业务对象或 `void`。

### 2.4 lh-spring-boot-starter-jackson

该 starter 统一所有服务的 JSON 序列化规则。

```text
lh-spring-boot-starter-jackson/
├── autoconfigure/JacksonAutoConfiguration.java
└── META-INF/spring/...
```

`JacksonAutoConfiguration` 提供两个 Bean：

1. `Jackson2ObjectMapperBuilderCustomizer`：统一时区为 `Asia/Shanghai`，忽略未知字段和空 Bean，并配置日期格式；
2. `SmartInitializingSingleton`：Spring 初始化完成后，将统一的 `ObjectMapper` 注入 `JsonUtils`。

当前日期格式：

| 类型 | 格式 |
| --- | --- |
| `LocalDateTime` | `yyyy-MM-dd HH:mm:ss` |
| `LocalDate` | `yyyy-MM-dd` |
| `LocalTime` | `HH:mm:ss` |

使用方式只需添加依赖，不需要业务模块再声明 `ObjectMapper`：

```xml
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>lh-spring-boot-starter-jackson</artifactId>
</dependency>
```

业务中需要手动转 JSON 时使用：

```java
String payload = JsonUtils.toJsonString(event);
```

### 2.5 lh-spring-boot-starter-redis

该 starter 只提供 Redis 技术能力，不保存验证码、权限、订单等业务 Key。

```text
lh-spring-boot-starter-redis/
├── autoconfigure/RedisTemplateAutoConfiguration.java
└── META-INF/spring/...
```

| 文件 | 作用 |
| --- | --- |
| `RedisTemplateAutoConfiguration` | 在 Boot 默认 Redis 配置之前创建项目自己的 `redisTemplate` |

序列化规则：

- Key 和 Hash Key 使用字符串；
- Value 和 Hash Value 使用 Jackson JSON。

业务服务需要同时引入 starter，并从 Nacos 导入 Redis 连接配置：

```xml
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>lh-spring-boot-starter-redis</artifactId>
</dependency>
```

```yaml
spring:
  config:
    import:
      - nacos:shared-redis.yaml?group=DEFAULT_GROUP&refreshEnabled=true
```

```java
@Resource
private RedisTemplate<String, Object> redisTemplate;

String key = VerificationCodeCacheKeys.verificationCode(phone);
redisTemplate.opsForValue().set(key, code, 3, TimeUnit.MINUTES);
```

上例中的 `VerificationCodeCacheKeys` 属于 Auth 服务，并不是 Redis starter 的内容。共享权限 Key 则来自 `idle-store-cache-contract`。

Gateway、Auth、User 和 Chat 当前引入了 Redis starter。是否真正需要 Redis，应以该服务是否读写 Redis 为准。

### 2.6 lh-spring-boot-starter-logging

该 starter 只有统一的 `logback-spring.xml`，不包含 Java 自动配置。

引入依赖后，Spring Boot 自动发现 starter 内的日志配置：

```xml
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>lh-spring-boot-starter-logging</artifactId>
</dependency>
```

日志行为：

| 环境 | 控制台 | 文件 |
| --- | --- | --- |
| `dev` | 普通文本 | 同步滚动文件 |
| `prod` | JSON | 异步滚动文件 |
| 其他 | WARN 文本 | 不写文件 |

默认日志路径为：

```text
./logs/${spring.application.name}/${spring.application.name}-yyyy-MM-dd-i.log
```

每个文件最大 10 MB，保留 30 天。需要改变根路径时配置：

```yaml
logging:
  file:
    path: ./logs
```

业务模块不再各自维护 `logback-spring.xml`，否则会覆盖统一规则。

### 2.7 lh-spring-boot-starter-biz-context

该 starter 解决“业务服务如何获取当前登录用户，以及 Feign 下一跳如何继续携带用户身份”的问题。

```text
lh-spring-boot-starter-biz-context/
├── autoconfigure/
│   ├── ContextAutoConfiguration.java
│   └── FeignContextAutoConfiguration.java
├── filter/UserContextFilter.java
├── holder/LoginUserContextHolder.java
├── interceptor/FeignRequestInterceptor.java
└── META-INF/spring/...
```

| 文件 | 作用 |
| --- | --- |
| `ContextAutoConfiguration` | 自动注册 Servlet `UserContextFilter` |
| `FeignContextAutoConfiguration` | 自动注册 Feign 请求拦截器 |
| `UserContextFilter` | 从 `userId` Header 读取用户 ID，写入上下文，并在 `finally` 清理 |
| `LoginUserContextHolder` | 基于 `TransmittableThreadLocal` 保存和读取用户 ID |
| `FeignRequestInterceptor` | Feign 调用前把当前上下文中的用户 ID继续写入 Header |

完整流程：

```text
客户端 Token
  -> Gateway 校验 Token
  -> Gateway 删除客户端伪造的 userId
  -> Gateway 写入可信 userId Header
  -> UserContextFilter 写入 LoginUserContextHolder
  -> Service 使用 LoginUserContextHolder.getUserId()
  -> FeignRequestInterceptor 向下游继续透传
  -> 请求结束清理 ThreadLocal
```

业务服务使用：

```xml
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>lh-spring-boot-starter-biz-context</artifactId>
</dependency>
```

```java
Long userId = LoginUserContextHolder.getUserId();
if (userId == null) {
    throw new IllegalStateException("用户上下文缺失");
}
```

不需要业务模块自己注册 Filter 或 Feign 拦截器。这个 starter 面向 Servlet 服务，不能直接用于 WebFlux Gateway。

它负责传递上下文，但不等于完整的服务间认证。内部敏感接口仍需由网关路径拦截、网络策略或服务间鉴权保护。

### 2.8 lh-spring-boot-starter-biz-operationlog

该 starter 用 AOP 统一记录接口描述、耗时、请求和响应，并在写日志前脱敏。

```text
lh-spring-boot-starter-biz-operationlog/
├── annotation/ApiOperationLog.java
├── aspect/ApiOperationLogAspect.java
├── autoconfigure/ApiOperationLogAutoConfiguration.java
├── support/OperationLogSanitizer.java
└── META-INF/spring/...
```

| 文件 | 作用 |
| --- | --- |
| `ApiOperationLog` | 标记需要记录操作日志的 Controller 方法 |
| `ApiOperationLogAspect` | 环绕执行方法，记录开始、结果、异常类型和耗时 |
| `OperationLogSanitizer` | 递归处理对象和集合中的敏感字段 |
| `ApiOperationLogAutoConfiguration` | 自动创建 Sanitizer 和 Aspect |

默认会隐藏密码、验证码、Token、AccessKey、消息内容、真实姓名和地址等字段，手机号只保留前三位和后四位。

```xml
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>lh-spring-boot-starter-biz-operationlog</artifactId>
</dependency>
```

```java
@ApiOperationLog(description = "更新当前登录用户资料")
@PutMapping
public Response<UserProfileRespVO> update(
        @Valid @RequestBody UpdateUserInfoReqVO request) {
    return Response.success(userService.updateUserInfo(request));
}
```

登录、验证码等高敏感接口应关闭载荷日志：

```java
@ApiOperationLog(
        description = "小程序登录",
        logRequest = false,
        logResponse = false
)
```

该 starter 不会自动记录所有 Controller，只有显式添加注解的方法才记录。

### 2.9 lh-spring-boot-starter-thread-pool

该 starter 提供“多线程池配置模型 + 创建工厂”，但不会根据 YAML 自动创建所有业务线程池。

```text
lh-spring-boot-starter-thread-pool/
├── autoconfigure/ThreadPoolAutoConfiguration.java
├── core/ThreadPoolExecutorFactory.java
├── properties/ThreadPoolsProperties.java
└── META-INF/spring/...
```

| 文件 | 作用 |
| --- | --- |
| `ThreadPoolsProperties` | 绑定 `thread-pools.executors` 下的多组线程池参数 |
| `ThreadPoolExecutorFactory` | 校验参数并创建 `ThreadPoolTaskExecutor` |
| `ThreadPoolAutoConfiguration` | 自动提供 Properties 和 Factory Bean |

可配置参数：

| 配置 | 默认值 | 含义 |
| --- | ---: | --- |
| `core-pool-size` | 5 | 核心线程数 |
| `max-pool-size` | 20 | 最大线程数 |
| `queue-capacity` | 100 | 等待队列容量 |
| `keep-alive-seconds` | 60 | 非核心线程空闲时间 |
| `await-termination-seconds` | 60 | 停机等待任务完成时间 |
| `thread-name-prefix` | 无 | 线程名前缀，建议业务必填 |
| `rejection-policy` | `caller-runs` | 拒绝策略 |

支持 `caller-runs`、`abort`、`discard`、`discard-oldest` 四种拒绝策略。

Auth 的完整使用方式如下。

第一步，引入 starter：

```xml
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>lh-spring-boot-starter-thread-pool</artifactId>
</dependency>
```

第二步，配置一组业务线程池：

```yaml
thread-pools:
  executors:
    sms: # 一个业务可能用多个线程池组，这是其中线程名字
      core-pool-size: 10
      max-pool-size: 50
      queue-capacity: 200
      keep-alive-seconds: 30
      await-termination-seconds: 60
      thread-name-prefix: AuthSms-
      rejection-policy: caller-runs
```

第三步，业务模块决定 Bean 名称并创建线程池：

```java
@Configuration(proxyBeanMethods = false)
public class AuthThreadPoolConfiguration {

    @Bean
    public ThreadPoolTaskExecutor smsExecutor(
            ThreadPoolsProperties properties,
            ThreadPoolExecutorFactory factory) {
        return factory.create(properties.getRequired("sms")); // 这里放入名字
    }
}
```

这里的 `sms` 是 YAML 配置项名称，`smsExecutor` 是 Spring Bean 名称，两者职责不同。一个负责找到参数，一个负责业务代码按名称注入。

第四步，业务中注入并提交任务：

```java
@Resource(name = "smsExecutor")
private ThreadPoolTaskExecutor smsExecutor;

smsExecutor.submit(() -> smsSender.send(...));
```

新增另一个线程池时，在 YAML 增加新配置，再在当前业务模块声明对应 Bean。starter 不自动创建 Bean，是为了避免它猜测线程池的业务名称、用途和生命周期。

### 2.10 lh-spring-boot-starter-openfeign

该 starter 不负责声明 Feign 接口，也不负责启用 `@EnableFeignClients`。它负责统一检查 Feign 调用结果和转换调用异常。

```text
lh-spring-boot-starter-openfeign/
├── autoconfigure/OpenFeignAutoConfiguration.java
├── core/RemoteCallExecutor.java
├── exception/
│   ├── RemoteCallException.java
│   ├── RemoteBusinessException.java
│   ├── RemoteProtocolException.java
│   └── RemoteTransportException.java
└── META-INF/spring/...
```

| 文件 | 作用 |
| --- | --- |
| `OpenFeignAutoConfiguration` | 自动创建 `RemoteCallExecutor` Bean |
| `RemoteCallExecutor` | 执行调用、记录服务/操作/耗时、校验 `Response<T>` |
| `RemoteCallException` | 所有统一远程调用异常的父类 |
| `RemoteBusinessException` | 下游正常响应，但 `success=false` |
| `RemoteProtocolException` | 下游响应为空或必需的 data 为空 |
| `RemoteTransportException` | Feign 网络、超时或 HTTP 调用异常 |

三种调用方法：

```java
// 查询详情、创建后返回 ID：成功时 data 必须存在
T required(...);

// 查不到是合法结果：返回 Optional<T>
Optional<T> optional(...);

// 删除、通知等只关心成功状态的操作
void execute(...);
```

一个服务调用 User API 的完整步骤：

第一步，依赖 User API 和 OpenFeign starter：

```xml
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>idle-store-user-api</artifactId>
</dependency>
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>lh-spring-boot-starter-openfeign</artifactId>
</dependency>
```

第二步，在启动类明确启用需要的客户端：

```java
@SpringBootApplication
@EnableFeignClients(clients = {UserFeignApi.class})
public class IdleStoreAuthApplication {
}
```

第三步，在 `remote` 包封装调用，不在核心 Service 里直接处理 Feign：

```java
@Component
@RequiredArgsConstructor
public class UserRemoteService {

    private final UserFeignApi userFeignApi;
    private final RemoteCallExecutor remoteCallExecutor;

    public UserBriefResponse findById(Long userId) {
        try {
            return remoteCallExecutor.required(
                    UserApiConstants.SERVICE_NAME,
                    UserApiConstants.OPERATION_FIND_BY_ID, // 这两个都是在 user-api 定义的，用来做日志，好像不推荐去掉，也不推荐用反射，，，但是变麻烦了
                    () -> userFeignApi.findById(userId)
            );
        } catch (RemoteBusinessException exception) {
            // 将可识别的下游业务错误转换为当前服务语义
            throw new BizException(ChatResponseCodeEnum.TARGET_USER_NOT_FOUND);
        } catch (RemoteCallException exception) {
            // 协议或网络错误统一转换为当前服务调用失败
            throw new BizException(ChatResponseCodeEnum.USER_SERVICE_CALL_FAILED);
        }
    }
}
```

第四步，从 Nacos 导入超时配置：

```yaml
spring:
  config:
    import:
      - nacos:shared-openfeign.yaml?group=DEFAULT_GROUP&refreshEnabled=true # openfeign 的调用超时、重试配置
```

`service` 和 `operation` 参数不是用来找 Feign 客户端，而是用于日志和异常上下文。真正调用哪个服务由 `@FeignClient(name=...)` 决定。

配置文件

```yaml
spring:
  cloud:
    openfeign:
      client:
        # 允许 Nacos 刷新 Feign 的 Request.Options。
        refresh-enabled: true

        config:
          # 所有没有单独配置的 FeignClient 默认使用这里的超时。
          default:
            connect-timeout: 2000
            read-timeout: 5000
            logger-level: basic

          # User 服务主要是普通数据库查询，5 秒已经比较宽松。
          idle-store-user:
            connect-timeout: 2000
            read-timeout: 5000

          # 分布式 ID 应该快速返回，不能长时间阻塞消息发送。
          idle-store-distributed-id-generator:
            connect-timeout: 1000
            read-timeout: 3000

          # 文件上传耗时明显更长，需要单独放宽读取时间。
          idle-store-oss:
            connect-timeout: 3000
            read-timeout: 30000
```

## 3. API 模块

API 模块是服务间调用契约包。调用方只依赖下游的 API，不能依赖下游 Biz。

API 模块应该放：

- `@FeignClient` 接口；
- 服务名、路径和 operation 常量；
- 服务间 Request、Response；
- 调用方需要识别的公开错误码；
- 该 Feign 客户端专用的编码配置。

API 模块不放 Controller、Service 实现、Mapper、DO、数据库配置和应用启动类。

### 3.1 idle-store-user-api

```text
idle-store-user-api/
├── api/UserFeignApi.java
├── constant/UserApiConstants.java
├── dto/request/
│   ├── MiniLoginRequest.java
│   ├── FindUserByPhoneRequest.java
│   └── BatchFindUsersRequest.java
├── dto/response/
│   ├── MiniLoginResponse.java
│   ├── FindUserByPhoneResponse.java
│   └── UserBriefResponse.java
└── enums/UserResponseCodeEnum.java
```

| 文件 | 作用 |
| --- | --- |
| `UserFeignApi` | 声明登录/注册、按手机号、按 ID、批量查询用户接口 |
| `UserApiConstants` | 保存 `idle-store-user`、`/user/internal` 和操作名称 |
| `dto/request` | 调用 User 时传入的稳定请求契约 |
| `dto/response` | User 返回给其他服务的数据，不包含客户端页面专属字段 |
| `UserResponseCodeEnum` | User 对外公开的稳定错误码，供调用方精确识别 |

当前调用方：

- Auth 引入 User API，调用登录/注册和手机号查询；
- Chat 引入 User API，查询用户公开信息；
- User Biz 自己也依赖 User API，由 `UserInternalController implements UserFeignApi` 实现契约。

内部路径 `/user/internal/**` 被 Gateway 屏蔽，外部客户端不能经网关访问。

### 3.2 idle-store-oss-api

```text
idle-store-oss-api/
├── api/FileFeignApi.java
├── constant/OssApiConstants.java
└── config/FeignFormConfiguration.java
```

| 文件 | 作用 |
| --- | --- |
| `FileFeignApi` | 声明 multipart 文件上传接口 |
| `OssApiConstants` | 保存服务名、文件接口前缀和 operation 名称 |
| `FeignFormConfiguration` | 为文件上传客户端提供 `SpringFormEncoder` |

当前由 User Biz 引入，用于上传用户头像：

```java
@EnableFeignClients(clients = {FileFeignApi.class})
```

`FileFeignApi` 在 `@FeignClient(configuration=...)` 中绑定表单编码配置，调用方不需要手动创建 Encoder。

### 3.3 idle-store-distributed-id-generator-api

```text
idle-store-distributed-id-generator-api/
├── api/DistributedIdGeneratorFeignApi.java
└── constant/DistributedIdGeneratorApiConstants.java
```

| 文件 | 作用 |
| --- | --- |
| `DistributedIdGeneratorFeignApi` | 声明 Segment 和 Snowflake ID 获取接口 |
| `DistributedIdGeneratorApiConstants` | 保存服务名、`/id` 路径和 operation 名称 |

当前由 Chat Biz 引入：

```java
@EnableFeignClients(clients = {
        DistributedIdGeneratorFeignApi.class,
        UserFeignApi.class
})
```

当前 ID API 是 Leaf 适配接口，直接返回 `String`，没有使用统一 `Response<T>`，因此 Chat 的 `DistributedIdGeneratorRemoteService` 自己解析 Long 并转换异常。这是当前项目中的协议例外。

## 4. Biz 业务模块

Biz 是可运行的 Spring Boot 应用，负责实现业务、事务、持久化和第三方集成。

通用目录含义：

```text
controller/       HTTP 入站接口，只做校验、转换和响应包装
service/          业务能力接口
service/impl/     业务流程和事务实现
remote/           下游 API 调用适配
repository/       Mapper、DO、Entity 和查询投影
model/vo/         面向客户端的请求与响应
model/command/    服务内部执行命令
model/event/      MQ/领域事件载荷
config/           当前服务专用配置
integration/      第三方 SDK 适配
constant/         当前服务常量
enums/            类型、状态和错误码
```

### 4.1 idle-store-gateway

Gateway 是 WebFlux 网关，不是普通 Servlet Biz 服务。

| 目录/文件 | 作用 |
| --- | --- |
| `IdleStoreGatewayApplication` | 网关启动入口 |
| `application.yml` | 配置 Auth、User、OSS、Chat 和 WebSocket 路由 |
| `auth/SaTokenConfiguration` | Sa-Token 登录、角色和权限规则 |
| `auth/StpInterfaceImpl` | 从 Redis 获取用户角色和权限 |
| `filter/UserIdHeaderFilter` | 清理伪造 Header，并写入真实用户 ID |
| `filter/InternalEndpointBlockFilter` | 阻止内部接口经网关暴露 |
| `config/GatewayConfigProperties` | 绑定需要屏蔽的内部路径前缀 |
| `exception/GlobalExceptionHandler` | 处理 WebFlux 网关异常并设置正确 HTTP 状态 |

Gateway 使用 Redis、Jackson、Logging starter，但不使用 Servlet Web 和 Biz Context starter。

### 4.2 idle-store-auth

Auth 负责登录、登出、验证码和短信，不直接读写用户表。

| 目录/文件 | 作用 |
| --- | --- |
| `IdleStoreAuthApplication` | 启动服务并启用 `UserFeignApi` |
| `controller/AuthController` | 小程序/管理端登录和登出入口 |
| `controller/VerificationCodeController` | 验证码发送入口 |
| `service/AuthService` | 认证业务接口 |
| `service/impl/AuthServiceImpl` | 验证码校验、User 调用和 Sa-Token 登录 |
| `service/VerificationCodeService` | 验证码业务接口 |
| `service/impl/VerificationCodeServiceImpl` | 生成验证码、提交短信任务、操作 Redis |
| `remote/UserRemoteService` | 封装 User Feign 调用与错误转换 |
| `integration/sms` | 阿里云短信配置、客户端和发送适配器 |
| `config/AuthThreadPoolConfiguration` | 创建 `smsExecutor` |
| `config/thread-pools.yml` | 短信线程池参数 |

Auth 引入 User API、Redis、Web、Context、Operation Log、Thread Pool、OpenFeign、Jackson 和 Logging starter。

### 4.3 idle-store-user-biz

User 是用户、角色和权限数据的所有者。

| 目录/文件 | 作用 |
| --- | --- |
| `IdleStoreUserApplication` | 启动服务并启用 `FileFeignApi` |
| `controller/UserController` | 客户端用户资料接口 |
| `controller/UserInternalController` | 实现 `UserFeignApi`，仅供服务间调用 |
| `service/UserService` | 用户领域能力接口 |
| `service/impl/UserServiceImpl` | 注册、资料、角色关系和查询逻辑 |
| `remote/OssRemoteService` | 调用 OSS 上传头像并转换远程异常 |
| `repository/dataobject` | User、Role、Permission 等 MySQL 表对象 |
| `repository/mapper` | MyBatis Plus Mapper |
| `initializer` | 启动时初始化角色权限缓存 |
| `model/vo` | 客户端用户资料请求和响应 |

User Biz 依赖自己的 User API，是因为它需要实现其中的 Feign 契约；它还依赖 OSS API，但不能依赖 OSS Biz。

### 4.4 idle-store-oss-biz

OSS 封装具体文件存储厂商。

| 目录/文件 | 作用 |
| --- | --- |
| `IdleStoreOssApplication` | 文件服务启动入口 |
| `controller/FileController` | 实现文件上传 HTTP 接口 |
| `service/FileService` | 文件业务接口 |
| `service/impl/FileServiceImpl` | 根据配置选择策略并执行上传 |
| `strategy/FileStrategy` | 文件存储统一接口 |
| `strategy/impl/MinioFileStrategy` | MinIO 实现 |
| `strategy/impl/AliyunOssFileStrategy` | 阿里云 OSS 实现 |
| `factory/FileStrategyFactory` | 收集并按配置选择策略 |
| `config/StorageProperties` | 绑定当前存储策略 |
| `config/MinioProperties` | 绑定 MinIO 配置 |
| `config/AliyunOssProperties` | 绑定阿里云 OSS 配置 |

存储地址和密钥从 Nacos 的 `file-storage.yaml` 读取。业务调用方只看到 `FileFeignApi`，不知道底层厂商。

### 4.5 idle-store-chat-biz

Chat 负责会话、消息、未读数、系统通知和实时推送。

| 目录 | 作用 |
| --- | --- |
| `controller` | 创建会话、发送消息、分页查询和已读接口 |
| `service/impl` | 会话与消息核心业务 |
| `remote` | 调用 User API 和 ID API |
| `repository/mysql` | 会话、成员、Outbox 的 DO、Mapper 和查询投影 |
| `repository/cassandra` | 消息主键、实体和 Cassandra Repository |
| `model/event` | 序列化到 Outbox 和 RocketMQ 的事件 |
| `model/command` | 系统通知内部命令 |
| `model/ws` | WebSocket 推送模型 |
| `mq/producer/ChatOutboxPublisher` | 扫描 Outbox、投递 MQ、失败退避重试 |
| `mq/consumer/ChatMessageCreatedConsumer` | 消费事件、写 Cassandra、触发 WebSocket 推送 |
| `websocket` | 握手、连接注册、消息处理和在线推送 |

消息发送时，MySQL 会话更新、未读数和 Outbox 事件在同一事务提交。Outbox 再异步投递 RocketMQ，消费者把正文写入 Cassandra。这样避免本地事务成功但 MQ 消息丢失。

Chat 依赖 User API、Distributed ID API、OpenFeign、Context、Web、Redis、Jackson、Operation Log 和 Logging starter。

### 4.6 idle-store-distributed-id-generator-biz

该模块封装 Leaf 的 Segment 与 Snowflake 实现。

| 目录 | 作用 |
| --- | --- |
| `controller/LeafController` | 实现 ID API 的 HTTP 端点 |
| `service` | 对 Leaf 结果进行业务封装 |
| `core/segment` | 基于 MySQL 号段生成 ID |
| `core/snowflake` | 基于 ZooKeeper 节点协调生成 ID |
| `config/LeafProperties` | 绑定 `leaf` 配置 |
| `constant` | Leaf 状态和固定值 |
| `exception` | ID 服务内部异常 |

Biz 依赖自己的 ID API 来保持接口路径一致。Leaf 核心实现保留在 `core` 中，其他业务服务不能直接依赖这些类。

### 4.7 idle-store-data-align

Data Align 是 XXL-JOB 执行器，用于数据维护，不承载普通在线业务请求。

| 目录/文件 | 作用 |
| --- | --- |
| `IdleStoreDataAlignApplication` | 任务服务启动入口 |
| `config/XxlJobProperties` | 绑定执行器配置 |
| `config/XxlJobConfiguration` | 创建 XXL-JOB Executor |
| `job/CreateTableXxlJob` | 注册 `createTableJobHandler` 任务 |
| `domain/dataobject` | 任务涉及的数据表对象 |
| `domain/mapper` | 数据维护 SQL 访问 |

XXL-JOB 地址、Token、执行器名称等配置从 Nacos 的 `data-align.yaml` 获取。

## 5. 配置文件如何分工

每个服务的配置分成三层：

```text
application.yml       应用名、端口、默认 profile、稳定路由等
application-dev.yml   dev Nacos 地址、namespace 和 Data ID 导入
application-prod.yml  prod Nacos 地址、namespace 和 Data ID 导入
Nacos Data ID         数据库、Redis、密钥、超时等环境配置
```

| Data ID | 主要使用者 | 内容 |
| --- | --- | --- |
| `shared-mysql.yaml` | User、Chat、Data Align | MySQL 和连接池 |
| `shared-redis.yaml` | Gateway、Auth、User | Redis 连接 |
| `shared-cassandra.yaml` | Chat | Cassandra 和 Keyspace |
| `shared-openfeign.yaml` | Auth、User、Chat | Feign 连接和读取超时 |
| `auth-secrets.yaml` | Auth | 阿里云短信密钥 |
| `file-storage.yaml` | OSS | 存储策略、MinIO/OSS 地址和密钥 |
| `id-generator.yaml` | Distributed ID | Leaf Segment/Snowflake |
| `data-align.yaml` | Data Align | XXL-JOB 执行器 |

开发 namespace 为 `idlestore-dev`，生产 namespace 为 `idlestore-prod`。数据库密码、AccessKey、SecretKey 等敏感值不能提交到代码库。

## 6. 新模块应该怎样接入

### 新增普通 Servlet 业务服务

通常按需引入：

```xml
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>lh-spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>lh-spring-boot-starter-jackson</artifactId>
</dependency>
<dependency>
    <groupId>com.lh</groupId>
    <artifactId>lh-spring-boot-starter-logging</artifactId>
</dependency>
```

需要登录用户上下文时增加 `biz-context`；需要 Redis 时增加 `redis`；需要操作日志时增加 `biz-operationlog`；需要远程调用时增加下游 API 和 `openfeign`；需要业务线程池时增加 `thread-pool`。

不要为了统一一次性引入全部 starter，服务只引入自己真正使用的能力。

### 新增一个可被其他服务调用的领域

```text
idle-store-order/
├── idle-store-order-api/
│   ├── OrderFeignApi
│   ├── OrderApiConstants
│   ├── dto/request
│   ├── dto/response
│   └── OrderResponseCodeEnum
└── idle-store-order-biz/
    ├── OrderInternalController implements OrderFeignApi
    ├── controller
    ├── service
    ├── repository
    └── model/vo
```

调用方只添加 `idle-store-order-api`，启动类通过 `@EnableFeignClients(clients = OrderFeignApi.class)` 启用，再在自己的 `remote` 包封装调用。

## 7. 本地构建与阅读顺序

```bash
cd idle-store
mvn clean test
```

只构建单个服务及其依赖：

```bash
mvn -pl idle-store-auth -am clean package
```

建议第一次阅读按以下顺序：

1. 根 `pom.xml`，看所有 Maven 模块；
2. `idle-store-common`，看统一响应和异常；
3. `biz-context`、`openfeign`、`thread-pool`，理解公共能力如何提供；
4. `idle-store-user-api`，理解 API 契约包；
5. Auth 的启动类和 `UserRemoteService`，看调用方如何使用 API；
6. User 的 `UserInternalController`，看提供方如何实现 API；
7. Chat 的 Outbox Publisher 和 MQ Consumer，理解异步消息链路；
8. 各服务的 `application-dev.yml`，确认依赖哪些 Nacos Data ID。

核心规则只有三条：`common` 不自动改变服务行为，starter 只提供可复用技术能力，API 只暴露稳定服务契约。业务实现、数据和第三方细节始终留在各自 Biz 模块中。
