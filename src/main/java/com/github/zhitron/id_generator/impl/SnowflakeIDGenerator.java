package com.github.zhitron.id_generator.impl;

import com.github.zhitron.id_generator.IDGenerator;

import java.security.MessageDigest;

/**
 * Snowflake ID 生成器实现。
 * 基于时间戳、节点ID和序列号组合生成全局唯一ID。
 *
 * @author zhitron
 */
public class SnowflakeIDGenerator implements IDGenerator {
    // 节点部分占用的位数，支持最多1024个节点（2^10）
    private static final int NODE_BITS = 10;
    // 序列号部分占用的位数，每个节点每毫秒最多生成4096个ID（2^12）
    private static final int SEQUENCE_BITS = 12;

    // 最大序列号值，用于防止同一毫秒内序列号溢出
    private static final int MAX_SEQUENCE = ~(-1 << SEQUENCE_BITS);

    //MD5 消息摘要实例，用于生成整型ID时的哈希计算。在静态初始化块中进行初始化，确保类加载时即可使用。
    private static MessageDigest MD5;

    static {
        try {
            MD5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    // 起始时间戳（纪元），用于计算相对时间戳
    private final long epoch;
    // 当前节点ID（左移后，预留序列号位置）
    private final long nodeId;

    // 上一次生成ID的时间戳，用于检测时钟回拨
    private volatile long lastTimestamp = -1L;
    // 上一次使用的序列号，用于保证同一毫秒内的唯一性
    private volatile long lastSequence = 0L;

    /**
     * 使用默认纪元时间创建Snowflake ID生成器。
     *
     * @param nodeId 节点 ID，范围 [0, 1023]
     */
    public SnowflakeIDGenerator(long nodeId) {
        this(1751308800000L, nodeId);
    }

    /**
     * 创建一个新的 Snowflake ID 生成器。
     *
     * @param epoch  纪元时间（起始时间戳）
     * @param nodeId 节点 ID，范围 [0, 1023]
     */
    public SnowflakeIDGenerator(long epoch, long nodeId) {
        if (epoch < 0) {
            throw new IllegalArgumentException("Epoch must be greater than or equal to 0");
        }

        // 验证节点ID是否在有效范围内
        if (nodeId < 0 || nodeId > ~(-1L << NODE_BITS)) {
            throw new IllegalArgumentException("Node ID must be between 0 and " + (~(-1L << NODE_BITS)));
        }

        this.epoch = epoch;
        // 将节点ID左移至预留位置，为后续组合ID做准备
        this.nodeId = nodeId << SEQUENCE_BITS;
    }

    /**
     * 生成下一个整型ID。
     *
     * @return 下一个整型ID
     * @throws UnsupportedOperationException 如果生成的长整型ID超出int范围
     */
    @Override
    public int generateNextIntID() throws UnsupportedOperationException {
        try {
            long value = this.generateNextLongID();
            byte[] bytes = MD5.digest(String.valueOf(value).getBytes());
            return (bytes[0] << 24 | bytes[1] | bytes[2] << 8 | bytes[3]) & Integer.MAX_VALUE;
        } catch (Exception e) {
            throw new UnsupportedOperationException("The next int ID could not be generated", e);
        }
    }

    /**
     * 生成下一个长整型ID。
     * 根据Snowflake算法组合时间戳、节点ID和序列号生成唯一ID。
     *
     * @return 下一个长整型ID
     * @throws UnsupportedOperationException 如果时钟回拨或序列号在同一毫秒内溢出
     */
    @Override
    public long generateNextLongID() throws UnsupportedOperationException {
        long timestamp = current();

        // 检测时钟回拨
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Time callback");
        }

        synchronized (this) {
            if (timestamp == lastTimestamp) {
                // 同一毫秒内，增加序列号
                lastSequence = (lastSequence + 1) & MAX_SEQUENCE;
                // 如果序列号溢出，则等待下一毫秒
                if (lastSequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                // 不同毫秒，重置序列号
                lastSequence = 0;
            }

            // 更新最后使用的时间戳
            lastTimestamp = timestamp;

            // 组合时间戳、节点ID和序列号生成唯一ID
            timestamp = timestamp - epoch;
            timestamp <<= SEQUENCE_BITS;
            timestamp <<= NODE_BITS;
            timestamp |= nodeId;
            timestamp |= lastSequence;
            return timestamp;
        }
    }

    /**
     * 等待到下一毫秒直到获得新的时间戳。
     * 用于处理序列号溢出的情况。
     *
     * @param lastTimestamp 上一次使用的时间戳
     * @return 新的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = current();
        while (timestamp <= lastTimestamp) {
            timestamp = current();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳（毫秒）。
     * 可被子类重写以提供不同的时间源。
     *
     * @return 当前时间戳（毫秒）
     */
    protected long current() {
        return System.currentTimeMillis();
    }
}
