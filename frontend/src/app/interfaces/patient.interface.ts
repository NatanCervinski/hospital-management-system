export interface PatientRegistrationRequest {
  nome: string;
  cpf: string;
  email: string;
  dataNascimento: string;
  telefone: string;
  cep: string;
  logradouro: string;
  numero: string;
  complemento?: string;
  bairro: string;
  cidade: string;
  estado: string;
}

export interface PatientRegistrationResponse {
  id: string;
  email: string;
  nome: string;
  message: string;
}

export interface EmailCheckResponse {
  exists: boolean;
  message: string;
}

export interface CpfCheckResponse {
  exists: boolean;
  message: string;
}

export interface ViaCepResponse {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  localidade: string;
  uf: string;
  ibge: string;
  gia: string;
  ddd: string;
  siafi: string;
  erro?: boolean;
}

export interface ValidationErrors {
  [key: string]: string;
}

export interface FormValidationState {
  [key: string]: {
    valid: boolean;
    pending: boolean;
    error?: string;
  };
}