package cc.ddrpa.dorian.polystash.core.blobstore;

import cc.ddrpa.dorian.polystash.core.blob.Blob;
import cc.ddrpa.dorian.polystash.core.blob.BlobResult;
import cc.ddrpa.dorian.polystash.core.blob.payload.Payload;
import cc.ddrpa.dorian.polystash.core.exception.GeneralPolyStashException;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;

/**
 * 抽象 Blob 存储类，定义了对象存储的基本操作接口。
 * <p>
 * 该类提供了统一的 Blob 存储抽象，支持多种存储后端（如文件系统、S3 等）。
 * 主要功能包括：
 * <ul>
 *   <li>Blob 的增删改查操作</li>
 *   <li>目录列表和遍历</li>
 *   <li>元数据管理</li>
 *   <li>公共访问 URL 生成</li>
 * </ul>
 * <p>
 * 实现类需要继承此类并实现所有抽象方法，以提供具体的存储后端实现。
 */
public abstract class BlobStore {

    protected final BlobStoreContext context;
    protected BlobPublicAccessIdentifierHandler publicAccessIdentifierHandler = (BlobStoreContext ctx, String objectName) -> {
        throw new NotImplementedException(
                String.format("No public access identifier handler implemented for blob store '%s' and object '%s'",
                        ctx.getBlobStoreName(), objectName));
    };

    /**
     * 构造函数，初始化 BlobStore 实例。
     *
     * @param context BlobStore 的上下文信息，包含存储名称等配置
     */
    protected BlobStore(BlobStoreContext context) {
        this.context = context;
    }

    /**
     * 获取 BlobStore 的名称。
     *
     * @return BlobStore 的名称标识符
     */
    public String getBlobStoreName() {
        return context.getBlobStoreName();
    }

    /**
     * 替换默认的用于生成公开访问 URL 的处理器。
     * <p>
     * 通过此方法可以自定义如何生成对象的公共访问标识符，
     * 例如生成预签名 URL 或公共访问链接。
     *
     * @param handler 自定义的公共访问标识符处理器
     * @return 当前 BlobStore 实例，支持链式调用
     */
    public BlobStore replacePublicAccessIdentifierHandler(
            BlobPublicAccessIdentifierHandler handler) {
        this.publicAccessIdentifierHandler = handler;
        return this;
    }

    /**
     * 返回底层操作对象。
     * <p>
     * 此方法用于访问存储后端的原生对象，例如 AWS S3 的 S3Client、
     * 文件系统的 Path 对象等。主要用于高级操作和调试。
     *
     * @return 底层存储后端的原生对象
     */
    public abstract Object _raw();

    /**
     * 列举指定前缀下的所有 Blob 对象。
     * <p>
     * 根据提供的前缀和列表选项，返回符合条件的所有 Blob 对象。
     * 支持递归和非递归模式，可以指定路径分隔符等选项。
     *
     * @param prefix      对象名称的前缀，用于过滤结果
     * @param listOptions 列表选项，包含分隔符、递归等配置
     * @return 包含 BlobResult 的迭代器，每个元素代表一个匹配的对象
     * @throws GeneralPolyStashException 当列举操作失败时抛出
     */
    public abstract Iterable<BlobResult> list(String prefix, ListOptions listOptions)
            throws GeneralPolyStashException;

    /**
     * 获取指定名称的 Blob 对象。
     * <p>
     * 根据对象名称从存储中检索对应的 Blob 对象，包括其元数据和内容。
     * 如果对象不存在，将抛出相应的异常。
     *
     * @param objectName 要获取的对象名称
     * @return 包含对象信息和内容的 Blob 实例
     * @throws GeneralPolyStashException 当获取操作失败或对象不存在时抛出
     */
    public abstract Blob get(String objectName) throws GeneralPolyStashException;

    /**
     * 将数据存储到指定的前缀路径下。
     * <p>
     * 将提供的数据内容存储到存储系统中，支持自定义元数据和内容类型。
     * 如果指定前缀的目录不存在，将自动创建。
     *
     * @param prefix                存储路径的前缀
     * @param readableName          人类可读的文件名
     * @param payload               要存储的数据内容
     * @param userDefinedAttributes 用户自定义的属性键值对
     * @param contentType           内容的 MIME 类型
     * @return 存储成功后的 Blob 对象，包含存储后的元数据
     * @throws GeneralPolyStashException 当存储操作失败时抛出
     */
    public abstract Blob put(String prefix, String readableName, Payload<?> payload, Map<String, String> userDefinedAttributes, String contentType)
            throws GeneralPolyStashException;

    /**
     * 删除指定名称的 Blob 对象。
     * <p>
     * 从存储系统中永久删除指定的对象。如果 silent 为 true，
     * 当对象不存在时不会抛出异常；如果为 false，则会抛出异常。
     *
     * @param objectName 要删除的对象名称
     * @param silent     是否静默删除，true 表示对象不存在时不抛异常
     * @throws GeneralPolyStashException 当删除操作失败时抛出
     */
    public abstract void remove(String objectName, boolean silent) throws GeneralPolyStashException;

    /**
     * 获取指定 Blob 对象的元数据信息。
     * <p>
     * 返回对象的元数据，包括大小、修改时间、内容类型等，
     * 但不包含实际的数据内容。此操作比 get() 方法更轻量。
     *
     * @param objectName 要查询元数据的对象名称
     * @return 包含元数据信息的 Blob 对象
     * @throws GeneralPolyStashException 当查询操作失败或对象不存在时抛出
     */
    public abstract Blob stat(String objectName) throws GeneralPolyStashException;

    /**
     * 判断给定名称的对象是否存在。
     * <p>
     * 检查存储系统中是否存在指定名称的对象，返回布尔值。
     * 此方法不会抛出异常，适合用于对象存在性检查。
     *
     * @param objectName 要检查的对象名称
     * @return true 如果对象存在，false 如果对象不存在
     */
    public abstract boolean exist(String objectName) throws GeneralPolyStashException;

//    /**
//     * 获取指定对象的公共访问标识符。
//     * <p>
//     * 通过配置的处理器生成对象的公共访问标识符，通常是 URL 或访问链接。
//     * 如果未配置处理器，将抛出 NotImplementedException。
//     *
//     * @param objectName 要生成公共访问标识符的对象名称
//     * @return 对象的公共访问标识符（通常是 URL）
//     */
//    public String getPublicAccessIdentifier(String objectName) {
//        return publicAccessIdentifierHandler.get(context, objectName);
//    }
}