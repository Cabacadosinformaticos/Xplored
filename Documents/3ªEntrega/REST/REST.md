# 📱 Xplored API – Documentação

Esta API suporta a aplicação móvel **Xplored**, sendo responsável pela gestão de:

* Utilizadores
* Locais
* Avaliações e Reações
* Fotos
* Gamificação (Pedipapers)
* Recompensas (Cupões)

---

## 🌐 URL Base

```
http://10.0.2.2:9000
```

> Endereço usado em ambiente local (emulador Android)

---

# 👤 Utilizadores

## Registar Utilizador

Cria uma nova conta de utilizador na plataforma.

**Endpoint**

```
POST /user/save
```

**Body (JSON)**

```json
{
  "name": "Tiago Silva",
  "email": "tiago@xplored.pt",
  "passwordHash": "secret123",
  "country": "Portugal"
}
```

**Resposta de Sucesso**

* Código: `201 CREATED`

```json
{
  "id": 1,
  "name": "Tiago Silva",
  "email": "tiago@xplored.pt",
  "role": "USER",
  "points": 0,
  "profilePhotoUrl": null
}
```

---

## Login

Autentica um utilizador através de email e palavra‑passe.

**Endpoint**

```
GET /user/login
```

**Parâmetros URL**

* `email` (string)
* `password` (string)

**Resposta de Sucesso**

* Código: `200 OK`
* Conteúdo: Objeto `User`

**Resposta de Erro**

* Código: `401 UNAUTHORIZED`

---

## Obter Perfil de Utilizador

Obtém os dados de um utilizador através do email.

**Endpoint**

```
GET /user/by-email
```

**Parâmetros URL**

* `email` (string)

**Resposta de Sucesso**

* Código: `200 OK`

**Resposta de Erro**

* Código: `404 NOT FOUND`

---

## Atualizar Perfil

Atualiza os dados básicos do utilizador.

**Endpoint**

```
PUT /user/update-profile
```

**Parâmetros URL**

* `email`
* `name`
* `about`
* `country`

**Resposta de Sucesso**

* Código: `200 OK`

---

# 📍 Locais (Places)

## Obter Todos os Locais

Retorna todos os locais aprovados.

**Endpoint**

```
GET /places
```

**Resposta de Sucesso**

```json
[
  {
    "placeId": 10,
    "name": "Torre de Belém",
    "description": "Torre histórica.",
    "lat": 38.6916,
    "lng": -9.2160,
    "addressFull": "Av. Brasília, Lisboa",
    "categoryId": 4,
    "authorId": "admin@xplored.pt",
    "avgRating": 4.5,
    "coverImageUrl": "http://..."
  }
]
```

---

## Criar Local

Submete um novo local para aprovação.

**Endpoint**

```
POST /places
```

**Body (JSON)**

```json
{
  "name": "Oceanário de Lisboa",
  "description": "Melhor aquário do mundo.",
  "lat": 38.7636,
  "lng": -9.0937,
  "addressFull": "Esplanada Dom Carlos I",
  "categoryId": 1,
  "authorId": "tiago@xplored.pt"
}
```

---

## Apagar Local

Remove um local existente.

**Endpoint**

```
DELETE /places/{id}
```

**Resposta**

```
Deleted
```

---

# ⭐ Avaliações e Reações

## Criar Avaliação

Adiciona uma avaliação a um local específico.

**Endpoint**

```
POST /reviews
```

**Body (JSON)**

```json
{
  "userEmail": "tiago@xplored.pt",
  "placeId": 10,
  "rating": 5,
  "title": "Vista incrível!",
  "comment": "Vale totalmente a pena a visita."
}
```

---

## Avaliações por Local

Obtém todas as avaliações de um local.

**Endpoint**

```
GET /reviews/by-place/{placeId}
```

**Parâmetro Opcional**

* `userEmail`

---

## Avaliações por Utilizador

Lista avaliações escritas por um utilizador.

**Endpoint**

```
GET /reviews/by-user
```

**Parâmetros**

* `email`

---

## Alternar Reação (Like / Dislike)

**Endpoint**

```
POST /reactions/toggle
```

**Body (JSON)**

```json
{
  "userEmail": "tiago@xplored.pt",
  "reviewId": 55,
  "type": "USEFUL"
}
```

> `type` pode ser `USEFUL` ou `NOT_USEFUL`

---

# 📷 Fotos

## Carregar Foto

Upload de imagem associada a utilizador, local ou avaliação.

**Endpoint**

```
POST /photos/upload
```

**Formato**

```
multipart/form-data
```

**Campos**

* `file`
* `userId`
* `placeId` (opcional)
* `reviewId` (opcional)

---

## Fotos por Local

**Endpoint**

```
GET /photos/by-place/{placeId}
```

---

# 🚶 Pedipapers (Rotas)

## Listar Pedipapers

**Endpoint**

```
GET /pedipapers
```

---

## Obter Paragens

**Endpoint**

```
GET /pedipapers/{id}/stops
```

---

## Entrar no Pedipaper

**Endpoint**

```
POST /pedipapers/{id}/join
```

---

## Completar Pedipaper

**Endpoint**

```
POST /pedipapers/{id}/complete
```

---

# 🎟️ Cupões

## Obter Cupões Ativos

**Endpoint**

```
GET /coupons/active
```

---

## Resgatar Cupão

**Endpoint**

```
POST /coupons/redeem
```

**Body (JSON)**

```json
{
  "userEmail": "tiago@xplored.pt",
  "couponId": 1
}
```

**Erro Possível**

```
400 BAD REQUEST – Pontos insuficientes
```
