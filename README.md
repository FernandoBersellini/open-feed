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

Dados do usuário autenticado:
```
api/v1/auth/me
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
	"id": number,
	"email": string,
	"username": string
}
```

O token JWT não é retornado no corpo — é enviado num cookie `auth_token` (`HttpOnly`, `Secure`, `SameSite=Lax`). Ver [Autenticação](#autenticação).

Resposta (sair):
```jsonc
"Logout realizado com sucesso"
```

Resposta (me):
```jsonc
{
	"id": number,
	"email": string,
	"username": string
}
```

---

### Autenticação

O token JWT é entregue e lido via cookie `auth_token` (`HttpOnly`, `Secure`, `SameSite=Lax`, `Max-Age` = expiração do token), não mais via header `Authorization`. O cookie é setado em `entrar`/`criar-conta` e limpo em `sair`; o navegador o envia automaticamente em toda requisição para a API, sem intervenção do frontend.

Endpoints públicos (sem token necessário):
- `POST api/v1/auth/entrar`
- `POST api/v1/auth/criar-conta`
- `GET api/v1/posts/retornar-postagens/{userId}`
- `GET api/v1/comentarios/retornar-comentarios/{postId}`

**CSRF**

Como o cookie é enviado automaticamente pelo navegador em toda requisição (diferente de um Bearer token, que exige JS para ser anexado), os endpoints que alteram estado exigem proteção contra CSRF. `CookieCsrfTokenRepository` expõe o token num cookie legível por JS (`XSRF-TOKEN`); o frontend deve reenviá-lo no header `X-XSRF-TOKEN` a cada requisição mutável (Axios faz isso automaticamente por padrão, já que os nomes de cookie/header coincidem com o default do Axios). `entrar` e `criar-conta` são isentos de CSRF, pois não há sessão/cookie autenticado prévio contra o qual validar.

**Logout / revogação de token**

`POST api/v1/auth/sair` exige um cookie `auth_token` válido. O id do token (`jti`) é adicionado a uma denylist em memória com TTL até o horário de expiração original do token; qualquer requisição subsequente com esse mesmo token passa a receber `401 Unauthorized`, mesmo que ele ainda não tenha expirado. Fazer login novamente gera um token novo (`jti` diferente), não afetado pelo logout anterior.

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

## Decisões de arquitetura

**Rate limiting no login e cadastro**
- Contexto: `auth/entrar` e `auth/criar-conta` são públicos (`permitAll`) e, antes desta mudança, não tinham nenhuma proteção contra força bruta ou spam de contas.
- Decisão: limitar por IP do cliente com Bucket4j — 5 req/min em `entrar`, 3 req/hora em `criar-conta`. Detalhes de comportamento em [Autenticação](#autenticação).
- Consequência conhecida: os contadores são mantidos em memória por instância da aplicação. Em caso de múltiplas instâncias atrás de um load balancer, cada uma tem seus próprios contadores (os limites efetivos multiplicam pelo número de instâncias). Além disso, se a aplicação for hospedada atrás de um proxy reverso, `getRemoteAddr()` passa a retornar o IP do proxy para todas as requisições — ainda **não configurado**, pendente da escolha da plataforma de deploy (precisa de `server.forward-headers-strategy` + lista de proxies confiáveis).

**Revogação de token (logout)**
- Contexto: os JWTs são stateless — antes desta mudança, não havia como invalidar um token antes da sua expiração natural (1h).
- Decisão: `POST auth/sair` adiciona o `jti` do token a uma denylist em memória (`TokenDenylist`) com TTL até a expiração original do token; `JwtFilter` rejeita qualquer token cujo `jti` esteja na denylist. Detalhes em [Autenticação](#autenticação).
- Consequência conhecida: assim como o rate limiting, a denylist é por instância — não persiste a reinícios nem é compartilhada entre múltiplas instâncias.

**Validação de duplicidade no cadastro**
- Contexto: `signUp` inseria o usuário diretamente e dependia da constraint `UNIQUE` do banco lançar `DataIntegrityViolationException`, traduzida pelo `GlobalExceptionHandler` num `409` genérico que não indicava qual campo conflitava.
- Decisão: `AuthService.signUp` agora verifica `existsByEmail`/`existsByUsername` antes de criar o usuário, retornando `409 Conflict` com mensagem específica (`"Email ja cadastrado"` / `"Username ja cadastrado"`).

**Migrações de banco de dados (Flyway)**
- Contexto: `spring.jpa.hibernate.ddl-auto=update` deixava o schema do Supabase (produção) sujeito a alterações automáticas a cada mudança de entidade JPA, sem revisão nem histórico.
- Decisão: adotado Flyway. `ddl-auto` mudou para `validate` — Hibernate passa a apenas conferir se o schema bate com as entidades, nunca alterá-lo. O schema já existente no Supabase foi documentado em `src/main/resources/db/migration/V1__baseline_schema.sql` (reconstruído a partir das entidades JPA, não extraído diretamente do banco) e marcado como baseline (`spring.flyway.baseline-on-migrate=true`, `spring.flyway.baseline-version=1`), então essa migração nunca roda contra o banco de produção — ela existe como referência e para inicializar um banco novo/vazio (dev, testes) do zero.
- Consequência: qualquer alteração de schema futura exige uma nova migração versionada (`V2__...sql`, etc.) em `src/main/resources/db/migration`. Se o schema real do Supabase divergir do que está documentado no baseline, vale a pena conferir com uma ferramenta como `pg_dump`.

**Containerização e CI/CD**
- Contexto: o projeto não tinha Dockerfile, imagem de container nem pipeline de CI/CD — nenhuma forma automatizada de build/validação/publicação existia.
- Decisão: `Dockerfile` multi-stage (build com `eclipse-temurin:21-jdk-jammy`, runtime com `eclipse-temurin:21-jre-jammy`, usuário non-root, `HEALTHCHECK` via `/api/v1/actuator/health`), `docker-compose.yml` para conveniência local (sem serviço de banco — Postgres permanece externo via Supabase), e dois workflows do GitHub Actions: `ci.yml` (roda testes unitários e build em todo push/PR, excluindo `OpenFeedApplicationTests` por depender de um banco real) e `docker-publish.yml` (builda e publica a imagem no GitHub Container Registry a cada push em `main`, com tags `latest` e SHA curto).
- Consequência conhecida: assim como o rate limiting e a denylist de tokens, a aplicação deve rodar como instância única — nada aqui pressupõe múltiplas réplicas. `OpenFeedApplicationTests` continua fora do CI até existir um perfil de teste próprio (H2/Testcontainers). O deploy em uma plataforma de hospedagem real ainda é uma decisão em aberto, separada deste trabalho.

**Token JWT em cookie `HttpOnly` (em vez de retornado no corpo)**
- Contexto: o token era retornado no corpo de `entrar`/`criar-conta` e o frontend o guardava (tipicamente em `localStorage`) para reenviá-lo manualmente no header `Authorization: Bearer <token>`. Qualquer XSS na aplicação frontend conseguiria ler esse token diretamente.
- Decisão: `entrar`/`criar-conta`/`sair` agora setam/limpam um cookie `auth_token` (`HttpOnly`, `Secure`, `SameSite=Lax`, `Max-Age` = `jwt.expiration`) em vez de devolver o token no JSON; `JwtFilter` passou a ler o token desse cookie. Como o cookie é enviado automaticamente pelo navegador (diferente do header `Authorization`, que exigia JS), CSRF deixou de poder ser ignorado: `SecurityConfig` passou a usar `CookieCsrfTokenRepository` (cookie `XSRF-TOKEN` legível por JS, reenviado no header `X-XSRF-TOKEN` — convenção que o Axios já implementa por padrão), isento apenas em `entrar`/`criar-conta`, que não têm cookie de sessão prévio para validar contra.
- Consequência conhecida: `cookie.secure` (`COOKIE_SECURE`) precisa ser `false` em desenvolvimento local sobre HTTP puro — em produção (HTTPS) deve ficar `true`, o default. `SameSite=Lax` funciona sem ajustes enquanto frontend e backend estiverem no mesmo *site* (mesmo domínio registrável, portas/subdomínios diferentes tudo bem, como `localhost:5173`/`localhost:8080`); se um dia ficarem em domínios totalmente diferentes, isso precisa virar `SameSite=None` (exige `Secure=true`).

---

## Convenções gerais

Padrão de nomeação: camelCase  
Idioma: Português
