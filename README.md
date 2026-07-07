# Openfeed

App de posts simples.

## MVP

Entrar com uma conta e poder fazer postagens com tags específicas.

## Features
- Postagens
- Comentarios
- Autenticação JWT
- Curtidas (likes) em postagens e comentários

## Backend

- Spring
- Postgres com Supabase
- Autenticação de usuário: JWT (Bearer Token)

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
api/v1/posts/deletar-postagem/{postId}
```

Curtir/descurtir um post (toggle):
```
api/v1/posts/interagir-com-postagem/{postId}
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

Resposta (retornar posts):
```jsonc
{
	"id": integer,
	"titulo":string,
	"conteudo": string,
	"tag": string,
	"dataPostagem": string,
	"totalLikes": number,
	"usuarioAtualCurtiu": boolean
}
```

`usuarioAtualCurtiu` reflete o usuário autenticado que fez a requisição (`false` se não autenticado).

Resposta de curtir/descurtir post:
```jsonc
{
	"totalLikes": number,
	"usuarioAtualCurtiu": boolean
}
```

Deletar um post remove em cascata (via `ON DELETE CASCADE` no banco) seus comentários, curtidas do post e curtidas dos comentários associados.


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
api/v1/comentarios/editar-comentario/{commentId}
```

Deletar um comentario:
```
api/v1/comentarios/deletar-comentario/{commentId}
```

Curtir/descurtir um comentario (toggle):
```
api/v1/comentarios/interagir-com-comentario/{commentId}
```

**DTOs**

Fazer um comentario:
```jsonc
{
	// Obrigatório, mínimo de 1 caractere, máximo de 250, string
	"conteudo": string
}
```

Editar um comentario:
```jsonc
{
	// Opcional, mínimo de 1 caractere, máximo de 250, string
	"conteudo": string
}
```

Resposta (retornar comentarios):
```jsonc
{
	"idComentario": integer,
	"conteudo": string,
	"dataComentario": string,
	"idUsuario": integer,
	"username": string,
	"totalLikes": number,
	"usuarioAtualCurtiu": boolean
}
```

Resposta de curtir/descurtir comentario:
```jsonc
{
	"totalLikes": number,
	"usuarioAtualCurtiu": boolean
}
```

Deletar um comentario remove em cascata (via `ON DELETE CASCADE` no banco) suas curtidas associadas.

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

Sair do sistema (invalida o token atual):
```
api/v1/auth/sair
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
	"password": string
}
```

Entrar:
```jsonc
{
	// Obrigatório, email
	"email": string,

	// Obrigatório, string
	"password": string
}
```

Resposta (entrar/criar-conta):
```jsonc
{
	"token": string,
	"id": number,
	"email": string,
	"username": string
}
```

Resposta (sair):
```jsonc
"Logout realizado com sucesso"
```

---

### Autenticação

Os endpoints de criação, edição, deleção e curtida de posts e comentários exigem um token JWT válido no header:

```
Authorization: Bearer <token>
```

Endpoints públicos (sem token necessário):
- `POST api/v1/auth/entrar`
- `POST api/v1/auth/criar-conta`
- `GET api/v1/posts/retornar-postagens/{userId}`
- `GET api/v1/comentarios/retornar-comentarios/{postId}`

**Logout / revogação de token**

`POST api/v1/auth/sair` exige um token válido no header `Authorization`. O id do token (`jti`) é adicionado a uma denylist em memória com TTL até o horário de expiração original do token; qualquer requisição subsequente com esse mesmo token passa a receber `401 Unauthorized`, mesmo que ele ainda não tenha expirado. Fazer login novamente gera um token novo (`jti` diferente), não afetado pelo logout anterior.

A denylist é mantida apenas em memória por instância da aplicação — não persiste a reinícios e não é compartilhada entre múltiplas instâncias.

**Rate limiting**

Para mitigar força bruta e spam de contas, `POST api/v1/auth/entrar` e `POST api/v1/auth/criar-conta` são limitados por IP do cliente (Bucket4j):

| Endpoint | Limite |
| --- | --- |
| `auth/entrar` | 5 requisições / minuto |
| `auth/criar-conta` | 3 requisições / hora |

Ao exceder o limite, a resposta é `429 Too Many Requests` com um header `Retry-After` (em segundos). Assim como a denylist de tokens, os contadores são mantidos em memória por instância.

---

### Tratamento de erros

Erros não tratados diretamente por um endpoint são capturados por um `GlobalExceptionHandler` central, que padroniza a resposta:

| Situação | Status |
| --- | --- |
| Tag inválida ao criar/atualizar um post | `400 Bad Request` |
| Post/comentario não pertence ao usuário autenticado | `403 Forbidden` |
| Recurso não encontrado | `404 Not Found` |
| Violação de integridade de dados (ex: conflito de curtida duplicada) | `409 Conflict` |
| Erro interno não mapeado | `500 Internal Server Error` |

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
