# Comentarios

## MVP

Usuarios podem adicionar comentarios em postagens, sem cadeias de comentarios, ou seja, comentarios nao podem receber respostas

### Entidade

**Comentario**

| Campo           | Tipo               |
|-----------------|--------------------|
| id_comentario   | numérico (PK)      |
| conteudo        | string             |
| data_comentario | date  (server-set) |
| id_user         | numérico (FK)      |
| id_post         | numérico (FK)      |

### Endpoints

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

Criar comentario:
```jsonc
{
	// Obrigatório, string, mín. 1 caracteres, máx. 250
	"conteudo": string
	
	// Obrigatorio, numerico
	"idUser": integer
}

```

Editar comentario:
```jsonc
{
	// Opcional, string, mín. 1 caracteres, máx. 250
	"conteudo": string
}
```

Resposta:
```jsonc
{
	"idComentario": integer,
	"conteudo": string,
	"dataComentario": Date,
	"idUsuario": integer,
	//Opcional
	"username": string
}
```