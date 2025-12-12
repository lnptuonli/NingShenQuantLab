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

-- 创建用户表（根据实际表结构）
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `cust_id` MEDIUMTEXT NULL DEFAULT NULL,
    `cust_name` VARCHAR(50) NULL DEFAULT NULL,
    `cust_key` VARCHAR(50) NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) COLLATE='utf8mb4_0900_ai_ci' ENGINE=InnoDB;

-- 插入测试数据
INSERT INTO user (cust_id, cust_key, cust_name) VALUES
('100372', 'TestKey@123', '张三'),
('100373', 'TestKey@456', '李四'),
('100374', 'TestKey@789', '王五');
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
- **获取用户列表**：http://localhost:8080/api/v1/users?page=1&size=10
- **获取单个用户**：http://localhost:8080/api/v1/users/100372
- **创建用户**：POST http://localhost:8080/api/v1/users
- **更新用户**：PUT http://localhost:8080/api/v1/users/100372
- **删除用户**：DELETE http://localhost:8080/api/v1/users/100372

## API 文档

### 1. 查询用户列表（分页）

**请求：**
```
GET /api/v1/users?page=1&size=10&search=张三
```

**参数：**
- `page`：页码（从 1 开始），默认 1
- `size`：每页数量，默认 10
- `search`：搜索关键词（可选）

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "custId": "100372",
        "custName": "张三",
        "custKey": "TestKey@123"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10,
    "totalPages": 1
  },
  "timestamp": 1234567890
}
```

### 2. 获取单个用户

**请求：**
```
GET /api/v1/users/{custId}
```

**参数：**
- `custId`：客户 ID（String 类型）

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "custId": "100372",
    "custName": "张三",
    "custKey": "TestKey@123"
  },
  "timestamp": 1234567890
}
```

### 3. 创建用户

**请求：**
```
POST /api/v1/users
Content-Type: application/json

{
  "custId": "100375",
  "custName": "李四",
  "custKey": "Abc@1234"
}
```

**校验规则：**
- `custId`：必填，正数
- `custName`：必填，2-50 字符
- `custKey`：必填，8-50 字符，必须包含数字、字母和特殊符号

**响应示例：**
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 4,
    "custId": "100375",
    "custName": "李四",
    "custKey": "Abc@1234"
  },
  "timestamp": 1234567890
}
```

### 4. 更新用户

**请求：**
```
PUT /api/v1/users/{custId}
Content-Type: application/json

{
  "custName": "张三（修改）",
  "custKey": "NewPass@123"
}
```

### 5. 删除用户

**请求：**
```
DELETE /api/v1/users/{custId}
```

### 6. 批量删除用户

**请求：**
```
DELETE /api/v1/users?custIds=100372,100373,100374
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
7. ✅ 统一返回结果封装（Result）
8. ✅ 参数校验（Bean Validation）
9. ✅ 自定义校验注解
10. ✅ 全局异常处理
11. ✅ 声明式事务管理
12. ✅ 分页查询
13. ✅ 模糊搜索

## 📚 详细文档

- **CODE_REVIEW_AND_FIXES.md**：完整的代码审查报告和修复说明，包含所有知识点讲解
- **GITHUB_SETUP.md**：GitHub 仓库设置和 Git 使用指南
- **QUICK_START.md**：快速上传到 GitHub 的简明指南

## 许可证

MIT License


