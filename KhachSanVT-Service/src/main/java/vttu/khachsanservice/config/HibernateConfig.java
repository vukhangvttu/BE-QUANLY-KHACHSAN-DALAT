package vttu.khachsanservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Hibernate để sử dụng StatementInspector
 */
@Configuration
@RequiredArgsConstructor
public class HibernateConfig {

    private final HibernateInterceptorWithUser hibernateInterceptorWithUser;

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties ->
            hibernateProperties.put("hibernate.session_factory.statement_inspector", hibernateInterceptorWithUser);
    }
}

