export type EspecieMascota = 'PERRO' | 'GATO' | 'PAJARO' | 'OTRO';
export type SexoMascota = 'MACHO' | 'HEMBRA' | 'DESCONOCIDO';
export type EstadoEsterilizacion = 'SI' | 'NO' | 'DESCONOCIDO';
export type EstadoPaciente = 'ACTIVO' | 'INACTIVO';

export interface PropietarioClinica {
  id: number;
  nombre: string;
  apellidos: string;
  dniNif?: string;
  telefono: string;
  email?: string;
  notificacionesInternas: boolean;
  mascotasTotales?: number;
}

export interface PacienteClinica {
  id: number;
  clinicaId: number;
  propietarioId: number;
  nombre: string;
  especie: EspecieMascota;
  raza?: string;
  microchip?: string;
  fechaNacimiento?: string;
  fechaNacimientoEstimada: boolean;
  edadAproxAnos?: number;
  edadAproxMeses?: number;
  sexo: SexoMascota;
  esterilizado: EstadoEsterilizacion;
  pesoInicial?: number;
  unidadPeso?: 'kg' | 'g';
  alergias: boolean;
  manejoNervioso: boolean;
  patologiaCronica: boolean;
  notaBanderas?: string;
  estadoPaciente: EstadoPaciente;
  propietario: PropietarioClinica;
}

export interface AltaPropietarioRequest {
  nombre: string;
  apellidos: string;
  dniNif?: string;
  telefono: string;
  email?: string;
}

export interface AltaPacienteRequest {
  clienteExistenteId?: number;
  nuevoCliente?: AltaPropietarioRequest;
  nombre: string;
  especie: EspecieMascota;
  raza?: string;
  microchip?: string;
  sinMicrochip: boolean;
  fechaNacimiento?: string;
  fechaNacimientoEstimada: boolean;
  edadAproxAnos?: number;
  edadAproxMeses?: number;
  sexo: SexoMascota;
  esterilizado: EstadoEsterilizacion;
  pesoInicial?: number;
  alergias: boolean;
  manejoNervioso: boolean;
  patologiaCronica: boolean;
  notaBanderas?: string;
}

export interface AltaPacienteResponse {
  paciente: PacienteClinica;
  propietario: PropietarioClinica;
}

export interface PacientePage {
  content: PacienteClinica[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ConsultaClinica {
  id: number;
  clinicaId: number;
  pacienteId: number;
  nombrePaciente: string;
  especie: EspecieMascota;
  raza?: string;
  microchip?: string;
  propietarioId: number;
  nombrePropietario: string;
  apellidosPropietario: string;
  fechaConsulta: string;
  motivo?: string;
  notas?: string;
  proximaVacuna?: string;
}
