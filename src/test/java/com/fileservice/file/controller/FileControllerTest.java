package com.fileservice.file.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileservice.common.exception.BusinessException;
import com.fileservice.common.exception.GlobalExceptionHandler;
import com.fileservice.file.pojo.dto.FileListItemResponse;
import com.fileservice.file.pojo.dto.FileMetadataResponse;
import com.fileservice.file.pojo.dto.FileUploadResult;
import com.fileservice.file.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("File Controller Tests")
class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    // Test constants
    private static final String FILES_BASE_URL = "/api/files";
    private static final Long OWNER_ID = 1L;
    private static final String PUBLIC_ID = "abc123xyz";
    private static final String FILENAME = "test-document.pdf";
    private static final String CONTENT_TYPE = "application/pdf";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(fileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("GET /api/files")
    class ListFilesEndpointTests {

        @Test
        @DisplayName("Should return 200 and file list when files exist")
        void listFiles_WithExistingFiles_ShouldReturn200AndFileList() throws Exception {
            // Given
            FileListItemResponse file1 = FileListItemResponse.builder()
                    .publicId("file1")
                    .originalName("document1.pdf")
                    .extension("PDF")
                    .contentType("application/pdf")
                    .sizeBytes(1024L)
                    .createdAt(Instant.now())
                    .temp(false)
                    .build();

            FileListItemResponse file2 = FileListItemResponse.builder()
                    .publicId("file2")
                    .originalName("image.png")
                    .extension("PNG")
                    .contentType("image/png")
                    .sizeBytes(2048L)
                    .createdAt(Instant.now())
                    .temp(false)
                    .build();

            List<FileListItemResponse> files = Arrays.asList(file1, file2);

            when(fileService.listFiles(OWNER_ID)).thenReturn(files);

            // When & Then
            mockMvc.perform(get(FILES_BASE_URL)
                            .param("ownerId", OWNER_ID.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].publicId").value("file1"))
                    .andExpect(jsonPath("$[0].originalName").value("document1.pdf"))
                    .andExpect(jsonPath("$[0].extension").value("PDF"))
                    .andExpect(jsonPath("$[0].sizeBytes").value(1024))
                    .andExpect(jsonPath("$[1].publicId").value("file2"))
                    .andExpect(jsonPath("$[1].originalName").value("image.png"))
                    .andExpect(jsonPath("$[1].extension").value("PNG"));

            verify(fileService, times(1)).listFiles(OWNER_ID);
        }

        @Test
        @DisplayName("Should return 200 and empty list when no files exist")
        void listFiles_WithNoFiles_ShouldReturn200AndEmptyList() throws Exception {
            // Given
            when(fileService.listFiles(OWNER_ID)).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get(FILES_BASE_URL)
                            .param("ownerId", OWNER_ID.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(fileService, times(1)).listFiles(OWNER_ID);
        }

        @Test
        @DisplayName("Should return 400 when ownerId parameter is missing")
        void listFiles_WithoutOwnerId_ShouldReturn400() throws Exception {
            // When & Then
            mockMvc.perform(get(FILES_BASE_URL))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, never()).listFiles(anyLong());
        }

        @Test
        @DisplayName("Should return 400 when ownerId is not a valid number")
        void listFiles_WithInvalidOwnerId_ShouldReturn400() throws Exception {
            // When & Then
            mockMvc.perform(get(FILES_BASE_URL)
                            .param("ownerId", "invalid"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, never()).listFiles(anyLong());
        }
    }

    @Nested
    @DisplayName("GET /api/files/{publicId}")
    class GetMetadataEndpointTests {

        @Test
        @DisplayName("Should return 200 and metadata when file exists")
        void getMetadata_WithExistingFile_ShouldReturn200AndMetadata() throws Exception {
            // Given
            FileMetadataResponse metadata = FileMetadataResponse.builder()
                    .publicId(PUBLIC_ID)
                    .originalName(FILENAME)
                    .storedName(PUBLIC_ID + ".pdf")
                    .extension("PDF")
                    .contentType(CONTENT_TYPE)
                    .sizeBytes(1024L)
                    .storagePath("2025/11/26/" + PUBLIC_ID + ".pdf")
                    .temp(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(fileService.getFileMetadata(PUBLIC_ID)).thenReturn(metadata);

            // When & Then
            mockMvc.perform(get(FILES_BASE_URL + "/{publicId}", PUBLIC_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.publicId").value(PUBLIC_ID))
                    .andExpect(jsonPath("$.originalName").value(FILENAME))
                    .andExpect(jsonPath("$.extension").value("PDF"))
                    .andExpect(jsonPath("$.sizeBytes").value(1024))
                    .andExpect(jsonPath("$.contentType").value(CONTENT_TYPE));

            verify(fileService, times(1)).getFileMetadata(PUBLIC_ID);
        }

        @Test
        @DisplayName("Should return 404 when file does not exist")
        void getMetadata_WithNonExistentFile_ShouldReturn404() throws Exception {
            // Given
            when(fileService.getFileMetadata(PUBLIC_ID))
                    .thenThrow(new BusinessException("File not found"));

            // When & Then
            mockMvc.perform(get(FILES_BASE_URL + "/{publicId}", PUBLIC_ID))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, times(1)).getFileMetadata(PUBLIC_ID);
        }
    }

    @Nested
    @DisplayName("GET /api/files/{publicId}/content")
    class DownloadFileEndpointTests {

        @Test
        @DisplayName("Should return 200 and file content when file exists")
        void downloadFile_WithExistingFile_ShouldReturn200AndContent() throws Exception {
            // Given
            byte[] fileContent = "Test file content".getBytes();
            Resource resource = new ByteArrayResource(fileContent);

            FileMetadataResponse metadata = FileMetadataResponse.builder()
                    .originalName(FILENAME)
                    .contentType(CONTENT_TYPE)
                    .sizeBytes((long) fileContent.length)
                    .build();

            when(fileService.loadFileAsResource(PUBLIC_ID, OWNER_ID)).thenReturn(resource);
            when(fileService.getFileMetadata(PUBLIC_ID)).thenReturn(metadata);

            // When & Then
            mockMvc.perform(get(FILES_BASE_URL + "/{publicId}/content", PUBLIC_ID)
                            .param("ownerId", OWNER_ID.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", CONTENT_TYPE))
                    .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + FILENAME + "\""))
                    .andExpect(header().string("Content-Length", String.valueOf(fileContent.length)))
                    .andExpect(content().bytes(fileContent));

            verify(fileService, times(1)).loadFileAsResource(PUBLIC_ID, OWNER_ID);
            verify(fileService, times(1)).getFileMetadata(PUBLIC_ID);
        }

        @Test
        @DisplayName("Should return 404 when file does not exist")
        void downloadFile_WithNonExistentFile_ShouldReturn404() throws Exception {
            // Given
            when(fileService.loadFileAsResource(PUBLIC_ID, OWNER_ID))
                    .thenThrow(new BusinessException("File not found"));

            // When & Then
            mockMvc.perform(get(FILES_BASE_URL + "/{publicId}/content", PUBLIC_ID)
                            .param("ownerId", OWNER_ID.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, times(1)).loadFileAsResource(PUBLIC_ID, OWNER_ID);
        }

        @Test
        @DisplayName("Should return 403 when non-owner tries to download")
        void downloadFile_WithNonOwner_ShouldReturn403() throws Exception {
            // Given
            Long otherUserId = 999L;

            when(fileService.loadFileAsResource(PUBLIC_ID, otherUserId))
                    .thenThrow(new BusinessException("You are not allowed to access this file"));

            // When & Then
            mockMvc.perform(get(FILES_BASE_URL + "/{publicId}/content", PUBLIC_ID)
                            .param("ownerId", otherUserId.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, times(1)).loadFileAsResource(PUBLIC_ID, otherUserId);
        }

        @Test
        @DisplayName("Should return 400 when ownerId parameter is missing")
        void downloadFile_WithoutOwnerId_ShouldReturn400() throws Exception {
            // When & Then
            mockMvc.perform(get(FILES_BASE_URL + "/{publicId}/content", PUBLIC_ID))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, never()).loadFileAsResource(anyString(), anyLong());
        }
    }

    @Nested
    @DisplayName("POST /api/files")
    class UploadFileEndpointTests {

        @Test
        @DisplayName("Should return 201 and upload result when file is valid")
        void upload_WithValidFile_ShouldReturn201AndUploadResult() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    FILENAME,
                    CONTENT_TYPE,
                    "Test file content".getBytes()
            );

            FileUploadResult uploadResult = FileUploadResult.builder()
                    .publicId(PUBLIC_ID)
                    .originalName(FILENAME)
                    .extension("PDF")
                    .sizeBytes(file.getSize())
                    .storagePath("2025/11/26/pdf/T/" + PUBLIC_ID + ".pdf")
                    .build();

            // ✅ DOĞRU PARAMETRE SIRASI: ownerId, file
            when(fileService.uploadFile(eq(OWNER_ID), any())).thenReturn(uploadResult);

            // When & Then
            mockMvc.perform(multipart(FILES_BASE_URL)
                            .file(file)
                            .param("ownerId", OWNER_ID.toString()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.publicId").value(PUBLIC_ID))
                    .andExpect(jsonPath("$.originalName").value(FILENAME))
                    .andExpect(jsonPath("$.extension").value("PDF"))
                    .andExpect(jsonPath("$.sizeBytes").value(file.getSize()));

            verify(fileService, times(1)).uploadFile(eq(OWNER_ID), any());
        }

        @Test
        @DisplayName("Should return 400 when file is empty")
        void upload_WithEmptyFile_ShouldReturn400() throws Exception {
            // Given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    FILENAME,
                    CONTENT_TYPE,
                    new byte[0]
            );

            when(fileService.uploadFile(eq(OWNER_ID), any()))
                    .thenThrow(new BusinessException("Uploaded file must not be empty"));

            // When & Then
            mockMvc.perform(multipart(FILES_BASE_URL)
                            .file(emptyFile)
                            .param("ownerId", OWNER_ID.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, times(1)).uploadFile(eq(OWNER_ID), any());
        }

        @Test
        @DisplayName("Should return 400 when file size exceeds limit")
        void upload_WithOversizedFile_ShouldReturn400() throws Exception {
            // Given
            byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file",
                    FILENAME,
                    CONTENT_TYPE,
                    largeContent
            );

            when(fileService.uploadFile(eq(OWNER_ID), any()))
                    .thenThrow(new BusinessException("File size exceeds the maximum limit of 5 MB"));

            // When & Then
            mockMvc.perform(multipart(FILES_BASE_URL)
                            .file(largeFile)
                            .param("ownerId", OWNER_ID.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, times(1)).uploadFile(eq(OWNER_ID), any());
        }

        @Test
        @DisplayName("Should return 400 when file extension is not allowed")
        void upload_WithInvalidExtension_ShouldReturn400() throws Exception {
            // Given
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "file",
                    "malicious.exe",
                    "application/x-msdownload",
                    "fake content".getBytes()
            );

            when(fileService.uploadFile(eq(OWNER_ID), any()))
                    .thenThrow(new BusinessException("File extension 'exe' is not allowed"));

            // When & Then
            mockMvc.perform(multipart(FILES_BASE_URL)
                            .file(invalidFile)
                            .param("ownerId", OWNER_ID.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, times(1)).uploadFile(eq(OWNER_ID), any());
        }

        @Test
        @DisplayName("Should return 400 when owner does not exist")
        void upload_WithNonExistentOwner_ShouldReturn400() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    FILENAME,
                    CONTENT_TYPE,
                    "Test content".getBytes()
            );

            when(fileService.uploadFile(eq(OWNER_ID), any()))
                    .thenThrow(new BusinessException("Owner not found"));

            // When & Then
            mockMvc.perform(multipart(FILES_BASE_URL)
                            .file(file)
                            .param("ownerId", OWNER_ID.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, times(1)).uploadFile(eq(OWNER_ID), any());
        }

        @Test
        @DisplayName("Should return 400 when ownerId parameter is missing")
        void upload_WithoutOwnerId_ShouldReturn400() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    FILENAME,
                    CONTENT_TYPE,
                    "Test content".getBytes()
            );

            // When & Then
            mockMvc.perform(multipart(FILES_BASE_URL)
                            .file(file))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, never()).uploadFile(anyLong(), any());
        }

        @Test
        @DisplayName("Should return 400 when file parameter is missing")
        void upload_WithoutFile_ShouldReturn400() throws Exception {
            // When & Then
            mockMvc.perform(multipart(FILES_BASE_URL)
                            .param("ownerId", OWNER_ID.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, never()).uploadFile(anyLong(), any());
        }
    }

    @Nested
    @DisplayName("PUT /api/files/{publicId}")
    class UpdateFileEndpointTests {

        @Test
        @DisplayName("Should return 200 and updated info when update is successful")
        void updateFile_WithValidData_ShouldReturn200AndUpdatedInfo() throws Exception {
            // Given
            MockMultipartFile newFile = new MockMultipartFile(
                    "file",
                    "updated-document.pdf",
                    CONTENT_TYPE,
                    "Updated content".getBytes()
            );

            FileUploadResult updateResult = FileUploadResult.builder()
                    .publicId(PUBLIC_ID)
                    .originalName("updated-document.pdf")
                    .extension("PDF")
                    .sizeBytes(newFile.getSize())
                    .storagePath("2025/11/26/pdf/U/" + PUBLIC_ID + ".pdf")
                    .build();

            // ✅ DOĞRU PARAMETRE SIRASI: publicId, ownerId, file
            when(fileService.updateFile(eq(PUBLIC_ID), eq(OWNER_ID), any())).thenReturn(updateResult);

            // When & Then
            mockMvc.perform(multipart(FILES_BASE_URL + "/{publicId}", PUBLIC_ID)
                            .file(newFile)
                            .param("ownerId", OWNER_ID.toString())
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.publicId").value(PUBLIC_ID))
                    .andExpect(jsonPath("$.originalName").value("updated-document.pdf"))
                    .andExpect(jsonPath("$.extension").value("PDF"));

            verify(fileService, times(1)).updateFile(eq(PUBLIC_ID), eq(OWNER_ID), any());
        }

        @Test
        @DisplayName("Should return 404 when file does not exist")
        void updateFile_WithNonExistentFile_ShouldReturn404() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    FILENAME,
                    CONTENT_TYPE,
                    "Content".getBytes()
            );

            when(fileService.updateFile(eq(PUBLIC_ID), eq(OWNER_ID), any()))
                    .thenThrow(new BusinessException("File not found"));

            // When & Then
            mockMvc.perform(multipart(FILES_BASE_URL + "/{publicId}", PUBLIC_ID)
                            .file(file)
                            .param("ownerId", OWNER_ID.toString())
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, times(1)).updateFile(eq(PUBLIC_ID), eq(OWNER_ID), any());
        }

        @Test
        @DisplayName("Should return 403 when non-owner tries to update")
        void updateFile_WithNonOwner_ShouldReturn403() throws Exception {
            // Given
            Long otherUserId = 999L;
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    FILENAME,
                    CONTENT_TYPE,
                    "Content".getBytes()
            );

            when(fileService.updateFile(eq(PUBLIC_ID), eq(otherUserId), any()))
                    .thenThrow(new BusinessException("You are not allowed to update this file"));

            // When & Then
            mockMvc.perform(multipart(FILES_BASE_URL + "/{publicId}", PUBLIC_ID)
                            .file(file)
                            .param("ownerId", otherUserId.toString())
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, times(1)).updateFile(eq(PUBLIC_ID), eq(otherUserId), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/files/{publicId}")
    class DeleteFileEndpointTests {

        @Test
        @DisplayName("Should return 204 when file is successfully deleted")
        void delete_WithValidRequest_ShouldReturn204() throws Exception {
            // Given - void method, no stubbing needed
            doNothing().when(fileService).deleteFile(OWNER_ID, PUBLIC_ID);

            // When & Then
            mockMvc.perform(delete(FILES_BASE_URL + "/{publicId}", PUBLIC_ID)
                            .param("ownerId", OWNER_ID.toString()))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(fileService, times(1)).deleteFile(OWNER_ID, PUBLIC_ID);
        }

        @Test
        @DisplayName("Should return 404 when file does not exist")
        void delete_WithNonExistentFile_ShouldReturn404() throws Exception {
            // Given
            doThrow(new BusinessException("File not found"))
                    .when(fileService)
                    .deleteFile(OWNER_ID, PUBLIC_ID);

            // When & Then
            mockMvc.perform(delete(FILES_BASE_URL + "/{publicId}", PUBLIC_ID)
                            .param("ownerId", OWNER_ID.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, times(1)).deleteFile(OWNER_ID, PUBLIC_ID);
        }

        @Test
        @DisplayName("Should return 403 when non-owner tries to delete")
        void delete_WithNonOwner_ShouldReturn403() throws Exception {
            // Given
            Long otherUserId = 999L;

            doThrow(new BusinessException("You are not allowed to delete this file"))
                    .when(fileService)
                    .deleteFile(otherUserId, PUBLIC_ID);

            // When & Then
            mockMvc.perform(delete(FILES_BASE_URL + "/{publicId}", PUBLIC_ID)
                            .param("ownerId", otherUserId.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, times(1)).deleteFile(otherUserId, PUBLIC_ID);
        }

        @Test
        @DisplayName("Should return 400 when ownerId parameter is missing")
        void delete_WithoutOwnerId_ShouldReturn400() throws Exception {
            // When & Then
            mockMvc.perform(delete(FILES_BASE_URL + "/{publicId}", PUBLIC_ID))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(fileService, never()).deleteFile(anyLong(), anyString());
        }
    }
}