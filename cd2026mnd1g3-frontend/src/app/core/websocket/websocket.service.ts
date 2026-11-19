import { Injectable, OnDestroy, signal } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { BehaviorSubject } from 'rxjs';
import { AlertaSistema } from '../models/domain.models';

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {
  readonly alerts = new BehaviorSubject<readonly AlertaSistema[]>([]);
  readonly connected = signal<boolean>(false);
  private readonly client: Client;
  private subscriptions: readonly StompSubscription[] = [];
  private reconnectAttempt = 0;
  private reconnectTimer: ReturnType<typeof setTimeout> | undefined;

  constructor() {
    this.client = new Client({
      brokerURL: 'ws://localhost:8080/ws-vetcontrol',
      reconnectDelay: 0,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000
    });
    this.client.onConnect = () => {
      this.reconnectAttempt = 0;
      this.connected.set(true);
      this.subscriptions = [
        this.client.subscribe('/topic/alertas/1', (message: IMessage) => this.receive(message)),
        this.client.subscribe('/topic/ganaderia/1', (message: IMessage) => this.receive(message))
      ];
    };
    this.client.onDisconnect = () => this.connected.set(false);
    this.client.onStompError = () => this.connected.set(false);
    this.client.onWebSocketClose = () => this.scheduleReconnect();
  }

  connect(): void {
    if (!this.client.active) {
      this.client.activate();
    }
  }

  disconnect(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = undefined;
    }
    for (const subscription of this.subscriptions) {
      subscription.unsubscribe();
    }
    this.subscriptions = [];
    this.connected.set(false);
    void this.client.deactivate();
  }

  setInitialAlerts(initial: readonly AlertaSistema[]): void {
    this.merge(initial);
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  private receive(message: IMessage): void {
    const alert = JSON.parse(message.body) as AlertaSistema;
    this.merge([alert]);
  }

  private merge(incoming: readonly AlertaSistema[]): void {
    const map = new Map<number, AlertaSistema>();
    for (const alert of [...incoming, ...this.alerts.value]) {
      map.set(alert.id, alert);
    }
    this.alerts.next([...map.values()].sort((left: AlertaSistema, right: AlertaSistema) =>
      right.fechaProgramada.localeCompare(left.fechaProgramada)));
  }

  private scheduleReconnect(): void {
    this.connected.set(false);
    if (this.reconnectTimer || this.client.active) {
      return;
    }
    const delay = Math.min(30000, 1000 * (2 ** this.reconnectAttempt));
    this.reconnectAttempt += 1;
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = undefined;
      if (!this.client.active) {
        this.client.activate();
      }
    }, delay);
  }
}
