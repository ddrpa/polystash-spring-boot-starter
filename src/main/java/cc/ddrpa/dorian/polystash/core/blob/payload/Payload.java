package cc.ddrpa.dorian.polystash.core.blob.payload;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * 数据载荷抽象基类，用于封装不同类型的数据内容。
 * <p>
 * 该抽象类定义了数据载荷的基本接口，支持将各种类型的数据转换为
 * 统一的 InputStream 格式，便于在存储系统中进行传输和处理。
 * <p>
 * Payload 类实现了 Closeable 接口，确保资源能够被正确释放。
 * 不同的实现类可以封装不同类型的数据源，如字节数组、文件、网络流等。
 * <p>
 * 泛型参数 T 表示原始数据的类型，具体实现类应该根据实际需求
 * 选择合适的类型。
 *
 * @param <T> 原始数据的类型
 */
public abstract class Payload<T> implements Closeable {

    /**
     * 原始数据内容。
     * <p>
     * 存储载荷的原始数据，具体类型由泛型参数 T 决定。
     */
    protected final T content;

    /**
     * 构造函数，使用指定的内容创建 Payload 实例。
     * <p>
     * 子类应该调用此构造函数来初始化内容字段。
     *
     * @param content 要封装的数据内容
     */
    protected Payload(T content) {
        this.content = content;
    }

    /**
     * 将数据转换为输入流。
     * <p>
     * 抽象方法，子类必须实现此方法来提供数据的流式访问。
     * 返回的 InputStream 应该包含完整的载荷数据。
     * <p>
     * 调用方负责关闭返回的 InputStream 以释放相关资源。
     * </p>
     *
     * @return 包含载荷数据的输入流
     * @throws IOException 当创建输入流失败时抛出
     */
    public abstract InputStream stream() throws IOException;

    /**
     * 返回原始数据内容。
     * <p>
     * 提供对原始数据的直接访问，适用于需要直接操作原始数据类型的场景。
     * 返回的数据类型由泛型参数 T 决定。
     *
     * @return 载荷的原始数据内容
     */
    public T raw() {
        return content;
    }

    /**
     * 检查数据是否可重复读取。
     * <p>
     * 某些数据源（如 InputStream）只能读取一次，而其他数据源
     * （如字节数组、文件）可以多次读取。此方法用于判断数据的
     * 重复读取能力。
     * <p>
     * 默认实现返回 false，子类应该根据实际的数据特性重写此方法。
     *
     * @return true 如果数据可以重复读取，false 如果不能重复读取
     */
    public boolean isRepeatable() {
        return false;
    }

    /**
     * 关闭载荷并释放相关资源。
     * <p>
     * 默认实现不执行任何操作，子类应该重写此方法来释放
     * 特定的资源，如文件句柄、网络连接等。
     * <p>
     * 实现应该确保即使发生异常也能正确释放资源。
     *
     * @throws IOException 当关闭操作失败时抛出
     */
    @Override
    public void close() throws IOException {
        // do nothing
    }
}