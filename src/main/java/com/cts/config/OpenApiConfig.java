package com.cts.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration.
 *
 * Wires up:
 *  - High-level project metadata shown at the top of the Swagger UI.
 *  - A Bearer-JWT security scheme so users can click the "Authorize" padlock,
 *    paste the token returned from POST /auth/login, and call protected endpoints
 *    directly from the docs page.
 *  - A global security requirement that marks every operation as JWT-protected
 *    by default. Public operations (login, register, change-password) override
 *    this by annotating with {@code @SecurityRequirements({})}.
 *
 * Once the app is running, the UI is available at:
 *   http://localhost:{server.port}/swagger-ui.html
 *
 * The raw OpenAPI JSON is at:
 *   http://localhost:{server.port}/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8085}")
    private String serverPort;

    @Bean
    public OpenAPI gencDossierOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Genc Dossier API")
                        .description(
                                "REST API for the Genc Dossier talent management platform.\n\n" +
                                "**Roles**: Admin, Leader, Trainee — each with role-scoped endpoints.\n\n" +
                                "**Authenticating**:\n" +
                                "1. Call `POST /auth/login` with email + password.\n" +
                                "2. Copy the `token` field from the response.\n" +
                                "3. Click the green **Authorize** button at the top right and paste the token.\n" +
                                "4. All subsequent requests from this page will include the JWT automatically.\n\n" +
                                "The token expires after 24 hours by default (`app.jwt.expiration`)."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Genc Dossier Team")
                                .email("noreply@cognizant.com"))
                        .license(new License().name("Internal — Cognizant")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local development"),
                        new Server().url("/").description("Current host")
                ))
                // Default: every operation requires JWT. Override on public endpoints
                // with @SecurityRequirements({}) (empty annotation).
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Paste the JWT returned from `POST /auth/login`. " +
                                                "The 'Bearer ' prefix is added automatically by Swagger UI."
                                        )));
    }
}
