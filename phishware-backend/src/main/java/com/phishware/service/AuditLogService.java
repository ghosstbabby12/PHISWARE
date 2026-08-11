package com.phishware.service;

import com.phishware.entity.AuditLog;
import com.phishware.entity.User;
import com.phishware.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User user, String action, String entityType, Long entityId,
                    boolean success, String errorMsg, String ipAddress) {
        AuditLog log = AuditLog.builder()
            .user(user)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .success(success)
            .errorMsg(errorMsg)
            .ipAddress(ipAddress)
            .build();
        auditLogRepository.save(log);
    }
}
