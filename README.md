# Openfeed

App de posts simples.

## MVP

Entrar com uma conta e poder fazer postagens com tags específicas.

## Features
- Postagens
- Comentarios

## Backend

- Spring
- Postgres com Supabase
- Autenticação de usuário: lookup simples no banco de dados

### Entidades

**Post**

| Campo | Tipo                                               |
| --- |----------------------------------------------------|
| id_post | numérico (PK)                                      |
| titulo | string                                             |
| conteudo | string                                             |
| data_postagem | date (server-set)                                  |
| tags | array de tags (pode ser um enum do banco de dados) |
| id_user | numérico (FK)                                      |

**User**

| Campo | Tipo |
| --- | --- |
| id_user | numérico (PK) |
| username | string |
| email | string |
| senha | string (criptografada no banco de dados) |

**Tags disponíveis:** Videogames, Cinema, Esportes, Lazer, Comida, Viagens

**Comentario**

| Campo | Tipo |
| --- | --- |
| id_comentario | numérico (PK) |
| conteudo | string |
| data_comentario | date (server-set) |
| id_user | numérico (FK) |
| id_post | numérico (FK) |

### Endpoints

Convenção de nomeação: kebab-case  
Endpoint base global: `api/v1`

#### Posts

Endpoint base: `posts/`

Criar um post:
```
api/v1/posts/criar-postagem
```

Retornar posts:
```
api/v1/posts/retornar-postagens/{userId}
```

Atualizar post:
```
api/v1/posts/atualizar-postagem/{postId}
```

Deletar post:
```
api/v1/posts/deletar-postagem/{postId}/?idUsuario={userId}
```

**DTOs**

Criar post:
```jsonc
{
	// Obrigatório, mínimo de 5 caracteres, máximo de 100 caracteres, string
	"titulo": string,

	// Obrigatório, mínimo de 1 caractere, máximo de 250, string
	"conteudo": string,

	// Opcional, deve ser compatível com as tags disponíveis
	"tag": string
}
```

Atualizar post:
```jsonc
{
	// Opcional, mínimo de 5 caracteres, máximo de 100 caracteres, string
	"titulo": string,

	// Opcional, mínimo de 1 caractere, máximo de 250, string
	"conteudo": string,

	// Opcional, deve ser compatível com as tags disponíveis
	"tag": string
}
```

---

#### Comentarios

Endpoint base: `comentarios/`

Fazer um comentario:
```
api/v1/comentarios/criar-comentario/{postId}
```

Retornar comentarios:
```
api/v1/comentarios/retornar-comentarios/{postId}
```

Editar um comentario:
```
api/v1/comentarios/editar-comentario/{commentId}/?idUsuario={userId}
```

Deletar um comentario:
```
api/v1/comentarios/deletar-comentario/{commentId}/?idUsuario={userId}
```

**DTOs**

Fazer um comentario:
```jsonc
{
	// Obrigatório, mínimo de 1 caractere, máximo de 250, string
	"conteudo": string,

	// Obrigatório, numérico
	"idUsuario": number
}
```

Editar um comentario:
```jsonc
{
	// Opcional, mínimo de 1 caractere, máximo de 250, string
	"conteudo": string
}
```

---

#### Users

Endpoint base de autenticação: `auth/`

Criar uma conta:
```
api/v1/auth/criar-conta
```

Entrar no sistema:
```
api/v1/auth/entrar
```

**DTOs**

Criar conta:
```jsonc
{
	// Opcional, string, mín. 5 caracteres, máx. 15
	"username": string,

	// Obrigatório, email
	"email": string,

	// Obrigatório, string, mín. 8 caracteres, máx. 25
	"senha": string
}
```

Entrar:
```jsonc
{
	// Obrigatório, email
	"email": string,

	// Obrigatório, string
	"senha": string
}
```

---

## Frontend

- React
- Vite
- Tailwind
- Axios

### Mapa do app

- Iniciar com página de login / criar conta
- Entrar no feed pessoal

### Hooks

- Hook de autenticação
- Hook para postagens

---

## Convenções gerais

Padrão de nomeação: camelCase  
Idioma: Português
