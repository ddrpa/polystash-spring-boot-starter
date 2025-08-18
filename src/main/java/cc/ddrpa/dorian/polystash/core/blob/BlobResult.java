package cc.ddrpa.dorian.polystash.core.blob;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.minio.errors.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Blob 操作结果包装类，用于处理 Blob 操作的成功和异常情况。
 * <p>
 * 受 MinIO 的 io.minio.Result 类启发，提供了统一的异常处理机制。
 * 当 Blob 操作成功时，返回 Blob 对象；当操作失败时，包装相应的异常信息。
 * <p>
 * 通过 get() 方法可以获取 Blob 对象或抛出相应的异常，实现了统一的
 * 结果处理接口，简化了调用方的异常处理逻辑。
 */
public class BlobResult {

    private final Blob blob;
    private final Throwable t;

    /**
     * 构造函数，创建包含成功结果的 BlobResult 实例。
     * <p>
     * 当 Blob 操作成功完成时使用此构造函数，结果中包含有效的 Blob 对象。
     *
     * @param blob 操作成功返回的 Blob 对象
     */
    public BlobResult(Blob blob) {
        this.blob = blob;
        this.t = null;
    }

    /**
     * 构造函数，创建包含异常结果的 BlobResult 实例。
     * <p>
     * 当 Blob 操作失败时使用此构造函数，结果中包含异常信息。
     *
     * @param ex 操作过程中发生的异常
     */
    public BlobResult(Exception ex) {
        this.blob = null;
        this.t = ex;
    }

    /**
     * 获取 Blob 对象或抛出相应的异常。
     * <p>
     * 如果操作成功，返回 Blob 对象；如果操作失败，根据异常类型
     * 抛出相应的异常。该方法处理了所有可能的异常类型，确保
     * 调用方能够获得明确的错误信息。
     * <p>
     * 支持的异常类型包括：
     * <ul>
     *   <li>MinIO 相关异常（ErrorResponseException、ServerException 等）</li>
     *   <li>IO 异常（IOException、InsufficientDataException 等）</li>
     *   <li>安全相关异常（InvalidKeyException、NoSuchAlgorithmException 等）</li>
     *   <li>JSON 解析异常（JsonParseException、JsonMappingException 等）</li>
     *   <li>其他运行时异常</li>
     * </ul>
     *
     * @return 操作成功时的 Blob 对象
     * @throws ErrorResponseException    当 MinIO 返回错误响应时
     * @throws IllegalArgumentException  当参数无效时
     * @throws InsufficientDataException 当数据不足时
     * @throws InternalException         当 MinIO 内部错误发生时
     * @throws InvalidKeyException       当密钥无效时
     * @throws InvalidResponseException  当响应无效时
     * @throws IOException               当 IO 操作失败时
     * @throws NoSuchAlgorithmException  当算法不可用时
     * @throws ServerException           当服务器错误发生时
     * @throws XmlParserException        当 XML 解析失败时
     * @throws RuntimeException          当遇到未处理的异常类型时
     */
    public Blob get()
            throws ErrorResponseException, IllegalArgumentException, InsufficientDataException,
            InternalException, InvalidKeyException, InvalidResponseException, IOException,
            NoSuchAlgorithmException, ServerException, XmlParserException {
        if (t == null) {
            return blob;
        }

        if (t instanceof ErrorResponseException ere) {
            throw ere;
        } else if (t instanceof IllegalArgumentException iae) {
            throw iae;
        } else if (t instanceof InsufficientDataException ide) {
            throw ide;
        } else if (t instanceof InternalException ie) {
            throw ie;
        } else if (t instanceof InvalidKeyException ike) {
            throw ike;
        } else if (t instanceof InvalidResponseException ire) {
            throw ire;
        } else if (t instanceof IOException ioe) {
            throw ioe;
        } else if (t instanceof JsonMappingException jme) {
            throw jme;
        } else if (t instanceof JsonParseException jpe) {
            throw jpe;
        } else if (t instanceof NoSuchAlgorithmException nae) {
            throw nae;
        } else if (t instanceof ServerException se) {
            throw se;
        } else if (t instanceof XmlParserException xpe) {
            throw xpe;
        } else {
            throw new RuntimeException(
                    String.format("Unhandled exception type '%s' in BlobResult. This indicates a missing exception handler for: %s",
                            t.getClass().getSimpleName(), t.getMessage()), t);
        }
    }
}