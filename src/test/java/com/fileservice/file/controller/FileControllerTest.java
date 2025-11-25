package com.fileservice.file.controller;

import com.fileservice.file.pojo.dto.FileListItemResponse;
import com.fileservice.file.pojo.dto.FileMetadataResponse;
import com.fileservice.file.pojo.dto.FileUploadResult;
import com.fileservice.file.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller layer tests for {@link FileController}.
 * Tests HTTP mapping, request/response handling, and status codes.
 */
@WebMvcTest(FileController.class)
@DisplayName("FileController Tests")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    private static final Long TEST_OWNER_ID = 2L;
    private static final String TEST_PUBLIC_ID = "11111111-1111-1111-1111-111111111111";

    @Test
    @WithMockUser
    @DisplayName("Should upload file successfully")
    void shouldUploadFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        FileUploadResult result = FileUploadResult.builder()
                .publicId(TEST_PUBLIC_ID)
                .originalName("test.pdf")
                .extension("PDF")
                .sizeBytes(1024L)
                .storagePath("2025/11/25/pdf/T/test.pdf")
                .build();

        when(fileService.uploadFile(anyLong(), any())).thenReturn(result);

        // When & Then
        mockMvc.perform(multipart("/api/files")
                        .file(file)
                        .param("ownerId", TEST_OWNER_ID.toString())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").value(TEST_PUBLIC_ID))
                .andExpect(jsonPath("$.originalName").value("test.pdf"))
                .andExpect(jsonPath("$.extension").value("PDF"));

        verify(fileService).uploadFile(anyLong(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("Should list files successfully")
    void shouldListFiles() throws Exception {
        // Given
        FileListItemResponse item = FileListItemResponse.builder()
                .publicId(TEST_PUBLIC_ID)
                .originalName("test.pdf")
                .extension("PDF")
                .contentType("application/pdf")
                .sizeBytes(1024L)
                .createdAt(Instant.now())
                .temp(false)
                .build();

        List<FileListItemResponse> files = Arrays.asList(item);
        when(fileService.listFiles(anyLong())).thenReturn(files);

        // When & Then
        mockMvc.perform(get("/api/files")
                        .param("ownerId", TEST_OWNER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publicId").value(TEST_PUBLIC_ID))
                .andExpect(jsonPath("$[0].originalName").value("test.pdf"));

        verify(fileService).listFiles(TEST_OWNER_ID);
    }

    @Test
    @WithMockUser
    @DisplayName("Should get file metadata successfully")
    void shouldGetFileMetadata() throws Exception {
        // Given
        FileMetadataResponse metadata = FileMetadataResponse.builder()
                .publicId(TEST_PUBLIC_ID)
                .originalName("test.pdf")
                .storedName(TEST_PUBLIC_ID + ".pdf")
                .extension("PDF")
                .contentType("application/pdf")
                .sizeBytes(1024L)
                .storagePath("2025/11/25/pdf/T/test.pdf")
                .temp(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(fileService.getFileMetadata(anyString())).thenReturn(metadata);

        // When & Then
        mockMvc.perform(get("/api/files/{publicId}", TEST_PUBLIC_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(TEST_PUBLIC_ID))
                .andExpect(jsonPath("$.originalName").value("test.pdf"))
                .andExpect(jsonPath("$.extension").value("PDF"));

        verify(fileService).getFileMetadata(TEST_PUBLIC_ID);
    }

    @Test
    @WithMockUser
    @DisplayName("Should download file content successfully")
    void shouldDownloadFileContent() throws Exception {
        // Given
        byte[] content = "file content".getBytes();
        Resource resource = new ByteArrayResource(content);

        FileMetadataResponse metadata = FileMetadataResponse.builder()
                .publicId(TEST_PUBLIC_ID)
                .originalName("test.pdf")
                .contentType("application/pdf")
                .sizeBytes((long) content.length)
                .build();

        when(fileService.loadFileAsResource(anyString(), anyLong())).thenReturn(resource);
        when(fileService.getFileMetadata(anyString())).thenReturn(metadata);

        // When & Then
        mockMvc.perform(get("/api/files/{publicId}/content", TEST_PUBLIC_ID)
                        .param("ownerId", TEST_OWNER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/pdf")))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(content));

        verify(fileService).loadFileAsResource(TEST_PUBLIC_ID, TEST_OWNER_ID);
        verify(fileService).getFileMetadata(TEST_PUBLIC_ID);
    }

    @Test
    @WithMockUser
    @DisplayName("Should update file successfully")
    void shouldUpdateFile() throws Exception {
        // Given
        MockMultipartFile newFile = new MockMultipartFile(
                "file",
                "updated.pdf",
                "application/pdf",
                "updated content".getBytes()
        );

        FileUploadResult result = FileUploadResult.builder()
                .publicId(TEST_PUBLIC_ID)
                .originalName("updated.pdf")
                .extension("PDF")
                .sizeBytes(2048L)
                .storagePath("2025/11/25/pdf/U/updated.pdf")
                .build();

        when(fileService.updateFile(anyString(), anyLong(), any())).thenReturn(result);

        // When & Then
        mockMvc.perform(multipart("/api/files/{publicId}", TEST_PUBLIC_ID)
                        .file(newFile)
                        .param("ownerId", TEST_OWNER_ID.toString())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(TEST_PUBLIC_ID))
                .andExpect(jsonPath("$.originalName").value("updated.pdf"));

        verify(fileService).updateFile(anyString(), anyLong(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("Should delete file successfully")
    void shouldDeleteFile() throws Exception {
        // Given
        doNothing().when(fileService).deleteFile(anyLong(), anyString());

        // When & Then
        mockMvc.perform(delete("/api/files/{publicId}", TEST_PUBLIC_ID)
                        .param("ownerId", TEST_OWNER_ID.toString())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(fileService).deleteFile(TEST_OWNER_ID, TEST_PUBLIC_ID);
    }
}
