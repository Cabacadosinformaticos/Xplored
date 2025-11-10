-- =====================================================
-- queries.sql — Xplored
-- Autor: Tiago Cabaça
-- Descrição: Conjunto de queries úteis para testes e exemplos.
-- Inclui listagens por localização, categorias e moderação.
-- =====================================================

USE xplored_simplefied;

-- Coordenadas de referência (Moscavide)
SET @lat   = 38.7785;
SET @lng   = -9.1017;
SET @delta = 0.015;  -- ~1,5 km

-- 1) Locais próximos (aprovados)
SELECT p.*
FROM places p
WHERE p.status = 'APPROVED'
  AND p.lat BETWEEN (@lat - @delta) AND (@lat + @delta)
  AND p.lng BETWEEN (@lng - @delta) AND (@lng + @delta)
ORDER BY ABS(p.lat - @lat) + ABS(p.lng - @lng) ASC;

-- 2) Locais aprovados por categoria (melhor rating primeiro)
SELECT
  c.name AS categoria,
  p.name AS local,
  p.avg_rating AS avaliacao
FROM places p
JOIN categories c ON c.category_id = p.category_id
WHERE p.status = 'APPROVED'
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
SELECT 'foto' AS tipo, photo_id AS id, status, created_at
FROM photos
WHERE status = 'PENDING'
UNION ALL
SELECT 'local' AS tipo, place_id AS id, status, created_at
FROM places
WHERE status = 'PENDING'
ORDER BY created_at ASC;

-- 5) Resumo de contribuições por utilizador
SELECT 
  u.user_id,
  u.name,
  COUNT(DISTINCT p.photo_id)              AS total_fotos,
  COALESCE(SUM(p.status = 'APPROVED'),0) AS fotos_aprovadas
FROM users u
LEFT JOIN photos p ON p.user_id = u.user_id
GROUP BY u.user_id, u.name
ORDER BY fotos_aprovadas DESC, total_fotos DESC;
