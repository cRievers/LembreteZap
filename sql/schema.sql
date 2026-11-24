-- Script SQL entregavel - LembreteZap (MySQL 8+)

CREATE DATABASE IF NOT EXISTS lembretezap
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE lembretezap;

CREATE TABLE IF NOT EXISTS compromisso (
    codigo INT AUTO_INCREMENT PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    data_compromisso DATE NOT NULL,
    horario TIME NOT NULL,
    nome_contato VARCHAR(120) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    situacao ENUM('PENDENTE', 'CONCLUIDO') NOT NULL DEFAULT 'PENDENTE'
);

-- Registros iniciais de exemplo:
-- 1) pendente atrasado, 2) pendente futuro, 3) concluido
INSERT INTO compromisso (descricao, data_compromisso, horario, nome_contato, telefone, situacao)
VALUES
    ('Consulta medica de rotina', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:30:00', 'Dra. Mariana Silva', '5531999991111', 'PENDENTE'),
    ('Entrega do trabalho de POO II', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '18:00:00', 'Prof. Daniela Terra', '5531999992222', 'PENDENTE'),
    ('Reuniao de alinhamento do projeto', DATE_SUB(CURDATE(), INTERVAL 5 DAY), '14:00:00', 'Carlos Eduardo', '5531999993333', 'CONCLUIDO');
