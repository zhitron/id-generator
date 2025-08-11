package com.github.zhitron.id_generator.impl;

import com.github.zhitron.id_generator.IDGenerator;

import java.util.UUID;

/**
 * 使用UUID实现的ID生成器。
 * <p>
 * 该类提供了三种不同类型的ID生成方法：
 * <ul>
 * <li>{@link #generateNextIntID()} 生成整型ID</li>
 * <li>{@link #generateNextLongID()} 生成长整型ID</li>
 * <li>{@link #generateNextStringID()} 生成字符串类型ID</li>
 * </ul>
 *
 * @author zhitron
 * @see IDGenerator
 * @see UUID
 */
public class UUIDGenerator implements IDGenerator {
    /**
     * 构造函数，用于创建UUIDGenerator实例。
     */
    public UUIDGenerator() {
    }

    /**
     * 生成下一个整型ID，通过UUID的哈希值生成。
     * <p>
     * 该方法通过获取UUID的高位和低位比特，然后进行位运算和哈希混合，
     * 以减少哈希冲突的概率，最终返回一个整型值。
     * </p>
     *
     * @return 下一个整型ID
     * @see UUID#randomUUID()
     * @see UUID#getMostSignificantBits()
     * @see UUID#getLeastSignificantBits()
     */
    @Override
    public int generateNextIntID() {
        UUID id = UUID.randomUUID();
        long mostSignificantBits = id.getMostSignificantBits();
        long leastSignificantBits = id.getLeastSignificantBits();

        // 使用更好的哈希混合算法减少冲突概率
        long combinedBits = mostSignificantBits ^ (leastSignificantBits << 32 | (leastSignificantBits >>> 32));
        return (int) (combinedBits ^ (combinedBits >>> 32));
    }

    /**
     * 生成下一个长整型ID，默认实现为将UUID的哈希值转换为长整型。
     * <p>
     * 该方法通过获取UUID的高位和低位比特，然后进行复杂的位运算组合，
     * 以增强生成ID的唯一性，最终返回一个长整型值。
     * </p>
     *
     * @return 下一个长整型ID
     * @see UUID#randomUUID()
     * @see UUID#getMostSignificantBits()
     * @see UUID#getLeastSignificantBits()
     */
    @Override
    public long generateNextLongID() {
        UUID id = UUID.randomUUID();
        long mostSignificantBits = id.getMostSignificantBits();
        long leastSignificantBits = id.getLeastSignificantBits();

        // 使用更复杂的位运算组合增强唯一性
        return (mostSignificantBits << 32 | (mostSignificantBits >>> 32)) ^
                (leastSignificantBits << 32 | (leastSignificantBits >>> 32));
    }

    /**
     * 生成下一个字符串类型ID，返回标准的UUID字符串表示。
     * <p>
     * 该方法直接调用UUID的toString()方法，返回标准的UUID字符串格式，
     * 格式为8-4-4-4-12的32个字符的十六进制数字。
     * </p>
     *
     * @return 下一个字符串类型ID，格式为8-4-4-4-12的36个字符
     * @see UUID#randomUUID()
     * @see UUID#toString()
     */
    @Override
    public String generateNextStringID() {
        return UUID.randomUUID().toString();
    }
}
