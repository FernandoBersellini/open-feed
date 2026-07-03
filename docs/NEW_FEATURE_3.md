# Likes

## MVP
Posts e comentarios podem receber likes. Sem valores negativos, e apenas um like por conta 
por postagem e comentario. Estes likes vao ser utilizados para futuros filtros, como por exemplo, filtrar por maiores likes.

## Endpoints
Para dar likes e retirar likes, sera utilizado um unico endpoint de toggle para posts e comentarios, com o metodo POST

Endpoint base para posts: `posts/`

```
api/v1/posts/interagir-com-postagem/{postId}
```

Endpoint base para comentarios: `comentarios/`
```
api/v1/comentarios/interagir-com-comentario/{commentId}
```

## DTOs
DTO de resposta de toggle:
Para evitar um refetch de um comentario ou post inteiro no frontend, um DTO do toggle sera enviado
```jsonc
{
	"totalLikes": integer,
	"usuarioAtualCurtiu": boolean
}
```


Alguns DTOs serao atualizados: 

### Posts
DTO de resposta:
```jsonc
{
	"id": integer,
	"titulo":string,
	"conteudo": string,
	"tag": string,
	"dataPostagem": string
	"totalLikes": integer,
	"usuarioAtualCurtiu": boolean
}
```

### Comentarios
DTO de resposta:
```jsonc
{
	"idComentario": integer,
	"conteudo": string,
	"dataComentario": Date,
	"idUsuario": integer,
	"username": string,
	"totalLikes": integer,
	"usuarioAtualCurtiu": boolean
}
```

## Autenticacao
Usuarios nao autenticados podem ver a quantidade de likes, porem, nao podem dar um like se nao tiverem uma conta, ou um token valido

## Join tables
unique constraint: (id_user, id_post)

| Campo        | Tipo                    |
|--------------|-------------------------|
| id_post_like | numérico (PK)           |
| id_user      | numérico (FK -> users)  |
| id_post      | numérico (FK -> post)   |
| data_curtida | date (server-set)       |


unique constraint: (id_user, id_comment)

| Campo           | Tipo                     |
|-----------------|--------------------------|
| id_comment_like | numérico (PK)            |
| id_user         | numérico (FK -> users)   |
| id_comment      | numérico (FK -> comment) |
| data_curtida    | date (server-set)        |
