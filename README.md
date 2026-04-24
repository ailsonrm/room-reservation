# Room Reservation API

API REST para gerenciamento de reservas de salas de reunião.

---

## Objetivo

Implementar uma solução simples e eficiente para controle de reservas de salas, garantindo regras de negócio como:

- Não permitir reservas no passado
- Garantir que o horário inicial seja anterior ao final
- Evitar conflitos de horários para a mesma sala

O modelo de domínio segue o enunciado: entidade **Reserva** com `salaId`, `usuarioId`, `inicio` e `fim` (`LocalDateTime`). O serviço expõe `reservar`, `estaDisponivel`, `listarReservasPorSala`, `listarTodasReservas` e `excluirReservaPorId`.

---

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- H2 Database (em memória)
- Lombok

---

## Funcionalidades

### Criar reserva (`reservar`)

Registra uma nova **Reserva** para uma sala, após validar intervalo, horário no futuro e ausência de sobreposição.

### Listar reservas por sala (`listarReservasPorSala`)

Retorna todas as reservas cadastradas para uma `salaId` específica.

### Listar todas as reservas (`listarTodasReservas`)

Retorna todas as reservas persistidas (todas as salas).

### Excluir reserva por id (`excluirReservaPorId`)

Remove uma reserva pelo `id` numérico gerado pelo banco.

### Validação de regras de negócio

- `inicio` deve ser anterior a `fim`
- `inicio` deve estar no futuro (não aceita início no passado ou “agora”)
- Não pode haver sobreposição de horários na mesma sala (`estaDisponivel`)

---

## Regras de negócio

Uma reserva é considerada inválida quando:

- `inicio` é nulo ou `fim` é nulo
- `salaId` ou `usuarioId` ausentes ou em branco
- `inicio >= fim`
- `inicio` não é estritamente posterior a `LocalDateTime.now()`

Conflito de agenda: existe outra reserva na mesma `salaId` tal que o novo intervalo se sobrepõe ao existente.

### Regra de conflito

Duas reservas entram em conflito quando:

`novo.inicio < existente.fim && novo.fim > existente.inicio`

---

## Como executar o projeto

### Pré-requisitos

- Java 21+
- Maven 3+ (ou use o Maven Wrapper incluso)

### Rodando a aplicação

```bash
mvn spring-boot:run
```

ou

```bash
./mvnw spring-boot:run
```

A aplicação ficará disponível em:

`http://localhost:8080`

### Console do H2

Acesse: `http://localhost:8080/h2-console`

Configuração:

- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: (vazio)

Tabela JPA: `reservas` (entidade `Reserva`).

---

## Endpoints

Base path: **`/reservas`**

### POST — criar reserva

**`POST /reservas`**

Headers:

- `Content-Type: application/json`

Corpo (campos alinhados ao enunciado; `id` é opcional no POST — gerado pelo banco):

```json
{
  "salaId": "A1",
  "usuarioId": "user1",
  "inicio": "2026-04-25T10:00:00",
  "fim": "2026-04-25T11:00:00"
}
```

Respostas:

| Status | Situação | Corpo (exemplo) |
|--------|----------|------------------|
| `201 Created` | Reserva gravada | `"Reserva criada com sucesso."` |
| `400 Bad Request` | Validação (datas, campos obrigatórios, início no passado) | `"O início deve ser anterior ao fim."` |
| `409 Conflict` | Sobreposição na mesma sala | `"Horário indisponível: já existe reserva sobreposta nesta sala."` |

Exemplo de sucesso (`201`):

```http
HTTP/1.1 201 Created
Content-Type: text/plain;charset=UTF-8

Reserva criada com sucesso.
```

Exemplo de validação (`400`):

```http
HTTP/1.1 400 Bad Request
Content-Type: text/plain;charset=UTF-8

A reserva deve ter início no futuro.
```

Exemplo de conflito (`409`):

```http
HTTP/1.1 409 Conflict
Content-Type: text/plain;charset=UTF-8

Horário indisponível: já existe reserva sobreposta nesta sala.
```

---

### GET — listar todas as reservas

**`GET /reservas`**

Resposta `200 OK` — mesmo formato de array JSON que o listar por sala; pode ser `[]` se não houver registros.

Exemplo:

```http
GET /reservas
```

---

### GET — listar reservas por sala

**`GET /reservas/{salaId}`**

Exemplo:

```http
GET /reservas/A1
```

Resposta `200 OK` — lista de objetos **Reserva** (inclui `id` persistido):

```json
[
  {
    "id": 1,
    "salaId": "A1",
    "usuarioId": "user1",
    "inicio": "2026-04-25T10:00:00",
    "fim": "2026-04-25T11:00:00"
  }
]
```

Lista vazia `[]` se não houver reservas para a sala.

Erro `400` se `salaId` inválido na rota (vazio após normalização, conforme validação do serviço), com corpo em texto com a mensagem da exceção.

---

### DELETE — excluir reserva por id

**`DELETE /reservas/{id}`**

O `{id}` é o identificador numérico da entidade **Reserva** (campo `id`), não o `salaId`.

| Status | Situação |
|--------|----------|
| `204 No Content` | Reserva encontrada e removida (sem corpo) |
| `404 Not Found` | Não existe reserva com esse `id` — corpo: `"Não existe reserva com o id informado."` |
| `400 Bad Request` | `id` inválido (ex.: não positivo) — corpo com mensagem de validação |

Exemplo:

```bash
curl -s -X DELETE http://localhost:8080/reservas/1 -i
```

---

## Decisões técnicas

- H2 em memória para facilitar execução e testes
- Camadas Controller → `ReservationService` → `ReservaRepository`
- Validações e regra de sobreposição centralizadas no serviço
- `ValidacaoReservaException` para erros de validação; conflito de agenda retorna `409` com `reservar` retornando `false`

---

## Testes

### Postman (coleção pronta)

No repositório há uma coleção **Postman v2.1**:

`postman/Room-Reservation-API.postman_collection.json`

**Como importar**

1. Suba a API (`./mvnw spring-boot:run` ou equivalente).
2. Abra o Postman → **Import** → **Upload Files** (ou arraste o arquivo) → selecione `Room-Reservation-API.postman_collection.json`.
3. A coleção **Room Reservation API** aparece na barra lateral; dentro há as pastas **Reservas** e **Cenários (validação e conflito)**.

**Variáveis da coleção** (aba **Variables** ao editar a coleção, ou pelo ícone de olho no canto superior direito):

| Variável     | Valor padrão              | Uso |
|-------------|---------------------------|-----|
| `baseUrl`   | `http://localhost:8080`   | Base da API; altere se a porta ou o host forem outros. |
| `salaId`    | `A1`                      | Usada no body do **Criar reserva** e na rota **Listar reservas por sala**. |
| `reservaId` | `1`                       | Usada no **Excluir reserva por id**; após criar ou listar, copie o `id` retornado no JSON e atualize esta variável. |

**Fluxo sugerido**

1. **Listar todas as reservas** — confere se a API está no ar e o estado atual.
2. **Criar reserva** — confira **201** e mensagem em texto; se **400** por data, ajuste `inicio`/`fim` no body para um intervalo **estritamente no futuro** em relação ao relógio da máquina que roda o Spring.
3. **Listar reservas por sala** — valida o retorno JSON com `id`, `salaId`, `usuarioId`, `inicio`, `fim`.
4. **Excluir reserva por id** — defina `reservaId` com o `id` listado; espere **204** (sem corpo) ou **404** se o id não existir.
5. Pasta **Cenários** — útil para reproduzir **400** (intervalo inválido ou passado) e **409** (rode **Criar reserva** com 14:00–15:00 e depois o item que sobrepõe 14:30–15:30 na mesma sala).

Dica: com H2 em memória, ao **reiniciar** a aplicação os dados somem; nesse caso execute de novo **Criar reserva** antes dos cenários de conflito.

### Linha de comando (`curl`)

Exemplos equivalentes sem Postman:

```bash
curl -s -X POST http://localhost:8080/reservas \
  -H "Content-Type: application/json" \
  -d '{"salaId":"A1","usuarioId":"u1","inicio":"2026-12-01T14:00:00","fim":"2026-12-01T15:00:00"}'
```

```bash
curl -s http://localhost:8080/reservas
```

```bash
curl -s http://localhost:8080/reservas/A1
```

```bash
curl -s -X DELETE http://localhost:8080/reservas/1
```

Também é possível usar **Insomnia** ou outro cliente HTTP importando o mesmo arquivo, se o produto suportar o formato de coleção Postman.

---

## Autor

- Ailson Ramos Moreira
