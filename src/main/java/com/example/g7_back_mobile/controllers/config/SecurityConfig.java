package com.example.g7_back_mobile.controllers.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.g7_back_mobile.repositories.entities.Role;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        @Autowired
        private final JwtAuthenticationFilter jwtAuthFilter;
        @Autowired
        private final AuthenticationProvider authenticationProvider;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf
						.ignoringRequestMatchers("/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**"))
					.headers(headers -> headers
							.frameOptions(frameOptions -> frameOptions.sameOrigin()))
					.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(req -> req
							//h2-console
							.requestMatchers("/h2-console/**").permitAll()
							// Swagger
							.requestMatchers("/swagger-ui/**").permitAll()
							.requestMatchers("/v3/api-docs/**").permitAll()
							//Auth - endpoints públicos
                            .requestMatchers("/auth/iniciar-registro", "/auth/finalizar-registro", "/auth/authenticate").permitAll()
                            // Auth - logout requiere autenticación
                            .requestMatchers("/auth/logout").authenticated()
                            // User
                            .requestMatchers("/users/**").authenticated()
							// Rutas públicas para cargar datos por default y acceder a ellos
                        	.requestMatchers(HttpMethod.POST, "/headquarters/initializeHeadquarters").permitAll()
							.requestMatchers(HttpMethod.GET, "/headquarters/**").permitAll()
                        	.requestMatchers(HttpMethod.POST, "/teachers/initializeTeachers").permitAll()
							.requestMatchers(HttpMethod.GET, "/teachers/**").permitAll()
							.requestMatchers(HttpMethod.POST, "/sports/initializeSports").permitAll()
							.requestMatchers(HttpMethod.GET, "/sports/**").permitAll()
							.requestMatchers(HttpMethod.POST, "/courses/initializeCourses").permitAll()
							.requestMatchers(HttpMethod.GET, "/courses/**").permitAll()
							// Accesos publicos en general
							.requestMatchers(HttpMethod.GET, "/reservations/**").permitAll()
							.requestMatchers(HttpMethod.GET, "/shifts/**").permitAll()
							.requestMatchers(HttpMethod.GET, "/inscriptions/**").permitAll()
							.requestMatchers(HttpMethod.GET, "/asistencias/**").permitAll()

                            // Default
                            .anyRequest().authenticated())
                    .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                    .authenticationProvider(authenticationProvider)
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
        
        // Configuracion de CORS
	@Bean
	public UrlBasedCorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration corsConfig = new CorsConfiguration();
		// Permitir orígenes específicos para desarrollo
    	corsConfig.setAllowedOrigins(List.of(
			"http://10.0.2.2:8080",     // Emulador Android
			"http://localhost:8080",     // Desarrollo local
			"http://127.0.0.1:8080",     // Loopback
			"http://192.168.*",          // Red local (wildcard)
			"*"                          // Fallback para desarrollo
    	));
    
    	// O usar setAllowedOriginPatterns para más flexibilidad
    	// corsConfig.setAllowedOriginPatterns(List.of("*"));
		corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		corsConfig.setAllowCredentials(true);
		corsConfig.setAllowedHeaders(List.of(
			"*",
			"Authorization",
			"Content-Type",
			"X-Requested-With",
			"Accept",
			"Origin",
			"Access-Control-Request-Method",
			"Access-Control-Request-Headers"
    	));
		corsConfig.setExposedHeaders(List.of(
			"Access-Control-Allow-Origin",
			"Access-Control-Allow-Credentials"
		));
		// Tiempo de cache para preflight requests
    	corsConfig.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);
		return source;
	}

}