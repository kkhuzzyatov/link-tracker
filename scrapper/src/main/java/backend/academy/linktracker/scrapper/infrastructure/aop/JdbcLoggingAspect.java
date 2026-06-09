package backend.academy.linktracker.scrapper.infrastructure.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class JdbcLoggingAspect {

    @Around("@annotation(loggableQuery)")
    public Object logExecution(ProceedingJoinPoint pjp, LoggableQuery loggableQuery) throws Throwable {

        String queryName = loggableQuery.value();
        long start = System.nanoTime();

        try {
            Object result = pjp.proceed();

            long durationMs = (System.nanoTime() - start) / 1_000_000;

            log.atDebug()
                    .addKeyValue("event", "db_query")
                    .addKeyValue("query", queryName)
                    .addKeyValue("duration_ms", durationMs)
                    .log();

            // slow query
            if (durationMs > 200) {
                log.atWarn()
                        .addKeyValue("event", "slow_db_query")
                        .addKeyValue("query", queryName)
                        .addKeyValue("duration_ms", durationMs)
                        .log();
            }

            return result;

        } catch (Exception e) {
            long durationMs = (System.nanoTime() - start) / 1_000_000;

            log.atError()
                    .addKeyValue("event", "db_query_error")
                    .addKeyValue("query", queryName)
                    .addKeyValue("duration_ms", durationMs)
                    .setCause(e)
                    .log("Database query failed");

            throw e;
        }
    }
}
