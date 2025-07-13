package com.github.zhitron.id_generator.impl;

import com.github.zhitron.id_generator.IDGenerator;

import java.util.UUID;

/**
 * 使用UUID实现的ID生成器。
 * @author zhitron
 */
public class UUIDGenerator implements IDGenerator {

    /**
     * 生成下一个整型ID，通过UUID的哈希值生成。
     *
     * @return 下一个整型ID
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
     *
     * @return 下一个长整型ID
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
     *
     * @return 下一个字符串类型ID
     */
    @Override
    public String generateNextStringID() {
        return UUID.randomUUID().toString();
    }
}
