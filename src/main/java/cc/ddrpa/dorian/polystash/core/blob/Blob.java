package cc.ddrpa.dorian.polystash.core.blob;

import cc.ddrpa.dorian.polystash.core.blob.payload.Payload;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Blob 对象，定义了存储对象的基本属性和操作
 * <p>
 * Blob（Binary Large Object）是对象存储系统中的基本数据单元，
 * 代表一个完整的文件或数据对象
 */
public class Blob {

    /**
     * 用户自定义属性
     * <p>
     * 存储用户定义的键值对属性，用于业务逻辑相关的信息存储
     * 默认为空映射，避免空指针异常
     */
    private Map<String, String> userDefinedAttributes = Collections.emptyMap();

    /**
     * Blob 对象的名称标识符，例如 "example.txt" 或 "images/photo.jpg"
     */
    private String objectName;

    /**
     * 对象的最后修改时间
     * <p>
     * 默认为当前时间，表示对象刚刚被创建或修改
     */
    private Instant lastModified = Instant.now();

    /**
     * 人类可读的文件名，通常与系统生成的对象名称不同，用于用户界面显示
     */
    private String readableName;

    /**
     * 对象内容的 MIME 类型
     */
    private String contentType;

    /**
     * 对象的 ETag 值，用于缓存验证和并发控制
     */
    private String etag;

    /**
     * 对象的字节大小，默认值为 -1，表示大小未设置
     */
    private long length = -1;

    /**
     * 对象的校验和值，用于数据完整性验证
     */
    private String checksum;

    /**
     * 校验和算法标识符，标识用于计算校验和的哈希算法
     */
    private String checksumAlgorithm;

    /**
     * 载荷对象，包含实际的数据
     */
    private Payload payload;

    /**
     * 对象的 Payload 是否可重复读取
     */
    private boolean isRepeatable = false;

    /**
     * 检查 Blob 对象是否包含有效载荷
     */
    public boolean containsPayload() {
        return Objects.nonNull(payload);
    }

    /**
     * 获取 Blob 对象的名称
     *
     * @return 对象的名称标识符
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * 设置对象名称，支持链式调用
     *
     * @param objectName 要设置的对象名称
     * @return 当前 Blob 实例，支持链式调用
     */
    public Blob setObjectName(String objectName) {
        this.objectName = objectName;
        return this;
    }

    /**
     * 获取 Blob 对象的 ETag 值
     *
     * @return 对象的 ETag 值
     */
    public String getETag() {
        return etag;
    }

    /**
     * 设置 ETag，支持链式调用
     *
     * @param etag 要设置的 ETag 值
     * @return 当前 Blob 实例，支持链式调用
     */
    public Blob setETag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * 获取 Blob 对象的最后修改时间
     *
     * @return 对象的最后修改时间
     */
    public Instant getLastModified() {
        return lastModified;
    }

    /**
     * 设置最后修改时间，支持链式调用
     *
     * @param lastModified 要设置的最后修改时间
     * @return 当前 Blob 实例，支持链式调用
     */
    public Blob setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * 获取 Blob 对象的长度（字节数）
     *
     * @return 对象的字节大小
     */
    public long getLength() {
        return length;
    }

    /**
     * 设置字节大小，支持链式调用
     *
     * @param length 要设置的字节大小
     * @return 当前 Blob 实例，支持链式调用
     */
    public Blob setLength(long length) {
        this.length = length;
        return this;
    }

    /**
     * 获取 Blob 对象的内容类型
     *
     * @return 对象的 MIME 内容类型
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 设置内容类型，支持链式调用
     *
     * @param contentType 要设置的内容类型
     * @return 当前 Blob 实例，支持链式调用
     */
    public Blob setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 获取 Blob 对象的校验和
     *
     * @return 对象的校验和值
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * 设置校验和，支持链式调用
     *
     * @param checksum 要设置的校验和值
     * @return 当前 Blob 实例，支持链式调用
     */
    public Blob setChecksum(String checksum) {
        this.checksum = checksum;
        return this;
    }

    /**
     * 获取 Blob 对象的校验和算法
     *
     * @return 校验和算法的名称
     */
    public String getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    /**
     * 设置校验和算法，支持链式调用
     *
     * @param checksumAlgorithm 要设置的校验和算法名称
     * @return 当前 Blob 实例，支持链式调用
     */
    public Blob setChecksumAlgorithm(String checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
        return this;
    }

    /**
     * 获取 Blob 对象的可读文件名
     *
     * @return 人类可读的文件名
     */
    public String getReadableName() {
        return readableName;
    }

    /**
     * 设置可读文件名，支持链式调用
     *
     * @param readableName 要设置的可读文件名
     * @return 当前 Blob 实例，支持链式调用
     */
    public Blob setReadableName(String readableName) {
        this.readableName = readableName;
        return this;
    }

    /**
     * 获取 Blob 对象的用户自定义属性
     *
     * @return 用户自定义属性的键值对映射
     */
    public Map<String, String> getUserDefinedAttributes() {
        return userDefinedAttributes;
    }

    /**
     * 设置用户自定义属性，支持链式调用
     *
     * @param userDefinedAttributes 要设置的用户自定义属性映射
     * @return 当前 Blob 实例，支持链式调用
     */
    public Blob setUserDefinedAttributes(@NotNull Map<String, String> userDefinedAttributes) {
        this.userDefinedAttributes = userDefinedAttributes;
        return this;
    }

    /**
     * 获取 Blob 对象的载荷内容
     *
     * @return 包含对象数据的 Payload 实例
     */
    public Payload getPayload() {
        return payload;
    }

    /**
     * 设置文件载荷，支持链式调用
     *
     * @param payload 要设置的文件载荷对象
     * @return 当前 Blob 实例，支持链式调用
     */
    public Blob setPayload(@NotNull Payload payload) {
        this.payload = payload;
        this.isRepeatable = payload.isRepeatable();
        return this;
    }

    /**
     * 检查 Blob 对象是否可重复读取
     * <p>
     * Blob 始终支持重复读取，因此返回 true
     *
     * @return true，表示可以重复读取
     */
    public boolean isRepeatable() {
        return isRepeatable;
    }

    public Blob setRepeatable(boolean repeatable) {
        this.isRepeatable = repeatable;
        return this;
    }
}
