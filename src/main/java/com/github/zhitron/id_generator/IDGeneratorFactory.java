package com.github.zhitron.id_generator;

import com.github.zhitron.id_generator.impl.RandomIDGenerator;
import com.github.zhitron.id_generator.impl.SnowflakeIDGenerator;
import com.github.zhitron.id_generator.impl.ULIDGenerator;
import com.github.zhitron.id_generator.impl.UUIDGenerator;

/**
 * ID生成器的静态工厂类，用于通过静态方法创建不同的ID生成器实例。
 *
 * @author zhitron
 */
public class IDGeneratorFactory {

    /**
     * 创建基于安全随机数的ID生成器。
     *
     * @return RandomIDGenerator 实例
     */
    public static IDGenerator createRandom() {
        return new RandomIDGenerator();
    }

    /**
     * 创建基于Snowflake算法的ID生成器。
     *
     * @param nodeId 节点 ID，范围 [0, 1023]
     * @return SnowflakeIDGenerator 实例
     */
    public static IDGenerator createSnowflake(long nodeId) {
        return new SnowflakeIDGenerator(nodeId);
    }

    /**
     * 创建基于Snowflake算法的ID生成器。
     *
     * @param center 节点中心ID，范围 [0, 31]
     * @param worker 工作节点ID，范围 [0, 31]
     * @return SnowflakeIDGenerator 实例
     */
    public static IDGenerator createSnowflake(long center, long worker) {
        if (center < 0 || center > 31) {
            throw new IllegalArgumentException("Center ID must be between 0 and 31");
        }
        if (worker < 0 || worker > 31) {
            throw new IllegalArgumentException("Worker ID must be between 0 and 31");
        }
        long nodeId = (center << 5) | worker;
        return new SnowflakeIDGenerator(nodeId);
    }

    /**
     * 创建基于UUID的ID生成器。
     *
     * @return UUIDGenerator 实例
     */
    public static IDGenerator createUUID() {
        return new UUIDGenerator();
    }

    /**
     * 创建基于ULID的ID生成器。
     *
     * @return ULIDGenerator 实例
     */
    public static IDGenerator createULID() {
        return new ULIDGenerator();
    }
}
