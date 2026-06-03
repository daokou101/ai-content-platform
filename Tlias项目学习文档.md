# Tlias 智能学习辅助系统 — 项目学习文档

---

> **写给初学者的你：**
> 这份文档从零开始解释了项目的每一个部分。如果你有看不懂的地方，大概率是因为跳过了前面的章节。建议按顺序阅读。

---

## 目录

1. [项目整体架构](#1-项目整体架构)
2. [后端项目结构详解](#2-后端项目结构详解)
3. [请求调用链分析](#3-请求调用链分析)
4. [注解大全（附解释）](#4-注解大全附解释)
5. [后端核心类详解](#5-后端核心类详解)
6. [前端项目结构详解](#6-前端项目结构详解)
7. [前端核心概念和函数详解](#7-前端核心概念和函数详解)
8. [企业级功能详解](#8-企业级功能详解)
   - [8.1 登录功能（JWT）](#81-登录功能jwt)
   - [8.2 拦截器（Interceptor）](#82-拦截器interceptor)
   - [8.3 AOP 操作日志（Aspect）](#83-aop-操作日志aspect)
   - [8.4 全局异常处理](#84-全局异常处理)
   - [8.5 启动监听器](#85-启动监听器)
   - [8.6 文件上传](#86-文件上传)
9. [前后端数据交互格式](#9-前后端数据交互格式)
10. [从零开始运行项目](#10-从零开始运行项目)

---

## 1. 项目整体架构

### 1.1 技术栈

| 层次 | 技术 | 说明 |
|------|------|------|
| **前端** | Vue 3 + Vite | 用于构建用户界面 |
| **前端UI** | Element Plus | 基于 Vue 3 的组件库（表格、对话框、按钮等） |
| **前端状态管理** | Pinia | 管理全局状态（如登录信息） |
| **前端路由** | Vue Router | 管理页面间的跳转 |
| **HTTP 客户端** | Axios | 前端发送 HTTP 请求到后端 |
| **后端框架** | Spring Boot 4.0 | 提供 RESTful API |
| **数据库** | MySQL | 存储数据 |
| **ORM 框架** | MyBatis | 简化数据库操作（通过注解或 XML 写 SQL） |
| **分页插件** | PageHelper | 自动给 SQL 加 LIMIT 实现分页 |
| **认证方式** | JWT | 用户登录后生成令牌，校验身份 |

### 1.2 项目目录结构

```
E:\aSpringBoot_start01_0525\
├── tilasdemo/                      # 后端项目（Spring Boot）
│   ├── pom.xml                     # 依赖配置文件
│   ├── database.sql                # 数据库建表脚本
│   └── src/main/
│       ├── java/com/pzhu/
│       │   ├── TilasdemoApplication.java    # 启动类
│       │   ├── controller/         # 控制层（接收请求）
│       │   ├── service/            # 业务层接口
│       │   ├── service/impl/       # 业务层实现
│       │   ├── dao/                # 数据访问层（MyBatis）
│       │   ├── pojo/               # 实体类（数据模型）
│       │   ├── interceptor/        # 拦截器
│       │   ├── config/             # 配置类
│       │   ├── aspect/             # AOP 切面
│       │   ├── listener/           # 监听器
│       │   └── utils/              # 工具类
│       └── resources/
│           ├── application.yml     # 配置文件
│           └── mapper/             # MyBatis XML映射文件
│
└── vue-tlias-management/           # 前端项目（Vue 3）
    ├── package.json                # Node.js 依赖配置
    ├── vite.config.js              # Vite 构建配置
    └── src/
        ├── main.js                 # 前端入口文件
        ├── App.vue                 # 根组件
        ├── api/                    # API 接口封装
        ├── router/                 # 路由配置
        ├── stores/                 # Pinia 状态管理
        ├── utils/                  # 工具类（Axios）
        └── views/                  # 页面组件
            ├── layout/             # 布局组件
            ├── login/              # 登录页
            ├── dept/               # 部门管理
            ├── emp/                # 员工管理
            └── ...
```

### 1.3 三层架构（后端）

后端采用经典的三层架构：

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  Controller   │ ──▶  │   Service    │ ──▶  │     DAO      │
│  （控制层）   │      │  （业务层）   │      │  （数据层）   │
│               │      │              │      │              │
│ 接收请求      │      │ 业务逻辑处理  │      │ 数据库操作   │
│ 返回响应      │      │ 调用DAO      │      │ 执行SQL      │
└──────────────┘      └──────────────┘      └──────────────┘
                                                      │
                                                      ▼
                                                 ┌──────────┐
                                                 │  MySQL   │
                                                 │  数据库   │
                                                 └──────────┘
```

**调用链：** 前端请求 → Controller（接收）→ Service（处理业务）→ DAO（操作数据库）→ 数据库

---

## 2. 后端项目结构详解

### 2.1 各包的作用

| 包名 | 作用 | 类比 |
|------|------|------|
| `controller` | 接收 HTTP 请求，调用 Service，返回结果 | 餐厅"服务员"—— 接单、传菜 |
| `service` | 业务逻辑处理 | 餐厅"厨师"—— 真正做菜 |
| `dao` | 数据库操作（MyBatis） | 餐厅"采购员"—— 拿食材 |
| `pojo` | 数据模型（对应数据库表） | 餐厅"菜单"—— 数据格式 |
| `interceptor` | 请求拦截校验（如 JWT 验证） | 小区"保安"—— 检查通行证 |
| `config` | 配置类（注册拦截器、静态资源映射等） | 餐厅"规章制度" |
| `aspect` | AOP 切面（如自动记录日志） | 餐厅"摄像头"—— 自动记录 |
| `listener` | 监听事件（如应用启动事件） | 餐厅"开门提醒" |
| `utils` | 工具类（如 JWT 生成解析） | 餐厅"工具箱" |

### 2.2 项目配置文件（application.yml）

`application.yml` 是 Spring Boot 的核心配置文件，相当于项目的"控制面板"：

```yaml
# 各部分的作用：
spring.datasource     → 配置数据库连接（告诉应用数据库在哪）
spring.servlet.multipart → 文件上传限制
mybatis              → MyBatis 配置（SQL日志、驼峰命名转换）
file.upload-path     → 自定义配置（文件保存路径）
```

---

## 3. 请求调用链分析

以"查询所有部门"为例，完整的调用过程：

### 3.1 调用链图示

```
用户点击"部门管理"
        │
        ▼
┌─────────────────────────────────────────────────────┐
│                    前端部分                          │
│                                                      │
│ dept/index.vue                                       │
│   └─ search() 函数                                   │
│       └─ queryAllDeptApi()  ← api/dept.js            │
│           └─ request.get('/depts')  ← utils/request.js│
│               └─ Axios 发送 HTTP GET 请求             │
│                                                    │
│ 【请求拦截器】自动添加 token 到请求头                  │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP 请求
                       ▼
┌─────────────────────────────────────────────────────┐
│                    后端部分                          │
│                                                      │
│ 【拦截器】LoginInterceptor.preHandle()               │
│   └─ 检查 token 是否有效                             │
│       └─ 无效 → 返回 401                             │
│       └─ 有效 → 放行                                 │
│                                                      │
│ 【日志切面】LogAspect.recordLog()                     │
│   └─ 方法执行前：记录开始时间、参数                   │
│                                                      │
│ DeptController.selectAll()                            │
│   └─ 调用 DeptService.selectAll()                    │
│       └─ 调用 DeptDao.selectAll()                    │
│           └─ 执行 SQL: SELECT * FROM dept            │
│               └─ 返回 List<Dept>                     │
│                                                      │
│ 【日志切面】LogAspect.recordLog()                     │
│   └─ 方法执行后：计算耗时、保存日志到 operate_log 表  │
│                                                      │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP 响应（JSON）
                       ▼
┌─────────────────────────────────────────────────────┐
│                    前端部分                          │
│                                                      │
│ 【响应拦截器】检查状态码                             │
│   └─ 401 → 跳转到登录页                              │
│   └─ 成功 → 返回数据                                │
│                                                      │
│ dept/index.vue                                       │
│   └─ result.data → deptList.value                    │
│       └─ 页面自动更新，显示部门列表                  │
└─────────────────────────────────────────────────────┘
```

### 3.2 一句话总结

**前端发请求 → Axios拦截器加token → 后端拦截器验token → AOP记日志 → Controller收请求 → Service处理业务 → DAO查数据库 → 原路返回**

---

## 4. 注解大全（附解释）

### 4.1 Spring Boot 核心注解

| 注解 | 位置 | 作用 | 通俗解释 |
|------|------|------|----------|
| `@SpringBootApplication` | 启动类 | 标记为 Spring Boot 应用入口 | "这是大门的钥匙" |
| `@Configuration` | 配置类 | 标记为配置类 | "这是规章制度文件" |
| `@Component` | 普通类 | 让 Spring 管理这个类 | "登记到员工名册" |

### 4.2 控制器相关

| 注解 | 位置 | 作用 | 通俗解释 |
|------|------|------|----------|
| `@RestController` | Controller 类 | = @Controller + @ResponseBody，返回 JSON 数据 | "我是服务员，只说 JSON 话" |
| `@RequestMapping("/depts")` | Controller 类 | 设置请求路径前缀 | "本店招牌：/depts" |
| `@GetMapping` | 方法 | 处理 GET 请求 | "有人来查数据" |
| `@PostMapping` | 方法 | 处理 POST 请求 | "有人来提交数据" |
| `@PutMapping` | 方法 | 处理 PUT 请求 | "有人来修改数据" |
| `@DeleteMapping` | 方法 | 处理 DELETE 请求 | "有人来删除数据" |
| `@RequestParam` | 方法参数 | 从 URL 参数中取值 | "从 URL 问号后面拿值" |
| `@PathVariable` | 方法参数 | 从 URL 路径中取值 | "从 URL 斜杠中间拿值" |
| `@RequestBody` | 方法参数 | 从请求体中取 JSON 并转成 Java 对象 | "把 JSON 变成 Java 对象" |

### 4.3 业务层相关

| 注解 | 位置 | 作用 | 通俗解释 |
|------|------|------|----------|
| `@Service` | Service 实现类 | 标记为业务层组件 | "我是厨师（业务处理者）" |
| `@Autowired` | 字段/方法 | 自动注入依赖对象 | "帮我递个工具过来" |

### 4.4 数据层相关

| 注解 | 位置 | 作用 | 通俗解释 |
|------|------|------|----------|
| `@Mapper` | DAO 接口 | 标记为 MyBatis 映射器 | "我是数据库操作员" |
| `@Select` | DAO 方法 | 写查询 SQL | "查一下数据" |
| `@Insert` | DAO 方法 | 写插入 SQL | "加一条数据" |
| `@Update` | DAO 方法 | 写更新 SQL | "改一条数据" |
| `@Delete` | DAO 方法 | 写删除 SQL | "删一条数据" |
| `@Param` | 方法参数 | 给参数取名，用于 XML 中引用 | "给这个参数贴个标签" |

### 4.5 Lombok 注解

| 注解 | 作用 | 通俗解释 |
|------|------|----------|
| `@Data` | 自动生成 getter/setter/toString/equals/hashCode | "帮我写好所有 get/set 方法" |
| `@NoArgsConstructor` | 自动生成无参构造方法 | "帮我写一个 new Xxx()" |
| `@AllArgsConstructor` | 自动生成全参构造方法 | "帮我写 new Xxx(所有参数)" |
| `@Slf4j` | 自动生成 log 对象，用于打印日志 | "给我一个记日记的本子" |

### 4.6 新增功能相关注解

| 注解 | 位置 | 作用 | 通俗解释 |
|------|------|------|----------|
| `@RestControllerAdvice` | 异常处理类 | 全局异常处理 | "我是全店的投诉处理中心" |
| `@ExceptionHandler` | 异常处理方法 | 处理指定类型的异常 | "这种问题我来处理" |
| `@Aspect` | 切面类 | 标记为 AOP 切面 | "我是监控摄像头" |
| `@Around` | 切面方法 | 环绕通知（方法前后都执行） | "进出都记录" |
| `@EventListener` | 监听器方法 | 监听特定事件 | "我在等某个消息" |
| `@Value` | 字段 | 读取配置文件中的值 | "从说明书上读参数" |

### 4.7 前端注解/指令

| 指令 | 作用 | 通俗解释 |
|------|------|----------|
| `v-model` | 双向绑定（输入框 ↔ 数据） | "输入框和变量实时同步" |
| `v-for` | 循环渲染列表 | "遍历数组，显示每一项" |
| `v-if` | 条件渲染 | "满足条件才显示" |
| `@click` | 点击事件 | "点击时执行某函数" |
| `:` 或 `v-bind` | 动态绑定属性 | "把 JS 变量绑定到 HTML 属性" |
| `ref` | 引用 DOM 或组件 | "给元素贴个标签，方便找到它" |

---

## 5. 后端核心类详解

### 5.1 控制层（Controller）

#### DeptController.java
```
路径：com.pzhu.controller.DeptController
作用：处理部门相关的 HTTP 请求（增删改查）
方法：
├── selectAll()    → GET    /depts       → 查询所有部门
├── deleteById()   → DELETE /depts?id=1  → 删除部门
├── addDept()      → POST   /depts       → 新增部门
├── updateDept()   → PUT    /depts       → 修改部门
└── selectById()   → GET    /depts/1     → 根据 ID 查部门（编辑回显）
```

#### EmpController.java
```
路径：com.pzhu.controller.EmpController
作用：处理员工相关的 HTTP 请求
方法：
├── page()         → GET /emps           → 分页条件查询员工
└── countByJob()   → GET /emps/stats/job → 按职位统计人数（图表用）
```

#### LoginController.java（新增）
```
路径：com.pzhu.controller.LoginController
作用：处理登录请求
方法：
└── login()        → POST /login         → 验证用户名密码，返回 JWT 令牌
```

#### UploadController.java（新增）
```
路径：com.pzhu.controller.UploadController
作用：处理文件上传（员工头像）
方法：
└── upload()       → POST /upload        → 接收图片文件，保存到磁盘，返回URL
```

### 5.2 业务层（Service）

#### DeptService / DeptServiceImpl
```
作用：部门管理的业务逻辑
- selectAll()：查询所有部门
- deleteById()：删除部门
- addDept()：新增前设置创建时间和修改时间
- updateDept()：修改前设置修改时间
- selectById()：根据ID查询
```

#### EmpService / EmpServiceImpl
```
作用：员工管理的业务逻辑
- page()：分页查询（使用 PageHelper）
- countByJob()：按职位统计
```

#### LogService / LogServiceImpl（新增）
```
作用：操作日志的业务逻辑
- saveLog()：保存操作日志到 operate_log 表
```

### 5.3 数据层（DAO）

#### DeptDao.java
```
注解版 MyBatis，直接在接口方法上用 @Select/@Insert/@Delete/@Update 写 SQL。
都是简单 SQL，所以不需要 XML 文件。
```

#### EmpDao.java
```
XML 版 MyBatis，SQL 写在 resources/mapper/EmpDao.xml 中。
因为涉及多表关联（LEFT JOIN）和动态 SQL（<if> 条件判断），用 XML 更清晰。
```

#### LogDao.java（新增）
```
注解版 MyBatis：
- insert() → 插入一条操作日志（INSERT INTO operate_log）
```

### 5.4 实体类（POJO）

| 类名 | 对应数据库表 | 说明 |
|------|-------------|------|
| Dept.java | dept | 部门（id, name, createTime, updateTime） |
| Emp.java | emp | 员工（id, username, password, name, 等） |
| OperateLog.java | operate_log（新增） | 操作日志（谁、什么时候、做了什么、花了多久） |
| Result.java | 无 | 统一响应格式（code, msg, data） |
| PageResult.java | 无 | 分页结果（total 总记录数, rows 当前页数据） |
| EmpQueryBen.java | 无 | 封装员工查询参数 |

### 5.5 配置/工具/拦截器/切面/监听器（新增）

#### JwtUtils.java（工具类）
```
作用：JWT 令牌的生成和解析
- generateToken(claims) → 生成 JWT 字符串
- parseToken(token) → 解析 JWT，获取用户信息
```

#### LoginInterceptor.java（拦截器）
```
作用：拦截请求，验证 JWT 令牌
- preHandle() → 在请求到达 Controller 前执行
  - 从请求头获取 token
  - 如果 token 为空 → 返回 401 错误
  - 如果 token 无效（过期/篡改）→ 返回 401 错误
  - 如果 token 有效 → 放行
```

#### WebConfig.java（配置类）
```
作用：注册拦截器 + 配置静态资源映射
- addInterceptors() → 注册登录拦截器，排除 /login 和 /images/**
- addResourceHandlers() → 让 /images/** 的请求访问本地磁盘的上传目录
```

#### LogAspect.java（AOP 切面）
```
作用：自动记录 Controller 中每个方法的操作日志
- recordLog() → 环绕通知（方法执行前后都插入代码）
  - 前：记录操作时间、操作人、类名、方法名、参数、开始时间
  - 中：执行原方法
  - 后：计算耗时、保存日志到数据库
```

#### GlobalExceptionHandler.java（全局异常处理）
```
作用：统一处理所有 Controller 抛出的异常
- handleException() → 兜底处理所有异常
- handleTypeMismatch() → 参数类型不匹配
- handleMethodNotSupported() → 请求方法不支持
- handleNotFound() → 404 资源未找到
```

#### StartupListener.java（启动监听器）
```
作用：应用启动成功后打印提示信息
- onApplicationReady() → 在 ApplicationReadyEvent 事件发生时执行
```

---

## 6. 前端项目结构详解

### 6.1 各目录的作用

| 目录 | 作用 | 通俗解释 |
|------|------|----------|
| `api/` | 封装后端 API 调用 | "电话本"——记录后端的电话号码 |
| `router/` | 路由配置，定义 URL 与页面的对应关系 | "地图"——告诉你去哪找什么页面 |
| `stores/` | Pinia 状态管理，存全局数据 | "公共黑板"——所有人都能看到 |
| `utils/` | 工具类，封装通用逻辑 | "工具箱" |
| `views/` | 页面组件 | "每个房间"——就是一个个页面 |

### 6.2 文件调用关系图

```
main.js（应用入口）
  ├── 创建 Vue 应用
  ├── 注册 Pinia（状态管理）
  ├── 注册 Router（路由）
  ├── 注册 Element Plus（UI 组件库）
  └── 挂载 App.vue

App.vue（根组件）
  └── <router-view>（路由出口）

router/index.js（路由配置）
  ├── /login → Login 页面（不需要登录）
  └── / → Layout 布局（需要登录）
       ├── /index → 首页
       ├── /dept  → 部门管理
       ├── /emp   → 员工管理
       └── ...

stores/auth.js（认证状态）
  ├── token：JWT 令牌
  ├── userName：用户名
  ├── login()：保存登录信息
  └── logout()：清除登录信息

utils/request.js（Axios 封装）
  ├── 请求拦截器：自动加 token
  └── 响应拦截器：处理 401 跳登录

api/dept.js → 部门 API（调用 request.get/post/put/delete）
api/emp.js  → 员工 API
api/user.js → 登录 API
```

---

## 7. 前端核心概念和函数详解

### 7.1 Vue 3 核心概念

#### 什么是"响应式"？
**传统 JS：** `let name = '张三'` → 修改 name 后，页面不会自动更新。
**Vue 响应式：** `let name = ref('张三')` → 修改 name.value 后，页面自动更新。

Vue 的"响应式"就是：**数据变了，页面自动变**。

#### ref() 是什么？
```
作用：创建一个响应式的数据
用法：let count = ref(0)
取值：count.value（在 JS 中要加 .value）
显示：{{ count }}（在模板中不需要 .value）

类比：ref 是一个"魔法盒子"，里面装的值变了，所有用到它的地方都会自动更新。
```

#### computed() 是什么？
```
作用：根据已有的数据计算出一个新值（会自动更新）
用法：let doubleCount = computed(() => count.value * 2)
类比：excel 表格中的公式——当原始数据变化时，计算结果自动更新。
```

#### watch() 是什么？
```
作用：监听一个数据的变化，变化时执行某些操作
用法：
watch(searchEmp, (newValue, oldValue) => {
  // 当 searchEmp 变化时执行
})

场景：日期选择器选了日期范围后，自动拆分成 begin 和 end。
类比：装了一个"监控"，一旦数据变了就触发警报。
```

### 7.2 生命周期钩子函数

Vue 组件从创建到销毁有一个"生命周期"，在特定阶段会自动执行的函数就叫"钩子函数"。

| 钩子函数 | 执行时机 | 常见用途 |
|----------|----------|----------|
| `onMounted()` | 组件显示到页面后 | 发送请求获取数据、初始化图表 |
| `onUnmounted()` | 组件从页面移除时 | 清理定时器、释放资源 |

**类比：** 就像人的一生——`出生（onMounted）→ 活着（页面使用中）→ 死亡（onUnmounted）`。

### 7.3 Vue Router 相关

#### router.push() 是什么？
```
作用：编程式导航，跳转到指定页面
用法：router.push('/login') → 跳转到登录页
类比：在浏览器地址栏输入网址然后回车。
```

#### 路由守卫 beforeEach()
```
作用：在每次切换路由前执行
用法：
router.beforeEach((to, from, next) => {
  // to：要去哪里
  // from：从哪里来
  // next()：放行
  // next('/login')：强制跳到登录页
})

类比：每次进小区都要刷门禁卡（检查 token）。
```

### 7.4 Axios 拦截器

#### 请求拦截器
```
作用：在每个请求发送前，自动给请求头加上 token
类比：每次寄快递都要在包裹上贴个标签（token），快递员才知道是谁寄的。
```

#### 响应拦截器
```
作用：在收到响应后，自动处理错误（如 401 未授权）
类比：收快递时检查一下包裹有没有破损，破损了就退回。
```

### 7.5 Pinia 状态管理

#### defineStore() 是什么？
```
作用：定义一个"状态仓库"，多个组件可以共享数据
用法：
const useAuthStore = defineStore('auth', () => { ... })
// 在组件中使用：const authStore = useAuthStore()

类比：公司的"共享文档"，所有人都能看，所有人都能编辑。
```

---

## 8. 企业级功能详解

### 8.1 登录功能（JWT）

#### 流程图

```
前端：用户输入用户名密码 → 点击登录
  │
  ▼
POST /api/login {username, password}
  │
  ▼
LoginController.login()
  │
  ├─ 校验参数是否为空
  │
  ├─ 查询 user 表：SELECT * FROM user WHERE username=? AND password=?
  │
  ├─ 没查到 → 返回 {code:0, msg:"用户名或密码错误"}
  │
  └─ 查到用户 →
       ├─ 生成 JWT（包含用户 id, username, name）
       ├─ 返回 {code:1, data: {token: "xxx...", name: "张三"}}
       │
       ▼
前端收到 token →
  ├─ 存入 Pinia（authStore.login(token, name)）
  ├─ 存入 localStorage（刷新页面后仍然有效）
  └─ 跳转到首页
```

#### JWT 的结构

```
一个 JWT 长这样：eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MX0.xxxxx

它由三部分组成（用点分隔）：
1. Header（头部）→ 加密算法信息
2. Payload（载荷）→ 存放的数据（用户信息）
3. Signature（签名）→ 防伪标识（用密钥加密生成）
```

**为什么用 JWT 而不用 Session？**

| 特性 | Session | JWT |
|------|---------|-----|
| 存储位置 | 存在服务器内存 | 存在客户端（浏览器） |
| 扩展性 | 多台服务器需要共享 Session | 天然支持分布式 |
| 安全性 | 相对安全（存在服务器） | 需要 HTTPS 防止窃取 |
| 性能 | 服务器需要查 Session | 服务器只需验签名，无需查库 |

### 8.2 拦截器（Interceptor）

#### 什么是拦截器？

拦截器是 Spring MVC 的机制，可以在请求到达 Controller 之前"拦截"下来，做一些处理。

**通俗类比：** 就像进图书馆要刷卡 —— 没卡（没 token）就进不去。

#### 拦截器的工作流程

```
请求进来
  │
  ▼
LoginInterceptor.preHandle()
  │
  ├─ 获取请求头中的 token
  │
  ├─ token 为空 → 返回 401（未登录）
  │
  ├─ token 无效 → 返回 401（登录已过期）
  │
  └─ token 有效 → 放行，请求到达 Controller
```

#### 哪些请求需要拦截？

在 `WebConfig.java` 中配置：
- **拦截：** 所有 `/**, 除了下面排除的路径
- **不拦截：** `/login`（登录接口）、`/images/**`（静态图片）、`/error`（错误页面）

#### 为什么要排除登录接口？

如果登录接口也被拦截了，那用户还没登录就无法访问登录接口，形成"死循环"。

### 8.3 AOP 操作日志（Aspect）

#### 什么是 AOP？

AOP（Aspect Oriented Programming，面向切面编程）是 Spring 的重要特性。

**通俗理解：**
- 正常情况下：方法 A → 方法 B → 方法 C
- 加了 AOP 后：**记录日志 →** 方法 A → 方法 B → 方法 C → **记录日志**
- 日志记录代码不用写在每个方法里，而是"切入"到方法执行前后。

**类比：** 超市的监控摄像头 —— 不需要每个店员都记录谁进了店，摄像头会自动记录。

#### 切点表达式说明

```java
@Around("execution(* com.pzhu.controller.*.*(..))")
```

这个表达式分解如下：

| 部分 | 含义 |
|------|------|
| `execution(` | 在方法执行时切入 |
| `*` | 任意返回值 |
| `com.pzhu.controller.*` | controller 包下的所有类 |
| `.*` | 所有方法 |
| `(..)` | 任意参数 |

**整句意思：** 当 `controller` 包下**任何一个类的任何一个方法**被调用时，都执行这个切面。

#### 日志切面记录了什么？

每条日志包含：
1. **谁**做的操作（从 JWT 中解析用户名）
2. **什么时间**做的
3. **哪个类**的**哪个方法**
4. **传入的参数**是什么
5. **返回的结果**是什么
6. **花了多少毫秒**

#### 操作描述自动推断

LogAspect 会根据方法名自动推断操作描述：

| 方法名 | 推断结果 |
|--------|----------|
| `addDept()` | "新增部门" |
| `deleteById()` | "删除部门" |
| `updateDept()` | "修改部门" |
| `selectAll()` | "查询部门" |
| `upload()` | "上传文件" |

### 8.4 全局异常处理

#### 为什么需要全局异常处理？

**没有全局异常处理时：**
- 用户看到一堆英文错误堆栈 → 体验差
- 错误信息可能暴露系统内部细节 → 不安全

**有全局异常处理时：**
- 用户看到友好的提示："服务器繁忙，请稍后重试"
- 后端在日志中记录详细错误 → 方便排查

#### GlobalExceptionHandler 的处理规则

```java
@ExceptionHandler(Exception.class)     → 处理所有异常（兜底）
@ExceptionHandler(TypeMismatchException.class) → 参数类型错误
@ExceptionHandler(HttpRequestMethodNotSupportedException.class) → 请求方法错误
@ExceptionHandler(NoResourceFoundException.class) → 404 错误
```

**类比：** 医院的分诊台 —— 不同类型的病人去不同的科室。

### 8.5 启动监听器

#### 什么是监听器？

监听器可以监听某个事件的发生，当事件发生时自动执行一些代码。

**类比：** 你把手机设置了闹钟（监听器），到了早上7点（事件发生），闹钟响了（代码执行）。

#### StartupListener 的工作

```java
@EventListener(ApplicationReadyEvent.class)
public void onApplicationReady() {
    // 应用启动完成后自动执行
    log.info("系统启动成功！");
    log.info("后端地址：http://localhost:8080");
}
```

`ApplicationReadyEvent` 是 Spring Boot 在应用完全启动后发出的"事件"。

### 8.6 文件上传

#### 前端上传流程

```
用户选择头像图片
  │
  ▼
el-upload 组件
  │
  ├─ before-upload：上传前校验（格式、大小）
  │
  └─ 发送 POST /api/upload 请求（携带图片文件）
       │
       ▼
UploadController.upload()
  │
  ├─ 生成唯一文件名（UUID + 扩展名）
  ├─ 保存到本地磁盘（E:/uploads/）
  └─ 返回图片URL（/images/uuid.jpg）
       │
       ▼
前端显示头像
```

#### 文件上传配置

在 `application.yml` 中：
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB    # 单个文件最大 10MB
      max-request-size: 20MB # 一次请求最大 20MB
```

在 `WebConfig.java` 中配置了静态资源映射，让 `/images/**` 的请求能从磁盘读取文件。

---

## 9. 前后端数据交互格式

### 9.1 统一响应格式（Result）

所有后端接口都返回统一格式的 JSON：

```json
// 成功时
{
  "code": 1,
  "msg": "success",
  "data": { ... }  // 实际数据
}

// 失败时
{
  "code": 0,
  "msg": "错误描述",
  "data": null
}
```

### 9.2 各接口的请求和响应

#### 登录
```
POST /api/login
请求：{"username": "admin", "password": "123456"}
成功：{"code": 1, "data": {"token": "xxx", "name": "管理员"}}
失败：{"code": 0, "msg": "用户名或密码错误"}
```

#### 查询所有部门
```
GET /api/depts
成功：{"code": 1, "data": [{"id":1, "name":"研发部", ...}, ...]}
```

#### 新增部门
```
POST /api/depts
请求：{"name": "新部门"}
成功：{"code": 1, "msg": "新增部门成功"}
```

#### 分页查询员工
```
GET /api/emps?page=1&pageSize=10&name=&gender=&begin=&end=
成功：{"code": 1, "data": {"total": 100, "rows": [...员工数据...]}}
```

#### 文件上传
```
POST /api/upload（multipart/form-data，字段名：file）
成功：{"code": 1, "data": "/images/a1b2c3.jpg"}
```

---

## 10. 从零开始运行项目

### 10.1 环境要求

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | 运行 Spring Boot 后端 |
| Maven | 3.6+ | 管理 Java 依赖 |
| Node.js | 16+ | 运行前端开发服务器 |
| MySQL | 8.0+ | 数据库 |
| IDE | 任意 | 推荐 IntelliJ IDEA 或 VS Code |

### 10.2 数据库配置

1. 确保 MySQL 已启动
2. 执行 `database.sql` 创建表：
```bash
mysql -u root -p < E:\aSpringBoot_start01_0525\tilasdemo\database.sql
```
3. 修改 `application.yml` 中的数据库密码（如果需要）

### 10.3 启动后端

```bash
# 方式一：使用 Maven 直接启动
cd E:\aSpringBoot_start01_0525\tilasdemo
mvn spring-boot:run

# 方式二：先打包成 jar，再运行
mvn clean package -DskipTests
java -jar target/tilasdemo-0.0.1-SNAPSHOT.jar
```

后端启动后访问：http://localhost:8080

### 10.4 启动前端

```bash
# 进入前端项目目录
cd E:\aSpringBoot_start01_0525\vue-tlias-management

# 安装依赖（第一次启动时需要）
npm install

# 启动开发服务器
npm run dev
```

前端启动后访问：http://localhost:5173

### 10.5 测试账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| admin | 123456 | 管理员 |
| xiaoqiao | 123456 | 普通用户 |
| diaochan | 123456 | 普通用户 |
| lvbu | 123456 | 普通用户 |

---

## 附：学习路线建议

如果你是刚学 Spring Boot + Vue，建议按以下顺序学习：

### 后端学习路线
1. **Maven**：理解依赖管理和项目构建
2. **Spring Boot**：理解 IOC（控制反转）和 DI（依赖注入）
3. **Spring MVC**：理解 @RestController、请求映射、参数接收
4. **MyBatis**：理解 ORM、@Mapper、SQL 映射
5. **PageHelper**：分页插件的使用
6. **拦截器**：HandlerInterceptor 的使用
7. **AOP**：@Aspect、@Around 切面编程
8. **JWT**：令牌生成和校验
9. **全局异常处理**：@RestControllerAdvice

### 前端学习路线
1. **HTML + CSS**：页面结构和样式
2. **JavaScript 基础**：变量、函数、Promise、async/await
3. **Vue 3**：ref、computed、watch、生命周期
4. **Vue Router**：路由配置、导航守卫
5. **Pinia**：状态管理
6. **Axios**：HTTP 请求、拦截器
7. **Element Plus**：UI 组件库使用
8. **ECharts**：图表展示

---

> **最后的话：** 编程是"做中学"的过程，遇到不懂的概念不要怕，先试着运行代码，看看效果，慢慢就理解了。坚持就是胜利！
