package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import static uk.gov.companieshouse.disqualifiedofficersdataapi.DisqualifiedOfficersDataApiApplication.NAMESPACE;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.FeatureFlags;
import uk.gov.companieshouse.disqualifiedofficersdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Aspect
@Component
public class DisqualifiedOfficerApiServiceAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final FeatureFlags featureFlags;

    public DisqualifiedOfficerApiServiceAspect(FeatureFlags featureFlags) {
        this.featureFlags = featureFlags;
    }

    @Around("execution(public uk.gov.companieshouse.api.model.ApiResponse<Void> uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService.invokeChsKafkaApi(..))")
    Object invokeChsKafkaApi(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (featureFlags.isStreamHookEnabled()) {
            LOGGER.debug("Stream hook enabled; publishing change to chs-kafka-api", DataMapHolder.getLogMap());
            return proceedingJoinPoint.proceed();
        } else {
            LOGGER.debug("Stream hook disabled; not publishing change to chs-kafka-api", DataMapHolder.getLogMap());
            return null;
        }
    }
}
