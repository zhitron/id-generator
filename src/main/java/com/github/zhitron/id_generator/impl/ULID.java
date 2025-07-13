package com.github.zhitron.id_generator.impl;


import java.io.Serial;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 表示ULID的类。
 * <p>
 * ULID 是一个128位的值，包含两个部分：
 * <ul>
 * <li><b>时间部分</b>: 自1970-01-01（Unix纪元）以来的毫秒数。</li>
 * <li><b>随机部分</b>: 由安全随机生成器生成的80位随机序列。</li>
 * </ul>
 * <p>
 * ULID 与 {@link UUID} 兼容128位。和UUID一样，ULID也可以存储为16字节数组。</p>
 * <p>
 * 此类的实例是<b>不可变的</b>。
 * </p>
 */
public final class ULID implements Serializable, Comparable<ULID> {
    /**
     * 全部128位都为0的特殊ULID。
     */
    public static final ULID MIN = new ULID(0x0000000000000000L, 0x0000000000000000L);
    /**
     * 全部128位都为1的特殊ULID。
     */
    public static final ULID MAX = new ULID(0xffffffffffffffffL, 0xffffffffffffffffL);
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 字符映射数组，用于快速查找字符对应的数值。
     */
    private static final long[] ALPHABET_VALUES = new long[256];

    /**
     * 大写字母表，用于编码ULID的字符集。
     */
    private static final char[] ALPHABET_UPPERCASE = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    /**
     * 小写字母表，用于编码ULID的字符集。
     */
    private static final char[] ALPHABET_LOWERCASE = "0123456789abcdefghjkmnpqrstvwxyz".toCharArray();

    static {
        // 初始化字符映射数组为-1，表示未映射
        Arrays.fill(ALPHABET_VALUES, -1);

        // 映射大写字母到对应的数值
        for (int i = 0; i < ALPHABET_UPPERCASE.length; i++) {
            ALPHABET_VALUES[ALPHABET_UPPERCASE[i]] = i;
        }

        // 映射小写字母到对应的数值
        for (int i = 0; i < ALPHABET_LOWERCASE.length; i++) {
            ALPHABET_VALUES[ALPHABET_LOWERCASE[i]] = i;
        }

        // 特殊处理字母O、I、L的大写形式（映射到特定值）
        ALPHABET_VALUES['O'] = 0x00;
        ALPHABET_VALUES['I'] = 0x01;
        ALPHABET_VALUES['L'] = 0x01;

        // 特殊处理字母o、i、l的小写形式（映射到特定值）
        ALPHABET_VALUES['o'] = 0x00;
        ALPHABET_VALUES['i'] = 0x01;
        ALPHABET_VALUES['l'] = 0x01;
    }

    /**
     * 最重要的64位（时间戳和部分随机数）
     */
    private final long msb;

    /**
     * 最不重要的64位（随机数部分）
     */
    private final long lsb;

    /**
     * 创建一个新的ULID。
     * <p>
     * 用于复制现有的ULID实例。
     *
     * @param input 一个ULID
     */
    public ULID(ULID input) {
        this.msb = input.msb;
        this.lsb = input.lsb;
    }

    /**
     * 将UUID转换为ULID。
     *
     * @param input 一个UUID
     */
    public ULID(UUID input) {
        this.msb = input.getMostSignificantBits();
        this.lsb = input.getLeastSignificantBits();
    }

    /**
     * 创建一个新的ULID。
     * <p>
     * 如果你想复制一个{@link UUID}，请使用这个构造函数。
     *
     * @param mostSignificantBits  前8个字节的long值表示
     * @param leastSignificantBits 后8个字节的long值表示
     */
    public ULID(long mostSignificantBits, long leastSignificantBits) {
        this.msb = mostSignificantBits;
        this.lsb = leastSignificantBits;
    }

    /**
     * 创建一个新的ULID。
     * <p>
     * 时间参数是从1970-01-01（Unix纪元）开始的毫秒数。它必须是一个不超过2^48-1的正数。
     * <p>
     * random参数必须是包含10个字节的任意数组。
     * <p>
     * 注意：由于嵌入的时间戳被内部视为无符号整数，ULID不能由1970-01-01之前的日期组成，
     * 它只能表示从零开始到2^48-1的自然数集合。
     *
     * @param time   自1970-01-01以来的毫秒数
     * @param random 一个长度为10的字节数组
     * @throws IllegalArgumentException 如果时间是负数或大于2^48-1
     * @throws IllegalArgumentException 如果random为空或其长度不等于10
     */
    public ULID(long time, byte[] random) {

        // 时间部分占48位。
        if ((time & 0xffff000000000000L) != 0) {
            // ULID规范：
            // "任何尝试解码或编码超过此限制的ULID（time > 2^48-1）
            // 都应被所有实现拒绝，以防止溢出错误。"
            throw new IllegalArgumentException("无效的时间值"); // 溢出或负时间!
        }
        // 随机部分占80位（10个字节）。
        if (random == null || random.length != 10) {
            throw new IllegalArgumentException("无效的随机字节，随机字节长度必须为10"); // 空或长度错误!
        }

        long msb = 0;
        long lsb = 0;

        msb |= time << 16;
        msb |= (long) (random[0x0] & 0xff) << 8;
        msb |= random[0x1] & 0xff;

        lsb |= (long) (random[0x2] & 0xff) << 56;
        lsb |= (long) (random[0x3] & 0xff) << 48;
        lsb |= (long) (random[0x4] & 0xff) << 40;
        lsb |= (long) (random[0x5] & 0xff) << 32;
        lsb |= (long) (random[0x6] & 0xff) << 24;
        lsb |= (long) (random[0x7] & 0xff) << 16;
        lsb |= (long) (random[0x8] & 0xff) << 8;
        lsb |= random[0x9] & 0xff;

        this.msb = msb;
        this.lsb = lsb;
    }

    /**
     * 使用默认的安全随机数生成器创建一个新的ULID
     *
     * @return 一个新的ULID实例
     */
    public static ULID randomULID() {
        return ULID.randomULID(new SecureRandom());
    }

    /**
     * 使用指定的随机数生成器创建一个新的ULID
     *
     * @param random 随机数生成器
     * @return 一个新的ULID实例
     */
    public static ULID randomULID(Random random) {
        if (random == null) random = ThreadLocalRandom.current();
        long msb = System.currentTimeMillis() << 16 | random.nextLong() & 0xffffL;
        long lsb = random.nextLong();
        return new ULID(msb, lsb);
    }

    /**
     * 返回指定时间的最小ULID
     * <p>
     * 时间部分使用指定时间填充，随机部分全部为0
     *
     * @param time 自1970-01-01以来的毫秒数
     * @return 一个新的ULID实例
     * @since 5.2.0
     */
    public static ULID min(long time) {
        return new ULID(time << 16, 0x0000000000000000L);
    }

    /**
     * 返回指定时间的最大ULID
     * <p>
     * 时间部分使用指定时间填充，随机部分全部为1
     *
     * @param time 自1970-01-01以来的毫秒数
     * @return 一个新的ULID实例
     * @since 5.2.0
     */
    public static ULID max(long time) {
        return new ULID((time << 16) | 0xffffL, 0xffffffffffffffffL);
    }

    /**
     * 创建ULID的通用方法
     *
     * @param time          时间戳
     * @param entropy       随机数生成器
     * @param useByteRandom 是否使用字节随机数
     * @param lastULID      上一个ULID实例（用于生成单调递增ULID）
     * @return 一个新的ULID实例
     */
    public static ULID randomULID(long time, Random entropy, boolean useByteRandom, ULID lastULID) {
        if (lastULID == null) {
            if (useByteRandom) {
                byte[] bytes = new byte[10];
                entropy.nextBytes(bytes);
                return new ULID(time, bytes);
            } else {
                return new ULID((time << 16) | (entropy.nextLong() & 0xffffL), entropy.nextLong());
            }
        } else {
            // 创建单调递增ULID
            final long lastTime = lastULID.getTime();

            // 检查当前时间是否与上一次相同或倒退了（系统时钟调整或闰秒后）
            // 偏移容差 = (上一次时间 - 10秒) < 当前时间 <= 上一次时间
            if ((time > lastTime - 10_000) && (time <= lastTime)) {
                lastULID = lastULID.increment();
            } else {
                lastULID = ULID.randomULID(time, entropy, useByteRandom, null);
            }
            return lastULID;
        }
    }

    /**
     * 从字符数组创建ULID
     *
     * @param chars 包含ULID的字符数组
     * @return 一个新的ULID实例
     * @throws IllegalArgumentException 如果输入字符无效
     */
    public static ULID randomULID(long time, char[] chars) {
        checkCharArray(chars);
        long random0 = 0;
        long random1 = 0;

        random0 |= ALPHABET_VALUES[chars[0x0a]] << 35;
        random0 |= ALPHABET_VALUES[chars[0x0b]] << 30;
        random0 |= ALPHABET_VALUES[chars[0x0c]] << 25;
        random0 |= ALPHABET_VALUES[chars[0x0d]] << 20;
        random0 |= ALPHABET_VALUES[chars[0x0e]] << 15;
        random0 |= ALPHABET_VALUES[chars[0x0f]] << 10;
        random0 |= ALPHABET_VALUES[chars[0x10]] << 5;
        random0 |= ALPHABET_VALUES[chars[0x11]];

        random1 |= ALPHABET_VALUES[chars[0x12]] << 35;
        random1 |= ALPHABET_VALUES[chars[0x13]] << 30;
        random1 |= ALPHABET_VALUES[chars[0x14]] << 25;
        random1 |= ALPHABET_VALUES[chars[0x15]] << 20;
        random1 |= ALPHABET_VALUES[chars[0x16]] << 15;
        random1 |= ALPHABET_VALUES[chars[0x17]] << 10;
        random1 |= ALPHABET_VALUES[chars[0x18]] << 5;
        random1 |= ALPHABET_VALUES[chars[0x19]];

        byte[] bytes = new byte[10];

        bytes[0] = (byte) (random0 >>> 32);
        bytes[1] = (byte) (random0 >>> 24);
        bytes[2] = (byte) (random0 >>> 16);
        bytes[3] = (byte) (random0 >>> 8);
        bytes[4] = (byte) (random0);
        bytes[5] = (byte) (random1 >>> 32);
        bytes[6] = (byte) (random1 >>> 24);
        bytes[7] = (byte) (random1 >>> 16);
        bytes[8] = (byte) (random1 >>> 8);
        bytes[9] = (byte) (random1);

        return new ULID(time, bytes);
    }

    /**
     * 创建基于哈希的ULID
     *
     * @param time  时间戳
     * @param bytes 要哈希的字节数组
     * @return 一个新的ULID实例
     */
    public static ULID randomULID(long time, byte[] bytes) {
        // 计算哈希并取前10个字节
        String algorithm = "SHA-256";
        try {
            byte[] hash = MessageDigest.getInstance(algorithm).digest(bytes);
            byte[] rand = Arrays.copyOf(hash, 10);
            return new ULID(time, rand);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(String.format("%s not supported", algorithm));
        }
    }

    /**
     * 将字节数组转换为ULID
     *
     * @param bytes 包含ULID的16字节数组
     * @return 一个新的ULID实例
     * @throws IllegalArgumentException 如果字节数组无效
     */
    public static ULID randomULID(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            throw new IllegalArgumentException("Invalid ULID bytes"); // null或长度错误!
        }
        long msb = 0, lsb = 0;
        msb |= (bytes[0x0] & 0xffL) << 56;
        msb |= (bytes[0x1] & 0xffL) << 48;
        msb |= (bytes[0x2] & 0xffL) << 40;
        msb |= (bytes[0x3] & 0xffL) << 32;
        msb |= (bytes[0x4] & 0xffL) << 24;
        msb |= (bytes[0x5] & 0xffL) << 16;
        msb |= (bytes[0x6] & 0xffL) << 8;
        msb |= (bytes[0x7] & 0xffL);

        lsb |= (bytes[0x8] & 0xffL) << 56;
        lsb |= (bytes[0x9] & 0xffL) << 48;
        lsb |= (bytes[0xa] & 0xffL) << 40;
        lsb |= (bytes[0xb] & 0xffL) << 32;
        lsb |= (bytes[0xc] & 0xffL) << 24;
        lsb |= (bytes[0xd] & 0xffL) << 16;
        lsb |= (bytes[0xe] & 0xffL) << 8;
        lsb |= (bytes[0xf] & 0xffL);
        return new ULID(msb, lsb);
    }

    /**
     * 将标准字符串转换为ULID
     *
     * @param chars 包含ULID的字符数组
     * @return 一个新的ULID实例
     * @throws IllegalArgumentException 如果输入字符串无效
     */
    public static ULID randomULID(char[] chars) {
        checkCharArray(chars);
        long random0 = 0, random1 = 0, random2 = 0;
        for (int i = 0; i < 10; i++) random0 |= ALPHABET_VALUES[chars[i]] << (45 - i * 5);
        for (int i = 0; i < 8; i++) random1 |= ALPHABET_VALUES[chars[0x0a + i]] << (35 - i * 5);
        for (int i = 0; i < 8; i++) random2 |= ALPHABET_VALUES[chars[0x12 + i]] << (35 - i * 5);
        long msb = random0 << 16 | (random1 >>> 24), lsb = random1 << 40 | (random2 & 0xffffffffffL);
        return new ULID(msb, lsb);
    }

    /**
     * 检查字符串是否是有效的ULID。
     * <p>
     * 有效的ULID字符串是一串26个字符，来自的Base 32字母表。
     * <p>
     * 输入字符串的第一个字符必须在0到7之间。
     */
    private static void checkCharArray(final char[] chars) {
        if (chars == null || chars.length != 26) {
            throw new IllegalArgumentException("无效的随机字符，字符长度必须为26"); // 空或长度错误!
        }

        for (char c : chars) {
            try {
                if (ALPHABET_VALUES[c] == -1) {
                    throw new IllegalArgumentException("无效的随机字符 '" + c + "'"); // 空或无效字符!
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("无效的随机字符 '" + c + "', 超出字母表范围");
            }
        }

        // 时间部分占48位。
        // Base32编码的时间部分占50位。
        // 时间部分不能超过2^48-1。
        // 因此，base32解码后时间部分的前两位必须为0。
        // 所以输入字符串的第一个字符必须在0到7之间。
        // ULID规范：
        // "任何尝试解码或编码超过此限制的ULID（time > 2^48-1）
        // 都应被所有实现拒绝，以防止溢出错误。"
        if ((ALPHABET_VALUES[chars[0]] & 0b11000) != 0) {
            throw new IllegalArgumentException("第一个字符无效: '" + chars[0] + "'");
        }
        // 检查通过。
    }

    /**
     * 将ULID转换为UUID。
     * <p>
     * ULID与{@link UUID}在128位上兼容。
     * <p>
     * 如果你需要一个符合RFC-4122的UUIDv4，请这样做：{@code Ulid.toRfc4122().toUuid()}。
     *
     * @return 一个UUID。
     */
    public UUID toUUID() {
        return new UUID(this.msb, this.lsb);
    }

    /**
     * 将ULID转换为字节数组。
     *
     * @return 一个字节数组。
     */
    public byte[] toBytes() {
        byte[] bytes = new byte[16];
        bytes[0x0] = (byte) (msb >>> 56);
        bytes[0x1] = (byte) (msb >>> 48);
        bytes[0x2] = (byte) (msb >>> 40);
        bytes[0x3] = (byte) (msb >>> 32);
        bytes[0x4] = (byte) (msb >>> 24);
        bytes[0x5] = (byte) (msb >>> 16);
        bytes[0x6] = (byte) (msb >>> 8);
        bytes[0x7] = (byte) (msb);
        bytes[0x8] = (byte) (lsb >>> 56);
        bytes[0x9] = (byte) (lsb >>> 48);
        bytes[0xa] = (byte) (lsb >>> 40);
        bytes[0xb] = (byte) (lsb >>> 32);
        bytes[0xc] = (byte) (lsb >>> 24);
        bytes[0xd] = (byte) (lsb >>> 16);
        bytes[0xe] = (byte) (lsb >>> 8);
        bytes[0xf] = (byte) (lsb);
        return bytes;
    }

    /**
     * 将ULID转换为标准的大写字符串。
     * <p>
     * 输出字符串的长度为26个字符，且仅包含Crockford的Base 32字母表中的字符。
     * <p>
     * 如果你需要小写字符串，请使用快捷方法 {@code Ulid#toLowerCase()}，
     * 而不是使用 {@code Ulid#toString()#toLowerCase()}。
     *
     * @return 一个ULID字符串
     * @see <a href="https://www.crockford.com/base32.html">Crockford's Base 32</a>
     */
    @Override
    public String toString() {
        char[] chars = new char[26];

        long time = this.msb >>> 16;
        long random0 = ((this.msb & 0xffffL) << 24) | (this.lsb >>> 40);
        long random1 = (this.lsb & 0xffffffffffL);

        chars[0x00] = ALPHABET_UPPERCASE[(int) (time >>> 45 & 0b11111)];
        chars[0x01] = ALPHABET_UPPERCASE[(int) (time >>> 40 & 0b11111)];
        chars[0x02] = ALPHABET_UPPERCASE[(int) (time >>> 35 & 0b11111)];
        chars[0x03] = ALPHABET_UPPERCASE[(int) (time >>> 30 & 0b11111)];
        chars[0x04] = ALPHABET_UPPERCASE[(int) (time >>> 25 & 0b11111)];
        chars[0x05] = ALPHABET_UPPERCASE[(int) (time >>> 20 & 0b11111)];
        chars[0x06] = ALPHABET_UPPERCASE[(int) (time >>> 15 & 0b11111)];
        chars[0x07] = ALPHABET_UPPERCASE[(int) (time >>> 10 & 0b11111)];
        chars[0x08] = ALPHABET_UPPERCASE[(int) (time >>> 5 & 0b11111)];
        chars[0x09] = ALPHABET_UPPERCASE[(int) (time & 0b11111)];

        chars[0x0a] = ALPHABET_UPPERCASE[(int) (random0 >>> 35 & 0b11111)];
        chars[0x0b] = ALPHABET_UPPERCASE[(int) (random0 >>> 30 & 0b11111)];
        chars[0x0c] = ALPHABET_UPPERCASE[(int) (random0 >>> 25 & 0b11111)];
        chars[0x0d] = ALPHABET_UPPERCASE[(int) (random0 >>> 20 & 0b11111)];
        chars[0x0e] = ALPHABET_UPPERCASE[(int) (random0 >>> 15 & 0b11111)];
        chars[0x0f] = ALPHABET_UPPERCASE[(int) (random0 >>> 10 & 0b11111)];
        chars[0x10] = ALPHABET_UPPERCASE[(int) (random0 >>> 5 & 0b11111)];
        chars[0x11] = ALPHABET_UPPERCASE[(int) (random0 & 0b11111)];

        chars[0x12] = ALPHABET_UPPERCASE[(int) (random1 >>> 35 & 0b11111)];
        chars[0x13] = ALPHABET_UPPERCASE[(int) (random1 >>> 30 & 0b11111)];
        chars[0x14] = ALPHABET_UPPERCASE[(int) (random1 >>> 25 & 0b11111)];
        chars[0x15] = ALPHABET_UPPERCASE[(int) (random1 >>> 20 & 0b11111)];
        chars[0x16] = ALPHABET_UPPERCASE[(int) (random1 >>> 15 & 0b11111)];
        chars[0x17] = ALPHABET_UPPERCASE[(int) (random1 >>> 10 & 0b11111)];
        chars[0x18] = ALPHABET_UPPERCASE[(int) (random1 >>> 5 & 0b11111)];
        chars[0x19] = ALPHABET_UPPERCASE[(int) (random1 & 0b11111)];

        return new String(chars);
    }

    /**
     * 将ULID转换为另一个与UUIDv4兼容的ULID。
     * <p>
     * 返回的ULID的字节符合RFC-4122版本4规范。
     * <p>
     * 如果你需要一个符合RFC-4122的UUIDv4，请这样做：{@code Ulid.toRfc4122().toUuid()}。
     * <p>
     * <b>注意：</b>如果你使用此方法，将无法还原为原始的ULID，因为它会修改其中的6位以生成UUIDv4。
     *
     * @return 一个ULID
     * @see <a href="https://www.rfc-editor.org/rfc/rfc4122">RFC-4122</a>
     */
    public ULID toRfc4122() {

        // 设置第7个字节的最高4位为 0, 1, 0, 0
        final long msb4 = (this.msb & 0xffffffffffff0fffL) | 0x0000000000004000L; // RFC-4122 版本4
        // 设置第9个字节的最高2位为 1, 0
        final long lsb4 = (this.lsb & 0x3fffffffffffffffL) | 0x8000000000000000L; // RFC-4122 变体2

        return new ULID(msb4, lsb4);
    }

    /**
     * 获取创建ULID时的时间戳。
     * <p>
     * 该时间戳是从时间组件中提取的。
     *
     * @return 创建时间的{@link Instant}
     */
    public Instant getInstant() {
        return Instant.ofEpochMilli(this.getTime());
    }

    /**
     * 获取时间组件的数值。
     * <p>
     * 时间组件是一个介于0和2^48-1之间的数字。它等同于从1970-01-01（Unix纪元）开始的毫秒数。
     *
     * @return 毫秒数
     */
    public long getTime() {
        return this.msb >>> 16;
    }

    /**
     * 获取随机组件的字节数组。
     * <p>
     * 随机组件是一个包含10个字节（80位）的数组。
     *
     * @return 一个字节数组
     */
    public byte[] getRandom() {

        final byte[] bytes = new byte[10];

        bytes[0x0] = (byte) (msb >>> 8);
        bytes[0x1] = (byte) (msb);

        bytes[0x2] = (byte) (lsb >>> 56);
        bytes[0x3] = (byte) (lsb >>> 48);
        bytes[0x4] = (byte) (lsb >>> 40);
        bytes[0x5] = (byte) (lsb >>> 32);
        bytes[0x6] = (byte) (lsb >>> 24);
        bytes[0x7] = (byte) (lsb >>> 16);
        bytes[0x8] = (byte) (lsb >>> 8);
        bytes[0x9] = (byte) (lsb);

        return bytes;
    }

    /**
     * 获取最高有效位的数值。
     *
     * @return 一个数值。
     */
    public long getMostSignificantBits() {
        return this.msb;
    }

    /**
     * 获取最低有效位的数值。
     *
     * @return 一个数值。
     */
    public long getLeastSignificantBits() {
        return this.lsb;
    }

    /**
     * 通过对当前ULID的随机组件进行递增操作返回一个新的ULID。
     * <p>
     * 由于随机组件包含80位：
     * <ul>
     * <li>(1) 此方法每毫秒最多可以生成1208925819614629174706176 (2^80)个ULID；
     * <li>(2) 此方法在单个毫秒区间内，即使以每毫秒10亿个ULID的不现实速度生成，
     * 也能在99.999999999999992% ((2^80 - 10^9) / (2^80)) 的情况下生成单调递增的ULID。
     * </ul>
     * <p>
     * 由于上述两点，它不会抛出规范中推荐的错误信息。当随机的80位发生溢出时，
     * 时间组件将简单递增以<b>保持单调性</b>。
     *
     * @return 一个新的ULID
     */
    public ULID increment() {
        long newMsb = this.msb;
        long newLsb = this.lsb + 1; // 递增最低有效位
        if (newLsb == 0) {
            newMsb += 1; // 如果最低有效位溢出，则递增最高有效位
        }
        return new ULID(newMsb, newLsb);
    }

    /**
     * 返回ULID的哈希码值。
     */
    @Override
    public int hashCode() {
        final long bits = msb ^ lsb;
        return (int) (bits ^ (bits >>> 32));
    }

    /**
     * 检查另一个ULID是否与此ULID相等。
     */
    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other.getClass() != ULID.class)
            return false;
        ULID that = (ULID) other;
        if (lsb != that.lsb)
            return false;
        else return msb == that.msb;
    }

    /**
     * 将两个ULID作为无符号的128位整数进行比较。
     * <p>
     * 如果两个ULID在最高有效字节中不同，则第一个ULID大于第二个ULID。
     *
     * @param that 要比较的ULID
     * @return 如果{@code this}小于、等于或大于{@code that}，则分别返回-1、0或1
     */
    @Override
    public int compareTo(ULID that) {

        // 用于比较无符号长整型
        final long min = 0x8000000000000000L;

        final long a = this.msb + min;
        final long b = that.msb + min;

        if (a > b)
            return 1;
        else if (a < b)
            return -1;

        final long c = this.lsb + min;
        final long d = that.lsb + min;

        if (c > d)
            return 1;
        else if (c < d)
            return -1;

        return 0;
    }
}
