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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileServiceImpl Unit Tests")
class FileServiceImplTest {

    @Mock
    private FileDao fileDao;

    @Mock
    private UserDao userDao;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private FileServiceImpl fileService;

    @TempDir
    Path tempDir;

    private static final Long OWNER_ID = 1L;
    private static final String TEST_EMAIL = "owner@example.com";
    private static final String PUBLIC_ID = "test-uuid-12345";
    private static final String ORIGINAL_FILENAME = "test-document.pdf";
    private static final long VALID_FILE_SIZE = 2 * 1024 * 1024; // 2 MB
    private static final long OVERSIZED_FILE = 6 * 1024 * 1024; // 6 MB (exceeds 5 MB limit)

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "rootDirectory", tempDir.toString());
    }

    @Nested
    @DisplayName("Upload File Tests")
    class UploadFileTests {

        @Test
        @DisplayName("Should successfully upload valid PDF file")
        void uploadFile_WithValidPdf_ShouldReturnUploadResult() throws IOException {
            // Given
            User owner = createTestUser();
            byte[] fileContent = "PDF content".getBytes();

            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getOriginalFilename()).thenReturn(ORIGINAL_FILENAME);
            when(multipartFile.getSize()).thenReturn(VALID_FILE_SIZE);
            when(multipartFile.getContentType()).thenReturn("application/pdf");
            when(userDao.findById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(fileDao.save(any(FileEntity.class))).thenAnswer(invocation -> {
                FileEntity entity = invocation.getArgument(0);
                entity.setId(1L);
                return entity;
            });

            // When
            FileUploadResult result = fileService.uploadFile(OWNER_ID, multipartFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPublicId()).isNotNull();
            assertThat(result.getOriginalName()).isEqualTo(ORIGINAL_FILENAME);
            assertThat(result.getExtension()).isEqualTo("PDF");
            assertThat(result.getSizeBytes()).isEqualTo(VALID_FILE_SIZE);
            assertThat(result.getStoragePath()).isNotNull();

            ArgumentCaptor<FileEntity> captor = ArgumentCaptor.forClass(FileEntity.class);
            verify(fileDao).save(captor.capture());

            FileEntity savedEntity = captor.getValue();
            assertThat(savedEntity.getExtension()).isEqualTo("PDF");
            assertThat(savedEntity.getContentType()).isEqualTo("application/pdf");
            assertThat(savedEntity.getSizeBytes()).isEqualTo(VALID_FILE_SIZE);
            assertThat(savedEntity.isTemp()).isFalse();
        }

        @Test
        @DisplayName("Should successfully upload PNG image")
        void uploadFile_WithValidPng_ShouldReturnUploadResult() throws IOException {
            // Given
            User owner = createTestUser();
            String filename = "image.png";

            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getOriginalFilename()).thenReturn(filename);
            when(multipartFile.getSize()).thenReturn(VALID_FILE_SIZE);
            when(multipartFile.getContentType()).thenReturn("image/png");
            when(userDao.findById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(fileDao.save(any(FileEntity.class))).thenAnswer(invocation -> {
                FileEntity entity = invocation.getArgument(0);
                entity.setId(1L);
                return entity;
            });

            // When
            FileUploadResult result = fileService.uploadFile(OWNER_ID, multipartFile);

            // Then
            assertThat(result.getExtension()).isEqualTo("PNG");
        }

        @Test
        @DisplayName("Should successfully upload DOCX file")
        void uploadFile_WithValidDocx_ShouldReturnUploadResult() throws IOException {
            // Given
            User owner = createTestUser();
            String filename = "document.docx";

            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getOriginalFilename()).thenReturn(filename);
            when(multipartFile.getSize()).thenReturn(VALID_FILE_SIZE);
            when(multipartFile.getContentType()).thenReturn("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            when(userDao.findById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(fileDao.save(any(FileEntity.class))).thenAnswer(invocation -> {
                FileEntity entity = invocation.getArgument(0);
                entity.setId(1L);
                return entity;
            });

            // When
            FileUploadResult result = fileService.uploadFile(OWNER_ID, multipartFile);

            // Then
            assertThat(result.getExtension()).isEqualTo("DOCX");
        }

        @Test
        @DisplayName("Should throw BusinessException when file is empty")
        void uploadFile_WithEmptyFile_ShouldThrowBusinessException() {
            // Given
            when(multipartFile.isEmpty()).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> fileService.uploadFile(OWNER_ID, multipartFile))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Uploaded file must not be empty");

            verify(userDao, never()).findById(anyLong());
            verify(fileDao, never()).save(any(FileEntity.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when owner not found")
        void uploadFile_WithNonExistentOwner_ShouldThrowBusinessException() {
            // Given
            when(multipartFile.isEmpty()).thenReturn(false);
            // ✅ Sadece isEmpty() yeterli - diğer stubbing'ler gereksiz
            when(userDao.findById(OWNER_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> fileService.uploadFile(OWNER_ID, multipartFile))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Owner not found");

            verify(fileDao, never()).save(any(FileEntity.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when file size exceeds 5 MB")
        void uploadFile_WithOversizedFile_ShouldThrowBusinessException() {
            // Given
            User owner = createTestUser();

            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getOriginalFilename()).thenReturn(ORIGINAL_FILENAME);
            when(multipartFile.getSize()).thenReturn(OVERSIZED_FILE);
            when(userDao.findById(OWNER_ID)).thenReturn(Optional.of(owner));

            // When & Then
            assertThatThrownBy(() -> fileService.uploadFile(OWNER_ID, multipartFile))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("File size exceeds the maximum limit of 5 MB");

            verify(fileDao, never()).save(any(FileEntity.class));
        }

        @Test
        @DisplayName("Should throw BusinessException for unsupported file extension")
        void uploadFile_WithUnsupportedExtension_ShouldThrowBusinessException() {
            // Given
            User owner = createTestUser();
            String unsupportedFile = "malicious.exe";

            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getOriginalFilename()).thenReturn(unsupportedFile);
            when(multipartFile.getSize()).thenReturn(VALID_FILE_SIZE);
            when(userDao.findById(OWNER_ID)).thenReturn(Optional.of(owner));

            // When & Then
            assertThatThrownBy(() -> fileService.uploadFile(OWNER_ID, multipartFile))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("is not allowed");

            verify(fileDao, never()).save(any(FileEntity.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when disk write fails")
        void uploadFile_WhenDiskWriteFails_ShouldThrowBusinessException() throws IOException {
            // Given
            User owner = createTestUser();

            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getOriginalFilename()).thenReturn(ORIGINAL_FILENAME);
            when(multipartFile.getSize()).thenReturn(VALID_FILE_SIZE);
            when(multipartFile.getContentType()).thenReturn("application/pdf");
            when(userDao.findById(OWNER_ID)).thenReturn(Optional.of(owner));
            doThrow(new IOException("Disk full"))
                    .when(multipartFile)
                    .transferTo(any(Path.class));

            // When & Then
            assertThatThrownBy(() -> fileService.uploadFile(OWNER_ID, multipartFile))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Failed to store file on disk");

            verify(fileDao, never()).save(any(FileEntity.class));
        }

        @Test
        @DisplayName("Should store extension in uppercase")
        void uploadFile_ShouldStoreExtensionInUppercase() throws IOException {
            // Given
            User owner = createTestUser();
            String mixedCaseFilename = "Document.PdF";

            when(multipartFile.isEmpty()).thenReturn(false);
            when(multipartFile.getOriginalFilename()).thenReturn(mixedCaseFilename);
            when(multipartFile.getSize()).thenReturn(VALID_FILE_SIZE);
            when(multipartFile.getContentType()).thenReturn("application/pdf");
            when(userDao.findById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(fileDao.save(any(FileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            fileService.uploadFile(OWNER_ID, multipartFile);

            // Then
            ArgumentCaptor<FileEntity> captor = ArgumentCaptor.forClass(FileEntity.class);
            verify(fileDao).save(captor.capture());
            assertThat(captor.getValue().getExtension()).isEqualTo("PDF");
        }
    }

    @Nested
    @DisplayName("Delete File Tests")
    class DeleteFileTests {

        @Test
        @DisplayName("Should successfully soft-delete file when owner deletes")
        void deleteFile_WhenOwnerDeletes_ShouldSoftDelete() {
            // Given
            FileEntity fileEntity = createTestFileEntity();

            when(fileDao.findByPublicIdAndDeletedAtIsNull(PUBLIC_ID)).thenReturn(Optional.of(fileEntity));
            when(fileDao.save(any(FileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            fileService.deleteFile(OWNER_ID, PUBLIC_ID);

            // Then
            ArgumentCaptor<FileEntity> captor = ArgumentCaptor.forClass(FileEntity.class);
            verify(fileDao).save(captor.capture());
            assertThat(captor.getValue().getDeletedAt()).isNotNull();
            assertThat(captor.getValue().isDeleted()).isTrue();
        }

        @Test
        @DisplayName("Should throw BusinessException when file not found")
        void deleteFile_WhenFileNotFound_ShouldThrowBusinessException() {
            // Given
            when(fileDao.findByPublicIdAndDeletedAtIsNull(PUBLIC_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> fileService.deleteFile(OWNER_ID, PUBLIC_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("File not found");

            verify(fileDao, never()).save(any(FileEntity.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when non-owner tries to delete")
        void deleteFile_WhenNonOwnerTriesToDelete_ShouldThrowBusinessException() {
            // Given
            FileEntity fileEntity = createTestFileEntity();
            Long otherUserId = 999L;

            when(fileDao.findByPublicIdAndDeletedAtIsNull(PUBLIC_ID)).thenReturn(Optional.of(fileEntity));

            // When & Then
            assertThatThrownBy(() -> fileService.deleteFile(otherUserId, PUBLIC_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("You are not allowed to delete this file");

            verify(fileDao, never()).save(any(FileEntity.class));
        }
    }

    @Nested
    @DisplayName("Get File Metadata Tests")
    class GetFileMetadataTests {

        @Test
        @DisplayName("Should return metadata for existing file")
        void getFileMetadata_WithExistingFile_ShouldReturnMetadata() {
            // Given
            FileEntity fileEntity = createTestFileEntity();

            when(fileDao.findByPublicIdAndDeletedAtIsNull(PUBLIC_ID)).thenReturn(Optional.of(fileEntity));

            // When
            FileMetadataResponse response = fileService.getFileMetadata(PUBLIC_ID);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPublicId()).isEqualTo(PUBLIC_ID);
            assertThat(response.getOriginalName()).isEqualTo(ORIGINAL_FILENAME);
            assertThat(response.getExtension()).isEqualTo("PDF");
            assertThat(response.getSizeBytes()).isEqualTo(VALID_FILE_SIZE);
            assertThat(response.getContentType()).isEqualTo("application/pdf");
        }

        @Test
        @DisplayName("Should throw BusinessException when file not found")
        void getFileMetadata_WithNonExistentFile_ShouldThrowBusinessException() {
            // Given
            when(fileDao.findByPublicIdAndDeletedAtIsNull(PUBLIC_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> fileService.getFileMetadata(PUBLIC_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("File not found");
        }
    }

    @Nested
    @DisplayName("List Files Tests")
    class ListFilesTests {

        @Test
        @DisplayName("Should return list of files for owner")
        void listFiles_WithExistingFiles_ShouldReturnList() {
            // Given
            FileEntity file1 = createTestFileEntity();
            FileEntity file2 = createTestFileEntity();
            file2.setPublicId("another-uuid");
            file2.setOriginalName("another-file.docx");

            when(fileDao.findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(OWNER_ID))
                    .thenReturn(Arrays.asList(file1, file2));

            // When
            List<FileListItemResponse> result = fileService.listFiles(OWNER_ID);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPublicId()).isEqualTo(PUBLIC_ID);
            assertThat(result.get(1).getPublicId()).isEqualTo("another-uuid");
        }

        @Test
        @DisplayName("Should return empty list when owner has no files")
        void listFiles_WithNoFiles_ShouldReturnEmptyList() {
            // Given
            when(fileDao.findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(OWNER_ID))
                    .thenReturn(List.of());

            // When
            List<FileListItemResponse> result = fileService.listFiles(OWNER_ID);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Load File As Resource Tests")
    class LoadFileAsResourceTests {

        @Test
        @DisplayName("Should throw BusinessException when file not found in database")
        void loadFileAsResource_WhenFileNotFound_ShouldThrowBusinessException() {
            // Given
            when(fileDao.findByPublicIdAndDeletedAtIsNull(PUBLIC_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> fileService.loadFileAsResource(PUBLIC_ID, OWNER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("File not found");
        }

        @Test
        @DisplayName("Should throw BusinessException when non-owner tries to access")
        void loadFileAsResource_WhenNonOwnerTriesToAccess_ShouldThrowBusinessException() {
            // Given
            FileEntity fileEntity = createTestFileEntity();
            Long otherUserId = 999L;

            when(fileDao.findByPublicIdAndDeletedAtIsNull(PUBLIC_ID)).thenReturn(Optional.of(fileEntity));

            // When & Then
            assertThatThrownBy(() -> fileService.loadFileAsResource(PUBLIC_ID, otherUserId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("You are not allowed to access this file");
        }
    }

    // Helper methods
    private User createTestUser() {
        User user = new User();
        user.setId(OWNER_ID);
        user.setEmail(TEST_EMAIL);
        return user;
    }

    private FileEntity createTestFileEntity() {
        FileEntity entity = new FileEntity();
        entity.setId(1L);
        entity.setPublicId(PUBLIC_ID);
        entity.setOwner(createTestUser());
        entity.setOriginalName(ORIGINAL_FILENAME);
        entity.setStoredName(PUBLIC_ID + ".pdf");
        entity.setExtension("PDF");
        entity.setContentType("application/pdf");
        entity.setSizeBytes(VALID_FILE_SIZE);
        entity.setStoragePath("2025/11/26/pdf/T/" + PUBLIC_ID + ".pdf");
        entity.setTemp(false);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}