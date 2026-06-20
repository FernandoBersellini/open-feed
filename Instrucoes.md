# Openfeed
- App  de posts simples
# MVP
Entrar com uma conta, e poder fazer postagens com tags especificas
## Backend
- Spring
- Postgres (Supabase pode ser uma boa)
- Autenticacao de usuario: Lookup simples no banco de dados;
### Entidades

Post  
id_post: numerico (PK)  
titulo: string  
conteudo: string  
data_postagem: date  
tags: array de tags (pode ser um enum do banco de dados)  
id_user: numero (FK)

User  
id_user: numerico (PK)  
username: string  
email: string  
senha: string (criptografada no banco de dados)

Tags disponiveis:
Videogames, Cinema, Esportes, Lazer, Comida, Viagens

### Endpoints 
Convencao de nomeacao: kebab-case  
Endpoint base global: api/v1
### Posts
Endpoint base: posts/

Criar um post
```
api/v1/posts/criar-postagem
```

Retornar posts
```
api/v1/posts/retornar-postagens
```

Atualizar post
```
api/v1/posts/atualizar-postagem/{id}
```

Deleter post
```
api/v1/posts/deletar-postagem/{id}
```

DTOs:

Criar post:
```
{
	//Obrigatorio, minimo de 5 caracteres, maximo de 100 caracteres, string
	"titulo":string,
	
	//Obrigatorio, minimo de 1 caractere, maximo de 250, string
	"conteudo":string,
	
	//Opcional, deve ser compativel com as tags disponiveis
	"tag": string
}
```

Atualizar post:
```
{
	//Opcional, minimo de 5 caracteres, maximo de 100 caracteres, string
	"titulo":string,
	
	//Opcional, minimo de 1 caractere, maximo de 250, string
	"conteudo":string,
	
	//Opcional, deve ser compativel com as tags disponiveis
	"tag": string
}

```

---
### Users

Endpoint base: auth/

Criar uma conta
```
api/v1/criar-conta
```

Entrar no sistema
```
api/v1/entrar
```

DTOs:

Criar conta:
```
{
	//opcional, string, min 5 caracteres, max 15
	"username":string,
	
	//obrigatorio, email
	"email":string,
	
	//obrigatorio, string, min 8 caracteres, max 25
	"senha": string
}
```

Entrar:
```
{
	//obrigatorio, email
	"email":string,
	
	//obrigatorio, string
	"senha": string
}
```

--- 
## Frontend
- React
- Vite
- Tailwind
- Axios, router
### Mapa do app

- Iniciar com pagina de login / criar conta
- Entrar no feed pessoal

### Hooks
- Hook de autenticacao
- Hook para postagens

---
## Convencoes gerais

Padrao de nomeacao: camelCase  
Idioma: Portugues



