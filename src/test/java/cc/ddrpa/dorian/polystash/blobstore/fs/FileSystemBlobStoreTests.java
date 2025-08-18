package cc.ddrpa.dorian.polystash.blobstore.fs;

import cc.ddrpa.dorian.polystash.blobstore.AbstractBlobStoreTests;
import cc.ddrpa.dorian.polystash.core.blob.Blob;
import cc.ddrpa.dorian.polystash.core.blob.payload.ByteArrayPayload;
import cc.ddrpa.dorian.polystash.core.blob.payload.FilePayload;
import cc.ddrpa.dorian.polystash.core.blobstore.BlobStore;
import cc.ddrpa.dorian.polystash.core.exception.GeneralPolyStashException;
import cc.ddrpa.dorian.polystash.provider.filesystem.FileSystemBlobStoreBuilder;
import cc.ddrpa.dorian.polystash.springboot.autoconfigure.FullBlobStoreProperties;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileSystemBlobStoreTests extends AbstractBlobStoreTests {
    private static BlobStore blobStore;

    @Override
    protected BlobStore getBlobStore() throws GeneralPolyStashException {
        if (Objects.isNull(blobStore)) {
            blobStore = new FileSystemBlobStoreBuilder()
                    .name("local")
                    .properties(
                            new FullBlobStoreProperties("fs")
                                    .setBaseDir("filesystem-storage/test1"))
                    .build();
        }
        return blobStore;
    }

    @Test
    void checksumInPutTest() throws GeneralPolyStashException {
        // save byte array as blob
        Blob blob = getBlobStore().put("text",
                "some-text.txt",
                new ByteArrayPayload("Hello, Forvariz!".getBytes(StandardCharsets.UTF_8)),
                Collections.emptyMap(),
                "text/plain");
        assertNotNull(blob);
        // get blob stat only
        Blob fetched = getBlobStore().stat(blob.getObjectName());
        assertNotNull(blob.getChecksum());
        assertEquals(blob.getChecksum(), fetched.getChecksum());

        // fetch actual payload
        fetched = getBlobStore().get(blob.getObjectName());
        assertNotNull(blob.getChecksum());
        assertEquals(blob.getChecksum(), fetched.getChecksum());
    }

    @Test
    void largeFileTest() throws GeneralPolyStashException {
        // run shell command to create a large file
        // fallocate -l 195m filesystem-storage/fake-195m-file.bin
        Blob blob = getBlobStore().put("blob",
                "fake-195m-file.bin",
                new FilePayload(new File("filesystem-storage/fake-195m-file.bin")),
                Collections.emptyMap(),
                "application/octet-stream");
        getBlobStore().stat(blob.getObjectName());
        // it should be same with output of
        // xxh64sum filesystem-storage/fake-195m-file.bin
        logger.info("blob checksum: {}", blob.getChecksum());
    }
}