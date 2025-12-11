# GitHub 仓库设置指南

## 方案一：创建独立的 Git 仓库（推荐）

### 1. 移除父目录的 Git 跟踪

由于当前项目在父目录 `D:\IdeaProjects` 的 git 仓库中，我们需要先创建独立仓库。

```powershell
# 在当前目录 (alphaForge_demo1) 下执行
cd D:\IdeaProjects\alphaForge_demo1

# 初始化新的 git 仓库
git init

# 查看状态
git status
```

### 2. 添加文件到暂存区

```powershell
# 添加所有必要文件
git add .gitignore
git add README.md
git add pom.xml
git add mvnw
git add mvnw.cmd
git add .mvn/
git add src/

# 查看暂存状态
git status
```

### 3. 创建首次提交

```powershell
git commit -m "Initial commit: Spring Boot demo project with three-tier architecture"
```

### 4. 在 GitHub 上创建仓库

1. 访问 https://github.com/new
2. 填写仓库信息：
   - **Repository name**: `alphaForge_demo1`
   - **Description**: `Spring Boot 学习项目 - 三层架构示例`
   - **Public/Private**: 选择 Public（公开）或 Private（私有）
   - ⚠️ **不要勾选** "Initialize this repository with a README"（我们已经有了）
   - ⚠️ **不要添加** .gitignore 和 license（我们已经有了）
3. 点击 **Create repository**

### 5. 关联远程仓库并推送

GitHub 会显示命令，使用以下命令（替换 YOUR_USERNAME 为你的 GitHub 用户名）：

```powershell
# 添加远程仓库
git remote add origin https://github.com/YOUR_USERNAME/alphaForge_demo1.git

# 重命名分支为 main（GitHub 默认使用 main）
git branch -M main

# 推送到 GitHub
git push -u origin main
```

### 6. 验证

访问 `https://github.com/YOUR_USERNAME/alphaForge_demo1` 查看你的项目！

---

## 方案二：使用现有的父目录 Git 仓库

如果你想保留父目录的 git 仓库，可以使用 git subtree 或 git submodule，但这比较复杂，**不推荐新手使用**。

---

## 在另一台电脑上克隆项目

### 1. 克隆仓库

```bash
git clone https://github.com/YOUR_USERNAME/alphaForge_demo1.git
cd alphaForge_demo1
```

### 2. 配置数据库

```bash
# 复制配置文件模板
cp src/main/resources/application.properties.example src/main/resources/application.properties

# 编辑配置文件，修改数据库密码
# Windows: notepad src/main/resources/application.properties
# Linux/Mac: nano src/main/resources/application.properties
```

### 3. 创建数据库

在 MySQL 中执行：

```sql
CREATE DATABASE alphaforge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE alphaforge;

CREATE TABLE user (
    id BIGINT PRIMARY KEY,
    `key` VARCHAR(255),
    username VARCHAR(255)
);

INSERT INTO user (id, `key`, username) VALUES 
(100372, 'test-key-001', '张三'),
(100373, 'test-key-002', '李四'),
(100374, 'test-key-003', '王五');
```

### 4. 运行项目

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 5. 测试

访问：
- http://localhost:8080/hello
- http://localhost:8080/user/100372

---

## 常用 Git 命令

### 日常开发

```bash
# 查看状态
git status

# 查看修改内容
git diff

# 添加修改的文件
git add .

# 提交
git commit -m "描述你的修改"

# 推送到 GitHub
git push

# 拉取最新代码
git pull
```

### 查看历史

```bash
# 查看提交历史
git log

# 查看简洁的提交历史
git log --oneline

# 查看某个文件的修改历史
git log -- src/main/java/com/ningshenquantlab/alphaforge_demo1/controller/UserController.java
```

### 分支管理

```bash
# 查看所有分支
git branch -a

# 创建新分支
git branch feature-xxx

# 切换分支
git checkout feature-xxx

# 创建并切换到新分支
git checkout -b feature-xxx

# 合并分支
git checkout main
git merge feature-xxx
```

---

## 注意事项

### ⚠️ 安全提醒

1. **永远不要提交敏感信息**：
   - 数据库密码
   - API 密钥
   - 私钥文件
   - 个人信息

2. **如果不小心提交了敏感信息**：
   - 立即修改密码
   - 使用 `git filter-branch` 或 `BFG Repo-Cleaner` 清理历史
   - 或者删除仓库重新创建

3. **使用 .gitignore**：
   - 已配置忽略 `application.properties`
   - 使用 `application.properties.example` 作为模板

### 📝 提交规范

建议使用清晰的提交信息：

```bash
# 功能开发
git commit -m "feat: 添加用户查询接口"

# Bug 修复
git commit -m "fix: 修复用户查询时的空指针异常"

# 文档更新
git commit -m "docs: 更新 README 安装说明"

# 代码重构
git commit -m "refactor: 优化 UserService 代码结构"

# 性能优化
git commit -m "perf: 优化数据库查询性能"

# 测试
git commit -m "test: 添加 UserController 单元测试"
```

---

## 问题排查

### 推送失败

如果 `git push` 失败，可能是：

1. **认证问题**：
   - 使用 Personal Access Token (PAT) 而不是密码
   - 在 GitHub Settings → Developer settings → Personal access tokens 创建

2. **分支冲突**：
   ```bash
   git pull --rebase
   git push
   ```

3. **首次推送**：
   ```bash
   git push -u origin main
   ```

### 查看远程仓库

```bash
# 查看远程仓库
git remote -v

# 修改远程仓库地址
git remote set-url origin https://github.com/YOUR_USERNAME/alphaForge_demo1.git
```

---

## 完成！

现在你的项目已经成功上传到 GitHub，可以在任何地方克隆和运行了！🎉

