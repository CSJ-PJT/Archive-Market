package com.csj.archive.market.audit;

import com.csj.archive.market.common.TraceIdFilter;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(AuditAction action, String targetType, String targetId,
                       String beforeStatus, String afterStatus, String detail) {
        auditLogRepository.save(new AuditLogEntity(
                MDC.get(TraceIdFilter.TRACE_ID),
                "synthetic-market-system",
                action,
                targetType,
                targetId,
                beforeStatus,
                afterStatus,
                detail));
    }
}
