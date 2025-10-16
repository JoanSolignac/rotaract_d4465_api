package com.unapi.rotaract.rotaract_d4465_api.auth.security;

import com.unapi.rotaract.rotaract_d4465_api.auth.jwt.JwtAuthenticationFilter;
import com.unapi.rotaract.rotaract_d4465_api.auth.service.UserDetailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailServiceImpl userDetailService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Activa CORS
            .authorizeHttpRequests(auth -> auth
                // .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/auth/**", 
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/webjars/**"
                        ).permitAll() // Endpoints públicos
                .anyRequest().authenticated()                   // Todo lo demás requiere token
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Proporciona la configuración de seguridad principal para la aplicación.
     *
     * Esta configuración desactiva CSRF, habilita CORS con la configuración
     * proporcionada por {@link #corsConfigurationSource()}, configura la
     * política de sesiones como stateless, establece el proveedor de
     * autenticación y añade el filtro JWT antes del filtro de autenticación
     * por nombre de usuario y contraseña.
     *
     * @param http objeto HttpSecurity proporcionado por Spring Security
     * @return la cadena de filtros de seguridad construida
     * @throws Exception si ocurre un error durante la construcción de la cadena
     */

    /**
     * Configuración de CORS para permitir acceso desde React local.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (React ejecutándose localmente)
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174", "http://localhost:3000"));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cabeceras permitidas (incluye Authorization)
        configuration.setAllowedHeaders(List.of("*"));

        // Permite envío de credenciales (Authorization header, cookies, etc.)
        configuration.setAllowCredentials(true);

        // Tiempo de cacheo del preflight request (en segundos)
        configuration.setMaxAge(3600L);

        // Aplica la configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Define la configuración de CORS usada por la aplicación.
     *
     * Se permiten orígenes locales típicos de entornos de desarrollo (React),
     * se habilitan los métodos HTTP habituales, se permiten todas las cabeceras
     * y se aceptan credenciales. La configuración se aplica a todas las rutas.
     *
     * @return CorsConfigurationSource que se registra como bean de Spring
     */

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configura y devuelve el AuthenticationProvider que utiliza un
     * UserDetailsService para cargar usuarios y un PasswordEncoder para validar
     * credenciales.
     *
     * @return AuthenticationProvider configurado con DAO y encriptador BCrypt
     */

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Expone el AuthenticationManager gestionado por Spring Security.
     *
     * @param config configuración de autenticación proporcionada por Spring
     * @return AuthenticationManager
     * @throws Exception si no es posible obtener el AuthenticationManager
     * 
     * @return PasswordEncoder basado en BCrypt
     */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
