package cc.ddrpa.dorian.polystash.utils.digest;

/**
 * 支持的校验和算法接口，定义了 PolyStash 支持的校验和算法常量。
 * <p>
 * 该接口定义了用于数据完整性验证的校验和算法标识符。
 * 通过统一的常量定义，确保在整个系统中使用一致的算法名称。
 * <p>
 * 当前支持的算法包括 xxHash64，这是一个快速的非加密哈希算法，
 * 适用于数据完整性检查但不适用于安全目的。
 */
public interface SupportedChecksumAlgorithm {

    /**
     * xxHash64 算法标识符。
     * <p>
     * xxHash64 是一个极快的非加密哈希算法，由 Yann Collet 开发。
     * 它提供了优秀的性能（比 MD5 快约 15 倍）和良好的碰撞抗性，
     * 特别适用于数据完整性验证和快速哈希计算。
     * <p>
     * 注意：xxHash64 不是加密安全的，仅适用于数据完整性检查。
     */
    String ALG_XXHASH_64 = "xxHash64";
}