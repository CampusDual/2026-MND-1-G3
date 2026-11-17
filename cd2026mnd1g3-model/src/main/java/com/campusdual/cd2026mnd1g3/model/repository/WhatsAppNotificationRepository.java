package com.campusdual.cd2026mnd1g3.model.repository;

import com.campusdual.cd2026mnd1g3.model.entity.WhatsAppNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WhatsAppNotificationRepository extends JpaRepository<WhatsAppNotificationEntity, Long> {
    List<WhatsAppNotificationEntity> findAllByOrderByScheduledAtAsc();
}
