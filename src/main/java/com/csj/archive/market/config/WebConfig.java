package com.csj.archive.market.config;

import com.csj.archive.market.security.ArchiveSecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final ArchiveSecurityProperties security;
    public WebConfig(ArchiveSecurityProperties security) { this.security = security; }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedOrigins(security.allowedOrigins().toArray(String[]::new))
                .allowedHeaders("Authorization", "X-Archive-Source-System", "X-Archive-Service-Scope", "Content-Type");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/dashboard").setViewName("forward:/dashboard/index.html");
        registry.addViewController("/dashboard/").setViewName("forward:/dashboard/index.html");
    }
}
