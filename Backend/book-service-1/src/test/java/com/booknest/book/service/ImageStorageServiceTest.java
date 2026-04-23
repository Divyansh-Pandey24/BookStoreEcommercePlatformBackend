package com.booknest.book.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ImageStorageServiceTest {

    @InjectMocks
    private ImageStorageService imageStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageStorageService, "uploadDir", tempDir.toString());
    }

    @Test
    void saveImage_validFile_savesAndReturnsPath() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-cover.jpg",
                "image/jpeg",
                "dummy image content".getBytes()
        );

        String savedPath = imageStorageService.saveImage(mockFile);

        assertThat(savedPath).startsWith("uploads/books/");
        assertThat(savedPath).endsWith("test-cover.jpg");
    }

    @Test
    void saveImage_emptyFile_throwsException() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        RuntimeException ex = assertThrows(RuntimeException.class, () -> imageStorageService.saveImage(mockFile));
        assertThat(ex.getMessage()).isEqualTo("Please select an image to upload");
    }

    @Test
    void saveImage_invalidExtension_throwsException() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "dummy pdf content".getBytes()
        );

        RuntimeException ex = assertThrows(RuntimeException.class, () -> imageStorageService.saveImage(mockFile));
        assertThat(ex.getMessage()).contains("Only image files are allowed");
    }

    @Test
    void saveImage_fileTooLarge_throwsException() {
        byte[] largeBytes = new byte[(5 * 1024 * 1024) + 10]; // Slightly larger than 5MB
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "large.png",
                "image/png",
                largeBytes
        );

        RuntimeException ex = assertThrows(RuntimeException.class, () -> imageStorageService.saveImage(mockFile));
        assertThat(ex.getMessage()).isEqualTo("Image size cannot exceed 5MB");
    }

    @Test
    void deleteImage_validPath_deletesFile() throws IOException {
        Path mockFile = tempDir.resolve("test_image.jpg");
        Files.write(mockFile, "Test image content".getBytes());

        imageStorageService.deleteImage(mockFile.toString());

        assertThat(Files.exists(mockFile)).isFalse();
    }

    @Test
    void deleteImage_nullOrEmptyPath_doesNothing() {
        // Will not throw any exception
        imageStorageService.deleteImage(null);
        imageStorageService.deleteImage("");
    }
}
