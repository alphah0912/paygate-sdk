# PayGate SDK 运维手册

## 本地开发

### 环境

| 工具 | 版本要求 | 用途 |
|------|----------|------|
| JDK | 8+ | Java SDK 编译 |
| Gradle | 8.5（wrapper 自包含） | 构建 |
| GPG | 2.x | Maven Central 签名 |

### 编译 & 测试

```bash
# 全量测试（36 个）
./gradlew test

# 编译（跳过测试）
./gradlew build -x test

# 清除
./gradlew clean
```

### 项目结构

```
paygate-sdk/
  build.gradle              # 唯一构建文件（根目录）
  settings.gradle            # JitPack 用，gradle 自动发现
  gradlew / gradlew.bat      # Gradle wrapper（零安装）
  gradle/                    # wrapper jar
  java/
    src/main/java/...        # 生产代码
    src/test/java/...        # 测试代码
    examples/                # 示例
  spec/
    error-codes.yaml         # 跨语言错误码定义
```

## 发版流程

### 1. 更新版本号

编辑 `build.gradle`：
```groovy
group = 'io.github.alphah0912'
version = '1.0.1'   // 改这里
```

### 2. 测试

```bash
./gradlew test
```

### 3. 本地发布（生成签名产物）

确保本机有 `gradle.properties`（不提交到仓库）：
```properties
signing.password=<GPG 密码>
```

确保本机有 `signing-key.asc`（不提交到仓库）。

```bash
./gradlew clean publish
```

产物输出到 `build/staging/io/github/alphah0912/paygate-sdk/<version>/`。

### 4. 打包上传

```powershell
cd build/staging
Compress-Archive -Path * -DestinationPath ..\..\central-bundle.zip -Force
```

打开 https://central.sonatype.com/publishing/deployments → Publish Component → 上传 ZIP。

### 5. 打 Git Tag

```bash
git tag v1.0.1
git push origin v1.0.1
```

### 发布后验证

几分钟后在 https://central.sonatype.com 搜索 `io.github.alphah0912.paygate-sdk` 确认可见。全局同步最长 24 小时。

## GPG 密钥管理

### 生成（一次性）

```powershell
gpg --gen-key
# Real name: alphah0912
# Email: 475553186@qq.com
```

### 导出私钥（保存好，丢失无法恢复）

```powershell
gpg --export-secret-keys --armor <KEY_ID> > signing-key.asc
```

### 上传公钥

```powershell
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
```

### 查看密钥

```powershell
gpg --list-keys
gpg --list-secret-keys
```

### 密钥过期怎么办

默认 3 年过期。到期前：
```powershell
gpg --edit-key <KEY_ID>
> expire
# 设新过期日期
> save
```
重新上传公钥。

## Maven Central Token

登录 https://central.sonatype.com → Account → Generate User Token。

保存到 `gradle.properties`（不提交到仓库）。

Token 有泄露风险时重新生成旧的自动失效。

## 多语言 SDK 扩展

本项目为 monorepo，每个语言独立目录：

```
paygate-sdk/
  java/          ← 已完成
  typescript/    ← 待开发
  python/        ← 待开发
  php/           ← 待开发
  go/            ← 待开发
```

各语言版本号独立，通过 Git tag 区分：`v1.0.0`、`typescript-v1.0.0`、`python-v1.0.0`。

新 SDK 实现参照 Java 版：
1. Client 模块：Builder 模式、自动签名、限流重试
2. Request/Response POJO：6 个接口
3. Webhook 模块：ISV 签名校验 + NotificationService 明文比对
4. 错误码对齐 `spec/error-codes.yaml`

## 常见问题

**Maven 拉不到依赖**

检查是否在 Maven Central 已同步（最长 24h → 阿里云镜像同步再加 24h）。国内可临时去掉 `settings.xml` 的 aliyun mirror 测试。

**签名失败**

确认 `signing-key.asc` 在项目根目录且 `gradle.properties` 有 `signing.password`。

**编译报错 List.of**

`List.of()` 是 Java 9 API，本项目用 JDK 8 编译所以不能用。用 `Collections.singletonList()` 替代。
