package com.hms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI hmsOpenAPI() {
		String bearerScheme = "bearerAuth";
		String csrfScheme = "csrfToken";
		return new OpenAPI()
				.info(new Info()
						.title("HMS - Hospital Management System API")
						.version("2.0.0")
						.description("API REST padronizada do HMS com JPA (Jakarta), JWT (15 min + Refresh Token), proteção CSRF stateless (GET /hms/auth/csrf), Agendamentos (Sol. 08), Lucro por Médico (Sol. 11) e Acessibilidade (Sol. 14)."))
				.addSecurityItem(new SecurityRequirement().addList(bearerScheme).addList(csrfScheme))
				.components(new Components()
						.addSecuritySchemes(bearerScheme, new SecurityScheme()
								.name(bearerScheme)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT"))
						.addSecuritySchemes(csrfScheme, new SecurityScheme()
								.name("X-CSRF-TOKEN")
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER)
								.description("Token obtido em GET /hms/auth/csrf")));
	}
}
