package com.cts.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileImageControllerTest {

    private MockMvc mvc;
    private ProfileImageController controller;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        controller = new ProfileImageController();
        ReflectionTestUtils.setField(controller, "uploadDir", tempDir.toString());
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ── POST /api/profile-image/{candidateId} ───────────────────────────

    @Test
    @DisplayName("✓ POST stores the JPG and returns 200 'Uploaded successfully'")
    void upload_happy_path() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "me.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});

        mvc.perform(multipart("/api/profile-image/{id}", 2308322L).file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Uploaded successfully"));

        assert Files.exists(tempDir.resolve("2308322.jpg"));
    }

    @Test
    @DisplayName("✗ POST returns 400 'Empty file' when no bytes are uploaded")
    void upload_empty_file() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "me.jpg", "image/jpeg", new byte[0]);

        mvc.perform(multipart("/api/profile-image/{id}", 1L).file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Empty file"));

        assert !Files.exists(tempDir.resolve("1.jpg"));
    }

    @Test
    @DisplayName("✓ POST overwrites an existing JPG when re-uploading")
    void upload_overwrites_existing() throws Exception {
        // Pre-existing image
        Files.write(tempDir.resolve("5.jpg"), new byte[]{1});

        MockMultipartFile file = new MockMultipartFile(
                "file", "new.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0});

        mvc.perform(multipart("/api/profile-image/{id}", 5L).file(file))
                .andExpect(status().isOk());

        byte[] stored = Files.readAllBytes(tempDir.resolve("5.jpg"));
        assert stored.length == 4; // not the original 1-byte file anymore
    }

    // ── GET /api/profile-image/{candidateId} ────────────────────────────

    @Test
    @DisplayName("✓ GET serves the JPG as image/jpeg when present")
    void get_existing_image() throws Exception {
        Files.write(tempDir.resolve("123.jpg"), new byte[]{(byte) 0xFF, (byte) 0xD8});

        mvc.perform(get("/api/profile-image/{id}", 123L))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));
    }

    @Test
    @DisplayName("✗ GET returns 404 when the image is missing (no default fallback)")
    void get_missing_image_returns_404() throws Exception {
        mvc.perform(get("/api/profile-image/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
