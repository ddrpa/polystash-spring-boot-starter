package cc.ddrpa.dorian.polystash.blobstore;

import cc.ddrpa.dorian.polystash.core.blob.Blob;
import cc.ddrpa.dorian.polystash.core.blob.BlobResult;
import cc.ddrpa.dorian.polystash.core.blob.payload.ByteArrayPayload;
import cc.ddrpa.dorian.polystash.core.blob.payload.FilePayload;
import cc.ddrpa.dorian.polystash.core.blob.payload.InputStreamPayload;
import cc.ddrpa.dorian.polystash.core.blob.payload.MultipartFilePayload;
import cc.ddrpa.dorian.polystash.core.blobstore.BlobStore;
import cc.ddrpa.dorian.polystash.core.blobstore.ListOptions;
import cc.ddrpa.dorian.polystash.core.exception.BlobNotFoundException;
import cc.ddrpa.dorian.polystash.core.exception.GeneralPolyStashException;
import cc.ddrpa.dorian.polystash.provider.filesystem.FileSystemBlobStore;
import cc.ddrpa.dorian.polystash.provider.s3.S3BlobStore;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractBlobStoreTests {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractBlobStoreTests.class);

    protected abstract BlobStore getBlobStore() throws GeneralPolyStashException;

    @Test
    void putBlobTest() throws GeneralPolyStashException, IOException {
        // save byte array as blob
        Blob blob = getBlobStore().put("text",
                "some-text.txt",
                new ByteArrayPayload("Hello, Forvariz!".getBytes(StandardCharsets.UTF_8)),
                Collections.emptyMap(),
                "text/plain");
        assertNotNull(blob);

        // blob should have been saved
        assertTrue(getBlobStore().exist(blob.getObjectName()));

        // get blob stat only
        Blob fetched = getBlobStore().stat(blob.getObjectName());

        assertNotNull(fetched);
        assertFalse(fetched.containsPayload());

        assertEquals(blob.getObjectName(), fetched.getObjectName());
        assertEquals("text/plain", fetched.getContentType());
        assertEquals("some-text.txt", fetched.getReadableName());
        assertEquals(16, fetched.getLength()); // "Hello, Forvariz!" 的长度
        assertNotNull(blob.getETag());
        assertEquals(blob.getETag(), fetched.getETag());

        if (getBlobStore() instanceof FileSystemBlobStore) {
            assertTrue(fetched.isRepeatable());
        } else if (getBlobStore() instanceof S3BlobStore) {
            assertFalse(fetched.isRepeatable());
        }

        // fetch actual payload
        fetched = getBlobStore().get(blob.getObjectName());

        assertNotNull(fetched);
        assertTrue(fetched.containsPayload());

        if (getBlobStore() instanceof FileSystemBlobStore) {
            assertTrue(fetched.isRepeatable());
        } else if (getBlobStore() instanceof S3BlobStore) {
            assertFalse(fetched.isRepeatable());
        }

        assertEquals(blob.getObjectName(), fetched.getObjectName());
        assertEquals("text/plain", fetched.getContentType());
        assertEquals("some-text.txt", fetched.getReadableName());
        assertEquals(16, fetched.getLength()); // "Hello, Forvariz!" 的长度
        assertNotNull(blob.getETag());
        assertEquals(blob.getETag(), fetched.getETag());

        String content = new String(fetched.getPayload().stream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals("Hello, Forvariz!", content);
    }

    @Test
    void putBlobWithUserDefinedAttributesTest() throws GeneralPolyStashException {
        Map<String, String> userAttributes = new HashMap<>();
        userAttributes.put("custom-key", "custom-value");
        userAttributes.put("version", "1.0");

        Blob blob = getBlobStore().put("text/",
                "test-with-attributes.txt",
                new ByteArrayPayload("Test content".getBytes(StandardCharsets.UTF_8)),
                userAttributes,
                "text/plain");
        Blob fetched = getBlobStore().stat(blob.getObjectName());
        Map<String, String> attributes = fetched.getUserDefinedAttributes();

        assertNotNull(attributes);
        assertEquals("custom-value", attributes.get("custom-key"));
        assertEquals("1.0", attributes.get("version"));
    }

    @Test
    void putBlobWithFilePayloadTest() throws GeneralPolyStashException, IOException {
        File avatarFile = new File("Awa-Subaru.png");
        Blob blob = getBlobStore().put("avatar",
                avatarFile.getName(),
                new FilePayload(avatarFile),
                Collections.emptyMap(),
                Files.probeContentType(avatarFile.toPath()));

        Blob fetched = getBlobStore().stat(blob.getObjectName());
        assertEquals("Awa-Subaru.png", fetched.getReadableName());
        assertEquals("image/png", fetched.getContentType());
    }

    @Test
    void putBlobWithInputStreamPayloadTest() throws GeneralPolyStashException, IOException {
        File avatarFile = new File("Awa-Subaru.png");
        FileInputStream fis = new FileInputStream(avatarFile);
        Blob blob = getBlobStore().put("avatar",
                avatarFile.getName(),
                new InputStreamPayload(fis),
                Collections.emptyMap(),
                Files.probeContentType(avatarFile.toPath()));

        Blob fetched = getBlobStore().stat(blob.getObjectName());
        assertEquals("Awa-Subaru.png", fetched.getReadableName());
        assertEquals("image/png", fetched.getContentType());
    }

    @Test
    void putBlobWithMultipartFilePayloadTest() throws GeneralPolyStashException, IOException {
        String name = "file"; // The name of the parameter in the multipart request
        String readableName = "Awa-Subaru.png";
        String contentType = "image/png";
        byte[] content = Files.readAllBytes(Path.of(readableName));
        MockMultipartFile multipartFile = new MockMultipartFile(
                name,
                readableName,
                contentType,
                content
        );

        Blob blob = getBlobStore().put("avatar",
                multipartFile.getOriginalFilename(),
                new MultipartFilePayload(multipartFile),
                Collections.emptyMap(),
                multipartFile.getContentType());

        Blob fetched = getBlobStore().stat(blob.getObjectName());
        assertEquals("Awa-Subaru.png", fetched.getReadableName());
        assertEquals("image/png", fetched.getContentType());
    }

    @Test
    void listTest() throws GeneralPolyStashException {
        // 测试列出所有文件
        Iterable<BlobResult> results = getBlobStore().list("", ListOptions.withDefault());
        List<Blob> blobs = StreamSupport.stream(results.spliterator(), false)
                .map(item -> {
                    try {
                        return item.get();
                    } catch (Exception e) {
                        logger.error("Error getting blob", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        logger.info("blob count: {}", blobs.size());
        for (Blob blob : blobs) {
            logger.info("blob: {}, etag: {}, checksum: {}", blob.getObjectName(), blob.getETag(), blob.getChecksum());
        }

        assertFalse(blobs.isEmpty());

        // 测试列出特定前缀的文件
        Iterable<BlobResult> prefixResults = getBlobStore().list("text/", ListOptions.withDefault());
        List<Blob> prefixBlobs = StreamSupport.stream(prefixResults.spliterator(), false)
                .map(item -> {
                    try {
                        return item.get();
                    } catch (Exception e) {
                        logger.error("Error getting blob", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        assertFalse(prefixBlobs.isEmpty());
    }

    @Test
    void accessNonExistBlobTest() {
        assertThrows(BlobNotFoundException.class, () -> getBlobStore().stat("text/nonexistent-file.txt"));
        assertThrows(BlobNotFoundException.class, () -> getBlobStore().get("nonexistent-file.txt"));
    }

    @Test
    void deepDirectoryCreationTest() throws GeneralPolyStashException, IOException {
        File avatarFile = new File("Awa-Subaru.png");
        Blob blob = getBlobStore().put("avatar/2025/uploads/",
                avatarFile.getName(),
                new FilePayload(avatarFile),
                Collections.emptyMap(),
                Files.probeContentType(avatarFile.toPath()));

        Blob fetched = getBlobStore().stat(blob.getObjectName());
        assertEquals("Awa-Subaru.png", fetched.getReadableName());
        assertEquals("image/png", fetched.getContentType());
    }

    @Test
    void escapeTest() {
        assertThrows(GeneralPolyStashException.class, () ->
                getBlobStore().put("../../avatar/2024/upload/",
                        "test-image.jpg",
                        new ByteArrayPayload("fake image content".getBytes(StandardCharsets.UTF_8)),
                        Collections.emptyMap(),
                        "image/jpeg"));
        assertThrows(GeneralPolyStashException.class, () -> getBlobStore().get("/etc/shadow"));
    }

    @Test
    void removeTest() throws GeneralPolyStashException {
        // 先创建一个测试文件
        Blob blob = getBlobStore().put("text//",
                "remove-test.txt",
                new ByteArrayPayload("Remove test content".getBytes(StandardCharsets.UTF_8)),
                Collections.emptyMap(),
                "text/plain");
        // 验证文件存在
        assertTrue(getBlobStore().exist(blob.getObjectName()));
        // 删除文件
        getBlobStore().remove(blob.getObjectName(), false);
        // 验证文件已被删除
        assertFalse(getBlobStore().exist(blob.getObjectName()));
        // 测试删除不存在的文件（silent = false）
        assertDoesNotThrow(() -> getBlobStore().remove("nonexistent-file.txt", false));
        // 测试删除不存在的文件（silent = true）
        assertDoesNotThrow(() -> getBlobStore().remove("nonexistent-file.txt", true));
    }

    @Test
    void specialCharactersTest() throws GeneralPolyStashException {
        // 测试文件名包含特殊字符
        String specialName = "file with spaces and special chars!@#$%^&*()_+-=[]{}|;':\",./<>?";
        Blob blob = getBlobStore().put("blob/special",
                specialName,
                new ByteArrayPayload("Special characters test".getBytes(StandardCharsets.UTF_8)),
                Collections.emptyMap(),
                "text/plain");

        assertNotNull(blob);
        assertTrue(getBlobStore().exist(blob.getObjectName()));
    }
}