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
		String schemeName = "bearerAuth";
		return new OpenAPI()
				.info(new Info()
						.title("HMS - Hospital Management System API")
						.version("2.0.0")
						.description("API REST padronizada do HMS com JPA (Jakarta), JWT (15 min + Refresh Token), Agendamentos (Sol. 08), Lucro por Médico (Sol. 11) e Acessibilidade (Sol. 14)."))
				.addSecurityItem(new SecurityRequirement().addList(schemeName))
				.components(new Components()
						.addSecuritySchemes(schemeName, new SecurityScheme()
								.name(schemeName)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
