package com.campusdual.cd2026mnd1g3.api.dto;

import com.campusdual.cd2026mnd1g3.api.enums.NotificationStatus;

import java.time.LocalDateTime;

public class WhatsAppNotificationDto {
    private Long id;
    private String destinationPhone;
    private String message;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private NotificationStatus sendStatus;
    private Long protocolEventId;

    public WhatsAppNotificationDto() {
    }

    public WhatsAppNotificationDto(Long id, String destinationPhone, String message, LocalDateTime scheduledAt,
                                   LocalDateTime sentAt, NotificationStatus sendStatus, Long protocolEventId) {
        this.id = id; this.destinationPhone = destinationPhone; this.message = message;
        this.scheduledAt = scheduledAt; this.sentAt = sentAt; this.sendStatus = sendStatus;
        this.protocolEventId = protocolEventId;
    }

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
    public Long getProtocolEventId() { return protocolEventId; }
    public void setProtocolEventId(Long protocolEventId) { this.protocolEventId = protocolEventId; }
}
