package com.github.zhitron.id_generator;

import org.junit.Test;

/**
 * @author zhitron
 */
public class IDGeneratorFactoryTest {
    @Test
    public void test() {
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
