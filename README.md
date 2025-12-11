# AlphaForge Demo1

这是一个 Spring Boot 学习项目，演示了基础的三层架构（Controller、Service、Dao）。

## 技术栈

- **Spring Boot 3.2.0**
- **Java 17**
- **MySQL 8.0**
- **JdbcTemplate**
- **Lombok**
- **Maven**

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/ningshenquantlab/alphaforge_demo1/
│   │       ├── AlphaForgeDemo1Application.java  # 主启动类
│   │       ├── controller/                       # 控制器层
│   │       │   ├── HelloController.java
│   │       │   └── UserController.java
│   │       ├── service/                          # 服务层
│   │       │   ├── UserService.java
│   │       │   └── impl/
│   │       │       └── UserServiceImpl.java
│   │       ├── dao/                              # 数据访问层
│   │       │   ├── UserDao.java
│   │       │   └── impl/
│   │       │       └── UserDaoImpl.java
│   │       └── entity/                           # 实体类
│   │           └── User.java
│   └── resources/
│       ├── application.properties.example        # 配置文件模板
│       ├── static/                               # 静态资源
│       └── templates/                            # 模板文件
└── test/                                         # 测试代码
```

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/YOUR_USERNAME/alphaForge_demo1.git
cd alphaForge_demo1
```

### 2. 配置数据库

#### 创建数据库

```sql
CREATE DATABASE alphaforge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE alphaforge;

-- 创建用户表
CREATE TABLE user (
cust_id BIGINT PRIMARY KEY,
cust_key VARCHAR(255),
cust_name VARCHAR(255)
);

-- 插入测试数据
INSERT INTO user (cust_id, cust_key, cust_name) VALUES
(100372, 'test-key-001', '张三'),
(100373, 'test-key-002', '李四'),
(100374, 'test-key-003', '王五');
```

#### 配置应用

复制配置文件模板并修改数据库连接信息：

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

编辑 `application.properties`，修改数据库密码：

```properties
spring.datasource.password=YOUR_ACTUAL_PASSWORD
```

### 3. 运行项目

#### 使用 Maven

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

#### 使用 IDEA

直接运行 `AlphaForgeDemo1Application.java` 的 main 方法。

### 4. 测试接口

启动成功后，访问以下接口：

- **Hello 接口**：http://localhost:8080/hello
- **获取用户**：http://localhost:8080/user/100372

## API 文档

### 获取用户信息

**请求：**
```
GET /user/{id}
```

**参数：**
- `id`：用户 ID（Long 类型）

**响应示例：**
```json
{
  "id": 100372,
  "key": "test-key-001",
  "username": "张三"
}
```

## 注意事项

⚠️ **重要：** 
- `application.properties` 文件包含敏感信息（数据库密码），已被 `.gitignore` 忽略
- 请使用 `application.properties.example` 作为模板创建自己的配置文件
- 不要将包含真实密码的配置文件提交到 Git

## 开发环境

- **JDK**：17+
- **Maven**：3.6+
- **MySQL**：8.0+
- **IDE**：IntelliJ IDEA（推荐）

## 学习笔记

这个项目演示了：

1. ✅ Spring Boot 基础配置
2. ✅ 三层架构（Controller → Service → Dao）
3. ✅ 依赖注入（构造器注入）
4. ✅ JdbcTemplate 数据库操作
5. ✅ RESTful API 设计
6. ✅ Lombok 简化代码

## 许可证

MIT License

