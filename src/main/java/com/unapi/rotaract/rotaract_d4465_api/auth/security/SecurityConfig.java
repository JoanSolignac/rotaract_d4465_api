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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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

/**
 * Configuración central de seguridad para la aplicación.
 *
 * - Desactiva CSRF (API REST sin sesiones tradicionales).
 * - Habilita CORS con una configuración adecuada para desarrollo local.
 * - Define la política de sesiones como stateless.
 * - Registra el filtro JWT y el proveedor de autenticación.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailServiceImpl userDetailService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configura la cadena de filtros de seguridad (endpoints públicos, política de sesiones,
     * proveedor de autenticación y filtro JWT).
     *
     * @param http objeto HttpSecurity provisto por Spring Security
     * @return instancia de {@link SecurityFilterChain}
     * @throws Exception si ocurre un error durante la configuración
     */
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
                        "/webjars/**",
                        "/webjars/**",
                        "/clubs/public/**",
                        "/proyectos/public/**",
                        "/convocatorias/public/**"
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
     * Configuración de CORS para permitir peticiones desde clientes locales
     * (por ejemplo aplicaciones React en desarrollo).
     *
     * @return CorsConfigurationSource que se registra como Bean en el contexto de Spring
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (React ejecutándose localmente)
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174", "http://localhost:3000", "https://rotaractd4465.up.railway.app"));

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
     * Proporciona un {@link AuthenticationProvider} que delega en un
     * {@link org.springframework.security.core.userdetails.UserDetailsService}
     * y utiliza BCrypt para verificar contraseñas.
     *
     * @return AuthenticationProvider configurado
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expone el {@link AuthenticationManager} gestionado por Spring Security.
     *
     * @param config configuración de autenticación proporcionada por Spring
     * @return AuthenticationManager
     * @throws Exception si no es posible obtener el AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Devuelve un {@link PasswordEncoder} basado en BCrypt para hashear contraseñas.
     *
     * @return PasswordEncoder configurado con BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
