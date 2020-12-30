CREATE TABLE pessoa (
	codigo BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
	nome VARCHAR(255) NOT NULL,
	ativo BOOLEAN NOT NULL,
    endereco_logradouro VARCHAR(255),
    endereco_numero VARCHAR(30),
    endereco_complemento  VARCHAR(30),
    endereco_bairro VARCHAR(255),
    endereco_cep VARCHAR(30),
    endereco_cidade varchar(255),
    endereco_estado VARCHAR(30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO pessoa (nome, endereco_logradouro, endereco_numero, endereco_complemento , endereco_bairro, endereco_cep, endereco_cidade, endereco_estado, ativo) values ('João Silva', 'Rua do Abacaxi', '10', null, 'Brasil', '38.400-121', 'Uberlândia', 'MG', true);
INSERT INTO pessoa (nome, endereco_logradouro, endereco_numero, endereco_complemento , endereco_bairro, endereco_cep, endereco_cidade, endereco_estado, ativo) values ('Maria Rita', 'Rua do Sabiá', '110', 'Apto 101', 'Colina', '11.400-121', 'Ribeirão Preto', 'SP', true);
INSERT INTO pessoa (nome, endereco_logradouro, endereco_numero, endereco_complemento , endereco_bairro, endereco_cep, endereco_cidade, endereco_estado, ativo) values ('Pedro Santos', 'Rua da Bateria', '23', null, 'Morumbi', '54.212-121', 'Goiânia', 'GO', true);
INSERT INTO pessoa (nome, endereco_logradouro, endereco_numero, endereco_complemento , endereco_bairro, endereco_cep, endereco_cidade, endereco_estado, ativo) values ('Ricardo Pereira', 'Rua do Motorista', '123', 'Apto 302', 'Aparecida', '38.400-121', 'Salvador', 'BA', true);
INSERT INTO pessoa (nome, endereco_logradouro, endereco_numero, endereco_complemento , endereco_bairro, endereco_cep, endereco_cidade, endereco_estado, ativo) values ('Josué Mariano', 'Av Rio Branco', '321', null, 'Jardins', '56.400-121', 'Natal', 'RN', true);
INSERT INTO pessoa (nome, endereco_logradouro, endereco_numero, endereco_complemento , endereco_bairro, endereco_cep, endereco_cidade, endereco_estado, ativo) values ('Pedro Barbosa', 'Av Brasil', '100', null, 'Tubalina', '77.400-121', 'Porto Alegre', 'RS', true);
INSERT INTO pessoa (nome, endereco_logradouro, endereco_numero, endereco_complemento , endereco_bairro, endereco_cep, endereco_cidade, endereco_estado, ativo) values ('Henrique Medeiros', 'Rua do Sapo', '1120', 'Apto 201', 'Centro', '12.400-121', 'Rio de Janeiro', 'RJ', true);
INSERT INTO pessoa (nome, endereco_logradouro, endereco_numero, endereco_complemento , endereco_bairro, endereco_cep, endereco_cidade, endereco_estado, ativo) values ('Carlos Santana', 'Rua da Manga', '433', null, 'Centro', '31.400-121', 'Belo Horizonte', 'MG', true);
INSERT INTO pessoa (nome, endereco_logradouro, endereco_numero, endereco_complemento , endereco_bairro, endereco_cep, endereco_cidade, endereco_estado, ativo) values ('Leonardo Oliveira', 'Rua do Músico', '566', null, 'Segismundo Pereira', '38.400-000', 'Uberlândia', 'MG', true);
INSERT INTO pessoa (nome, endereco_logradouro, endereco_numero, endereco_complemento , endereco_bairro, endereco_cep, endereco_cidade, endereco_estado, ativo) values ('Isabela Martins', 'Rua da Terra', '1233', 'Apto 10', 'Vigilato', '99.400-121', 'Manaus', 'AM', true);