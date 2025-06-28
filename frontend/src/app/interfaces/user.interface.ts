export interface UserRegistrationRequest {
  nome: string;
  cpf: string;
  email: string;
  cep: string;
}

export interface UserRegistrationResponse {
  id: number;
  cpf: string;
  email: string;
  nome: string;
  message: string;
}
