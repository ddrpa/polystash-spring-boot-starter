package cc.ddrpa.dorian.polystash.core.blob.payload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 字节数组载荷实现类，用于封装字节数组数据。
 * <p>
 * 该类继承自 Payload 抽象类，专门用于处理字节数组类型的数据。
 * 字节数组载荷具有以下特点：
 * <ul>
 *   <li>支持重复读取，可以多次创建输入流</li>
 *   <li>数据完全加载到内存中，访问速度快</li>
 *   <li>适合处理中小型数据</li>
 *   <li>内存占用相对较高</li>
 * </ul>
 * <p>
 * 适用于需要频繁访问或重复读取数据的场景，如缓存数据、
 * 临时数据等。
 *
 * @see Payload
 */
public class ByteArrayPayload extends Payload<byte[]> {

    /**
     * 构造函数，使用指定的字节数组创建载荷实例。
     *
     * @param content 要封装的字节数组数据
     */
    public ByteArrayPayload(byte[] content) {
        super(content);
    }

    /**
     * 获取载荷数据的长度（字节数）。
     * <p>
     * 直接返回字节数组的长度，提供快速的大小查询。
     *
     * @return 数据的字节长度
     */
    public long length() {
        return this.content.length;
    }

    /**
     * 获取原始的字节数组数据。
     * <p>
     * 提供对原始字节数组的直接访问，适用于需要直接操作
     * 字节数据的场景。
     *
     * @return 原始的字节数组数据
     */
    public byte[] bytes() {
        return this.content;
    }

    /**
     * 将字节数组转换为输入流。
     * <p>
     * 每次调用都会创建一个新的 ByteArrayInputStream 实例，
     * 支持多个并发读取操作。
     *
     * @return 包含字节数组数据的输入流
     * @throws IOException 当创建输入流失败时抛出（对于字节数组通常不会发生）
     */
    @Override
    public InputStream stream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    /**
     * 检查数据是否可重复读取。
     * <p>
     * 字节数组载荷支持重复读取，每次调用 stream() 方法都会
     * 返回新的输入流实例。
     *
     * @return true，表示字节数组可以重复读取
     */
    @Override
    public boolean isRepeatable() {
        return true;
    }
}