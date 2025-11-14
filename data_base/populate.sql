-- =====================================================
-- populate.sql
-- Xplored App – Dados de Teste
-- Localização: Moscavide & Parque das Nações (Lisboa)
-- DB: xplored_db
-- =====================================================

USE xplored_db;

-- =====================================================
-- 1. UTILIZADORES (USERS)
-- =====================================================
INSERT INTO users (name, email, password_hash, about, country, role, points, profile_photo) VALUES
('Tiago Cabaça', 'tiago@xplored.pt', '1234', 'Desenvolvedor e explorador urbano.', 'Portugal', 'ADMIN', 500, 'https://ui-avatars.com/api/?name=Tiago+Cabaca&background=0D8ABC&color=fff'),
('Ana Silva', 'ana@xplored.pt', '1234', 'Adoro fotografia e natureza.', 'Portugal', 'USER', 150, 'https://ui-avatars.com/api/?name=Ana+Silva&background=random'),
('João Comerciante', 'joao@loja.pt', '1234', 'Gerente do Cantinho do Bairro.', 'Portugal', 'BUSINESS', 50, NULL);

-- =====================================================
-- 2. CATEGORIAS (CATEGORIES)
-- =====================================================
-- NOTA: O script de criação (CREATE) já insere as categorias (IDs 1-5).
-- 1: Atividades, 2: Lojas, 3: Restauração, 4: Históricos, 5: Paisagens

-- =====================================================
-- 3. LOCAIS (PLACES)
-- =====================================================
INSERT INTO places (name, description, lat, lng, address_full, category_id, author_id, avg_rating, is_verified) VALUES
(
    'Jardim de Moscavide',
    'Um espaço verde tranquilo no coração de Moscavide, perfeito para relaxar à tarde.',
    38.776900,
    -9.102300,
    'Av. de Moscavide, Lisboa',
    5, -- Paisagens
    1, -- Tiago
    5.0,
    TRUE
),
(
    'Igreja de Santo António',
    'Igreja moderna com arquitetura distinta, ponto de referência na comunidade.',
    38.775800,
    -9.103100,
    'Rua da Igreja, Moscavide',
    4, -- Históricos
    1, -- Tiago
    4.0,
    TRUE
),
(
    'O Cantinho do Bairro',
    'O melhor bitoque da zona. Ambiente familiar e preços acessíveis.',
    38.776200,
    -9.101500,
    'Rua 1º de Maio, Moscavide',
    3, -- Restauração
    3, -- João
    4.5,
    TRUE
),
(
    'Centro Comercial da Portela',
    'Shopping tradicional com várias lojas locais e um cinema antigo.',
    38.779100,
    -9.104200,
    'Rotunda da Portela',
    2, -- Lojas
    2, -- Ana
    3.5,
    FALSE
);

-- =====================================================
-- 4. AVALIAÇÕES (REVIEWS)
-- =====================================================
INSERT INTO reviews (rating, title, comment, user_id, place_id) VALUES
(5, 'Excelente ambiente', 'Lugar muito tranquilo e bem cuidado, ótimo para ler um livro.', 2, 1), -- Ana -> Jardim
(4, 'Bitoque divinal', 'A comida é ótima, mas o serviço foi um pouco lento.', 1, 3), -- Tiago -> Restaurante
(3, 'Precisa de obras', 'O centro comercial está um pouco degradado, mas tem boas lojas.', 1, 4); -- Tiago -> Shopping

-- =====================================================
-- 5. FOTOS (PHOTOS)
-- =====================================================
INSERT INTO photos (url, place_id, user_id, review_id, kind, status) VALUES
-- Foto de capa do Jardim (Associada ao Local)
('https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/Jardim_de_Moscavide.jpg/800px-Jardim_de_Moscavide.jpg', 1, 1, NULL, 'GALLERY', 'APPROVED'),
-- Foto de capa da Igreja (Associada ao Local)
('https://upload.wikimedia.org/wikipedia/commons/5/52/Igreja_Santo_Antonio_Moscavide.jpg', 2, 1, NULL, 'GALLERY', 'APPROVED'),
-- Foto do Bitoque (Associada à Review do Tiago)
('https://www.nit.pt/wp-content/uploads/2018/10/d8b2d7156943890cacc0622501066746.jpg', 3, 1, 2, 'GALLERY', 'APPROVED');

-- =====================================================
-- 6. REAÇÕES (REACTIONS)
-- =====================================================
INSERT INTO reactions (type, user_id, review_id) VALUES
('USEFUL', 3, 2), -- João achou útil a review do Tiago sobre o restaurante
('NOT_USEFUL', 2, 3); -- Ana não achou útil a review do Shopping

-- =====================================================
-- 7. PEDIPAPERS (ROTES)
-- =====================================================
INSERT INTO pedipapers (name, description, total_points, active) VALUES
(
    'Descobrir Moscavide',
    'Um percurso curto para conhecer a essência do bairro, desde a natureza à gastronomia.',
    500,
    TRUE
);

-- =====================================================
-- 8. PARAGENS DA ROTA (ROUTE STOPS)
-- =====================================================
INSERT INTO route_stops (pedipaper_id, place_id, stop_order, task_description, requires_photo) VALUES
(1, 1, 1, 'Encontra a estátua no centro do jardim.', TRUE), -- Jardim
(1, 2, 2, 'Qual é o ano gravado na porta da igreja?', FALSE), -- Igreja
(1, 3, 3, 'Tira uma selfie a comer um pastel de nata.', TRUE); -- Restaurante

-- =====================================================
-- 9. PARTICIPAÇÕES (ROUTE PARTICIPATIONS)
-- =====================================================
INSERT INTO route_participations (user_id, pedipaper_id, completed, progress, started_at) VALUES
(2, 1, FALSE, 1, NOW()); -- Ana começou a rota, está na paragem 1

-- =====================================================
-- 10. CONTAS DE NEGÓCIO & CUPÕES
-- =====================================================
INSERT INTO business_accounts (name, email) VALUES
('Grupo Cantinho', 'cantinho@negocio.pt');

INSERT INTO coupons (title, description, cost_points, business_account_id, code, active) VALUES
(
    'Café Grátis',
    'Oferta de um café na compra de qualquer bolo.',
    100,
    1,
    'CAFE100',
    TRUE
),
(
    '10% Desconto Almoço',
    'Válido para pratos do dia, exceto bebidas.',
    250,
    1,
    'ALMOCO10',
    TRUE
);

-- =====================================================
-- 11. LEDGER DE PONTOS (Histórico)
-- =====================================================
INSERT INTO points_ledger (user_id, amount, reason) VALUES
(1, 500, 'Bónus de Registo Inicial'),
(2, 150, 'Completou perfil'),
(1, -250, 'Comprou cupão Almoço');

-- =====================================================
-- 12. REDEMPTIONS (Cupões usados)
-- =====================================================
INSERT INTO redemptions (user_id, coupon_id) VALUES
(1, 2); -- Tiago comprou o cupão de 10%