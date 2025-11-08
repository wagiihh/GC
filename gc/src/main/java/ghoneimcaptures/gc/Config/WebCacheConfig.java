package ghoneimcaptures.gc.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebCacheConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cache static resources (CSS, JS, images) for optimized performance
        // CSS and JS files - cache for 1 year (use versioning for cache busting)
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS)
                        .cachePublic()
                        .mustRevalidate());

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS)
                        .cachePublic()
                        .mustRevalidate());

        // Images - cache for 30 days
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS)
                        .cachePublic()
                        .mustRevalidate());

        // Videos - cache for 30 days
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("classpath:/static/videos/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS)
                        .cachePublic()
                        .mustRevalidate());
    }
}

