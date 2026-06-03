package com.cts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;

@RestController
@RequestMapping("/api/profile-image")
@Tag(name = "Profile Image (legacy)", description = "Legacy profile-photo endpoints kept for compatibility. Prefer /trainee/profile-photo or /admin/profile-photo for new clients.")
// CORS is handled globally by SecurityConfig#corsConfigurationSource using
// `app.cors.allowed-origins` from application.properties (env-driven).
// No per-controller @CrossOrigin needed — the hardcoded one was removed
// so localhost URLs don't leak into production builds.
public class ProfileImageController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // ✅ Upload / overwrite image
    @PostMapping("/{candidateId}")
    @Operation(summary = "Upload / overwrite profile photo (legacy)",
            description = "Same behaviour as /trainee/profile-photo/{associateId}. Kept for backwards compatibility.")
    public ResponseEntity<String> uploadProfileImage(
            @PathVariable Long candidateId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Empty file");
        }

        Files.createDirectories(Paths.get(uploadDir));

        String filename = candidateId + ".jpg";
        Path filePath = Paths.get(uploadDir).resolve(filename);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return ResponseEntity.ok("Uploaded successfully");
    }

    // ✅ Fetch image by candidateId
    @GetMapping("/{candidateId}")
    @Operation(summary = "Fetch a profile photo (legacy)",
            description = "Returns 404 if no photo exists for the given id. Unlike the trainee/admin endpoints there is no default-image fallback.")
    public ResponseEntity<Resource> getProfileImage(
            @PathVariable Long candidateId
    ) throws MalformedURLException {

        Path imagePath = Paths.get(uploadDir).resolve(candidateId + ".jpg");

        if (!Files.exists(imagePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(imagePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
}