package com.campusdual.cd2026mnd1g3.ws.listener;

import com.campusdual.cd2026mnd1g3.model.event.AlertaSistemaCreadaEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AlertaSistemaWebsocketListener {
    private final SimpMessagingTemplate messagingTemplate;

    public AlertaSistemaWebsocketListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(AlertaSistemaCreadaEvent event) {
        Long clinicaId = event.alerta().clinicaId();
        messagingTemplate.convertAndSend("/topic/alertas/" + clinicaId, event.alerta());
        messagingTemplate.convertAndSend("/topic/ganaderia/" + clinicaId, event.alerta());
    }
}
