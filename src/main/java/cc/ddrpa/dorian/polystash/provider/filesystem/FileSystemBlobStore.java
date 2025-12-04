package cc.ddrpa.dorian.polystash.provider.filesystem;

import cc.ddrpa.dorian.polystash.core.blob.Blob;
import cc.ddrpa.dorian.polystash.core.blob.BlobResult;
import cc.ddrpa.dorian.polystash.core.blob.payload.FilePayload;
import cc.ddrpa.dorian.polystash.core.blob.payload.Payload;
import cc.ddrpa.dorian.polystash.core.blobstore.BlobStore;
import cc.ddrpa.dorian.polystash.core.blobstore.ListOptions;
import cc.ddrpa.dorian.polystash.core.exception.*;
import cc.ddrpa.dorian.polystash.utils.StringPool;
import cc.ddrpa.dorian.polystash.utils.digest.SupportedChecksumAlgorithm;
import cc.ddrpa.dorian.polystash.utils.digest.XXHash64MessageDigest;
import cc.ddrpa.dorian.polystash.utils.filesystem.IAttributeHandler;
import com.google.common.io.BaseEncoding;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FileSystemBlobStore extends BlobStore {

    private final Path baseDir;
    private final String baseDirAsString;
    private final IAttributeHandler attributeHandler;
    private final Pattern directoryPattern = Pattern.compile("^/");

    protected FileSystemBlobStore(String blobStoreName, Path baseDir, IAttributeHandler attributeHandler) {
        super(new FileSystemBlobStoreContext(blobStoreName, baseDir));
        this.baseDir = baseDir;
        this.baseDirAsString = this.baseDir.toString();
        this.attributeHandler = attributeHandler;
    }

    /**
     * 生成一个对象名称，通常用于创建新的对象
     */
    protected Pair<Path, String> generateObjectName(String prefix) throws AccessDeniedException, IOErrorOccursException {
        // 指向磁盘上某个路径
        Path targetPath = this.baseDir.resolve(prefix).normalize();
        // 确保这个路径没有越过 baseDir
        if (!targetPath.startsWith(this.baseDir)) {
            throw new AccessDeniedException(
                    String.format("Access denied: path '%s' is outside of base directory '%s'", targetPath, this.baseDir));
        }
        if (Files.notExists(targetPath)) {
            try {
                Files.createDirectories(targetPath);
            } catch (IOException e) {
                throw new IOErrorOccursException(
                        String.format("Failed to create directory structure for prefix '%s' at path '%s'", prefix, targetPath),
                        e);
            }
        } else if (!Files.isDirectory(targetPath)) {
            throw new IOErrorOccursException(
                    String.format("Target path '%s' exists but is not a directory. Expected directory for prefix '%s'", targetPath, prefix));
        }
        String randomFilename = UUID.randomUUID().toString();
        Path relativePath = Path.of(prefix).resolve(randomFilename);
        Path absoluteFilePath = targetPath.resolve(randomFilename);
        // 统一使用 /
        return Pair.of(absoluteFilePath, relativePath.toString().replace("\\", "/"));
    }

    /**
     * 解构对象名称
     *
     * @param objectName 应当可以指向磁盘上的具体文件
     * @return
     * @throws AccessDeniedException
     * @throws IOErrorOccursException
     */
    protected Pair<Path, String> deconstructObjectName(String objectName) throws AccessDeniedException, IOErrorOccursException {
        // 指向磁盘上某个路径
        Path targetFilePath = this.baseDir.resolve(objectName).normalize();
        // 确保这个路径没有越过 baseDir
        if (!targetFilePath.startsWith(this.baseDir)) {
            throw new AccessDeniedException(
                    String.format("Access denied: object '%s' is outside of base directory '%s'", targetFilePath, this.baseDir));
        }
        if (Files.notExists(targetFilePath)) {
            // 确保这个路径的父级目录存在
            if (Files.notExists(targetFilePath.getParent())) {
                try {
                    Files.createDirectories(targetFilePath.getParent());
                } catch (IOException e) {
                    throw new IOErrorOccursException(
                            String.format("Failed to create parent directories for object '%s' at path '%s'", objectName, targetFilePath), e);
                }
            } else if (!Files.isDirectory(targetFilePath.getParent())) {
                throw new IOErrorOccursException(
                        String.format("Parent path '%s' exists but is not a directory. Expected directory for object '%s'", targetFilePath.getParent(), objectName));
            } else if (!Files.isRegularFile(targetFilePath)) {
                throw new IOErrorOccursException(
                        String.format("Target path '%s' exists but is not a regular file. Expected file for object '%s'", targetFilePath, objectName));
            }
        }
        Path relativePath = Path.of(objectName);
        // 统一使用 /
        return Pair.of(targetFilePath, relativePath.toString().replace("\\", "/"));
    }

    /**
     * 检查目标路径
     *
     * @param objectName
     */
    protected Path objectCheck(String objectName, boolean expectExist) throws AccessDeniedException, IOErrorOccursException, BlobNotFoundException {
        // 指向磁盘上某个路径
        Path targetObjectPath = this.baseDir.resolve(objectName);
        // 确保这个路径没有越过 baseDir
        if (!targetObjectPath.startsWith(this.baseDir)) {
            throw new AccessDeniedException(
                    String.format("Access denied: object path '%s' is outside of base directory '%s'", targetObjectPath, this.baseDir));
        }
        if (expectExist) {
            // 这个路径必须存在且是一个文件
            if (!Files.exists(targetObjectPath) || !Files.isRegularFile(targetObjectPath)) {
                throw new BlobNotFoundException(
                        String.format("Blob not found: object '%s' does not exist or is not a regular file at path '%s'", objectName, targetObjectPath));
            }
        } else {
            // 如果不期望文件存在
            if (Files.exists(targetObjectPath)) {
                throw new AccessDeniedException(
                        String.format("Access denied: object '%s' already exists at path '%s'", objectName, targetObjectPath));
            }
            // 确保这个路径的父级目录存在
            if (Files.notExists(targetObjectPath.getParent())) {
                try {
                    Files.createDirectories(targetObjectPath.getParent());
                } catch (IOException e) {
                    throw new IOErrorOccursException(
                            String.format("Failed to create parent directories for object '%s' at path '%s'", objectName, targetObjectPath), e);
                }
            }
        }
        return targetObjectPath;
    }

    protected String cleanObjectName(Path targetPath) {
        return directoryPattern.matcher(
                        targetPath.toString().replace(baseDirAsString, StringPool.EMPTY))
                .replaceFirst(StringPool.EMPTY);
    }

    @Override
    public Iterable<BlobResult> list(String prefix, ListOptions listOptions) throws GeneralPolyStashException {
        Path targetPath = baseDir.resolve(prefix);
        if (!targetPath.startsWith(this.baseDir)) {
            throw new AccessDeniedException(
                    String.format("Access denied: list path '%s' is outside of base directory '%s'", targetPath, this.baseDir));
        }
        if (!Files.isDirectory(targetPath)) {
            throw new OperationNotSupportedException(String.format(
                    "List operation failed: path '%s' is not a directory, cannot list objects. Base directory: '%s'", prefix, this.baseDir));
        }

        // walk through the directory and return an iterable object
        try (Stream<Path> filesWalkStream = listOptions.recursive()
                ? Files.walk(targetPath, Integer.MAX_VALUE).filter(Files::isRegularFile)
                : Files.walk(targetPath, 1).filter(Files::isRegularFile)) {

            var fileList = filesWalkStream.toList();

            return () -> new Iterator<>() {
                private final Iterator<Path> fileIterator = fileList.iterator();

                @Override
                public boolean hasNext() {
                    return fileIterator.hasNext();
                }

                @Override
                public BlobResult next() {
                    Path filePath = fileIterator.next();
                    String objectName = directoryPattern.matcher(
                                    filePath.toString().replace(baseDirAsString, StringPool.EMPTY))
                            .replaceFirst(StringPool.EMPTY);
                    Blob blob = get(filePath, false)
                            .setObjectName(objectName);
                    return new BlobResult(blob);
                }
            };
        } catch (IOException e) {
            throw new IOErrorOccursException(
                    String.format("IO error occurred while walking through directory '%s' at path '%s'. Recursive: %s",
                            prefix, targetPath, listOptions.recursive()),
                    e);
        }
    }

    @Override
    public Blob get(String objectName) throws GeneralPolyStashException {
        Path filePath = objectCheck(objectName, true);
        return get(filePath, true)
                .setObjectName(cleanObjectName(filePath));
    }

    @Override
    public Blob put(String prefix, String readableName, Payload<?> payload, Map<String, String> userDefinedAttributes, String contentType) throws GeneralPolyStashException {
        Pair<Path, String> pair = generateObjectName(prefix);
        Path targetFilePath = pair.getLeft();
        String objectName = pair.getRight();
        return save(targetFilePath, objectName, readableName, payload, userDefinedAttributes, contentType);
    }

    @Override
    public Blob putOrReplace(String objectName, String readableName, Payload<?> payload, Map<String, String> userDefinedAttributes, String contentType) throws GeneralPolyStashException {
        Pair<Path, String> pair = deconstructObjectName(objectName);
        Path targetFilePath = pair.getLeft();
        return save(targetFilePath, objectName, readableName, payload, userDefinedAttributes, contentType);
    }

    protected Blob save(Path targetFilePath, String objectName, String readableName, Payload<?> payload, Map<String, String> userDefinedAttributes, String contentType) throws GeneralPolyStashException {
        long contentLength;
        byte[] digest;
        Blob blob = new Blob()
                .setObjectName(objectName)
                .setLastModified(Instant.now())
                .setReadableName(readableName)
                .setContentType(contentType);
        MessageDigest messageDigest = new XXHash64MessageDigest();
        try (BoundedInputStream boundedInputStream = BoundedInputStream.builder()
                .setInputStream(payload.stream())
                .setPropagateClose(true)
                .get();
             DigestInputStream digestInputStream = new DigestInputStream(boundedInputStream, messageDigest);
             FileOutputStream fos = new FileOutputStream(targetFilePath.toFile())) {
            contentLength = digestInputStream.transferTo(fos);
            digest = messageDigest.digest();
        } catch (IOException e) {
            throw new IOErrorOccursException(
                    String.format("Failed to write blob data to file '%s' at path '%s'",
                            objectName, targetFilePath), e);
        }
        String digestAsHexString = BaseEncoding.base16().lowerCase().encode(digest);
        blob.setLength(contentLength)
                .setETag(digestAsHexString)
                .setChecksum(digestAsHexString)
                .setChecksumAlgorithm(SupportedChecksumAlgorithm.ALG_XXHASH_64);
        if (!userDefinedAttributes.isEmpty()) {
            // 如果有用户自定义属性，写入到文件属性中
            attributeHandler.writeUserDefinedAttributes(targetFilePath, userDefinedAttributes);
        }
        // 写入 metadata
        Map<String, String> metadataAttributes = new HashMap<>();
        metadataAttributes.put(IAttributeHandler.ATTR_ETAG, digestAsHexString);
        metadataAttributes.put(IAttributeHandler.ATTR_CHECKSUM, digestAsHexString);
        metadataAttributes.put(IAttributeHandler.ATTR_CHECKSUM_ALGORITHM, SupportedChecksumAlgorithm.ALG_XXHASH_64);
        metadataAttributes.put(IAttributeHandler.ATTR_READABLE_FILENAME, readableName);
        metadataAttributes.put(IAttributeHandler.ATTR_CONTENT_TYPE, contentType);
        attributeHandler.writeMetadataAttributes(targetFilePath, metadataAttributes);
        return blob;
    }

    @Override
    public Blob stat(String objectName) throws GeneralPolyStashException {
        Path filePath = objectCheck(objectName, true);
        return get(filePath, false)
                .setObjectName(cleanObjectName(filePath));
    }

    @Override
    public boolean exist(String objectName) throws GeneralPolyStashException {
        try {
            objectCheck(objectName, true);
        } catch (BlobNotFoundException e) {
            return false;
        }
        // 其他原因导致的异常会立即抛出，如果没有抛出异常，说明对象存在
        return true;
    }

    @Override
    public void remove(String objectName, boolean silent) throws GeneralPolyStashException {
        Path targetPath;
        try {
            targetPath = objectCheck(objectName, true);
        } catch (BlobNotFoundException e) {
            return; // 如果对象不存在，直接返回
        } catch (Exception ex) {
            if (!silent) {
                // 如果不是静默模式，抛出异常
                throw ex;
            }
            return;
        }
        try {
            Files.delete(targetPath);
        } catch (IOException e) {
            if (!silent) {
                throw new IOErrorOccursException(
                        String.format("Failed to delete blob file '%s' at path '%s'", objectName, targetPath),
                        e);
            }
        }
    }

    @Override
    public Object _raw() {
        throw new UnsupportedOperationException("Raw blob store access is not supported in FileSystemBlobStore implementation.");
    }

    private Blob get(Path filePath, boolean acquirePayload) {
        Map<String, String> metadata = attributeHandler.readMetadataAttributes(filePath);
        Map<String, String> userDefinedAttributes = attributeHandler.readUserDefinedAttributes(filePath);
        File file = filePath.toFile();
        Blob blob = new Blob()
                .setRepeatable(true)
                .setLastModified(Instant.ofEpochMilli(file.lastModified()))
                .setLength(file.length())
                .setETag(IAttributeHandler.parseETag(metadata).orElse(null))
                .setReadableName(IAttributeHandler.parseReadableFilename(metadata).orElse(null))
                .setContentType(IAttributeHandler.parseContentType(metadata).orElse(null))
                .setChecksum(IAttributeHandler.parseChecksum(metadata).orElse(null))
                .setChecksumAlgorithm(IAttributeHandler.parseChecksumAlgorithm(metadata).orElse(null))
                .setUserDefinedAttributes(userDefinedAttributes);
        if (acquirePayload) {
            blob.setPayload(new FilePayload(file));
        }
        return blob;
    }
}
