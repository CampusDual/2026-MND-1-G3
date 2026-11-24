export type GanaderiaGroupType = 'SUBTIPO' | 'UBICACION' | 'ESTADO_SANITARIO';
export type GanaderiaEstadoSanitario = 'SANO' | 'EN_TRATAMIENTO' | 'ENFERMO';

export interface GanaderiaSubtype {
  groupId: string;
  label: string;
  heads: number;
  percentage: number;
}

export interface GanaderiaFarmSummary {
  ownerId: number;
  farmId: number;
  clinicaId: number;
  name: string;
  regaCode: string;
  mainSpecies: string;
  totalHeads: number;
  subtypes: GanaderiaSubtype[];
  activeAlerts: number;
  pendingActions: number;
}

export interface GanaderiaGroup {
  groupId: string;
  label: string;
  heads: number;
  percentage: number;
  activeAlerts: number;
  pendingActions: number;
}

export interface GanaderiaAnimal {
  id: number;
  earTag: string;
  name?: string;
  species: string;
  birthDate: string;
  healthStatus: string;
  estadoSanitario?: 'SANO' | 'EN_TRATAMIENTO' | 'EN_RETIRADA_MEDICAMENTO' | 'ENFERMO' | 'CUARENTENA';
  fechaFinRetirada?: string;
  notas?: string;
  farmId: number;
  clinicaId: number;
  subtipo?: string;
  ubicacion?: string;
  cantidadCabezas?: number;
}

export interface GanaderiaAnimalPage {
  content: GanaderiaAnimal[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface GanaderiaFarmPage {
  content: GanaderiaFarmSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface GanaderiaOperacionCampo {
  operationId: string;
  farmId: number;
  animalId?: number;
  tipoOperacion: 'INCIDENCIA' | 'ACTUACION_RAPIDA' | 'VACUNA_BLOQUE' | 'TRATAMIENTO_BLOQUE';
  detalle?: string;
  createdAt: string;
  sincronizada: boolean;
}

export interface GanaderiaBulkEstadoRequest {
  animalIds: number[];
  estadoSanitario: 'SANO' | 'EN_TRATAMIENTO' | 'ENFERMO';
}

export interface GanaderiaBulkProtocoloRequest {
  animalIds: number[];
  eventType: 'VACUNACION' | 'TRATAMIENTO_MEDICO' | 'PARTO' | 'REVISION_PERIODICA';
  title: string;
  applicationDate: string;
  withdrawalDays: number;
  withdrawalEndDate?: string;
  medication?: string;
  nextCycleDays?: number;
}

export interface AltaGanaderiaAnimalRequest {
  crotal: string;
  nombre?: string;
  especie?: 'VACUNO_LECHE' | 'VACUNO_CARNE' | 'PORCINO' | 'OVINO';
  subtipo?: string;
  ubicacion?: string;
  estadoSanitario: GanaderiaEstadoSanitario;
  cabezas: number;
}

export interface ClasificacionGanaderiaRequest {
  animalIds: number[];
  subtipo?: string;
  ubicacion?: string;
  estadoSanitario?: GanaderiaEstadoSanitario;
}

export interface OpcionesClasificacion {
  subtipos: string[];
  ubicaciones: string[];
  subtiposRecomendados: string[];
  ubicacionesRecomendadas: string[];
}
