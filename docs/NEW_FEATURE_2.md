# Autenticacao JWT

## MVP
Para navegar no sistema, o usuario deve possuir um token JWT valido.

O token vai ser integrado via headers da requisicao. (Authorization: Bearer <token>)

Ao entrar ou criar uma conta, um token deve ser gerado.

O payload do JWT deve conter:
```jsonc
{
	"userId"
	"email"
	"username"
}
```

O token deve estar presente para a criacao, edicao e delecao de um post. Para visualizar posts ele nao sera necessario,<br>
pois futuramente sera implementado um feed real.

Para os comentarios, segue a mesma ideia