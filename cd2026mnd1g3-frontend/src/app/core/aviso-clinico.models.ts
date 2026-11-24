export type CategoriaAvisoClinico = 'VACUNACION' | 'TRATAMIENTO' | 'REVISION' | 'ESTACIONAL';
export type EstadoAvisoClinico =
  | 'PENDIENTE'
  | 'INTENTADO'
  | 'CONTACTADO'
  | 'CITA_AGENDADA'
  | 'POSPUESTA'
  | 'DESCARTADA';
export type MotivoDescarteAviso =
  | 'YA_REALIZADO'
  | 'CAMBIO_CRITERIO'
  | 'PROPIETARIO_RECHAZO'
  | 'FALLECIMIENTO_BAJA'
  | 'OTRO';

export interface AvisoClinico {
  id: number;
  clinicaId: number;
  pacienteId: number;
  propietarioId: number;
  nombrePaciente: string;
  nombrePropietario: string;
  telefonoPropietario?: string;
  emailPropietario?: string;
  categoria: CategoriaAvisoClinico;
  titulo: string;
  detalleClinico?: string;
  fechaObjetivo: string;
  estado: EstadoAvisoClinico;
  intentos: number;
  ultimoIntento?: string;
  ultimoUsuario?: string;
  fechaInicio?: string;
  fechaFin?: string;
  progreso?: string;
  motivoDescarte?: MotivoDescarteAviso;
  activo: boolean;
}

export interface AvisoPage {
  content: AvisoClinico[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
