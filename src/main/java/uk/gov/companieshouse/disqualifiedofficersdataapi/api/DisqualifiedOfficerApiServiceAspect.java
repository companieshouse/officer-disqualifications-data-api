package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.FeatureFlags;
import uk.gov.companieshouse.logging.Logger;

@Aspect
@Component
public class DisqualifiedOfficerApiServiceAspect {

    private final FeatureFlags featureFlags;
    private final Logger logger;

    public DisqualifiedOfficerApiServiceAspect(FeatureFlags featureFlags, Logger logger) {
        this.featureFlags = featureFlags;
        this.logger = logger;
    }

    @Around("execution(public uk.gov.companieshouse.api.model.ApiResponse<Void> uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService.invokeChsKafkaApi(..))")
    Object invokeChsKafkaApi(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (featureFlags.isStreamHookEnabled()) {
            logger.debug("Stream hook enabled; publishing change to chs-kafka-api");
            return proceedingJoinPoint.proceed();
        } else {
            logger.debug("Stream hook disabled; not publishing change to chs-kafka-api");
            return null;
        }
    }
}
