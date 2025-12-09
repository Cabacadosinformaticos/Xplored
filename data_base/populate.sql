-- =====================================================
-- populate.sql
-- Xplored App – Dados de Teste
-- Localização: Moscavide / Parque das Nações / Olivais / Oriente (Lisboa)
-- DB: xplored_db
-- =====================================================

USE xplored_db;

-- =====================================================
-- 1. UTILIZADORES (USERS)
-- =====================================================
-- Test logins (password 1234 para todos):
--   tiago@xplored.pt        (ADMIN)
--   ana@xplored.pt          (USER)
--   joao@loja.pt            (BUSINESS)
--   rita@xplored.pt         (USER)
--   miguel@xplored.pt       (USER)
--   beatriz@xplored.pt      (USER)
--   diogo@xplored.pt        (USER)
--   ines@xplored.pt         (USER)
--   pedro@xplored.pt        (USER)
--   carolina@xplored.pt     (USER)
--   andre@xplored.pt        (USER)
--   sofia@xplored.pt        (USER)
--   bruno@xplored.pt        (USER)
INSERT INTO users (id, name, email, password_hash, about, country, role, points, profile_photo) VALUES
(1, 'Tiago Cabaça', 'tiago@xplored.pt', '$2a$10$SPiaTvasqypx3wtJz6/jZuRblFjadpBjF/CBKMMpfhJMZexIitQ7G', 'Desenvolvedor e explorador urbano.', 'Portugal', 'ADMIN', 250, 'https://ui-avatars.com/api/?name=Tiago+Cabaca&background=0D8ABC&color=fff'),
(2, 'Ana Silva', 'ana@xplored.pt', '$2a$10$SPiaTvasqypx3wtJz6/jZuRblFjadpBjF/CBKMMpfhJMZexIitQ7G', 'Adoro fotografia e natureza.', 'Portugal', 'USER', 480, 'https://ui-avatars.com/api/?name=Ana+Silva&background=random'),
(3, 'João Comerciante', 'joao@loja.pt', '$2a$10$SPiaTvasqypx3wtJz6/jZuRblFjadpBjF/CBKMMpfhJMZexIitQ7G', 'Gerente do Cantinho do Bairro.', 'Portugal', 'BUSINESS', 0, NULL),
(4, 'Rita Ferreira', 'rita@xplored.pt', '$2a$10$SPiaTvasqypx3wtJz6/jZuRblFjadpBjF/CBKMMpfhJMZexIitQ7G', 'Guia turística amadora, apaixonada por miradouros e pontes.', 'Portugal', 'USER', 630, 'https://ui-avatars.com/api/?name=Rita+Ferreira&background=random'),
(5, 'Miguel Santos', 'miguel@xplored.pt', '$2a$10$SPiaTvasqypx3wtJz6/jZuRblFjadpBjF/CBKMMpfhJMZexIitQ7G', 'Moro em Moscavide e conheço cada esquina do bairro.', 'Portugal', 'USER', 120, 'https://ui-avatars.com/api/?name=Miguel+Santos&background=random'),
(6, 'Beatriz Costa', 'beatriz@xplored.pt', '$2a$10$SPiaTvasqypx3wtJz6/jZuRblFjadpBjF/CBKMMpfhJMZexIitQ7G', 'Coleciono fins de semana no Parque das Nações com os sobrinhos.', 'Portugal', 'USER', 320, 'https://ui-avatars.com/api/?name=Beatriz+Costa&background=random'),
(7, 'Diogo Almeida', 'diogo@xplored.pt', '$2a$10$SPiaTvasqypx3wtJz6/jZuRblFjadpBjF/CBKMMpfhJMZexIitQ7G', 'Corro pelo Parque Tejo todas as manhãs.', 'Portugal', 'USER', 130, 'https://ui-avatars.com/api/?name=Diogo+Almeida&background=random'),
(8, 'Inês Marques', 'ines@xplored.pt', '$2a$10$SPiaTvasqypx3wtJz6/jZuRblFjadpBjF/CBKMMpfhJMZexIitQ7G', 'Fotógrafa de arquitetura, ando sempre com a máquina atrás.', 'Portugal', 'USER', 480, 'https://ui-avatars.com/api/?name=Ines+Marques&background=random'),
(9, 'Pedro Nogueira', 'pedro@xplored.pt', '$2a$10$SPiaTvasqypx3wtJz6/jZuRblFjadpBjF/CBKMMpfhJMZexIitQ7G', 'Curioso por história local e igrejas antigas.', 'Portugal', 'USER', 110, 'https://ui-avatars.com/api/?name=Pedro+Nogueira&background=random'),
(10, 'Carolina Lopes', 'carolina@xplored.pt', '$2a$10$SPiaTvasqypx3wtJz6/jZuRblFjadpBjF/CBKMMpfhJMZexIitQ7G', 'Vou ao mercado todas as semanas, fruta fresca é lei.', 'Portugal', 'USER', 140, 'https://ui-avatars.com/api/?name=Carolina+Lopes&background=random'),
(11, 'André Rodrigues', 'andre@xplored.pt', '$2a$10$SPiaTvasqypx3wtJz6/jZuRblFjadpBjF/CBKMMpfhJMZexIitQ7G', 'Turista de fim de semana, adoro roteiros a pé.', 'Portugal', 'USER', 150, 'https://ui-avatars.com/api/?name=Andre+Rodrigues&background=random'),
(12, 'Sofia Martins', 'sofia@xplored.pt', '$2a$10$SPiaTvasqypx3wtJz6/jZuRblFjadpBjF/CBKMMpfhJMZexIitQ7G', 'Passeio o cão pelo Vale do Silêncio.', 'Portugal', 'USER', 120, 'https://ui-avatars.com/api/?name=Sofia+Martins&background=random'),
(13, 'Bruno Carvalho', 'bruno@xplored.pt', '$2a$10$SPiaTvasqypx3wtJz6/jZuRblFjadpBjF/CBKMMpfhJMZexIitQ7G', 'Pai de dois exploradores, o Oceanário é paragem obrigatória.', 'Portugal', 'USER', 540, 'https://ui-avatars.com/api/?name=Bruno+Carvalho&background=random');

-- =====================================================
-- 2. CATEGORIAS (CATEGORIES)
-- =====================================================
-- NOTA: O script de criação (CREATE) já insere as categorias (IDs 1-5).
-- 1: Atividades, 2: Lojas, 3: Restauração, 4: Históricos, 5: Paisagens

-- =====================================================
-- 3. LOCAIS (PLACES)
-- =====================================================
-- status: PENDING (criado por USER, aguarda moderação), APPROVED, REJECTED.
-- Os locais 8 e 17 foram criados por utilizadores com role USER e estão pendentes.
INSERT INTO places (id, name, description, lat, lng, address_full, category_id, author_id, avg_rating, is_verified, status) VALUES
(
    1,
    'Jardim de Moscavide',
    'Um espaço verde tranquilo no coração de Moscavide, perfeito para relaxar à tarde.',
    38.776900,
    -9.102300,
    'Av. de Moscavide, Lisboa',
    5, -- Paisagens
    1, -- Tiago
    5.00,
    TRUE,
    'APPROVED'
),
(
    2,
    'Igreja de Santo António',
    'Igreja moderna com arquitetura distinta, ponto de referência na comunidade.',
    38.775800,
    -9.103100,
    'Rua da Igreja, Moscavide',
    4, -- Históricos
    1, -- Tiago
    4.50,
    TRUE,
    'APPROVED'
),
(
    3,
    'O Cantinho do Bairro',
    'O melhor bitoque da zona. Ambiente familiar e preços acessíveis.',
    38.776200,
    -9.101500,
    'Rua 1º de Maio, Moscavide',
    3, -- Restauração
    3, -- João
    4.33,
    TRUE,
    'APPROVED'
),
(
    4,
    'Centro Comercial da Portela',
    'Shopping tradicional com várias lojas locais e um cinema antigo.',
    38.779100,
    -9.104200,
    'Rotunda da Portela',
    2, -- Lojas
    2, -- Ana
    3.00,
    FALSE,
    'APPROVED'
),
(
    5,
    'Oceanário de Lisboa',
    'Um dos maiores oceanários da Europa, com um aquário central de 5 milhões de litros e mais de 8 mil animais marinhos.',
    38.763517,
    -9.095928,
    'Esplanada D. Carlos I, 1990-005 Lisboa',
    1, -- Atividades
    1, -- Tiago
    4.50,
    TRUE,
    'APPROVED'
),
(
    6,
    'Pavilhão do Conhecimento',
    'Museu de ciência interativo da Ciência Viva, com exposições permanentes e temporárias para todas as idades.',
    38.762888,
    -9.095858,
    'Alameda dos Oceanos, 1990-223 Lisboa',
    1, -- Atividades
    1, -- Tiago
    4.33,
    TRUE,
    'APPROVED'
),
(
    7,
    'Teleférico do Parque das Nações',
    'Teleférico panorâmico que liga as Tágides ao jardim junto à Torre Vasco da Gama, com vista sobre o Tejo.',
    38.771892,
    -9.092551,
    'Passeio das Tágides, 1990-280 Lisboa',
    1, -- Atividades
    2, -- Ana
    4.50,
    TRUE,
    'APPROVED'
),
(
    8,
    'Jardins da Água',
    'Jardins ribeirinhos desenhados para a Expo 98, com fontes, espelhos de água e caminhos sombreados.',
    38.764812,
    -9.094468,
    'Alameda dos Oceanos, 1990-038 Lisboa',
    5, -- Paisagens
    4, -- Rita (USER)
    0.00,
    FALSE,
    'PENDING'
),
(
    9,
    'Torre Vasco da Gama',
    'Torre de 145 metros construída para a Expo 98, hoje integrada num hotel, com miradouro sobre a cidade.',
    38.774472,
    -9.092751,
    'Cais das Naus, 1990-202 Lisboa',
    4, -- Históricos
    1, -- Tiago
    4.00,
    TRUE,
    'APPROVED'
),
(
    10,
    'Gare do Oriente',
    'Estação ferroviária de Santiago Calatrava, ícone de arquitetura contemporânea e principal porta de entrada no Parque das Nações.',
    38.767943,
    -9.099732,
    'Av. Dom João II, 1900-233 Lisboa',
    4, -- Históricos
    1, -- Tiago
    4.00,
    TRUE,
    'APPROVED'
),
(
    11,
    'Parque Tejo',
    'Grande parque urbano junto ao estuário, com passadiços, zonas de piquenique e observação de aves.',
    38.779745,
    -9.088712,
    'Passeio do Tejo, 1990-286 Lisboa',
    5, -- Paisagens
    2, -- Ana
    5.00,
    TRUE,
    'APPROVED'
),
(
    12,
    'Passeio das Tágides',
    'Passeio ribeirinho sobre o Tejo, muito procurado para caminhadas e corridas ao fim do dia.',
    38.770209,
    -9.092078,
    'Passeio das Tágides, 1990-280 Lisboa',
    5, -- Paisagens
    4, -- Rita
    4.50,
    TRUE,
    'APPROVED'
),
(
    13,
    'Centro Vasco da Gama',
    'Shopping do Parque das Nações com mais de 150 lojas, cinemas e vista para a Gare do Oriente.',
    38.768084,
    -9.095257,
    'Av. Dom João II 40, 1990-091 Lisboa',
    2, -- Lojas
    1, -- Tiago
    2.50,
    TRUE,
    'APPROVED'
),
(
    14,
    'Mercado de Moscavide',
    'Mercado municipal com bancas de peixe, fruta e legumes frescos, com forte presença de produtores da região.',
    38.777252,
    -9.101867,
    'Av. de Moscavide, 1885-001 Lisboa',
    2, -- Lojas
    5, -- Miguel
    3.33,
    TRUE,
    'APPROVED'
),
(
    15,
    'Restaurante Sabores do Oriente',
    'Cozinha portuguesa contemporânea com peixe fresco do dia e uma esplanada virada para a Alameda dos Oceanos.',
    38.769412,
    -9.096337,
    'Alameda dos Oceanos 43, 1990-208 Lisboa',
    3, -- Restauração
    3, -- João
    4.50,
    TRUE,
    'APPROVED'
),
(
    16,
    'Churrasqueira do Bairro',
    'Churrasqueira de bairro conhecida pelo frango assado no carvão e pelos preços acessíveis.',
    38.776748,
    -9.100921,
    'Rua 1º de Maio 12, 1885-002 Moscavide',
    3, -- Restauração
    6, -- Beatriz
    3.50,
    TRUE,
    'APPROVED'
),
(
    17,
    'Pastelaria Nata de Lisboa',
    'Pastelaria de bairro com pastéis de nata saídos do forno a toda a hora e esplanada na avenida.',
    38.777041,
    -9.102540,
    'Av. de Moscavide 55, 1885-001 Lisboa',
    3, -- Restauração
    5, -- Miguel (USER)
    0.00,
    FALSE,
    'PENDING'
),
(
    18,
    'Igreja de Nossa Senhora dos Olivais',
    'Igreja matriz dos Olivais, com interior revestido a azulejos e raízes que remontam ao século XVI.',
    38.773118,
    -9.113216,
    'Largo da Igreja dos Olivais, 1800-161 Lisboa',
    4, -- Históricos
    2, -- Ana
    4.50,
    TRUE,
    'APPROVED'
),
(
    19,
    'Ponte Vasco da Gama',
    'A ponte mais longa da Europa, com 17 km sobre o estuário do Tejo e um percurso pedestre muito fotografado.',
    38.774836,
    -9.090461,
    'Ponte Vasco da Gama, 1990-286 Lisboa',
    5, -- Paisagens
    1, -- Tiago
    4.67,
    TRUE,
    'APPROVED'
),
(
    20,
    'Parque do Vale do Silêncio',
    'Parque tranquilo nos Olivais, com lago, prados amplos e trilhos ideais para passear o cão.',
    38.772688,
    -9.109529,
    'Av. Cidade de Luanda, 1800-122 Lisboa',
    5, -- Paisagens
    7, -- Diogo
    3.50,
    TRUE,
    'APPROVED'
);

-- =====================================================
-- 4. AVALIAÇÕES (REVIEWS)
-- =====================================================
-- avg_rating de cada local = média das suas reviews (0.00 quando não tem).
INSERT INTO reviews (id, rating, title, comment, user_id, place_id) VALUES
(1, 5, 'Excelente ambiente', 'Lugar muito tranquilo e bem cuidado, ótimo para ler um livro.', 2, 1),
(2, 4, 'Bitoque divinal', 'A comida é ótima, mas o serviço foi um pouco lento.', 1, 3),
(3, 3, 'Precisa de obras', 'O centro comercial está um pouco degradado, mas tem boas lojas.', 1, 4),
(4, 5, 'Templo acolhedor', 'Igreja simples mas muito acolhedora, com boa acústica para os coros.', 4, 2),
(5, 4, 'Missa das onze', 'Costumo vir à missa das onze e nunca está demasiado cheia.', 5, 2),
(6, 5, 'Bitoque de referência', 'Continuo a achar que é o melhor bitoque da zona de Moscavide.', 6, 3),
(7, 4, 'Bom e barato', 'Almoço completo por menos de dez euros, difícil de bater.', 7, 3),
(8, 4, 'Recuperou muito', 'Nos últimos anos melhorou imenso, com lojas novas e mais limpeza.', 8, 4),
(9, 2, 'Cinemateca fechada', 'Fui ao cinema e estava encerrado sem aviso na porta.', 9, 4),
(10, 5, 'Um dia inteiro a olhar para peixes', 'O aquário central é hipnotizante, ficámos lá uma hora sem dar por isso.', 2, 5),
(11, 4, 'Muito bom, mas caro', 'Vale cada cêntimo pela experiência, ainda assim o bilhete familiar pesa.', 10, 5),
(12, 5, 'Ciência que se toca', 'As crianças não queriam sair. As exposições são todas interativas.', 11, 6),
(13, 4, 'Ótimo para os mais novos', 'Muito educativo e bem organizado, só peca por ser pequeno.', 12, 6),
(14, 4, 'Fim de semana cheio', 'Ao sábado há filas para tudo, convém chegar cedo.', 13, 6),
(15, 4, 'Vista fantástica', 'A viagem é curta mas a vista sobre o rio compensa.', 4, 7),
(16, 5, 'Imperdível ao pôr do sol', 'Andámos ao fim da tarde e a luz sobre o Tejo é inesquecível.', 6, 7),
(17, 4, 'Arquitetura imponente', 'A torre vista de baixo é enorme e o miradouro vale a subida.', 10, 9),
(18, 4, 'Hotel e miradouro', 'Hoje é um hotel, mas continua a ser um marco do Parque das Nações.', 12, 9),
(19, 5, 'Estação de outro mundo', 'A estrutura de Calatrava é impressionante, parece uma floresta de aço.', 8, 10),
(20, 3, 'Confusa para quem chega', 'Bonita, mas a sinalização entre plataformas podia ser muito melhor.', 5, 10),
(21, 5, 'Verde e sossegado', 'Enorme, limpo e com passadiços novos junto à água.', 7, 11),
(22, 5, 'Ótimo para correr', 'Circuito plano e sombra suficiente, tornei-o o meu treino de sábado.', 13, 11),
(23, 4, 'Brisa do Tejo', 'Passeio agradável, com bancos suficientes para parar a ver o rio.', 9, 12),
(24, 5, 'Caminhada perfeita', 'Da Gare ao teleférico a pé é uma das melhores caminhadas da cidade.', 11, 12),
(25, 3, 'Cumpre o essencial', 'Tem tudo o que preciso, mas não tem nada de especial.', 2, 13),
(26, 2, 'Muito cheio ao fim de semana', 'Estacionamento impossível e praça de alimentação sempre a rebentar.', 12, 13),
(27, 4, 'Peixe sempre fresco', 'As bancas de peixe são excelentes e os preços justos.', 10, 14),
(28, 5, 'Fruta da estação', 'Encontro aqui fruta que não aparece nos supermercados.', 5, 14),
(29, 1, 'Difícil estacionar', 'O mercado é bom, mas a zona à volta é um caos para estacionar.', 9, 14),
(30, 5, 'Bacalhau à lagareiro excelente', 'Peixe fresco e bem confecionado, com esplanada muito agradável.', 6, 15),
(31, 4, 'Serviço atencioso', 'Fomos muito bem atendidos, apesar da casa estar cheia.', 13, 15),
(32, 3, 'Frango bom, sala ruidosa', 'O frango é saboroso mas a sala é muito barulhenta à hora de almoço.', 8, 16),
(33, 4, 'Preço justo', 'Rápido, barato e saboroso. Ideal para um almoço de trabalho.', 2, 16),
(34, 5, 'Azulejos magníficos', 'O interior revestido a azulejos é uma surpresa, vale a visita.', 4, 18),
(35, 4, 'Muito bem preservada', 'Igreja antiga e bem cuidada, com um largo simpático à frente.', 11, 18),
(36, 5, 'Obra de engenharia', 'Atravessar de carro é banal, mas ver a ponte de baixo é outra coisa.', 5, 19),
(37, 5, 'Vista do rio ao amanhecer', 'Fui de manhã cedo e tinha o passadiço só para mim.', 10, 19),
(38, 4, 'Longa mas bonita', 'O percurso pedestre é extenso, mas a paisagem compensa o esforço.', 12, 19),
(39, 4, 'Refúgio verde nos Olivais', 'Tem lago e sombra, é o meu escape depois do trabalho.', 13, 20),
(40, 3, 'Precisa de manutenção', 'Os trilhos estão um pouco degradados e faltam caixotes do lixo.', 9, 20);

-- =====================================================
-- 5. FOTOS (PHOTOS)
-- =====================================================
-- kind: GALLERY (foto do local/review) ou EVIDENCE (foto de check-in de pedipaper).
-- As fotos EVIDENCE usam o mesmo formato dos uploads reais: /uploads/<uuid>.<ext>
INSERT INTO photos (id, url, place_id, user_id, review_id, kind, status, created_at) VALUES
-- Fotos existentes
(1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3c/Jardim_na_Avenida_de_Moscavide%2C_em_Moscavide_07.jpg/960px-Jardim_na_Avenida_de_Moscavide%2C_em_Moscavide_07.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 1, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 100 DAY),
(2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b3/Igreja_de_Santo_Ant%C3%B3nio_de_Moscavide.jpg/960px-Igreja_de_Santo_Ant%C3%B3nio_de_Moscavide.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 2, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 100 DAY),
(3, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/50/Prego_%2850644636392%29.jpg/960px-Prego_%2850644636392%29.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 3, 1, 2, 'GALLERY', 'APPROVED', NOW() - INTERVAL 95 DAY);

-- Restauração: mesmo padrão de URL já usado na foto de comida existente.
INSERT INTO photos (id, url, place_id, user_id, review_id, kind, status, created_at) VALUES
(11, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Bacalhau_a_Bras.jpg/960px-Bacalhau_a_Bras.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 15, 3, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 30 DAY),
(12, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/32/2018-02-03_Chicken_Piri-Piri%2C_Albufeira_%281%29.JPG/960px-2018-02-03_Chicken_Piri-Piri%2C_Albufeira_%281%29.JPG?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 16, 6, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 28 DAY),
(13, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9c/Pastel_de_nata_septembre_2015.jpg/960px-Pastel_de_nata_septembre_2015.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 17, 5, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 26 DAY);

-- Fotos de galeria dos monumentos e espaços públicos (Wikimedia Commons, miniaturas de 800 px)
INSERT INTO photos (id, url, place_id, user_id, review_id, kind, status, created_at) VALUES
(20, 'https://upload.wikimedia.org/wikipedia/commons/e/ef/Centro_Comercial_da_Portela.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail_unscaled', 4, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 60 DAY),
(21, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/42/Lisbon_Oceanarium_%E2%80%A2_Ocean%C3%A1rio_de_Lisboa.jpg/960px-Lisbon_Oceanarium_%E2%80%A2_Ocean%C3%A1rio_de_Lisboa.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 5, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 59 DAY),
(22, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/53/Pavilh%C3%A3o_do_Conhecimento_01.jpg/960px-Pavilh%C3%A3o_do_Conhecimento_01.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 6, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 58 DAY),
(23, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/07/Telecabine_Lisboa%2C_Parque_das_Na%C3%A7%C3%B5es_%E2%80%A2_Park_of_Nations_%2849652019058%29.jpg/960px-Telecabine_Lisboa%2C_Parque_das_Na%C3%A7%C3%B5es_%E2%80%A2_Park_of_Nations_%2849652019058%29.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 7, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 57 DAY),
(24, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4c/Jardins_de_%C3%81gua%2C_Parque_das_Na%C3%A7%C3%B5es%2C_Lisbon%2C_Portugal_julesvernex2.jpg/960px-Jardins_de_%C3%81gua%2C_Parque_das_Na%C3%A7%C3%B5es%2C_Lisbon%2C_Portugal_julesvernex2.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 8, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 56 DAY),
(25, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/61/Lisbon_-_Vasco_da_Gama_tower.jpg/960px-Lisbon_-_Vasco_da_Gama_tower.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 9, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 55 DAY),
(26, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8f/Lisbon_Gare_do_Oriente.jpg/960px-Lisbon_Gare_do_Oriente.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 10, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 54 DAY),
(27, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fe/Ponte_Vasco_da_Gama_from_Parque_Tejo.jpg/960px-Ponte_Vasco_da_Gama_from_Parque_Tejo.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 11, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 53 DAY),
(28, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/70/Passeio_das_T%C3%A1gides_with_Torre_Vasco_da_Gama_in_background.JPG/960px-Passeio_das_T%C3%A1gides_with_Torre_Vasco_da_Gama_in_background.JPG?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 12, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 52 DAY),
(29, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7a/Centro_Vasco_de_Gama_%2819423028680%29.jpg/960px-Centro_Vasco_de_Gama_%2819423028680%29.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 13, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 51 DAY),
(30, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/73/Ponte_Vasco_da_Gama_at_Sunrise.jpg/960px-Ponte_Vasco_da_Gama_at_Sunrise.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 19, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 50 DAY),
(31, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8b/Mata_do_Vale_do_Sil%C3%AAncio.jpg/960px-Mata_do_Vale_do_Sil%C3%AAncio.jpg?utm_source=commons.wikimedia.org&utm_campaign=imageinfo&utm_content=thumbnail', 20, 1, NULL, 'GALLERY', 'APPROVED', NOW() - INTERVAL 49 DAY);

-- Fotos de evidência (check-ins de pedipaper)
INSERT INTO photos (id, url, place_id, user_id, review_id, kind, status, created_at) VALUES
(101, '/uploads/2a41c0d9-1b3e-4a52-9c77-1e0d6b4f7a01.jpg', 1, 2, NULL, 'EVIDENCE', 'APPROVED', NOW() - INTERVAL 40 DAY),
(102, '/uploads/5f8b7c12-9d2a-4be1-8f30-6a1c9d2e7b02.jpg', 5, 2, NULL, 'EVIDENCE', 'APPROVED', NOW() - INTERVAL 25 DAY),
(103, '/uploads/9c3e5a77-4f18-4d92-b6a1-3e8f0c5d9b03.jpg', 7, 2, NULL, 'EVIDENCE', 'APPROVED', NOW() - INTERVAL 25 DAY),
(104, '/uploads/1d7f2b84-6a5c-4e37-9b02-8c4e1a7f3d04.jpg', 19, 4, NULL, 'EVIDENCE', 'APPROVED', NOW() - INTERVAL 18 DAY),
(105, '/uploads/6b9a4e21-3c7d-4f85-a1e9-2d7c0b6e8f05.jpg', 5, 6, NULL, 'EVIDENCE', 'APPROVED', NOW() - INTERVAL 12 DAY),
(106, '/uploads/8e2c7d95-1f4b-4a63-8d27-5b9e3f1a0c06.jpg', 7, 6, NULL, 'EVIDENCE', 'APPROVED', NOW() - INTERVAL 12 DAY),
(107, '/uploads/3a5d9c48-7b2e-4c19-9f64-1e8b6d2a4f07.jpg', 1, 8, NULL, 'EVIDENCE', 'APPROVED', NOW() - INTERVAL 9 DAY),
(108, '/uploads/4c8e1f63-2a9d-4b75-8e13-7f0a5c9b2d08.jpg', 1, 11, NULL, 'EVIDENCE', 'APPROVED', NOW() - INTERVAL 7 DAY),
(109, '/uploads/7f1b6a39-5d8c-4e20-a47b-9c3d1e8f5a09.jpg', 5, 13, NULL, 'EVIDENCE', 'APPROVED', NOW() - INTERVAL 5 DAY),
(110, '/uploads/2e9d4b71-8c3f-4a56-b9d8-4a1f7c0e6b10.jpg', 7, 13, NULL, 'EVIDENCE', 'APPROVED', NOW() - INTERVAL 5 DAY),
(111, '/uploads/5a3c8e27-9f1b-4d64-8a02-6e4b9d1f7c11.jpg', 5, 10, NULL, 'EVIDENCE', 'APPROVED', NOW() - INTERVAL 3 DAY);

-- =====================================================
-- 6. REAÇÕES (REACTIONS)
-- =====================================================
INSERT INTO reactions (type, user_id, review_id) VALUES
('USEFUL', 3, 2),     -- João achou útil a review do Tiago sobre o restaurante
('NOT_USEFUL', 2, 3), -- Ana não achou útil a review do Shopping
('USEFUL', 2, 6),     -- Ana -> review do Cantinho
('USEFUL', 4, 11),    -- Rita -> review do Oceanário
('USEFUL', 5, 12),    -- Miguel -> review do Pavilhão do Conhecimento
('USEFUL', 6, 19),    -- Beatriz -> review da Gare do Oriente
('NOT_USEFUL', 7, 26),-- Diogo -> review do Centro Vasco da Gama
('USEFUL', 8, 13),    -- Inês -> review do Pavilhão do Conhecimento
('USEFUL', 9, 16),    -- Pedro -> review do Teleférico
('USEFUL', 10, 21),   -- Carolina -> review do Parque Tejo
('USEFUL', 11, 30),   -- André -> review dos Sabores do Oriente
('USEFUL', 12, 28),   -- Sofia -> review do Mercado de Moscavide
('USEFUL', 13, 3),    -- Bruno -> review do Centro Comercial da Portela
('USEFUL', 4, 24);    -- Rita -> review do Passeio das Tágides

-- =====================================================
-- 7. PEDIPAPERS (ROTAS)
-- =====================================================
INSERT INTO pedipapers (id, name, description, total_points, active) VALUES
(1, 'Descobrir Moscavide', 'Um percurso curto para conhecer a essência do bairro, desde a natureza à gastronomia.', 500, TRUE),
(2, 'Parque das Nações em família', 'Rota para famílias entre o Oceanário, o Pavilhão do Conhecimento, o teleférico e os jardins.', 400, TRUE),
(3, 'Origens de Moscavide', 'Do mercado à igreja, um percurso pela história e pelos sabores do bairro.', 350, TRUE),
(4, 'À beira Tejo', 'Passeio ribeirinho entre as Tágides e o Parque Tejo, com vista para a ponte.', 500, TRUE);

-- =====================================================
-- 8. PARAGENS DA ROTA (ROUTE STOPS)
-- =====================================================
INSERT INTO route_stops (id, pedipaper_id, place_id, stop_order, task_description, requires_photo) VALUES
-- Rota 1: Descobrir Moscavide
(1, 1, 1, 1, 'Encontra a estátua no centro do jardim.', TRUE), -- Jardim
(2, 1, 2, 2, 'Qual é o ano gravado na porta da igreja?', FALSE), -- Igreja
(3, 1, 3, 3, 'Tira uma selfie a comer um pastel de nata.', TRUE), -- Restaurante
-- Rota 2: Parque das Nações em família
(4, 2, 5, 1, 'Fotografa o aquário central e o peixe mais estranho que encontrares.', TRUE), -- Oceanário
(5, 2, 6, 2, 'Conta quantos módulos tem a exposição permanente.', FALSE), -- Pavilhão do Conhecimento
(6, 2, 7, 3, 'Tira uma foto da cabine com o Tejo em fundo.', TRUE), -- Teleférico
(7, 2, 8, 4, 'Conta quantas fontes encontras nos Jardins da Água.', FALSE), -- Jardins da Água
-- Rota 3: Origens de Moscavide
(8, 3, 14, 1, 'Compra uma peça de fruta da época e diz quanto custou.', FALSE), -- Mercado
(9, 3, 2, 2, 'Encontra a placa com o ano de construção da igreja.', FALSE), -- Igreja
(10, 3, 1, 3, 'Senta-te no banco central do jardim e tira uma selfie.', TRUE), -- Jardim
(11, 3, 16, 4, 'Pergunta qual é o prato mais pedido da casa.', FALSE), -- Churrasqueira
-- Rota 4: À beira Tejo
(12, 4, 12, 1, 'Caminha até à beira-rio e fotografa o Tejo.', FALSE), -- Passeio das Tágides
(13, 4, 11, 2, 'Encontra o painel informativo sobre as aves do estuário.', FALSE), -- Parque Tejo
(14, 4, 19, 3, 'Tira uma foto da ponte vista da margem norte.', TRUE), -- Ponte Vasco da Gama
(15, 4, 9, 4, 'Sobe até ao miradouro e descreve a vista.', FALSE); -- Torre Vasco da Gama

-- =====================================================
-- 9. PARTICIPAÇÕES (ROUTE PARTICIPATIONS)
-- =====================================================
INSERT INTO route_participations (id, user_id, pedipaper_id, completed, points_awarded, started_at, completed_at) VALUES
(1, 2, 1, FALSE, 0, NOW() - INTERVAL 40 DAY, NULL),          -- Ana começou "Descobrir Moscavide"
(2, 2, 2, TRUE, 400, NOW() - INTERVAL 27 DAY, NOW() - INTERVAL 25 DAY),  -- Ana concluiu
(3, 4, 4, TRUE, 500, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 18 DAY),  -- Rita concluiu
(4, 6, 2, TRUE, 400, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 12 DAY),  -- Beatriz concluiu
(5, 8, 3, TRUE, 350, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 9 DAY),   -- Inês concluiu
(6, 11, 3, TRUE, 350, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 7 DAY),   -- André concluiu
(7, 13, 2, TRUE, 400, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 5 DAY),   -- Bruno concluiu
(8, 5, 3, FALSE, 0, NOW() - INTERVAL 3 DAY, NULL),           -- Miguel a meio
(9, 7, 4, FALSE, 0, NOW() - INTERVAL 2 DAY, NULL),           -- Diogo a meio
(10, 10, 2, FALSE, 0, NOW() - INTERVAL 3 DAY, NULL),         -- Carolina a meio
(11, 12, 3, FALSE, 0, NOW() - INTERVAL 1 DAY, NULL);         -- Sofia começou agora

-- =====================================================
-- 9b. CHECK-INS DAS PARAGENS (ROUTE STOP CHECKINS)
-- =====================================================
INSERT INTO route_stop_checkins (participation_id, route_stop_id, lat, lng, photo_id, created_at) VALUES
-- Ana, rota 1 (parcial)
(1, 1, 38.776900, -9.102300, 101, NOW() - INTERVAL 40 DAY),
-- Ana, rota 2 (concluída)
(2, 4, 38.763517, -9.095928, 102, NOW() - INTERVAL 27 DAY),
(2, 5, 38.762888, -9.095858, NULL, NOW() - INTERVAL 27 DAY),
(2, 6, 38.771892, -9.092551, 103, NOW() - INTERVAL 25 DAY),
(2, 7, 38.764812, -9.094468, NULL, NOW() - INTERVAL 25 DAY),
-- Rita, rota 4 (concluída)
(3, 12, 38.770209, -9.092078, NULL, NOW() - INTERVAL 20 DAY),
(3, 13, 38.779745, -9.088712, NULL, NOW() - INTERVAL 20 DAY),
(3, 14, 38.774836, -9.090461, 104, NOW() - INTERVAL 18 DAY),
(3, 15, 38.774472, -9.092751, NULL, NOW() - INTERVAL 18 DAY),
-- Beatriz, rota 2 (concluída)
(4, 4, 38.763517, -9.095928, 105, NOW() - INTERVAL 14 DAY),
(4, 5, 38.762888, -9.095858, NULL, NOW() - INTERVAL 14 DAY),
(4, 6, 38.771892, -9.092551, 106, NOW() - INTERVAL 12 DAY),
(4, 7, 38.764812, -9.094468, NULL, NOW() - INTERVAL 12 DAY),
-- Inês, rota 3 (concluída)
(5, 8, 38.777252, -9.101867, NULL, NOW() - INTERVAL 11 DAY),
(5, 9, 38.775800, -9.103100, NULL, NOW() - INTERVAL 11 DAY),
(5, 10, 38.776900, -9.102300, 107, NOW() - INTERVAL 9 DAY),
(5, 11, 38.776748, -9.100921, NULL, NOW() - INTERVAL 9 DAY),
-- André, rota 3 (concluída)
(6, 8, 38.777252, -9.101867, NULL, NOW() - INTERVAL 9 DAY),
(6, 9, 38.775800, -9.103100, NULL, NOW() - INTERVAL 9 DAY),
(6, 10, 38.776900, -9.102300, 108, NOW() - INTERVAL 7 DAY),
(6, 11, 38.776748, -9.100921, NULL, NOW() - INTERVAL 7 DAY),
-- Bruno, rota 2 (concluída)
(7, 4, 38.763517, -9.095928, 109, NOW() - INTERVAL 7 DAY),
(7, 5, 38.762888, -9.095858, NULL, NOW() - INTERVAL 7 DAY),
(7, 6, 38.771892, -9.092551, 110, NOW() - INTERVAL 5 DAY),
(7, 7, 38.764812, -9.094468, NULL, NOW() - INTERVAL 5 DAY),
-- Miguel, rota 3 (a meio)
(8, 8, 38.777252, -9.101867, NULL, NOW() - INTERVAL 3 DAY),
(8, 9, 38.775800, -9.103100, NULL, NOW() - INTERVAL 3 DAY),
-- Diogo, rota 4 (a meio)
(9, 12, 38.770209, -9.092078, NULL, NOW() - INTERVAL 2 DAY),
-- Carolina, rota 2 (a meio)
(10, 4, 38.763517, -9.095928, 111, NOW() - INTERVAL 3 DAY);

-- =====================================================
-- 10. CONTAS DE NEGÓCIO & CUPÕES
-- =====================================================
INSERT INTO business_accounts (id, name, email) VALUES
(1, 'Grupo Cantinho', 'cantinho@negocio.pt'),
(2, 'Oceanário de Lisboa', 'bilheteira@oceanario.pt'),
(3, 'Grupo Oriente', 'geral@grupooriente.pt');

INSERT INTO coupons (id, title, description, cost_points, business_account_id, code, active) VALUES
(1, 'Café Grátis', 'Oferta de um café na compra de qualquer bolo.', 100, 1, 'CAFE100', TRUE),
(2, '10% Desconto Almoço', 'Válido para pratos do dia, exceto bebidas.', 250, 1, 'ALMOCO10', TRUE),
(3, '10% Desconto no Oceanário', 'Desconto no bilhete de adulto, válido de segunda a sexta.', 300, 2, 'OCEANO10', TRUE),
(4, 'Visita guiada ao Oceanário', 'Visita guiada de 45 minutos para duas pessoas, com marcação prévia.', 600, 2, 'OCEANOGUIDE', FALSE),
(5, 'Menu degustação no Oriente', 'Menu de degustação de cinco momentos na esplanada.', 300, 3, 'ORIENTE300', TRUE),
(6, 'Teleférico ida e volta', 'Bilhete de ida e volta no Teleférico do Parque das Nações.', 200, 3, 'TELEF200', TRUE);

-- =====================================================
-- 11. REDEMPTIONS (Cupões resgatados)
-- =====================================================
INSERT INTO redemptions (user_id, coupon_id, redeemed_at) VALUES
(1, 2, NOW() - INTERVAL 30 DAY),   -- Tiago -> 10% Desconto Almoço
(2, 1, NOW() - INTERVAL 24 DAY),   -- Ana -> Café Grátis
(6, 6, NOW() - INTERVAL 11 DAY),   -- Beatriz -> Teleférico ida e volta
(11, 3, NOW() - INTERVAL 6 DAY);   -- André -> 10% Desconto no Oceanário

-- =====================================================
-- 12. FAVORITOS (FAVORITES)
-- =====================================================
INSERT INTO favorites (user_id, place_id, created_at) VALUES
(1, 5, NOW() - INTERVAL 50 DAY),
(2, 5, NOW() - INTERVAL 45 DAY),
(2, 6, NOW() - INTERVAL 44 DAY),
(4, 19, NOW() - INTERVAL 20 DAY),
(4, 10, NOW() - INTERVAL 19 DAY),
(5, 14, NOW() - INTERVAL 15 DAY),
(6, 7, NOW() - INTERVAL 13 DAY),
(7, 11, NOW() - INTERVAL 12 DAY),
(8, 5, NOW() - INTERVAL 10 DAY),
(8, 15, NOW() - INTERVAL 10 DAY),
(9, 1, NOW() - INTERVAL 8 DAY),
(10, 16, NOW() - INTERVAL 6 DAY),
(11, 19, NOW() - INTERVAL 6 DAY),
(12, 9, NOW() - INTERVAL 4 DAY),
(13, 6, NOW() - INTERVAL 3 DAY);

-- =====================================================
-- 13. VISITAS (VISITS) – check-in GPS no local, +10 pontos
-- =====================================================
INSERT INTO visits (user_id, place_id, lat, lng, created_at) VALUES
(2, 1, 38.776900, -9.102300, NOW() - INTERVAL 40 DAY),
(2, 6, 38.762888, -9.095858, NOW() - INTERVAL 27 DAY),
(2, 5, 38.763517, -9.095928, NOW() - INTERVAL 25 DAY),
(4, 5, 38.763517, -9.095928, NOW() - INTERVAL 21 DAY),
(4, 10, 38.767943, -9.099732, NOW() - INTERVAL 19 DAY),
(4, 13, 38.768084, -9.095257, NOW() - INTERVAL 18 DAY),
(5, 14, 38.777252, -9.101867, NOW() - INTERVAL 15 DAY),
(5, 2, 38.775800, -9.103100, NOW() - INTERVAL 15 DAY),
(6, 6, 38.762888, -9.095858, NOW() - INTERVAL 14 DAY),
(6, 7, 38.771892, -9.092551, NOW() - INTERVAL 12 DAY),
(7, 12, 38.770209, -9.092078, NOW() - INTERVAL 10 DAY),
(7, 11, 38.779745, -9.088712, NOW() - INTERVAL 9 DAY),
(7, 19, 38.774836, -9.090461, NOW() - INTERVAL 9 DAY),
(8, 13, 38.768084, -9.095257, NOW() - INTERVAL 8 DAY),
(8, 5, 38.763517, -9.095928, NOW() - INTERVAL 7 DAY),
(8, 15, 38.769412, -9.096337, NOW() - INTERVAL 7 DAY),
(9, 1, 38.776900, -9.102300, NOW() - INTERVAL 6 DAY),
(10, 10, 38.767943, -9.099732, NOW() - INTERVAL 5 DAY),
(10, 14, 38.777252, -9.101867, NOW() - INTERVAL 5 DAY),
(10, 16, 38.776748, -9.100921, NOW() - INTERVAL 4 DAY),
(10, 18, 38.773118, -9.113216, NOW() - INTERVAL 4 DAY),
(12, 9, 38.774472, -9.092751, NOW() - INTERVAL 3 DAY),
(12, 20, 38.772688, -9.109529, NOW() - INTERVAL 2 DAY),
(13, 5, 38.763517, -9.095928, NOW() - INTERVAL 5 DAY),
(13, 6, 38.762888, -9.095858, NOW() - INTERVAL 5 DAY),
(13, 7, 38.771892, -9.092551, NOW() - INTERVAL 5 DAY),
(13, 12, 38.770209, -9.092078, NOW() - INTERVAL 4 DAY);

-- =====================================================
-- 14. LEDGER DE PONTOS (Histórico)
-- =====================================================
-- users.points = soma dos amounts de cada utilizador.
-- Formatos: "Visita: <local>", "Pedipaper concluído: <nome>",
--           "Cupão resgatado: <título>" e um bónus de registo/boas-vindas.
INSERT INTO points_ledger (user_id, amount, reason, created_at) VALUES
-- Tiago (250)
(1, 500, 'Bónus de Registo Inicial', NOW() - INTERVAL 120 DAY),
(1, -250, 'Cupão resgatado: 10% Desconto Almoço', NOW() - INTERVAL 30 DAY),
-- Ana (480)
(2, 150, 'Bónus de boas-vindas', NOW() - INTERVAL 90 DAY),
(2, 10, 'Visita: Jardim de Moscavide', NOW() - INTERVAL 40 DAY),
(2, 10, 'Visita: Pavilhão do Conhecimento', NOW() - INTERVAL 27 DAY),
(2, 10, 'Visita: Oceanário de Lisboa', NOW() - INTERVAL 25 DAY),
(2, 400, 'Pedipaper concluído: Parque das Nações em família', NOW() - INTERVAL 25 DAY),
(2, -100, 'Cupão resgatado: Café Grátis', NOW() - INTERVAL 24 DAY),
-- Rita (630)
(4, 100, 'Bónus de Registo Inicial', NOW() - INTERVAL 60 DAY),
(4, 10, 'Visita: Centro Vasco da Gama', NOW() - INTERVAL 21 DAY),
(4, 10, 'Visita: Oceanário de Lisboa', NOW() - INTERVAL 21 DAY),
(4, 10, 'Visita: Gare do Oriente', NOW() - INTERVAL 19 DAY),
(4, 500, 'Pedipaper concluído: À beira Tejo', NOW() - INTERVAL 18 DAY),
-- Miguel (120)
(5, 100, 'Bónus de Registo Inicial', NOW() - INTERVAL 50 DAY),
(5, 10, 'Visita: Mercado de Moscavide', NOW() - INTERVAL 15 DAY),
(5, 10, 'Visita: Igreja de Santo António', NOW() - INTERVAL 15 DAY),
-- Beatriz (320)
(6, 100, 'Bónus de Registo Inicial', NOW() - INTERVAL 45 DAY),
(6, 10, 'Visita: Pavilhão do Conhecimento', NOW() - INTERVAL 14 DAY),
(6, 10, 'Visita: Teleférico do Parque das Nações', NOW() - INTERVAL 12 DAY),
(6, 400, 'Pedipaper concluído: Parque das Nações em família', NOW() - INTERVAL 12 DAY),
(6, -200, 'Cupão resgatado: Teleférico ida e volta', NOW() - INTERVAL 11 DAY),
-- Diogo (130)
(7, 100, 'Bónus de Registo Inicial', NOW() - INTERVAL 40 DAY),
(7, 10, 'Visita: Passeio das Tágides', NOW() - INTERVAL 10 DAY),
(7, 10, 'Visita: Parque Tejo', NOW() - INTERVAL 9 DAY),
(7, 10, 'Visita: Ponte Vasco da Gama', NOW() - INTERVAL 9 DAY),
-- Inês (480)
(8, 100, 'Bónus de Registo Inicial', NOW() - INTERVAL 35 DAY),
(8, 10, 'Visita: Centro Vasco da Gama', NOW() - INTERVAL 8 DAY),
(8, 10, 'Visita: Oceanário de Lisboa', NOW() - INTERVAL 7 DAY),
(8, 10, 'Visita: Restaurante Sabores do Oriente', NOW() - INTERVAL 7 DAY),
(8, 350, 'Pedipaper concluído: Origens de Moscavide', NOW() - INTERVAL 9 DAY),
-- Pedro (110)
(9, 100, 'Bónus de Registo Inicial', NOW() - INTERVAL 30 DAY),
(9, 10, 'Visita: Jardim de Moscavide', NOW() - INTERVAL 6 DAY),
-- Carolina (140)
(10, 100, 'Bónus de Registo Inicial', NOW() - INTERVAL 30 DAY),
(10, 10, 'Visita: Gare do Oriente', NOW() - INTERVAL 5 DAY),
(10, 10, 'Visita: Mercado de Moscavide', NOW() - INTERVAL 5 DAY),
(10, 10, 'Visita: Churrasqueira do Bairro', NOW() - INTERVAL 4 DAY),
(10, 10, 'Visita: Igreja de Nossa Senhora dos Olivais', NOW() - INTERVAL 4 DAY),
-- André (150)
(11, 100, 'Bónus de Registo Inicial', NOW() - INTERVAL 25 DAY),
(11, 350, 'Pedipaper concluído: Origens de Moscavide', NOW() - INTERVAL 7 DAY),
(11, -300, 'Cupão resgatado: 10% Desconto no Oceanário', NOW() - INTERVAL 6 DAY),
-- Sofia (120)
(12, 100, 'Bónus de Registo Inicial', NOW() - INTERVAL 20 DAY),
(12, 10, 'Visita: Torre Vasco da Gama', NOW() - INTERVAL 3 DAY),
(12, 10, 'Visita: Parque do Vale do Silêncio', NOW() - INTERVAL 2 DAY),
-- Bruno (540)
(13, 100, 'Bónus de Registo Inicial', NOW() - INTERVAL 15 DAY),
(13, 10, 'Visita: Oceanário de Lisboa', NOW() - INTERVAL 5 DAY),
(13, 10, 'Visita: Pavilhão do Conhecimento', NOW() - INTERVAL 5 DAY),
(13, 10, 'Visita: Teleférico do Parque das Nações', NOW() - INTERVAL 5 DAY),
(13, 10, 'Visita: Passeio das Tágides', NOW() - INTERVAL 4 DAY),
(13, 400, 'Pedipaper concluído: Parque das Nações em família', NOW() - INTERVAL 5 DAY);
