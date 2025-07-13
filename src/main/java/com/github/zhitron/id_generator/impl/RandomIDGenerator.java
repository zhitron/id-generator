package com.github.zhitron.id_generator.impl;

import com.github.zhitron.id_generator.IDGenerator;

import java.security.SecureRandom;

/**
 * 随机ID生成器，基于SecureRandom实现，使用时间戳和随机数混合减少冲突。
 *
 * @author zhitron
 */
public class RandomIDGenerator implements IDGenerator {

    /**
     * 用于生成安全随机数的SecureRandom实例。
     */
    private final SecureRandom secureRandom;

    /**
     * 默认构造函数，使用默认的SecureRandom实例初始化。
     */
    public RandomIDGenerator() {
        this(new SecureRandom());
    }

    /**
     * 使用指定的SecureRandom实例进行初始化。
     *
     * @param secureRandom 提供的SecureRandom实例
     */
    public RandomIDGenerator(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    /**
     * 生成下一个整型ID，结合时间戳和随机数以减少冲突。
     *
     * @return 下一个整型ID（非负数）
     * @throws UnsupportedOperationException 如果不支持生成整型ID，则抛出此异常
     */
    @Override
    public int generateNextIntID() throws UnsupportedOperationException {
        // 取当前时间毫秒值的一部分与随机数混合运算生成ID
        long timestampPart = System.currentTimeMillis() & 0xFFFF; // 取低16位
        int randomPart = secureRandom.nextInt(Short.MAX_VALUE); // 生成短整型范围内的随机数
        int result = (int) ((timestampPart << 16) | randomPart); // 组合成32位整型值
        return Math.abs(result); // 使用Math.abs确保返回非负数
    }

    /**
     * 生成下一个长整型ID，结合时间戳和随机数以减少冲突。
     *
     * @return 下一个长整型ID（非负数）
     * @throws UnsupportedOperationException 如果不支持生成长整型ID，则抛出此异常
     */
    @Override
    public long generateNextLongID() throws UnsupportedOperationException {
        // 取当前时间毫秒值的一部分与64位随机数混合运算生成ID
        long timestampPart = System.currentTimeMillis();
        long randomPart = secureRandom.nextLong();
        long result = (timestampPart << 32) | (randomPart & 0xFFFFFFFFL); // 高32位为时间戳，低32位为随机数
        return Math.abs(result); // 使用Math.abs确保返回非负数
    }
}
