-- =====================================================
-- queries.sql - Xplored
-- Autor: Tiago Cabaça
-- Descrição: Conjunto de queries úteis para testes e exemplos.
-- Inclui listagens por localização, categorias e moderação.
-- =====================================================

USE xplored_db;

-- Coordenadas de referência (Moscavide)
SET @lat   = 38.7785;
SET @lng   = -9.1017;
SET @delta = 0.015;  -- ~1,5 km

-- 1) Locais próximos (verificados)
SELECT p.*
FROM places p
WHERE p.is_verified = TRUE
  AND p.lat BETWEEN (@lat - @delta) AND (@lat + @delta)
  AND p.lng BETWEEN (@lng - @delta) AND (@lng + @delta)
ORDER BY ABS(p.lat - @lat) + ABS(p.lng - @lng) ASC;

-- 2) Locais verificados por categoria (melhor rating primeiro)
SELECT
  c.name AS categoria,
  p.name AS local,
  p.avg_rating AS avaliacao
FROM places p
JOIN categories c ON c.id = p.category_id
WHERE p.is_verified = TRUE
ORDER BY c.name, p.avg_rating DESC;

-- 3) Última fotografia por local
SELECT ph.*
FROM photos ph
JOIN (
  SELECT place_id, MAX(created_at) AS max_created
  FROM photos
  GROUP BY place_id
) t ON t.place_id = ph.place_id AND t.max_created = ph.created_at;

-- 4) Fila de moderação (pendentes)
SELECT 'foto' AS tipo, id, status, created_at
FROM photos
WHERE status = 'PENDING'
UNION ALL
SELECT 'local' AS tipo, id, 'PENDING' AS status, created_at
FROM places
WHERE is_verified = FALSE
ORDER BY created_at ASC;

-- 5) Resumo de contribuições por utilizador
SELECT
  u.id,
  u.name,
  COUNT(DISTINCT p.id)                   AS total_fotos,
  COALESCE(SUM(p.status = 'APPROVED'),0) AS fotos_aprovadas
FROM users u
LEFT JOIN photos p ON p.user_id = u.id
GROUP BY u.id, u.name
ORDER BY fotos_aprovadas DESC, total_fotos DESC;
