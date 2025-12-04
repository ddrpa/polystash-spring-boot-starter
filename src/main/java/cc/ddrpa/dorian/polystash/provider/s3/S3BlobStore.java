package cc.ddrpa.dorian.polystash.provider.s3;

import cc.ddrpa.dorian.polystash.core.blob.Blob;
import cc.ddrpa.dorian.polystash.core.blob.BlobResult;
import cc.ddrpa.dorian.polystash.core.blob.payload.InputStreamPayload;
import cc.ddrpa.dorian.polystash.core.blob.payload.Payload;
import cc.ddrpa.dorian.polystash.core.blobstore.BlobStore;
import cc.ddrpa.dorian.polystash.core.blobstore.ListOptions;
import cc.ddrpa.dorian.polystash.core.exception.BlobNotFoundException;
import cc.ddrpa.dorian.polystash.core.exception.GeneralPolyStashException;
import cc.ddrpa.dorian.polystash.core.exception.IOErrorOccursException;
import cc.ddrpa.dorian.polystash.utils.StringPool;
import cc.ddrpa.dorian.polystash.utils.http.ContentDisposition;
import cc.ddrpa.dorian.polystash.utils.http.URIManipulation;
import io.minio.*;
import io.minio.GetPresignedObjectUrlArgs.Builder;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import okhttp3.Headers;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class S3BlobStore extends BlobStore {

    // 预签名 URL 的过期时间默认为 7 天，与 MinIO 保持一致
    private static final int DEFAULT_PRESIGNED_URL_EXPIRATION_IN_SECONDS = 7 * 24 * 60 * 60;
    private final String bucket;
    private final MinioClient minioClient;
    private final String delimiter;

    public S3BlobStore(String blobStoreName, String endpoint, String bucket, MinioClient minioClient) {
        super(new S3BlobStoreContext(blobStoreName, endpoint, bucket));
        this.bucket = bucket;
        this.delimiter = "/";
        this.minioClient = minioClient;
        replacePublicAccessIdentifierHandler((context, objectName) -> {
            S3BlobStoreContext blobStoreContext = (S3BlobStoreContext) context;
            return URIManipulation.uri(blobStoreContext.getEndpoint(), blobStoreContext.getBucket(),
                    objectName).toString();
        });
    }

    @Override
    public Iterable<BlobResult> list(String prefix, ListOptions listOptions) {
        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(prefix)
                .delimiter(listOptions.delimiter())
                .recursive(listOptions.recursive())
                .build();
        Iterable<Result<Item>> items = minioClient.listObjects(args);
        return new Iterable<>() {
            @NotNull
            @Override
            public Iterator<BlobResult> iterator() {
                return new Iterator<>() {
                    private final Iterator<Result<Item>> itemIterator = items.iterator();

                    @Override
                    public boolean hasNext() {
                        return itemIterator.hasNext();
                    }

                    @Override
                    public BlobResult next() {
                        try {
                            Item s3Object = itemIterator.next().get();
                            return new BlobResult(fromItem(s3Object));
                        } catch (ServerException | InsufficientDataException |
                                 ErrorResponseException |
                                 IOException | NoSuchAlgorithmException | InvalidKeyException |
                                 InvalidResponseException | XmlParserException |
                                 InternalException e) {
                            return new BlobResult(e);
                        }
                    }
                };
            }
        };
    }

    @Override
    public Blob get(String objectName) throws GeneralPolyStashException {
        try {
            GetObjectResponse res = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            return fromGetObjectResponse(res);
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equalsIgnoreCase("NoSuchKey")) {
                throw new BlobNotFoundException(e.errorResponse().message(), e);
            } else {
                throw new IOErrorOccursException(e.errorResponse().message(), e);
            }
        } catch (Exception e) {
            throw new IOErrorOccursException(
                    String.format("Unexpected error occurred while getting object '%s' from bucket '%s'", objectName, bucket), e);
        }
    }

    @Override
    public Blob put(String prefix, String readableName, Payload<?> payload, Map<String, String> userDefinedAttributes, String contentType) throws GeneralPolyStashException {
        String objectName = generateObjectName(prefix);
        return putOrReplace(objectName, readableName, payload, userDefinedAttributes, contentType);
    }

    @Override
    public Blob putOrReplace(String objectName, String readableName, Payload<?> payload, Map<String, String> userDefinedAttributes, String contentType) throws GeneralPolyStashException {
        var argsBuilder = PutObjectArgs.builder();
        if (userDefinedAttributes != null && !userDefinedAttributes.isEmpty()) {
            argsBuilder.userMetadata(userDefinedAttributes);
        }
        try (InputStream inputStream = payload.stream()) {
            argsBuilder.bucket(bucket)
                    .object(objectName)
                    .contentType(contentType)
                    .headers(Map.of("Content-Disposition", ContentDisposition.attachment(readableName)))
                    .stream(inputStream, -1, 5 * 1024 * 1024);
            minioClient.putObject(argsBuilder.build());
        } catch (Exception e) {
            throw new IOErrorOccursException(
                    String.format("Failed to put object '%s' to bucket '%s'", objectName, bucket), e);
        }
        return stat(objectName);
    }

    @Override
    public Blob stat(String objectName) throws GeneralPolyStashException {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build();
            return fromStatObjectResponse(minioClient.statObject(args));
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equalsIgnoreCase("NoSuchKey")) {
                throw new BlobNotFoundException(e.errorResponse().message(), e);
            } else {
                throw new IOErrorOccursException(e.errorResponse().message(), e);
            }
        } catch (ServerException | InsufficientDataException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException |
                 XmlParserException | InternalException e) {
            throw new IOErrorOccursException(
                    String.format("S3 operation failed while getting object metadata for '%s' from bucket '%s'", objectName, bucket), e);
        }
    }

    @Override
    public void remove(String objectName, boolean silent) throws GeneralPolyStashException {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (ErrorResponseException e) {
            // 如果抛出的错误是「不存在该对象」，则忽略异常
            if (!e.errorResponse().code().equalsIgnoreCase("NoSuchKey") && !silent) {
                throw new IOErrorOccursException(e.errorResponse().message(), e);
            }
        } catch (InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | NoSuchAlgorithmException | ServerException |
                 XmlParserException | IOException e) {
            if (silent) {
                return;
            }
            throw new IOErrorOccursException(
                    String.format("S3 operation failed while removing object '%s' from bucket '%s'", objectName, bucket), e);
        }
    }

    @Override
    public boolean exist(String objectName) throws GeneralPolyStashException {
        try {
            Blob blob = stat(objectName);
            return Objects.nonNull(blob);
        } catch (BlobNotFoundException e) {
            return false;
        }
    }

    /**
     * 获取一个预签名的 URL，用于访问对象
     *
     * @param objectName     对象名称
     * @param expireDuration 链接过期时间，默认为 7 天；受到 MinIO 等服务实现的限制，若要设置更长时间，请考虑通过 bucket 的 Policy 设置
     * @return 用于访问文件的 URL
     * @throws BlobNotFoundException 如果指定的对象不存在，抛出此异常
     */
    public String presignObjectUrlToGet(
            @NotNull String objectName,
            @Nullable Duration expireDuration) throws GeneralPolyStashException {
        return presignObjectUrlToGet(objectName, expireDuration, null, null);
    }

    /**
     * 获取一个预签名的 URL，用于访问对象
     *
     * @param objectName       对象名称
     * @param expireDuration   链接过期时间，默认为 7 天；受到 MinIO 等服务实现的限制，若要设置更长时间，请考虑通过 bucket 的 Policy 设置
     * @param extraHeaders     要求客户端发起的请求在请求头中包含指定键值对
     * @param extraQueryParams
     * @return 用于访问文件的 URL
     * @throws BlobNotFoundException 如果指定的对象不存在，抛出此异常
     */
    public String presignObjectUrlToGet(
            @NotNull String objectName,
            @Nullable Duration expireDuration,
            @Nullable Map<String, String> extraHeaders,
            @Nullable Map<String, String> extraQueryParams)
            throws GeneralPolyStashException {
        Builder builder = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .object(objectName);
        if (null == expireDuration) {
            builder.expiry(DEFAULT_PRESIGNED_URL_EXPIRATION_IN_SECONDS, TimeUnit.SECONDS);
        } else {
            builder.expiry(Math.toIntExact(expireDuration.toSeconds()), TimeUnit.SECONDS);
        }
        if (null != extraHeaders && !extraHeaders.isEmpty()) {
            builder.extraHeaders(extraHeaders);
        }
        if (null != extraQueryParams && !extraQueryParams.isEmpty()) {
            builder.extraQueryParams(extraQueryParams);
        }
        try {
            return minioClient.getPresignedObjectUrl(builder.build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equalsIgnoreCase("NoSuchKey")) {
                throw new BlobNotFoundException(e.errorResponse().message(), e);
            } else {
                throw new IOErrorOccursException(e.errorResponse().message(), e);
            }
        } catch (InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | NoSuchAlgorithmException | ServerException |
                 XmlParserException | IOException e) {
            throw new IOErrorOccursException(
                    String.format("Failed to generate presigned URL for object '%s' in bucket '%s'", objectName, bucket), e);
        }
    }

    /**
     * 获取一个预签名的 URL，用于上传对象
     * <p>
     * 注意：用户可以使用预签名的 URL 上传任意文件到对象存储而不需要通过验证。如果此行为不符合预期，请通过 lambda 或其他方式验证文件内容。
     *
     * @param objectName     对象名称
     * @param expireDuration 链接过期时间，默认为 7 天；受到 MinIO 等服务实现的限制，若要设置更长时间，请考虑通过 bucket 的 Policy 设置
     * @return 用于以 HTTP PUT 方式上传文件的 URL
     * @throws IOErrorOccursException
     * @see <a
     * href="https://www.reddit.com/r/aws/comments/zmbw4h/enforce_content_type_during_upload_with_s3_signed/">Enforce
     * content type during upload with S3 signed url</a>
     */
    public String presignObjectUrlToPut(
            @NotNull String objectName,
            @Nullable Duration expireDuration)
            throws IOErrorOccursException {
        return presignObjectUrlToPut(objectName, expireDuration, null, null);
    }

    /**
     * 获取一个预签名的 URL，用于上传对象
     * <p>
     * 注意：用户可以使用预签名的 URL 上传任意文件到对象存储而不需要通过验证。如果此行为不符合预期，请通过 lambda 或其他方式验证文件内容。
     * <p>
     * 注意：不要尝试通过 extraHeaders 参数设置 Content-Type 限制，恶意用户可以绕过这个机制
     *
     * @param objectName       对象名称
     * @param expireDuration   链接过期时间，默认为 7 天；受到 MinIO 等服务实现的限制，若要设置更长时间，请考虑通过 bucket 的 Policy 设置
     * @param extraHeaders     要求客户端发起的请求在请求头中包含指定键值对
     * @param extraQueryParams
     * @return 用于以 HTTP PUT 方式上传文件的 URL
     * @see <a
     * href="https://www.reddit.com/r/aws/comments/zmbw4h/enforce_content_type_during_upload_with_s3_signed/">Enforce
     * content type during upload with S3 signed url</a>
     */
    public String presignObjectUrlToPut(
            @NotNull String objectName,
            @Nullable Duration expireDuration,
            @Nullable Map<String, String> extraHeaders,
            @Nullable Map<String, String> extraQueryParams)
            throws IOErrorOccursException {
        GetPresignedObjectUrlArgs.Builder argsBuilder = GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucket)
                .object(objectName);
        if (null == expireDuration) {
            argsBuilder.expiry(DEFAULT_PRESIGNED_URL_EXPIRATION_IN_SECONDS, TimeUnit.SECONDS);
        } else {
            argsBuilder.expiry(Math.toIntExact(expireDuration.toSeconds()), TimeUnit.SECONDS);
        }
        if (null != extraHeaders && !extraHeaders.isEmpty()) {
            argsBuilder.extraHeaders(extraHeaders);
        }
        if (null != extraQueryParams && !extraQueryParams.isEmpty()) {
            argsBuilder.extraQueryParams(extraQueryParams);
        }
        try {
            return minioClient.getPresignedObjectUrl(argsBuilder.build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException |
                 ServerException | XmlParserException | IOException e) {
            throw new IOErrorOccursException("Unclassified Error", e);
        }
    }

    /**
     * 构造一个 Map，需要通过 POST 方式上传文件时，可以使用这个 Map 作为 form-data 的数据。
     * <p>
     * 注意：恶意用户可伪造 HTTP POST 的 FormData 从而绕过验证。如果此行为不符合预期，请通过 lambda 或其他方式验证文件内容。
     *
     * @param objectName              上传对象的名称
     * @param expireDuration          过期时间
     * @param contentTypePrefix       可指定请求的 Content-Type 为何种模式，例如 image/ 匹配所有图片类型
     * @param contentLengthLowerLimit 上传文件的最小长度
     * @param contentLengthUpperLimit 上传文件的最大长度
     * @return map of form data in HTTP POST
     * @see <a
     * href="https://www.reddit.com/r/aws/comments/zmbw4h/enforce_content_type_during_upload_with_s3_signed/">Enforce
     * content type during upload with S3 signed url</a>
     * @see <a
     * href="https://min.io/docs/minio/linux/developers/java/API.html#getPresignedPostFormData">MinIO
     * API - getPresignedPostFormData</a>
     */
    public Map<String, String> getPresignedPostFormData(
            @NotNull String objectName,
            @Nullable Duration expireDuration,
            @Nullable String contentTypePrefix,
            @Nullable Long contentLengthLowerLimit, @Nullable Long contentLengthUpperLimit)
            throws GeneralPolyStashException {
        ZonedDateTime policyExpiry;
        if (expireDuration == null) {
            policyExpiry = ZonedDateTime.now()
                    .plusSeconds(DEFAULT_PRESIGNED_URL_EXPIRATION_IN_SECONDS);
        } else {
            policyExpiry = ZonedDateTime.now().plus(expireDuration);
        }
        PostPolicy policy = new PostPolicy(this.bucket, policyExpiry);
        policy.addEqualsCondition("key", objectName);
        if (StringUtils.isNotEmpty(contentTypePrefix)) {
            policy.addStartsWithCondition("Content-Type", contentTypePrefix);
        }
        if (contentLengthLowerLimit != null && contentLengthUpperLimit != null
                && contentLengthLowerLimit < contentLengthUpperLimit) {
            policy.addContentLengthRangeCondition(contentLengthLowerLimit, contentLengthUpperLimit);
        }
        try {
            Map<String, String> formData = minioClient.getPresignedPostFormData(policy);
            formData.put("key", objectName);
            return formData;
        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException |
                 NoSuchAlgorithmException | ServerException | XmlParserException e) {
            throw new IOErrorOccursException("Unclassified Error", e);
        }
    }

    @Override
    public Object _raw() {
        return this.minioClient;
    }

//    /**
//     * 构造访问指定对象的公开 URL，当对象的 Prefix 或 Bucket 为 public-read 时可用
//     * <pre>Example: {@code
//     * {
//     *   "Action": [
//     *     "s3:GetObject"
//     *   ],
//     *   "Effect": "Allow",
//     *   "Principal": {
//     *     "AWS": [
//     *       "*"
//     *     ]
//     *   },
//     *   "Resource": [
//     *     "arn:aws:s3:::general/avatar/*",
//     *     "arn:aws:s3:::general/public-resources/*"
//     *   ]
//     * }}</pre>
//     *
//     * @param objectName 对象名称
//     * @return
//     */

    private String generateObjectName(String prefix) {
        // clean delimiter at the start and end of prefix
        if (prefix.startsWith(delimiter)) {
            prefix = prefix.substring(1);
        }
        if (prefix.endsWith(delimiter)) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        if (StringUtils.isBlank(prefix)) {
            return UUID.randomUUID().toString();
        } else {
            return prefix + delimiter + UUID.randomUUID();
        }
    }

    /**
     * 从 StatObjectResponse 构建 S3Blob，没有 payload
     *
     * @param s3StatResp
     * @return
     */
    private Blob fromStatObjectResponse(StatObjectResponse s3StatResp) {
        Blob blob = new Blob()
                .setObjectName(s3StatResp.object())
                .setLastModified(s3StatResp.lastModified().toInstant())
                .setContentType(s3StatResp.contentType())
                .setLength(s3StatResp.size())
                .setLength(s3StatResp.size())
                .setContentType(s3StatResp.contentType());
        String originalETag = s3StatResp.etag();
        // MinIO 返回的 ETag 可能被双引号包裹
        blob.setETag(originalETag.replaceAll("\"", StringPool.EMPTY));
        String filename = null;
        // 尝试从 Content-Disposition header 中获取文件名
        String contentDispositionInHeader = s3StatResp.headers().get("Content-Disposition");
        if (StringUtils.isNotBlank(contentDispositionInHeader)) {
            filename = ContentDisposition.parseFilename(contentDispositionInHeader);
        }
        // 尝试获取 UserDefinedAttributes
        Map<String, String> userMetadata = s3StatResp.userMetadata();
        if (Objects.nonNull(userMetadata) && !userMetadata.isEmpty()) {
            blob.setUserDefinedAttributes(userMetadata);
            // 如果 content-disposition 中没有 filename，则尝试从 userMetadata 中获取
            if (StringUtils.isBlank(filename)) {
                filename = userMetadata.getOrDefault("filename", StringPool.EMPTY);
            }
        }
        if (StringUtils.isNotBlank(filename)) {
            blob.setReadableName(filename);
        }
        return blob;
    }

    /**
     * 从 list 方法中返回的 Item 构建 S3Blob，没有 payload, content-type, content-disposition
     *
     * @param s3ItemResp
     * @return
     */
    private Blob fromItem(Item s3ItemResp) {
        Blob blob = new Blob()
                .setObjectName(s3ItemResp.objectName())
                .setETag(s3ItemResp.etag())
                .setLastModified(s3ItemResp.lastModified().toInstant())
                .setLength(s3ItemResp.size());
        Map<String, String> userMetadata = s3ItemResp.userMetadata();
        if (Objects.nonNull(userMetadata) && !userMetadata.isEmpty()) {
            blob.setUserDefinedAttributes(userMetadata);
        }
        return blob;
    }

    /**
     * 从 GetObjectResponse 构建 S3Blob，有 payload，没有其他信息
     *
     * @param s3GetResp
     * @return
     * @throws IOException
     */
    private Blob fromGetObjectResponse(GetObjectResponse s3GetResp) {
        Headers headers = s3GetResp.headers();
        Blob blob = new Blob()
                .setObjectName(s3GetResp.object())
                .setPayload(new InputStreamPayload(s3GetResp));
        String etag = headers.get("ETag");
        if (StringUtils.isNotBlank(etag)) {
            blob.setETag(etag.replaceAll("\"", StringPool.EMPTY));
        }
        String contentType = headers.get("Content-Type");
        if (StringUtils.isBlank(contentType)) {
            blob.setContentType("application/octet-stream");
        } else {
            blob.setContentType(contentType.trim());
        }
        try {
            blob.setLength(Long.parseLong(headers.get("Content-Length")));
        } catch (Exception ignored) {
        }
        try {
            blob.setLastModified(ZonedDateTime.parse(headers.get("Last-Modified"), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant());
        } catch (Exception ignored) {
        }
        String contentDisposition = headers.get("Content-Disposition");
        if (StringUtils.isNotBlank(contentDisposition)) {
            String readableName = ContentDisposition.parseFilename(contentDisposition);
            blob.setReadableName(readableName);
        }
        return blob;
    }
}
