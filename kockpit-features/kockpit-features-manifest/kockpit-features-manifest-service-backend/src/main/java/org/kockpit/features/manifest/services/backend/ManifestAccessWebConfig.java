package org.kockpit.features.manifest.services.backend;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ManifestAccessWebConfig implements WebMvcConfigurer {

    private final ManifestAccessService manifestAccessService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ManifestAccessInterceptor(manifestAccessService))
                .addPathPatterns(pathPatterns());
    }

    // "/{domain}/{env}/{feature}/**" for every guarded feature ("/**" also matches no segment).
    static String[] pathPatterns() {
        return ManifestAccessInterceptor.SERVICE_TYPE_BY_FEATURE.keySet().stream()
                .map(feature -> "/*/*/" + feature + "/**")
                .toArray(String[]::new);
    }
}
