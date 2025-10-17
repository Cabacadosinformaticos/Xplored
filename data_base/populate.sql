-- =====================================================
-- populate.sql — Xplored
-- Autor: Tiago Cabaça
-- Descrição: Este ficheiro serve para popular a base de dados com alguns dados de teste iniciais.
-- =====================================================

USE xplored_simplefied;

START TRANSACTION;

-- Utilizadores
INSERT INTO users (name, email, password_hash, role, country, points, profile_photo_url)
VALUES
  ('Nataly Costa',   'nataly.costa@gmail.com',       '$2y$10$eImiTXuWVxfM37uY4JANjQ9w4JpW2g1Jb0qf2p7N2sVZcHj2d8v4W', 'USER', 'Portugal', 120, 'https://cdn.example/avatars/nataly.jpg'),
  ('Teresa Rocha',   'teresa.rocha@gmail.com',       '$2y$10$eImiTXuWVxfM37uY4JANjQ9w4JpW2g1Jb0qf2p7N2sVZcHj2d8v4W', 'USER', 'Portugal',  35, 'https://cdn.example/avatars/teresa.jpg'),
  ('Rui Martins',    'rui.moscavide@gmail.com',      '$2y$10$eImiTXuWVxfM37uY4JANjQ9w4JpW2g1Jb0qf2p7N2sVZcHj2d8v4W', 'USER', 'Portugal',  40, NULL),
  ('Joana Silva',    'joana.silva@gmail.com',        '$2y$10$eImiTXuWVxfM37uY4JANjQ9w4JpW2g1Jb0qf2p7N2sVZcHj2d8v4W', 'USER', 'Portugal',  85, 'https://cdn.example/avatars/joana.jpg'),
  ('Miguel Ramos',   'miguel.ramos@gmail.com',       '$2y$10$eImiTXuWVxfM37uY4JANjQ9w4JpW2g1Jb0qf2p7N2sVZcHj2d8v4W', 'USER', 'Portugal',  15, NULL),
  ('Andreia Lopes',  'andreia.lopes@gmail.com',      '$2y$10$eImiTXuWVxfM37uY4JANjQ9w4JpW2g1Jb0qf2p7N2sVZcHj2d8v4W', 'USER', 'Portugal',  60, 'https://cdn.example/avatars/andreia.jpg'),
  ('Carlos Pinto',   'carlos.pinto@gmail.com',       '$2y$10$eImiTXuWVxfM37uY4JANjQ9w4JpW2g1Jb0qf2p7N2sVZcHj2d8v4W', 'USER', 'Portugal',  22, NULL),
  ('Beatriz Sousa',  'beatriz.sousa@gmail.com',      '$2y$10$eImiTXuWVxfM37uY4JANjQ9w4JpW2g1Jb0qf2p7N2sVZcHj2d8v4W', 'USER', 'Portugal', 140, 'https://cdn.example/avatars/beatriz.jpg'),
  ('Diogo Ferreira', 'diogo.ferreira@gmail.com',     '$2y$10$eImiTXuWVxfM37uY4JANjQ9w4JpW2g1Jb0qf2p7N2sVZcHj2d8v4W', 'USER', 'Portugal',  10, NULL),
  ('Sofia Almeida',  'sofia.almeida@gmail.com',      '$2y$10$eImiTXuWVxfM37uY4JANjQ9w4JpW2g1Jb0qf2p7N2sVZcHj2d8v4W', 'USER', 'Portugal',  75, 'https://cdn.example/avatars/sofia.jpg'),
  ('Pedro Figueiredo','pedro.figueiredo@gmail.com',  '$2y$10$eImiTXuWVxfM37uY4JANjQ9w4JpW2g1Jb0qf2p7N2sVZcHj2d8v4W', 'USER', 'Portugal',  55, NULL),
  ('Mariana Nunes',  'mariana.nunes@gmail.com',      '$2y$10$eImiTXuWVxfM37uY4JANjQ9w4JpW2g1Jb0qf2p7N2sVZcHj2d8v4W', 'USER', 'Portugal',  95, 'https://cdn.example/avatars/mariana.jpg');

-- Categorias (1=Atividades, 2=Lojas, 3=Restauração, 4=Locais Históricos, 5=Natureza)
INSERT INTO categories (name, color_hex, icon_name) VALUES
  ('Atividades',        '#3B82F6', 'activity'),
  ('Lojas',             '#8B5CF6', 'store'),
  ('Restauração',       '#FB923C', 'utensils'),
  ('Locais Históricos', '#F472B6', 'landmark'),
  ('Natureza',          '#22C55E', 'leaf');

-- Locais (Coordenadas de exemplo em Moscavide)
INSERT INTO places
  (name, description, lat, lng, address_full, postal_code, avg_rating, category_id, status, cover_image_url)
VALUES
  -- Atividades (1)
  ('Parque Infantil Central',
   'Espaço para crianças com equipamentos.',
   38.776800, -9.103300, 'Av. de Moscavide', '1885-200',
   4.1, 1, 'APPROVED', NULL),

  -- Lojas (2)
  ('Mercearia da Vila',
   'Loja local com produtos frescos e regionais.',
   38.777900, -9.104900, 'Rua da Mercearia, Moscavide', '1885-120',
   4.2, 2, 'APPROVED', NULL),

  -- Restauração (3)
  ('Tasquinha da Praça',
   'Comida caseira e ambiente familiar.',
   38.778500, -9.101700, 'Praça Central, Moscavide', '1885-000',
   4.6, 3, 'APPROVED', NULL),

  -- Locais Históricos (4)
  ('Igreja Paroquial de Moscavide',
   'Património local com vista sobre a vila.',
   38.772900, -9.110400, 'Largo da Igreja', '1885-058',
   4.5, 4, 'APPROVED', NULL),

  -- Natureza (5)
  ('Jardim do Rio Trancão',
   'Zona verde para caminhadas e piqueniques.',
   38.792300, -9.090200, 'Parque do Trancão', '2685-000',
   4.3, 5, 'APPROVED', NULL),

  -- Extra (Atividades; pendente para testar moderação)
  ('Painel Rua da República',
   'Mural de arte urbana recente.',
   38.775100, -9.106900, 'Rua da República', '1885-100',
   NULL, 1, 'PENDING', NULL);

-- Fotografias
INSERT INTO photos (place_id, user_id, url, status, created_at)
VALUES
  (
    (SELECT place_id FROM places WHERE name = 'Igreja Paroquial de Moscavide'),
    (SELECT user_id  FROM users  WHERE email = 'joana.silva@gmail.com'),
    'https://cdn.example/xplored/places/igreja-01.jpg',
    'APPROVED',
    '2025-09-28 15:22:10'
  ),
  (
    (SELECT place_id FROM places WHERE name = 'Tasquinha da Praça'),
    (SELECT user_id  FROM users  WHERE email = 'nataly.costa@gmail.com'),
    'https://cdn.example/xplored/places/tasquinha-01.jpg',
    'APPROVED',
    '2025-10-03 12:08:44'
  ),
  (
    (SELECT place_id FROM places WHERE name = 'Jardim do Rio Trancão'),
    (SELECT user_id  FROM users  WHERE email = 'miguel.ramos@gmail.com'),
    'https://cdn.example/xplored/places/trancao-01.jpg',
    'APPROVED',
    '2025-10-21 18:37:02'
  ),
  (
    (SELECT place_id FROM places WHERE name = 'Mercearia da Vila'),
    (SELECT user_id  FROM users  WHERE email = 'teresa.rocha@gmail.com'),
    'https://cdn.example/xplored/places/mercearia-01.jpg',
    'APPROVED',
    '2025-10-27 09:11:56'
  ),
  (
    (SELECT place_id FROM places WHERE name = 'Parque Infantil Central'),
    (SELECT user_id  FROM users  WHERE email = 'beatriz.sousa@gmail.com'),
    'https://cdn.example/xplored/places/parque-01.jpg',
    'APPROVED',
    '2025-11-01 16:49:33'
  ),
  (
    (SELECT place_id FROM places WHERE name = 'Painel Rua da República'),
    (SELECT user_id  FROM users  WHERE email = 'diogo.ferreira@gmail.com'),
    'https://cdn.example/xplored/places/painel-rua-republica-01.jpg',
    'PENDING',
    '2025-11-06 20:04:11'
  );

COMMIT;
