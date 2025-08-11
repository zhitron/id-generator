# ID Generator

## 📄 项目简介

`ID Generator` 是一个用于生成唯一标识符（ID）的 Java 工具库。支持多种 ID 生成策略，如 Snowflake、UUID、时间戳序列等，适用于分布式系统和大规模数据处理场景。

---

## 🚀 快速开始

### 构建要求

- JDK 21 或以上（推荐使用 JDK 21）
- Maven 3.x

### 添加依赖

你可以通过 Maven 引入该项目：

```xml

<dependency>
    <groupId>com.github.zhitron</groupId>
    <artifactId>id-generator</artifactId>
    <version>1.0.1</version>
</dependency>
```

### 使用案例

```java
package com.github.zhitron.id_generator;

/**
 * @author zhitron
 */
public class IDGeneratorFactoryTest {


    public static void main(String[] args) {
        // 使用默认配置创建 Snowflake 生成器
        IDGenerator snowflakeGenerator = IDGeneratorFactory.createSnowflake(1);
        long snowflakeId = snowflakeGenerator.generateNextLongID();
        System.out.println("Snowflake ID: " + snowflakeId);

        // 创建随机生成器
        IDGenerator randomGenerator = IDGeneratorFactory.createRandom();
        int randomId = randomGenerator.generateNextIntID();
        System.out.println("Random ID: " + randomId);

        // 创建 UUID 生成器
        IDGenerator uuidGenerator = IDGeneratorFactory.createUUID();
        String uuid = uuidGenerator.generateNextStringID();
        System.out.println("UUID: " + uuid);

        // 创建 ULID 生成器
        IDGenerator ulidGenerator = IDGeneratorFactory.createULID();
        String ulid = ulidGenerator.generateNextStringID();
        System.out.println("ULID: " + ulid);
    }
}

```

---

## 🧩 功能特性

- **多策略支持**：提供 Snowflake、UUID、时间戳等多种 ID 生成算法。
- **高性能**：低延迟、高吞吐量，适合高频调用场景。
- **可扩展性**：易于扩展新的 ID 生成策略。
- **线程安全**：所有实现均为线程安全，可在并发环境下稳定运行。
- **简单易用**：提供简洁的 API 接口和工厂类，便于集成与使用。

---

## ✍️ 开发者

- **Zhitron**
- 邮箱: zhitron@foxmail.com
- 组织: [Zhitron](https://github.com/zhitron)

---

## 📦 发布状态

当前版本：`1.0.0`

该项目已发布至 [Maven Central](https://search.maven.org/)，支持快照版本与正式版本部署。

---

## 🛠 源码管理

GitHub 地址：https://github.com/zhitron/id-generator

使用 Git 进行版本控制：

```bash
git clone https://github.com/zhitron/id-generator.git
```

---

## 📚 文档与社区

- Javadoc 文档可通过 `mvn javadoc:javadoc` 生成。
- 如有问题或贡献，请提交 Issues 或 PR 至 GitHub 仓库。

---

## 📎 License

Apache License, Version 2.0  
详见 [LICENSE](https://www.apache.org/licenses/LICENSE-2.0.txt)
