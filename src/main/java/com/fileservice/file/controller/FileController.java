package com.fileservice.file.controller;

import com.fileservice.file.pojo.dto.FileListItemResponse;
import com.fileservice.file.pojo.dto.FileMetadataResponse;
import com.fileservice.file.pojo.dto.FileUploadResult;
import com.fileservice.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller exposing file management operations.
 * <p>
 * For the coding challenge the caller passes {@code ownerId} as a request
 * parameter. In a real system this would typically be derived from the
 * authenticated user instead of being supplied explicitly.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "Operations for uploading, listing, reading metadata and deleting files.")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileService fileService;

    /**
     * Returns a list of files belonging to a specific owner.
     * <p>
     * Pagination is intentionally omitted for the challenge and can be added
     * later if needed.
     *
     * @param ownerId technical identifier of the owner
     * @return list of lightweight file DTOs
     */
    @GetMapping
    @Operation(summary = "List files of a user", description = "Returns a lightweight list of files that belong to the specified owner.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Files successfully retrieved", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FileListItemResponse.class))))})
    public ResponseEntity<List<FileListItemResponse>> listFiles(@Parameter(in = ParameterIn.QUERY, description = "Technical identifier of the file owner. " + "In a production system this would usually be resolved from the authenticated user.", required = true, example = "2")
                                                                @RequestParam("ownerId") Long ownerId) {
        List<FileListItemResponse> files = fileService.listFiles(ownerId);
        return ResponseEntity.ok(files);
    }

    /**
     * Returns detailed metadata for a single file identified by its public id.
     * <p>
     * This endpoint does not return the binary content, only descriptive
     * metadata. A dedicated download endpoint can be added later.
     *
     * @param publicId public UUID of the file
     * @return metadata DTO of the requested file
     */
    @GetMapping("/{publicId}")
    @Operation(summary = "Get file metadata", description = "Fetches detailed metadata for a single file identified by its public id.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Metadata successfully retrieved", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileMetadataResponse.class))), @ApiResponse(responseCode = "404", description = "File not found", content = @Content)})
    public ResponseEntity<FileMetadataResponse> getMetadata(@Parameter(in = ParameterIn.PATH, description = "Public UUID of the file.", required = true, example = "11111111-1111-1111-1111-111111111111")
                                                            @PathVariable("publicId") String publicId) {
        FileMetadataResponse metadata = fileService.getFileMetadata(publicId);
        return ResponseEntity.ok(metadata);
    }

    /**
     * Uploads a new file for the given owner.
     * <p>
     * The file is stored on disk and its metadata is persisted in the
     * {@code files} table. The response contains the generated public id
     * and basic metadata.
     *
     * @param ownerId owner identifier
     * @param file    multipart file payload
     * @return upload result DTO
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a new file", description = "Stores a new file on disk and persists its metadata for the specified owner.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "File successfully uploaded", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResult.class))), @ApiResponse(responseCode = "400", description = "Invalid request or unsupported file type", content = @Content)})
    public ResponseEntity<FileUploadResult> upload(@Parameter(in = ParameterIn.QUERY, description = "Technical identifier of the file owner.", required = true, example = "2")
                                                   @RequestParam("ownerId") Long ownerId,
                                                   @Parameter(description = "Binary file payload to be uploaded.", required = true)
                                                   @RequestParam("file") MultipartFile file) {
        FileUploadResult result = fileService.uploadFile(ownerId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Deletes a file identified by its public id for the given owner.
     * <p>
     * The underlying service currently performs a soft delete by setting the
     * {@code deleted_at} timestamp; physical removal from disk can be added
     * later if needed.
     *
     * @param publicId public UUID of the file
     * @param ownerId  technical identifier of the owner
     * @return empty response with HTTP 204 status code on success
     */
    @DeleteMapping("/{publicId}")
    @Operation(summary = "Delete a file", description = "Deletes a file identified by its public id for the specified owner.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "File successfully deleted", content = @Content), @ApiResponse(responseCode = "404", description = "File not found", content = @Content)})
    public ResponseEntity<Void> delete(@Parameter(in = ParameterIn.PATH, description = "Public UUID of the file.", required = true, example = "11111111-1111-1111-1111-111111111111")
                                       @PathVariable("publicId") String publicId,
                                       @Parameter(in = ParameterIn.QUERY, description = "Technical identifier of the file owner.", required = true, example = "2")
                                       @RequestParam("ownerId") Long ownerId) {
        fileService.deleteFile(ownerId, publicId);
        return ResponseEntity.noContent().build();
    }
}
