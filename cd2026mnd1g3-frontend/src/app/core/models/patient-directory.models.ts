export type PatientSpecies = 'PERRO' | 'GATO' | 'PAJARO' | 'OTRO';
export type PatientSort = 'name' | 'species' | 'breed' | 'birthDate';
export type VaccinationStatus = 'AL_DIA' | 'PROXIMA' | 'VENCIDA' | 'SIN_DATOS';

export interface PatientOwner {
  readonly id: number;
  readonly fullName: string;
  readonly phoneE164?: string;
  readonly notificationsEnabled: boolean;
}

export interface PatientSummary {
  readonly id: number;
  readonly name?: string;
  readonly species: PatientSpecies;
  readonly breed?: string;
  readonly microchip?: string;
  readonly birthDate: string;
  readonly estadoSanitario: string;
  readonly owner?: PatientOwner;
}

export interface PatientPage {
  readonly content: readonly PatientSummary[];
  readonly totalElements: number;
  readonly totalPages: number;
  readonly page: number;
  readonly size: number;
}

export interface PatientDetail {
  readonly patient: PatientSummary;
  readonly vaccinationStatus: VaccinationStatus;
  readonly nextVaccinationDate?: string;
  readonly ownerPets: readonly PatientSummary[];
}
