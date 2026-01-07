# 快速上传到 GitHub

## 🚀 三步上传

### 第 1 步：在 GitHub 创建仓库

1. 访问 https://github.com/new
2. 填写：
   - **Repository name**: `alphaForge_demo1`
   - **Description**: `Spring Boot 学习项目`
   - **Public** 或 **Private**（随你选择）
3. ⚠️ **不要勾选任何选项**（README、.gitignore、license 都不要）
4. 点击 **Create repository**

### 第 2 步：运行推送脚本（Windows）

双击运行 `push_to_github.bat`，按提示输入你的 GitHub 用户名。

**或者手动执行：**

```powershell
# 添加远程仓库（替换 YOUR_USERNAME）
git remote add origin https://github.com/YOUR_USERNAME/alphaForge_demo1.git

# 重命名分支
git branch -M main

# 推送
git push -u origin main
```

### 第 3 步：验证

访问 `https://github.com/YOUR_USERNAME/alphaForge_demo1` 查看你的项目！

---

## 📋 当前状态

✅ Git 仓库已初始化  
✅ 代码已提交到本地仓库  
✅ .gitignore 已配置（数据库密码不会上传）  
✅ README.md 已创建  
⏳ 等待推送到 GitHub

---

## 🔐 认证问题

如果推送时要求输入密码，GitHub 已经不支持密码认证了，需要使用 **Personal Access Token (PAT)**：

### 创建 Token

1. 访问 https://github.com/settings/tokens
2. 点击 **Generate new token** → **Generate new token (classic)**
3. 填写：
   - **Note**: `alphaForge_demo1`
   - **Expiration**: 选择有效期
   - **Select scopes**: 勾选 `repo`（所有子选项）
4. 点击 **Generate token**
5. **复制 token**（只显示一次！）

### 使用 Token

推送时，用户名输入你的 GitHub 用户名，密码输入刚才复制的 token。

---

## 🌐 在其他电脑克隆项目

```bash
# 克隆
git clone https://github.com/YOUR_USERNAME/alphaForge_demo1.git
cd alphaForge_demo1

# 配置数据库
cp src/main/resources/application.properties.old.old.example src/main/resources/application.properties.old.old
# 编辑 application.properties.old.old，修改数据库密码

# 运行
mvnw.cmd spring-boot:run  # Windows
./mvnw spring-boot:run     # Linux/Mac
```

---

## 📚 更多帮助

- 详细指南：查看 `GITHUB_SETUP.md`
- 项目说明：查看 `README.md`
- Git 基础：https://git-scm.com/book/zh/v2

---

## ✅ 提交的文件清单

**会提交：**
- ✅ 源代码（`src/` 目录）
- ✅ Maven 配置（`pom.xml`）
- ✅ Maven Wrapper（`mvnw`, `mvnw.cmd`, `.mvn/`）
- ✅ 配置模板（`application.properties.example`）
- ✅ 文档（`README.md`, `GITHUB_SETUP.md`）
- ✅ Git 配置（`.gitignore`）

**不会提交（已被 .gitignore 忽略）：**
- ❌ 编译文件（`target/`）
- ❌ IDEA 配置（`.idea/`, `*.iml`）
- ❌ 数据库密码（`application.properties`）
- ❌ 日志文件

---

**准备好了吗？开始第 1 步吧！** 🚀







