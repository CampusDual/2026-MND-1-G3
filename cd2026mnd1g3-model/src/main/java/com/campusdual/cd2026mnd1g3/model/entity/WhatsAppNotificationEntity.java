package com.campusdual.cd2026mnd1g3.model.entity;

import com.campusdual.cd2026mnd1g3.api.enums.NotificationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "whatsapp_notifications")
public class WhatsAppNotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "destination_phone", nullable = false, length = 30)
    private String destinationPhone;
    @Column(nullable = false, length = 1000)
    private String message;
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "send_status", nullable = false, length = 30)
    private NotificationStatus sendStatus;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "protocol_event_id", nullable = false)
    private ProtocolEventEntity protocolEvent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDestinationPhone() { return destinationPhone; }
    public void setDestinationPhone(String destinationPhone) { this.destinationPhone = destinationPhone; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public NotificationStatus getSendStatus() { return sendStatus; }
    public void setSendStatus(NotificationStatus sendStatus) { this.sendStatus = sendStatus; }
    public ProtocolEventEntity getProtocolEvent() { return protocolEvent; }
    public void setProtocolEvent(ProtocolEventEntity protocolEvent) { this.protocolEvent = protocolEvent; }
}
