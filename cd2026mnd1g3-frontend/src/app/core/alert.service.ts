import { Injectable, OnDestroy } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { BehaviorSubject } from 'rxjs';
import { AlertaSistema } from './livestock.service';

@Injectable({ providedIn: 'root' })
export class AlertService implements OnDestroy {
  readonly alerts = new BehaviorSubject<AlertaSistema[]>([]);
  readonly connected = new BehaviorSubject<boolean>(false);
  private readonly client = new Client({
    brokerURL: 'ws://localhost:8080/ws-vetcontrol',
    reconnectDelay: 3000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000
  });

  constructor() {
    this.client.onConnect = () => {
      this.connected.next(true);
      this.client.subscribe('/topic/alertas/1', (message: IMessage) => this.receive(message));
      this.client.subscribe('/topic/ganaderia/1', (message: IMessage) => this.receive(message));
    };
    this.client.onDisconnect = () => this.connected.next(false);
    this.client.onWebSocketClose = () => this.connected.next(false);
    this.client.onStompError = () => this.connected.next(false);
  }

  connect(): void {
    if (!this.client.active) {
      this.client.activate();
    }
  }

  setInitialAlerts(initial: AlertaSistema[]): void {
    this.merge(initial);
  }

  ngOnDestroy(): void {
    void this.client.deactivate();
  }

  private receive(message: IMessage): void {
    const alert = JSON.parse(message.body) as AlertaSistema;
    this.merge([alert]);
  }

  private merge(incoming: AlertaSistema[]): void {
    const map = new Map<number, AlertaSistema>();
    for (const alert of [...this.alerts.value, ...incoming]) {
      map.set(alert.id, alert);
    }
    this.alerts.next([...map.values()].sort((left, right) =>
      right.fechaProgramada.localeCompare(left.fechaProgramada)));
  }
}
