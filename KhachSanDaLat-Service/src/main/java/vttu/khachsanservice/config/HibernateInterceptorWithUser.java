package vttu.khachsanservice.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;
import vttu.khachsanservice.utils.SecurityUtil;

/**
 * Hibernate StatementInspector để log SQL kèm username
 */
@Slf4j
@Component
public class HibernateInterceptorWithUser implements StatementInspector {

    @Override
    public String inspect(String sql) {
        String username = SecurityUtil.getUsername();
        log.info("👤 [username: {}]", username);
        return sql;
    }
}

