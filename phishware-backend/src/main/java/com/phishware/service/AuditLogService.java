package com.phishware.service;

import com.phishware.entity.AuditLog;
import com.phishware.entity.User;
import com.phishware.repository.AuditLogRepository;
import com.phishware.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void log(User user, String action, String entityType, Long entityId,
                    boolean success, String errorMsg, String ipAddress) {
        eventPublisher.publishEvent(new AuditLogRequested(
            user == null ? null : user.getId(), action, entityType, entityId,
            success, errorMsg, ipAddress
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persist(AuditLogRequested event) {
        User user = event.userId() == null
            ? null
            : userRepository.findById(event.userId()).orElse(null);

        AuditLog log = AuditLog.builder()
            .user(user)
            .action(event.action())
            .entityType(event.entityType())
            .entityId(event.entityId())
            .success(event.success())
            .errorMsg(event.errorMsg())
            .ipAddress(event.ipAddress())
            .build();
        auditLogRepository.save(log);
    }

    public record AuditLogRequested(
        Long userId,
        String action,
        String entityType,
        Long entityId,
        boolean success,
        String errorMsg,
        String ipAddress
    ) {}
}
