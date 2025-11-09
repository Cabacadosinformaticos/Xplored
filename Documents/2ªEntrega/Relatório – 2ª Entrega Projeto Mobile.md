# Relatório Intermédio – 2ª Entrega do Projeto Mobile

> **Projeto:** Xplored
> **Unidade Curricular:** Projeto de Desenvolvimento Móvel – 3.º Semestre (2025/26)
> **Grupo:** t1-g01-cesar_lucas_tiago_muhammad
> **Elementos:**
> • Tiago Manuel Antunes Cabaça — 20241185
> • César de Oliveira Rodrigues — 20240449
> • Lucas Dernedde Sequeira Gomes Nicolau — 20241526
> • Muhammad Sudeis Abdul Latif Sacoor — 20241707
> **Repositório GitHub:** *a preencher no README com links para Documentos/*

---

## 1) Resumo Executivo

Nesta 2.ª entrega apresentamos o **protótipo/alfa** do Xplored com backend e base de dados funcionais e uma app Android (Kotlin + Jetpack Compose) a **consumir serviços REST**. A BD foi revista e simplificada face à 1.ª entrega, consolidando as entidades **users**, **categories**, **places** e **photos**, com regras de moderação por estado. Entregamos também a **v1 da documentação REST**, o **esboço do diagrama de classes**, a **v1 do Dicionário de Dados** e um **Guia de Dados** (amostra coerente para testes).

---

## 2) Personas e Guiões (versão final)

### Persona A – Nataly (Estudante Erasmus)

* **Objetivo:** Explorar pontos culturais gastando pouco; usar descontos.
* **Motivações:** Autonomia; partilha social; fotografia.
* **Dores:** Falta de curadoria local; tempo limitado.

**Guia A – Explorar e resgatar cupão**

1. Abre a app e ativa localização.
2. Filtra por *Locais Históricos* e *Restauração*.
3. Visita 3 locais próximos (mapa → detalhes).
4. Tira foto do recibo e submete.
5. Junta pontos e resgata cupão de 15% para café.

### Persona B – Família Porto (João & Marta)

* **Objetivo:** “Achados” fora do circuito turístico; atividades com crianças.
* **Motivações:** Segurança, recomendações verificadas.
* **Dores:** Excesso de ruído nos agregadores tradicionais.

**Guia B – Tarde em família**

1. Usa a lista “Para crianças” (categoria).
2. Consulta reviews com selo *Verificado*.
3. Marca 2 paragens; submete 2 fotos.
4. Troca pontos por desconto numa loja de brinquedos.

### Persona C – Teresa (Conta Business)

* **Objetivo:** Aumentar visitas e tickets médios.
* **Motivações:** Métricas e cupões com custo em pontos.
* **Dores:** Pouca visibilidade fora das zonas “óbvias”.

**Guia C – Ativar campanha local**

1. Regista conta Business e cria o seu **Place**.
2. Define cupão “-15%” e termos.
3. Acompanha métricas (redeems, fotos, reviews).
4. Ajusta stock e validade do cupão.

---

## 3) Arquitetura da Solução (alfa)

* **Mobile (Android):** Kotlin + Jetpack Compose; módulos: `ui`, `domain`, `data` (Repository + Retrofit), `auth`.
* **Backend:** Spring Boot (REST) com camadas `controller` → `service` → `repository` (JPA/Query); DTOs para requests/responses.
* **BD:** MySQL (UTF8MB4) com 4 tabelas base (users/categories/places/photos), chaves auto‑incremento e FKs, índices geográficos (lat/lng) por *bounding box*.
* **Infra:** Perfis `dev`/`test`, .env para segredos, validação, tratamento de erros padronizado (RFC 7807-like).

---

## 4) Diagrama de Classes (esboço)

```mermaid
classDiagram
  class User {+userId: Int +name: String +email: String +passwordHash: String +role: Role +country: String +points: Int +profilePhotoUrl: String +createdAt: Instant}
  class Category {+categoryId: Int +name: String +colorHex: String +iconName: String}
  class Place {+placeId: Int +name: String +description: String +lat: BigDecimal +lng: BigDecimal +addressFull: String +postalCode: String +avgRating: BigDecimal +status: Status +coverImageUrl: String +createdAt: Instant}
  class Photo {+photoId: Int +placeId: Int? +userId: Int +url: String +status: Status +createdAt: Instant}
  enum Role { USER; BUSINESS; ADMIN }
  enum Status { PENDING; APPROVED; REJECTED }

  User <.. Photo : "1..* by"
  Category <.. Place : "1 belongs"
  Place <.. Photo : "0..*"
```

> **Nota:** O diagrama acima é **conceptual** e alinha com a BD atual. Entidades para gamificação, reviews, cupões e pedipaper encontram-se planeadas para a 3.ª entrega.

---

## 5) Documentação REST – v1 (Bocoup‑style)

### Convenções

* Base URL: `/api/v1`
* Formato: `application/json; charset=utf-8`
* Autenticação: `Bearer <JWT>` (rotas públicas indicadas).
* Erros: objeto `{ type, title, status, detail, timestamp }`.

### Endpoints

#### Auth

* `POST /auth/register` – cria utilizador.
* `POST /auth/login` – login e emissão de JWT.

#### Users

* `GET /users/me` – perfil do utilizador autenticado.
* `PATCH /users/me` – atualizar `name`, `country`, `profilePhotoUrl`.

#### Categories (público)

* `GET /categories` – lista categorias.
* `POST /categories` *(admin)* – criar.

#### Places

* `GET /places?lat=&lng=&radiusKm=&categoryId=` – busca por proximidade e filtros.
* `GET /places/{id}` – detalhe do local.
* `POST /places` *(USER/BUSINESS)* – submeter local (`status=PENDING`).
* `PATCH /places/{id}/status` *(admin)* – moderar.

#### Photos

* `GET /places/{id}/photos` – fotos de um local.
* `POST /photos` *(USER)* – submeter foto (opcional `placeId`), `status=PENDING`.
* `PATCH /photos/{id}/status` *(admin)* – moderar.

### Exemplos (request/response)

**POST /auth/register**

```json
{
  "name":"Nataly",
  "email":"nataly@example.com",
  "password":"Strong!Pass1"
}
```

**201 Created**

```json
{
  "userId": 101,
  "name":"Nataly",
  "email":"nataly@example.com",
  "role":"USER",
  "createdAt":"2025-11-09T12:31:00Z"
}
```

**GET /places?lat=38.77&lng=-9.10&radiusKm=2&categoryId=3**
**200 OK**

```json
[{
  "placeId": 12,
  "name": "Miradouro da Igreja",
  "avgRating": 4.6,
  "category": {"categoryId":3, "name":"Locais Históricos", "colorHex":"#F472B6"},
  "distanceKm": 1.2
}]
```

---

## 6) Base de Dados – Dicionário de Dados (v1)

### users

* **user_id** (PK, INT UNSIGNED, AI)
* **name** (VARCHAR(100), NOT NULL)
* **email** (VARCHAR(150), NOT NULL, UNIQUE)
* **password_hash** (VARCHAR(255), NOT NULL)
* **role** (ENUM: USER|BUSINESS|ADMIN, DF=USER)
* **country** (VARCHAR(56), NULL)
* **points** (INT, DF=0)
* **profile_photo_url** (VARCHAR(255), NULL)
* **created_at** (TIMESTAMP, DF=CURRENT_TIMESTAMP)

### categories

* **category_id** (PK, INT UNSIGNED, AI)
* **name** (VARCHAR(50), NOT NULL, UNIQUE)
* **color_hex** (CHAR(7), NOT NULL, formato `#RRGGBB`)
* **icon_name** (VARCHAR(64), NULL)

### places

* **place_id** (PK, INT UNSIGNED, AI)
* **name** (VARCHAR(100), NOT NULL)
* **description** (TEXT, NULL)
* **lat** (DECIMAL(9,6), NOT NULL)
* **lng** (DECIMAL(9,6), NOT NULL)
* **address_full** (VARCHAR(255), NULL)
* **postal_code** (VARCHAR(15), NULL)
* **avg_rating** (DECIMAL(2,1), NULL)
* **category_id** (FK → categories.category_id, NOT NULL, `ON UPDATE CASCADE ON DELETE RESTRICT`)
* **status** (ENUM: PENDING|APPROVED|REJECTED, DF=PENDING)
* **cover_image_url** (VARCHAR(255), NULL)
* **created_at** (TIMESTAMP, DF=CURRENT_TIMESTAMP)
* **Índices:** `idx_places_category (category_id)`, `idx_places_lat_lng (lat,lng)`

### photos

* **photo_id** (PK, INT UNSIGNED, AI)
* **place_id** (FK → places.place_id, NULL, `ON DELETE SET NULL ON UPDATE CASCADE`)
* **user_id** (FK → users.user_id, NOT NULL, `ON DELETE CASCADE ON UPDATE CASCADE`)
* **url** (VARCHAR(255), NOT NULL)
* **status** (ENUM: PENDING|APPROVED|REJECTED, DF=PENDING)
* **created_at** (TIMESTAMP, DF=CURRENT_TIMESTAMP)
* **Índices:** `idx_photos_place (place_id)`, `idx_photos_user (user_id)`

---

## 7) Modelo ER (descrição)

* **User (1) — (N) Photo**: autoria de fotografias; eliminações em cascata removem fotos do utilizador.
* **Category (1) — (N) Place**: cada local pertence a uma única categoria; não é possível apagar uma categoria com locais associados (RESTRICT).
* **Place (1) — (N) Photo (opcional)**: galeria do local; fotos podem existir sem Place (ex.: submissões pendentes) graças a `place_id NULL`.

> **Estados de moderação**: `PENDING` ao submeter; `APPROVED` ou `REJECTED` por perfis com permissão.

---

## 8) Guia de Dados (amostra para testes)

> **Região:** foco em **Moscavide** e arredores (conjunto mínimo
> coerente para validação da app e das queries).

### Categorias (sugestão de semente)

1. Restauração — `#FB923C` — `utensils`
2. Natureza — `#22C55E` — `leaf`
3. Locais Históricos — `#F472B6` — `landmark`
4. Arte Urbana — `#06B6D4` — `spray`
5. Atividades — `#3B82F6` — `activity`

### Utilizadores (mínimo)

* `nataly@example.com` (USER), `teresa@saborestterra.pt` (BUSINESS), `admin@xplored.app` (ADMIN).

### Locais (exemplos)

* **“Tasquinha da Praça”** (Restauração) – lat 38.778500, lng -9.101700 – *APPROVED*.
* **“Jardim do Rio Trancão”** (Natureza) – lat 38.792300, lng -9.090200 – *APPROVED*.
* **“Igreja Paroquial de Moscavide”** (Histórico) – lat 38.772900, lng -9.110400 – *APPROVED*.
* **“Painel Rua da República”** (Arte Urbana) – lat 38.775100, lng -9.106900 – *PENDING*.
* **“Parque Infantil Central”** (Atividades) – lat 38.776800, lng -9.103300 – *APPROVED*.

### Fotos (exemplos)

* `https://cdn.example/xplored/places/igreja-01.jpg` (place=Igreja, user=Nataly, APPROVED)
* `https://cdn.example/xplored/receipts/tasquinha-241101.jpg` (place=Tasquinha, user=Nataly, PENDING)

---

## 9) Scripts SQL

> Os ficheiros serão organizados em `db/` com: `create.sql`, `populate.sql`, `queries.sql`.

### 9.1 create.sql (atual)

* Inclui DDL para `users`, `categories`, `places`, `photos`; charset `utf8mb4_general_ci`; chaves autoincremento; FKs com `CASCADE/RESTRICT/SET NULL`.

### 9.2 populate.sql (seed sugerido)

```sql
-- populate.sql (excerpt) — EN comments for clarity
START TRANSACTION;

-- Users
INSERT INTO users(name,email,password_hash,role,country,points,profile_photo_url)
VALUES
 ('Nataly','nataly@example.com','$2y$10$hash','USER','Portugal',120,NULL),
 ('Teresa','teresa@saborestterra.pt','$2y$10$hash','BUSINESS','Portugal',0,NULL),
 ('Admin','admin@xplored.app','$2y$10$hash','ADMIN',NULL,0,NULL);

-- Categories
INSERT INTO categories(name,color_hex,icon_name) VALUES
 ('Restauração','#FB923C','utensils'),
 ('Natureza','#22C55E','leaf'),
 ('Locais Históricos','#F472B6','landmark'),
 ('Arte Urbana','#06B6D4','spray'),
 ('Atividades','#3B82F6','activity');

-- Places (Moscavide area)
INSERT INTO places(name,description,lat,lng,address_full,postal_code,avg_rating,category_id,status,cover_image_url)
VALUES
 ('Tasquinha da Praça','Comida caseira e ambiente familiar.',38.778500,-9.101700,'Praça Central, Moscavide','1885-000',4.6,1,'APPROVED',NULL),
 ('Jardim do Rio Trancão','Zona verde para caminhadas e piqueniques.',38.792300,-9.090200,'Parque do Trancão','2685-000',4.3,2,'APPROVED',NULL),
 ('Igreja Paroquial de Moscavide','Património local com vista sobre a vila.',38.772900,-9.110400,'Largo da Igreja','1885-058',4.5,3,'APPROVED',NULL),
 ('Painel Rua da República','Mural de arte urbana recente.',38.775100,-9.106900,'Rua da República','1885-100',NULL,4,'PENDING',NULL),
 ('Parque Infantil Central','Espaço para crianças com equipamentos.',38.776800,-9.103300,'Av. de Moscavide','1885-200',4.1,5,'APPROVED',NULL);

-- Photos
INSERT INTO photos(place_id,user_id,url,status)
VALUES
 ((SELECT place_id FROM places WHERE name='Igreja Paroquial de Moscavide'),
  (SELECT user_id FROM users WHERE email='nataly@example.com'),
  'https://cdn.example/xplored/places/igreja-01.jpg','APPROVED'),
 ((SELECT place_id FROM places WHERE name='Tasquinha da Praça'),
  (SELECT user_id FROM users WHERE email='nataly@example.com'),
  'https://cdn.example/xplored/receipts/tasquinha-241101.jpg','PENDING');

COMMIT;
```

### 9.3 queries.sql (amostras relevantes)

```sql
-- 1) Nearby places within ~1.5km bounding box (simple bbox — fast index use)
-- Input: :lat, :lng, :delta = 0.015 ~ 1.5km approx
SELECT p.*
FROM places p
WHERE p.status='APPROVED'
  AND p.lat BETWEEN (:lat-:delta) AND (:lat+:delta)
  AND p.lng BETWEEN (:lng-:delta) AND (:lng+:delta)
ORDER BY ABS(p.lat-:lat)+ABS(p.lng-:lng) ASC;

-- 2) Places by category with average rating desc
SELECT c.name AS category, p.name, p.avg_rating
FROM places p
JOIN categories c ON c.category_id = p.category_id
WHERE p.status='APPROVED'
ORDER BY c.name, p.avg_rating DESC;

-- 3) Latest photos per place (window function fallback using self-join)
SELECT ph.*
FROM photos ph
JOIN (
  SELECT place_id, MAX(created_at) AS max_created
  FROM photos
  GROUP BY place_id
) t ON t.place_id = ph.place_id AND t.max_created = ph.created_at;

-- 4) Moderation queue
SELECT 'photo' AS entity, photo_id AS id, status, created_at
FROM photos WHERE status='PENDING'
UNION ALL
SELECT 'place', place_id, status, created_at FROM places WHERE status='PENDING'
ORDER BY created_at ASC;

-- 5) User contribution summary
SELECT u.user_id, u.name,
       COUNT(DISTINCT p.photo_id) AS photos_total,
       SUM(CASE WHEN p.status='APPROVED' THEN 1 ELSE 0 END) AS photos_approved
FROM users u
LEFT JOIN photos p ON p.user_id = u.user_id
GROUP BY u.user_id, u.name
ORDER BY photos_approved DESC;
```

---

## 10) Considerações de Segurança & RGPD

* **Autenticação** com JWT; refresh tokens; expiração curta.
* **Armazenamento** de `password_hash` com algoritmo forte (ex.: bcrypt/argon2).
* **Uploads** com validação de MIME e antivírus; limite de tamanho; *signed URLs*.
* **Privacidade**: recolha mínima; consentimento para geolocalização; retenção definida; anonimização de métricas.
* **Moderação** humana para conteúdos (estados **PENDING/APPROVED/REJECTED**).

---

## 11) Plano de Trabalhos Atualizado

* **S11–S12:** Refino de REST (`/places` bbox + rating + paginação).
* **S12–S13:** Reviews e gamificação (modelo + endpoints); cupões (esqueleto).
* **S13–S14:** Testes de usabilidade; estabilização; documentação final; vídeo.

---

## 12) Riscos & Mitigações

* **Carga de dados geográficos:** indexes + paginação + bbox simplificado → depois Haversine/GeoHash.
* **Uploads maliciosos:** AV + extensão normalizada + quotas + moderação.
* **Scope creep:** foco em MVP (explorar, fotos, categorias, locais; reviews/cupões faseados).

---

## 13) Próximos Passos (até 3.ª Entrega)

1. Adicionar **reviews** e **ratings** a locais.
2. Introduzir **cupões** (modelo + resgate).
3. **Leaderboard** (gamificação) e **pedipaper** (mínimo).
4. Completar **Guia de Dados** com mais amostras realistas.
5. Expandir **Documentação REST** com exemplos completos e códigos de erro.

---

## 14) Bibliografia (essencial)

* Bocoup – *Documenting Your API*.
* Documentação Android (Jetpack Compose).
* Spring Boot (REST + Validation).
* MySQL 8 (DDL/Índices/Charset).

---

> **Anexos** (no repositório, pasta `Documentos/2ªEntrega/`):
> • `db/create.sql`, `db/populate.sql`, `db/queries.sql`
> • `rest/openapi.yaml` (quando aplicável)
> • `modelos/diagrama-classes.mmd`
> • `bd/dicionario-de-dados.md`
> • `bd/guia-de-dados.md`
