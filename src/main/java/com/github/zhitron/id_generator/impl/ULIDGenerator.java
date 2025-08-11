package com.github.zhitron.id_generator.impl;

import com.github.zhitron.id_generator.IDGenerator;

/**
 * 使用ULID实现的ID生成器。
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
 * @see ULID
 */
public class ULIDGenerator implements IDGenerator {
    /**
     * 构造一个新的ULIDGenerator实例。
     * <p>
     * 该构造函数用于初始化ULIDGenerator对象，目前无需任何参数。
     */
    public ULIDGenerator() {
    }

    /**
     * 生成下一个整型ID，通过ULID的哈希值生成。
     * <p>
     * 该方法首先生成一个随机ULID，然后将其128位值（分为两个64位部分）进行位运算和异或操作，
     * 以生成一个分布较为均匀的整型哈希值。这种方法可以减少哈希冲突的概率。
     *
     * @return 下一个整型ID
     */
    @Override
    public int generateNextIntID() {
        ULID id = ULID.randomULID();
        long mostSignificantBits = id.getMostSignificantBits();
        long leastSignificantBits = id.getLeastSignificantBits();

        // 使用更好的哈希混合算法减少冲突概率
        // 将最低有效位的部分进行循环移位后与最高有效位异或
        long combinedBits = mostSignificantBits ^ (leastSignificantBits << 32 | (leastSignificantBits >>> 32));
        // 再次进行右移异或操作以进一步混合位
        return (int) (combinedBits ^ (combinedBits >>> 32));
    }

    /**
     * 生成下一个长整型ID，默认实现为将ULID的哈希值转换为长整型。
     * <p>
     * 该方法通过复杂的位运算组合ULID的两个64位部分，以增强生成ID的唯一性。
     * 对两个部分分别进行循环移位后再异或，可以更好地分散值的分布。
     *
     * @return 下一个长整型ID
     */
    @Override
    public long generateNextLongID() {
        ULID id = ULID.randomULID();
        long mostSignificantBits = id.getMostSignificantBits();
        long leastSignificantBits = id.getLeastSignificantBits();

        // 使用更复杂的位运算组合增强唯一性
        // 对最高有效位进行循环移位
        long msbShifted = (mostSignificantBits << 32) | (mostSignificantBits >>> 32);
        // 对最低有效位进行循环移位
        long lsbShifted = (leastSignificantBits << 32) | (leastSignificantBits >>> 32);
        // 异或两个结果以生成最终的长整型ID
        return msbShifted ^ lsbShifted;
    }

    /**
     * 生成下一个字符串类型ID，返回标准的ULID字符串表示。
     * <p>
     * 该方法直接生成一个随机ULID并调用其toString()方法，返回符合Crockford's Base 32编码的26字符字符串。
     *
     * @return 下一个字符串类型ID，格式为26个字符的Base32编码字符串
     * @see ULID#toString()
     */
    @Override
    public String generateNextStringID() {
        return ULID.randomULID().toString();
    }
}
