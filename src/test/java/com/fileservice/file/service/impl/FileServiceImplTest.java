package com.fileservice.file.service.impl;

import com.fileservice.auth.dao.UserDao;
import com.fileservice.auth.pojo.entity.User;
import com.fileservice.common.exception.BusinessException;
import com.fileservice.file.dao.FileDao;
import com.fileservice.file.pojo.dto.FileListItemResponse;
import com.fileservice.file.pojo.dto.FileMetadataResponse;
import com.fileservice.file.pojo.dto.FileUploadResult;
import com.fileservice.file.pojo.entity.FileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FileServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileService Unit Tests")
class FileServiceImplTest {

    @Mock
    private FileDao fileDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private FileServiceImpl fileService;

    @TempDir
    Path tempDir;

    private User testUser;
    private FileEntity testFile;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PUBLIC_ID = "11111111-1111-1111-1111-111111111111";
    private static final String TEST_FILENAME = "test-document.pdf";

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setEmail(TEST_EMAIL);

        // Set up test file entity
        testFile = new FileEntity();
        testFile.setId(1L);
        testFile.setPublicId(TEST_PUBLIC_ID);
        testFile.setOwner(testUser);
        testFile.setOriginalName(TEST_FILENAME);
        testFile.setStoredName(TEST_PUBLIC_ID + ".pdf");
        testFile.setExtension("PDF");
        testFile.setContentType("application/pdf");
        testFile.setSizeBytes(1024L);
        testFile.setStoragePath("2025/11/25/pdf/T/test.pdf");
        testFile.setTemp(false);
        testFile.setCreatedAt(Instant.now());
        testFile.setUpdatedAt(Instant.now());

        // Set root directory to temp directory
        ReflectionTestUtils.setField(fileService, "rootDirectory", tempDir.toString());
    }

    // ==================== Upload Tests ====================

    @Test
    @DisplayName("Should successfully upload a valid file")
    void shouldUploadFileSuccessfully() throws Exception {
        // Given
        byte[] content = "test content".getBytes();
        MultipartFile file = new MockMultipartFile(
                "file",
                TEST_FILENAME,
                "application/pdf",
                content
        );

        when(userDao.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(fileDao.save(any(FileEntity.class))).thenAnswer(invocation -> {
            FileEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // When
        FileUploadResult result = fileService.uploadFile(TEST_USER_ID, file);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPublicId()).isNotNull();
        assertThat(result.getOriginalName()).isEqualTo(TEST_FILENAME);
        assertThat(result.getExtension()).isEqualTo("PDF");
        assertThat(result.getSizeBytes()).isEqualTo(content.length);

        verify(userDao).findById(TEST_USER_ID);
        verify(fileDao).save(any(FileEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when file is empty")
    void shouldThrowExceptionWhenFileIsEmpty() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                TEST_FILENAME,
                "application/pdf",
                new byte[0]
        );

        // When & Then
        assertThatThrownBy(() -> fileService.uploadFile(TEST_USER_ID, emptyFile))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Uploaded file must not be empty");
    }

    @Test
    @DisplayName("Should throw exception when file exceeds size limit")
    void shouldThrowExceptionWhenFileTooLarge() {
        // Given
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6 MB
        MultipartFile largeFile = new MockMultipartFile(
                "file",
                TEST_FILENAME,
                "application/pdf",
                largeContent
        );

        when(userDao.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> fileService.uploadFile(TEST_USER_ID, largeFile))
                .isInstanceOf(BusinessException.class)
                .hasMessage("File size exceeds the maximum limit of 5 MB");
    }

    @Test
    @DisplayName("Should throw exception when file extension not allowed")
    void shouldThrowExceptionWhenExtensionNotAllowed() {
        // Given
        MultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.exe",
                "application/octet-stream",
                "content".getBytes()
        );

        when(userDao.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> fileService.uploadFile(TEST_USER_ID, invalidFile))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    @DisplayName("Should throw exception when owner not found")
    void shouldThrowExceptionWhenOwnerNotFound() {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                TEST_FILENAME,
                "application/pdf",
                "content".getBytes()
        );

        when(userDao.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> fileService.uploadFile(TEST_USER_ID, file))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Owner not found");
    }

    // ==================== List Tests ====================

    @Test
    @DisplayName("Should return list of files for owner")
    void shouldListFilesForOwner() {
        // Given
        List<FileEntity> entities = Arrays.asList(testFile);
        when(fileDao.findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(TEST_USER_ID))
                .thenReturn(entities);

        // When
        List<FileListItemResponse> result = fileService.listFiles(TEST_USER_ID);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPublicId()).isEqualTo(TEST_PUBLIC_ID);
        assertThat(result.get(0).getOriginalName()).isEqualTo(TEST_FILENAME);

        verify(fileDao).findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should return empty list when no files exist")
    void shouldReturnEmptyListWhenNoFiles() {
        // Given
        when(fileDao.findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(TEST_USER_ID))
                .thenReturn(Arrays.asList());

        // When
        List<FileListItemResponse> result = fileService.listFiles(TEST_USER_ID);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== Get Metadata Tests ====================

    @Test
    @DisplayName("Should return file metadata when file exists")
    void shouldReturnFileMetadata() {
        // Given
        when(fileDao.findByPublicIdAndDeletedAtIsNull(TEST_PUBLIC_ID))
                .thenReturn(Optional.of(testFile));

        // When
        FileMetadataResponse result = fileService.getFileMetadata(TEST_PUBLIC_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPublicId()).isEqualTo(TEST_PUBLIC_ID);
        assertThat(result.getOriginalName()).isEqualTo(TEST_FILENAME);
        assertThat(result.getExtension()).isEqualTo("PDF");

        verify(fileDao).findByPublicIdAndDeletedAtIsNull(TEST_PUBLIC_ID);
    }

    @Test
    @DisplayName("Should throw exception when file not found for metadata")
    void shouldThrowExceptionWhenFileNotFoundForMetadata() {
        // Given
        when(fileDao.findByPublicIdAndDeletedAtIsNull(TEST_PUBLIC_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> fileService.getFileMetadata(TEST_PUBLIC_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage("File not found");
    }

    // ==================== Download Tests ====================

    @Test
    @DisplayName("Should load file as resource when file exists")
    void shouldLoadFileAsResource() throws Exception {
        // Given
        Path filePath = tempDir.resolve("2025/11/25/pdf/T");
        Files.createDirectories(filePath);
        Path actualFile = filePath.resolve("test.pdf");
        Files.write(actualFile, "test content".getBytes());

        when(fileDao.findByPublicIdAndDeletedAtIsNull(TEST_PUBLIC_ID))
                .thenReturn(Optional.of(testFile));

        // When
        Resource result = fileService.loadFileAsResource(TEST_PUBLIC_ID, TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.exists()).isTrue();
        assertThat(result.isReadable()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when user not owner of file")
    void shouldThrowExceptionWhenNotOwner() {
        // Given
        when(fileDao.findByPublicIdAndDeletedAtIsNull(TEST_PUBLIC_ID))
                .thenReturn(Optional.of(testFile));

        // When & Then
        assertThatThrownBy(() -> fileService.loadFileAsResource(TEST_PUBLIC_ID, 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("You are not allowed to access this file");
    }

    // ==================== Delete Tests ====================

    @Test
    @DisplayName("Should soft delete file successfully")
    void shouldDeleteFileSuccessfully() {
        // Given
        when(fileDao.findByPublicIdAndDeletedAtIsNull(TEST_PUBLIC_ID))
                .thenReturn(Optional.of(testFile));
        when(fileDao.save(any(FileEntity.class))).thenReturn(testFile);

        // When
        fileService.deleteFile(TEST_USER_ID, TEST_PUBLIC_ID);

        // Then
        verify(fileDao).findByPublicIdAndDeletedAtIsNull(TEST_PUBLIC_ID);
        verify(fileDao).save(any(FileEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting file not owned by user")
    void shouldThrowExceptionWhenDeletingNotOwnedFile() {
        // Given
        when(fileDao.findByPublicIdAndDeletedAtIsNull(TEST_PUBLIC_ID))
                .thenReturn(Optional.of(testFile));

        // When & Then
        assertThatThrownBy(() -> fileService.deleteFile(999L, TEST_PUBLIC_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage("You are not allowed to delete this file");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent file")
    void shouldThrowExceptionWhenDeletingNonExistentFile() {
        // Given
        when(fileDao.findByPublicIdAndDeletedAtIsNull(TEST_PUBLIC_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> fileService.deleteFile(TEST_USER_ID, TEST_PUBLIC_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage("File not found");
    }

    // ==================== Update Tests ====================

    @Test
    @DisplayName("Should update file successfully")
    void shouldUpdateFileSuccessfully() throws Exception {
        // Given
        byte[] newContent = "new content".getBytes();
        MultipartFile newFile = new MockMultipartFile(
                "file",
                "updated.pdf",
                "application/pdf",
                newContent
        );

        when(fileDao.findByPublicIdAndDeletedAtIsNull(TEST_PUBLIC_ID))
                .thenReturn(Optional.of(testFile));
        when(fileDao.save(any(FileEntity.class))).thenReturn(testFile);

        // When
        FileUploadResult result = fileService.updateFile(TEST_PUBLIC_ID, TEST_USER_ID, newFile);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPublicId()).isEqualTo(TEST_PUBLIC_ID);

        verify(fileDao).findByPublicIdAndDeletedAtIsNull(TEST_PUBLIC_ID);
        verify(fileDao).save(any(FileEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when updating file not owned by user")
    void shouldThrowExceptionWhenUpdatingNotOwnedFile() {
        // Given
        MultipartFile newFile = new MockMultipartFile(
                "file",
                "updated.pdf",
                "application/pdf",
                "content".getBytes()
        );

        when(fileDao.findByPublicIdAndDeletedAtIsNull(TEST_PUBLIC_ID))
                .thenReturn(Optional.of(testFile));

        // When & Then
        assertThatThrownBy(() -> fileService.updateFile(TEST_PUBLIC_ID, 999L, newFile))
                .isInstanceOf(BusinessException.class)
                .hasMessage("You are not allowed to update this file");
    }
}
