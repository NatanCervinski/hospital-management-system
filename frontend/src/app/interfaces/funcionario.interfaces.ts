export interface FuncionarioResponseDTO {
  id: number;
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  especialidade?: string; // Medical specialty for doctors
  status: 'ATIVO' | 'INATIVO';
  ativo: boolean;
}

export interface FuncionarioCadastroDTO {
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  especialidade?: string; // Medical specialty for doctors (optional)
}

export interface FuncionarioUpdateDTO {
  nome: string;
  email: string;
  telefone: string;
  especialidade?: string; // Medical specialty for doctors (optional)
}
export interface PaginatedResponse<T> {
  content: T[]; // A lista de itens da página atual
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  numberOfElements: number;
  first: boolean;
  empty: boolean;
}

export interface CustomPaginatedResponse<T> {
  funcionarios: T[];
  paginaAtual: number;
  totalElementos: number;
  tamanhoPagina: number;
  primeiraPagina: boolean;
  ultimaPagina: boolean;
  totalPaginas: number;
}

