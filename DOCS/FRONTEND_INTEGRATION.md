# 🔌 Guia de Integração Frontend - NakaCRM API

Este documento fornece exemplos práticos de integração entre o frontend e a API do NakaCRM.

## 📋 Índice

- [Configuração Inicial](#-configuração-inicial)
- [Autenticação](#-autenticação)
- [Clientes (CRUD)](#-clientes-crud)
- [Emails](#-emails)
- [Tratamento de Erros](#-tratamento-de-erros)
- [TypeScript Interfaces](#-typescript-interfaces)

---

## ⚙️ Configuração Inicial

### Axios Setup

```typescript
// services/api.ts
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para adicionar token JWT automaticamente
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor para refresh token automático
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Se o erro for 401 e não for uma tentativa de refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refresh_token');
        const { data } = await axios.post(
          `${api.defaults.baseURL}/auth/refresh`,
          { refreshToken }
        );

        localStorage.setItem('access_token', data.data.accessToken);
        originalRequest.headers.Authorization = `Bearer ${data.data.accessToken}`;

        return api(originalRequest);
      } catch (refreshError) {
        // Redirecionar para login
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
```

### Fetch API Setup (Vanilla JS)

```javascript
// services/api.js
const BASE_URL = 'http://localhost:8080/api';

async function fetchAPI(endpoint, options = {}) {
  const token = localStorage.getItem('access_token');

  const config = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    },
  };

  const response = await fetch(`${BASE_URL}${endpoint}`, config);
  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || 'Erro na requisição');
  }

  return data;
}

export default fetchAPI;
```

---

## 🔐 Autenticação

### Login

```typescript
// services/authService.ts
import api from './api';

interface LoginRequest {
  email: string;
  senha: string;
}

interface LoginResponse {
  success: boolean;
  data: {
    accessToken: string;
    refreshToken: string;
    usuario: {
      id: number;
      nome: string;
      email: string;
      tipoUsuario: 'ADMIN' | 'VENDEDOR';
    };
  };
}

export async function login(credentials: LoginRequest): Promise<LoginResponse> {
  const { data } = await api.post<LoginResponse>('/auth/login', credentials);

  // Salvar tokens
  localStorage.setItem('access_token', data.data.accessToken);
  localStorage.setItem('refresh_token', data.data.refreshToken);
  localStorage.setItem('user', JSON.stringify(data.data.usuario));

  return data;
}

export function logout() {
  localStorage.clear();
  window.location.href = '/login';
}

export function getUser() {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
}

export function isAuthenticated(): boolean {
  return !!localStorage.getItem('access_token');
}
```

### Exemplo de Uso no React

```tsx
// components/LoginForm.tsx
import { useState } from 'react';
import { login } from '../services/authService';
import { useNavigate } from 'react-router-dom';

export function LoginForm() {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await login({ email, senha });
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao fazer login');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email"
        required
      />
      <input
        type="password"
        value={senha}
        onChange={(e) => setSenha(e.target.value)}
        placeholder="Senha"
        required
      />
      {error && <p className="error">{error}</p>}
      <button type="submit" disabled={loading}>
        {loading ? 'Entrando...' : 'Entrar'}
      </button>
    </form>
  );
}
```

---

## 👥 Clientes (CRUD)

### Service de Clientes

```typescript
// services/clienteService.ts
import api from './api';

export interface Cliente {
  id: number;
  nome: string;
  email: string;
  telefone?: string;
  endereco?: string;
  cidade?: string;
  estado?: string;
  cep?: string;
  empresa?: string;
  cargo?: string;
  origemLead: 'GOOGLE_FORMS' | 'LANDING_PAGE' | 'MANUAL' | 'INDICACAO' | 'OUTRO';
  statusLead: 'NOVO' | 'CONTATADO' | 'QUALIFICADO' | 'OPORTUNIDADE' | 'CLIENTE' | 'PERDIDO';
  dataPrimeiroContato?: string;
  dataUltimaInteracao?: string;
  observacoes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateClienteRequest {
  nome: string;
  email: string;
  telefone?: string;
  endereco?: string;
  cidade?: string;
  estado?: string;
  cep?: string;
  empresa?: string;
  cargo?: string;
  origemLead: Cliente['origemLead'];
  statusLead?: Cliente['statusLead'];
  observacoes?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Listar clientes com paginação
export async function listarClientes(params?: {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}): Promise<PageResponse<Cliente>> {
  const { data } = await api.get('/clientes', { params });
  return data.data;
}

// Buscar cliente por ID
export async function buscarCliente(id: number): Promise<Cliente> {
  const { data } = await api.get(`/clientes/${id}`);
  return data.data;
}

// Buscar por status
export async function buscarPorStatus(status: Cliente['statusLead']): Promise<Cliente[]> {
  const { data } = await api.get(`/clientes/status/${status}`);
  return data.data;
}

// Criar cliente
export async function criarCliente(cliente: CreateClienteRequest): Promise<Cliente> {
  const { data } = await api.post('/clientes', cliente);
  return data.data;
}

// Atualizar cliente
export async function atualizarCliente(
  id: number,
  cliente: Partial<CreateClienteRequest>
): Promise<Cliente> {
  const { data } = await api.put(`/clientes/${id}`, cliente);
  return data.data;
}

// Atualizar status
export async function atualizarStatus(
  id: number,
  novoStatus: Cliente['statusLead']
): Promise<Cliente> {
  const { data } = await api.patch(`/clientes/${id}/status`, null, {
    params: { novoStatus },
  });
  return data.data;
}

// Deletar cliente
export async function deletarCliente(id: number): Promise<void> {
  await api.delete(`/clientes/${id}`);
}
```

### Exemplo de Uso - Lista de Clientes (React)

```tsx
// components/ClientesList.tsx
import { useEffect, useState } from 'react';
import { listarClientes, Cliente, PageResponse } from '../services/clienteService';

export function ClientesList() {
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    carregarClientes();
  }, [page]);

  const carregarClientes = async () => {
    setLoading(true);
    try {
      const response = await listarClientes({
        page,
        size: 20,
        sortBy: 'createdAt',
        sortDirection: 'desc',
      });
      setClientes(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      console.error('Erro ao carregar clientes:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Carregando...</div>;

  return (
    <div>
      <h2>Clientes</h2>
      <table>
        <thead>
          <tr>
            <th>Nome</th>
            <th>Email</th>
            <th>Empresa</th>
            <th>Status</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {clientes.map((cliente) => (
            <tr key={cliente.id}>
              <td>{cliente.nome}</td>
              <td>{cliente.email}</td>
              <td>{cliente.empresa}</td>
              <td>{cliente.statusLead}</td>
              <td>
                <button onClick={() => handleEdit(cliente.id)}>Editar</button>
                <button onClick={() => handleDelete(cliente.id)}>Excluir</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Paginação */}
      <div className="pagination">
        <button onClick={() => setPage(p => p - 1)} disabled={page === 0}>
          Anterior
        </button>
        <span>Página {page + 1} de {totalPages}</span>
        <button onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}>
          Próxima
        </button>
      </div>
    </div>
  );

  function handleEdit(id: number) {
    // Implementar edição
  }

  function handleDelete(id: number) {
    // Implementar exclusão
  }
}
```

### Exemplo - Criar Cliente (React Hook Form)

```tsx
// components/ClienteForm.tsx
import { useForm } from 'react-hook-form';
import { criarCliente, CreateClienteRequest } from '../services/clienteService';
import { useNavigate } from 'react-router-dom';

export function ClienteForm() {
  const { register, handleSubmit, formState: { errors } } = useForm<CreateClienteRequest>();
  const navigate = useNavigate();

  const onSubmit = async (data: CreateClienteRequest) => {
    try {
      await criarCliente(data);
      alert('Cliente criado com sucesso!');
      navigate('/clientes');
    } catch (error: any) {
      alert(error.response?.data?.message || 'Erro ao criar cliente');
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div>
        <label>Nome *</label>
        <input {...register('nome', { required: 'Nome é obrigatório' })} />
        {errors.nome && <span>{errors.nome.message}</span>}
      </div>

      <div>
        <label>Email *</label>
        <input type="email" {...register('email', { required: 'Email é obrigatório' })} />
        {errors.email && <span>{errors.email.message}</span>}
      </div>

      <div>
        <label>Telefone</label>
        <input {...register('telefone')} />
      </div>

      <div>
        <label>Empresa</label>
        <input {...register('empresa')} />
      </div>

      <div>
        <label>Origem do Lead *</label>
        <select {...register('origemLead', { required: true })}>
          <option value="">Selecione...</option>
          <option value="GOOGLE_FORMS">Google Forms</option>
          <option value="LANDING_PAGE">Landing Page</option>
          <option value="MANUAL">Manual</option>
          <option value="INDICACAO">Indicação</option>
          <option value="OUTRO">Outro</option>
        </select>
      </div>

      <div>
        <label>Observações</label>
        <textarea {...register('observacoes')} rows={4} />
      </div>

      <button type="submit">Criar Cliente</button>
    </form>
  );
}
```

---

## 📧 Emails

### Service de Emails

```typescript
// services/emailService.ts
import api from './api';

// Enviar email de boas-vindas
export async function enviarBoasVindas(clienteId: number): Promise<void> {
  await api.post(`/emails/cliente/${clienteId}/boas-vindas`);
}

// Enviar email de follow-up
export async function enviarFollowUp(
  clienteId: number,
  mensagemPersonalizada: string
): Promise<void> {
  await api.post(`/emails/cliente/${clienteId}/follow-up`, null, {
    params: { mensagemPersonalizada },
  });
}

// Enviar email promocional
export async function enviarPromocional(
  clienteId: number,
  tituloProduto: string,
  descricao: string
): Promise<void> {
  await api.post(`/emails/cliente/${clienteId}/promocional`, null, {
    params: { tituloProduto, descricao },
  });
}

// Broadcast de boas-vindas (múltiplos clientes)
export async function broadcastBoasVindas(clienteIds: number[]): Promise<void> {
  await api.post('/emails/broadcast/boas-vindas', clienteIds);
}
```

### Exemplo de Uso

```tsx
// components/ClienteActions.tsx
import { enviarBoasVindas, enviarFollowUp } from '../services/emailService';

export function ClienteActions({ clienteId }: { clienteId: number }) {
  const handleEnviarBoasVindas = async () => {
    try {
      await enviarBoasVindas(clienteId);
      alert('Email de boas-vindas enviado!');
    } catch (error) {
      alert('Erro ao enviar email');
    }
  };

  const handleEnviarFollowUp = async () => {
    const mensagem = prompt('Digite a mensagem personalizada:');
    if (!mensagem) return;

    try {
      await enviarFollowUp(clienteId, mensagem);
      alert('Email de follow-up enviado!');
    } catch (error) {
      alert('Erro ao enviar email');
    }
  };

  return (
    <div>
      <button onClick={handleEnviarBoasVindas}>Enviar Boas-Vindas</button>
      <button onClick={handleEnviarFollowUp}>Enviar Follow-up</button>
    </div>
  );
}
```

---

## ⚠️ Tratamento de Erros

### Error Handler Global

```typescript
// utils/errorHandler.ts
interface ApiError {
  message: string;
  timestamp: string;
  path: string;
}

export function handleApiError(error: any): string {
  if (error.response) {
    // Erro da API
    const apiError: ApiError = error.response.data;
    return apiError.message || 'Erro desconhecido';
  } else if (error.request) {
    // Sem resposta do servidor
    return 'Servidor não respondeu. Verifique sua conexão.';
  } else {
    // Erro ao configurar a requisição
    return error.message;
  }
}
```

### Hook Customizado para Requisições (React)

```typescript
// hooks/useApi.ts
import { useState, useCallback } from 'react';
import { handleApiError } from '../utils/errorHandler';

export function useApi<T>(apiFunc: (...args: any[]) => Promise<T>) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const execute = useCallback(
    async (...args: any[]) => {
      setLoading(true);
      setError(null);

      try {
        const result = await apiFunc(...args);
        setData(result);
        return result;
      } catch (err) {
        const errorMessage = handleApiError(err);
        setError(errorMessage);
        throw err;
      } finally {
        setLoading(false);
      }
    },
    [apiFunc]
  );

  return { data, loading, error, execute };
}

// Uso:
// const { data, loading, error, execute } = useApi(listarClientes);
// await execute({ page: 0, size: 20 });
```

---

## 🔤 TypeScript Interfaces

### Interfaces Completas

```typescript
// types/api.ts

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export type StatusLead = 'NOVO' | 'CONTATADO' | 'QUALIFICADO' | 'OPORTUNIDADE' | 'CLIENTE' | 'PERDIDO';
export type OrigemLead = 'GOOGLE_FORMS' | 'LANDING_PAGE' | 'MANUAL' | 'INDICACAO' | 'OUTRO';
export type TipoUsuario = 'ADMIN' | 'VENDEDOR';

export interface Cliente {
  id: number;
  nome: string;
  email: string;
  telefone?: string;
  endereco?: string;
  cidade?: string;
  estado?: string;
  cep?: string;
  empresa?: string;
  cargo?: string;
  origemLead: OrigemLead;
  statusLead: StatusLead;
  dataPrimeiroContato?: string;
  dataUltimaInteracao?: string;
  observacoes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  tipoUsuario: TipoUsuario;
  ativo: boolean;
  createdAt: string;
  updatedAt: string;
}
```

---

## 🎯 Boas Práticas

### 1. **Environment Variables**

```env
# .env
VITE_API_URL=http://localhost:8080/api
VITE_APP_NAME=NakaCRM
```

### 2. **Loading States**

Sempre mostrar feedback visual durante requisições:

```tsx
{loading && <Spinner />}
{error && <ErrorMessage message={error} />}
{data && <DataDisplay data={data} />}
```

### 3. **Debounce em Buscas**

```typescript
import { debounce } from 'lodash';

const handleSearch = debounce(async (query: string) => {
  const results = await buscarClientes(query);
  setResults(results);
}, 300);
```

### 4. **Cache com React Query**

```typescript
import { useQuery } from '@tanstack/react-query';

function useClientes(page: number) {
  return useQuery({
    queryKey: ['clientes', page],
    queryFn: () => listarClientes({ page, size: 20 }),
    staleTime: 5 * 60 * 1000, // 5 minutos
  });
}
```

---

## 📞 Suporte

- 📖 Documentação Swagger: `http://localhost:8080/api/swagger-ui/index.html`
- 📧 Postman Collection: Importe o arquivo `NakaCRM-Postman-Collection.json`

---
