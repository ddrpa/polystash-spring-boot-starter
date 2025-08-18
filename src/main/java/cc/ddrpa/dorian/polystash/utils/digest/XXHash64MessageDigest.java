package cc.ddrpa.dorian.polystash.utils.digest;

import net.jpountz.xxhash.StreamingXXHash64;
import net.jpountz.xxhash.XXHashFactory;

import java.security.MessageDigest;

/**
 * XXHash64 消息摘要实现类，提供快速的 64 位哈希计算功能。
 * <p>
 * 该类继承自 Java 标准的 MessageDigest 类，实现了 XXHash64 算法。
 * XXHash64 是一个极快的非加密哈希算法，由 Yann Collet 开发，
 * 提供了优秀的性能（比 MD5 快约 15 倍）和良好的碰撞抗性。
 * <p>
 * 主要特点：
 * <ul>
 *   <li>极高的计算速度，适合大数据量的哈希计算</li>
 *   <li>64 位输出，提供足够的哈希空间</li>
 *   <li>支持流式处理，内存占用低</li>
 *   <li>非加密安全，仅适用于数据完整性检查</li>
 * </ul>
 * <p>
 * 适用于需要快速计算校验和的场景，如文件完整性验证、
 * 缓存键生成、数据去重等。
 *
 * @see MessageDigest
 * @see SupportedChecksumAlgorithm
 */
public class XXHash64MessageDigest extends MessageDigest {

    /**
     * XXHash64 算法的种子值。
     * <p>
     * 使用 0 作为种子值，这是 XXHash64 的标准配置。
     * 种子值影响哈希结果，相同的种子值会产生相同的哈希结果。
     */
    private static final long SEED = 0L;

    /**
     * XXHash64 流式哈希计算器。
     * <p>
     * 支持增量更新数据，适用于处理大文件或流式数据。
     */
    private final StreamingXXHash64 hasher;

    /**
     * 构造函数，创建 XXHash64 消息摘要实例。
     * <p>
     * 初始化流式哈希计算器，使用最快的 XXHash64 实现。
     */
    public XXHash64MessageDigest() {
        super("XXHash64");
        this.hasher = XXHashFactory.fastestInstance()
                .newStreamingHash64(SEED);
    }

    /**
     * 更新单个字节的哈希计算。
     * <p>
     * 将单个字节添加到哈希计算中，支持增量更新。
     *
     * @param input 要添加到哈希计算的字节
     */
    @Override
    protected void engineUpdate(byte input) {
        hasher.update(new byte[]{input}, 0, 1);
    }

    /**
     * 更新字节数组的哈希计算。
     * <p>
     * 将指定范围的字节数组添加到哈希计算中，支持增量更新。
     *
     * @param input  要添加到哈希计算的字节数组
     * @param offset 字节数组中的起始偏移量
     * @param len    要处理的字节数量
     */
    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        hasher.update(input, offset, len);
    }

    /**
     * 完成哈希计算并返回结果。
     * <p>
     * 计算最终的哈希值，将 64 位长整型转换为 8 字节的字节数组。
     * 计算完成后自动重置哈希器状态，准备下一次计算。
     *
     * @return 包含 64 位哈希值的 8 字节数组
     */
    @Override
    protected byte[] engineDigest() {
        long value = hasher.getValue();
        engineReset();
        return new byte[]{
                (byte) (value >>> 56),
                (byte) (value >>> 48),
                (byte) (value >>> 40),
                (byte) (value >>> 32),
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };
    }

    /**
     * 重置哈希器状态。
     * <p>
     * 清空之前的所有输入数据，准备开始新的哈希计算。
     * 重置后可以重新使用同一个实例进行新的哈希计算。
     */
    @Override
    protected void engineReset() {
        // 重新创建一个同样种子的新 hasher
        hasher.reset();
    }
}
