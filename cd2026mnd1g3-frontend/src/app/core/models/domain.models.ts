export type Role = 'VETERINARIAN' | 'CLINIC_ADMIN' | 'FARM_MANAGER';

export type TipoMovimiento =
  | 'ENTRADA_COMPRA'
  | 'SALIDA_GANADERIA'
  | 'SALIDA_CLINICA'
  | 'MERMA_CADUCIDAD'
  | 'AJUSTE_ANULACION';

export type AnimalHealthStatus =
  | 'SANO'
  | 'EN_TRATAMIENTO'
  | 'EN_RETIRADA_MEDICAMENTO'
  | 'ENFERMO'
  | 'CUARENTENA';

export interface UserDto {
  readonly id: number;
  readonly username: string;
  readonly enabled: boolean;
  readonly roles: readonly string[];
}

export interface AlertaSistema {
  readonly id: number;
  readonly clinicaId: number;
  readonly destinatarioRol: Role;
  readonly tipoAlerta: 'RECORDATORIO_CICLICO' | 'FIN_RETIRADA_MEDICAMENTO' | 'URGENCIA_ANIMAL_ENFERMO' | 'STOCK_BAJO';
  readonly titulo: string;
  readonly mensaje: string;
  readonly fechaProgramada: string;
  readonly leida: boolean;
  readonly confirmadaPorGranja: boolean;
  readonly explotacionId?: number;
  readonly animalId?: number;
}
