UFPR - TADS Trabalho de DS152 – DAC 

# Sistema de Gestão Hospitalar – 2025-1 

Objetivo 

Desenvolver um sistema de Gestão Hospitalar com arquitetura de 

microsserviços, autenticação JWT, comunicação via API Gateway, persistência 

com PostgreSQL e entrega dockerizada. O escopo foi reduzido para viabilizar sua 

execução em 8 semanas, mantendo os fundamentos essenciais para aplicações 

corporativas distribuídas. 

Perfis de Acesso 

 Paciente : acessa consultas, agendamentos e histórico de pontos. 

 Funcionário : cadastra consultas, confirma presença e realiza 

procedimentos. 

Requisitos Funcionais 

O que são Requisitos Funcionais? 

Requisitos funcionais são descrições detalhadas das funcionalidades que o 

sistema deve oferecer para atender às necessidades dos usuários. Eles 

especificam o que o sistema deve fazer, incluindo as operações, 

comportamentos e respostas esperadas diante de determinadas ações dos 

usuários. No contexto deste projeto, os requisitos funcionais indicam as 

funcionalidades essenciais para pacientes e funcionários dentro do Sistema de 

Gestão Hospitalar, como cadastro, agendamento, cancelamento de consultas, 

entre outros. 

 R01: Autocadastro de Paciente  – Cadastro com CPF, nome, e-mail, CEP, 

senha (4 dígitos aleatórios via e-mail). Endereço preenchido via ViaCEP. 

Início com 0 pontos. o Permite que pacientes se cadastrem no sistema informando CPF, 

nome, e-mail e CEP. O endereço completo é preenchido 

automaticamente por meio da API ViaCEP. O paciente inicia com 0 

pontos e recebe por e-mail uma senha numérica gerada 

aleatoriamente. 

 R02: Login/Logout  – Autenticação via e-mail e senha com token JWT. 

o Usuários acessam o sistema com seu e-mail e senha. Após 

validação, é gerado um token JWT que será usado para autenticação 

nas demais requisições. Todas as funcionalidades subsequentes 

exigem autenticação válida. 

Perfil Paciente 

 R03: Tela Inicial do Paciente  – Menu com saldo de pontos e

agendamentos (futuros, realizados, cancelados). 

o Apresenta um menu com as opções disponíveis para o paciente. 

Exibe também o saldo atual de pontos e uma tabela com os 

agendamentos realizados, futuros ou cancelados. Permite 

visualização rápida e organizada do histórico de atendimentos. 

 R04: Compra de Pontos  – 1 ponto = R$ 5,00. Transações com registro de 

data/hora e descrição "COMPRA DE PONTOS". 

o O paciente pode adquirir pontos para obter descontos em 

consultas. Cada ponto custa R$ 5,00. As compras são registradas em 

um histórico de transações, com data/hora, valor em reais, 

quantidade de pontos e a descrição “COMPRA DE PONTOS”. 

 R05: Agendar Consulta  – Busca por especialidade e médico. Seleciona 

consulta, usa pontos para desconto, confirma pagamento. Código único 

de agendamento gerado. Status: CRIADO. 

o O paciente busca consultas disponíveis por especialidade e/ou 

profissional. Ao selecionar uma consulta, define quantos pontos 

usará para obter desconto, e o restante é pago em dinheiro. Após 

confirmação, o sistema registra o agendamento com um código 

único e status CRIADO. 

 R06: Cancelar Agendamento  – Permitido apenas se CRIADO ou CHECK-IN. 

Pontos devolvidos com histórico registrado. 

o Permite ao paciente cancelar agendamentos com status CRIADO ou 

CHECK-IN. Os pontos utilizados são devolvidos ao saldo, e ohistórico registra a transação com a descrição “CANCELAMENTO DE 

AGENDAMENTO”. 

 R07: Check-in  – Disponível para consultas nas próximas 48h. Status muda 

para CHECK-IN. 

o Nas 48 horas anteriores à consulta, o paciente pode realizar o check-

in, indicando que comparecerá. Isso atualiza o status do 

agendamento para CHECK-IN, permitindo que o funcionário 

confirme sua presença posteriormente. 

Perfil Funcionário 

 R08: Tela Inicial do Funcionário  – Lista consultas nas próximas 48h com 

botões para confirmar presença (R09), cancelar (R10) ou realizar (R11). 

o Mostra as consultas que ocorrerão nas próximas 48 horas, com 

ações disponíveis para cada uma: confirmar comparecimento, 

cancelar ou registrar como realizada. 

 R09: Confirmar Comparecimento  – Mediante código e status CHECK-IN. 

Altera para COMPARECEU. 

o O funcionário digita o código do agendamento para confirmar a 

presença do paciente. O agendamento deve estar no estado CHECK-

IN. Após confirmação, o status é alterado para COMPARECEU. 

 R10: Cancelar Consulta  – Só possível se menos de 50% dos agendamentos 

estiverem confirmados. Status: CANCELADA. Agendamentos vinculados 

também são cancelados. 

o Permite ao funcionário cancelar uma consulta caso menos de 50% 

das vagas estejam ocupadas. Isso atualiza o status da consulta e de 

todos os agendamentos vinculados para CANCELADO, e os pontos 

são devolvidos aos pacientes. 

 R11: Realizar Consulta  – Marca consulta como REALIZADA. Quem não 

compareceu recebe status FALTOU. 

o Ao término da consulta, o funcionário marca a consulta como 

REALIZADA. Agendamentos no estado COMPARECEU passam a

REALIZADO; os demais (sem check-in ou confirmação) são marcados 

como FALTOU. 

 R12: Cadastro de Consulta  – Data/hora, especialidade, médico, valor, 

vagas. Código gerado automaticamente. Status: DISPONÍVEL. o O funcionário pode cadastrar novas consultas, informando 

data/hora, médico responsável, especialidade, valor da consulta e 

número de vagas. O sistema gera um código único para cada 

consulta, que inicia com o status DISPONÍVEL. 

 R13-R15: CRUD de Funcionário  – Inserção, alteração (exceto CPF) e 

inativação (não exclusão). 

o Permite o cadastro de novos funcionários, com nome, CPF, e-mail e 

telefone. A senha é enviada por e-mail, gerada automaticamente. O 

CPF é único e o e-mail será utilizado para login. 

o Permite atualizar os dados do funcionário, com exceção do CPF. 

Essa funcionalidade facilita a manutenção das informações sem 

perder a identificação original. 

o Ao excluir um funcionário, seus dados não são apagados do 

sistema, apenas marcados como inativos. Isso preserva o histórico 

de operações realizadas pelo profissional. 

O que são Requisitos Não Funcionais? 

Requisitos não funcionais definem as qualidades, restrições e padrões técnicos 

que o sistema deve cumprir, independentemente das funcionalidades 

específicas. Eles garantem que o sistema seja eficiente, seguro, acessível, 

compatível e mantenha um bom desempenho sob diferentes condições de uso. 

Requisitos Não Funcionais do Projeto 

Toda e qualquer suposição, que não esteja definida aqui e que a equipe 

faça, deve ser devidamente documentada e entregue em um arquivo .pdf 

que acompanha o trabalho. 

1.  Autenticação segura:  o sistema deve utilizar autenticação JWT e

armazenar senhas criptografadas com SHA256 + salt. 

2.  Separação por microsserviços:  cada domínio (usuário, paciente, 

consulta) deve estar implementado em seu próprio microsserviço com 

banco de dados separado. 

3.  API RESTful:  todos os endpoints devem seguir o padrão REST, no mínimo 

o Nível 2 do Modelo de Maturidade de Richardson. 

4.  Interface moderna e responsiva:  o front-end deve ser construído com 

Angular e utilizar um framework visual (Bootstrap). 5.  Validação de dados:  todos os campos devem ser validados no front-end 

e, quando necessário, no back-end (por exemplo: CPF, CEP, e-mail, senha). 

6.  Máscaras e formatações:  campos como CPF, CEP, telefone, data e valor 

devem utilizar máscaras e seguir o padrão brasileiro. 

7.  Formato brasileiro de datas e valores:  datas devem seguir o padrão 

dd/mm/aaaa e valores monetários o padrão R$ 1.000,00. 

8.  Dockerização:  todos os serviços, incluindo bancos de dados e API 

Gateway, devem ser empacotados e executados em contêineres 

separados. 

9.  Integração por API Gateway:  o front-end deve se comunicar 

exclusivamente com o API Gateway, e este deve rotear requisições aos 

microsserviços corretos. 

10. Tráfego de dados com DTOs:  o sistema não deve trafegar objetos de 

entidades persistentes; apenas objetos DTO devem ser usados nas 

requisições/respostas. 

11. Execução automatizada:  o projeto deve incluir um script (como docker-

compose) para subir todos os serviços automaticamente. 

12. Documentação de suposições:  qualquer suposição feita pela equipe, 

caso algo não esteja especificado, deve ser documentada e anexada à 

entrega. 

Estrutura por Microsserviço 

1. API Gateway 

O API Gateway é o ponto de entrada para todas as requisições feitas ao sistema. 

Em vez de o front-end se comunicar diretamente com cada microsserviço, ele 

envia todas as requisições ao Gateway, que redireciona essas chamadas ao 

serviço apropriado. Isso centraliza o controle de acesso, autenticação e

roteamento. 

Principais funções: 

 Roteamento inteligente:  encaminha as requisições HTTP para o

microsserviço correspondente com base na URL. 

 Autenticação com JWT:  valida o token JWT enviado no cabeçalho da 

requisição (Authorization: Bearer ...) para permitir ou negar o acesso.  Middleware de segurança:  protege rotas, verifica o tipo de usuário 

(paciente ou funcionário) e impede acesso indevido. 

 Abstração da arquitetura interna:  o front-end não precisa conhecer a 

estrutura dos microsserviços — apenas interage com uma única API. 

2. MS Autenticação 

O Microsserviço de Autenticação é responsável pela criação de contas e

autenticação de usuários. Ele fornece os mecanismos necessários para login 

seguro e emissão de tokens JWT para sessões válidas. 

Principais responsabilidades: 

 Cadastro de usuário:  armazena nome, CPF, e-mail, senha (criptografada) 

e tipo de usuário (PACIENTE ou FUNCIONÁRIO). 

 Login com JWT:  valida credenciais e gera token com as informações do 

usuário. 

 Persistência no PostgreSQL:  armazena os dados em um banco 

específico, sem compartilhamento com outros serviços. 

 Envio de senha inicial:  no caso de autocadastro, uma senha numérica 

aleatória é enviada ao e-mail do usuário. 

3. MS Paciente 

O Microsserviço de Paciente trata dos dados cadastrais dos pacientes e do 

sistema de pontos, utilizado como benefício na compra de consultas. 

Principais responsabilidades: 

 Dados pessoais:  CPF, nome, endereço completo, e-mail e saldo de pontos. 

 Compra de pontos:  registra cada compra de pontos feita pelo paciente 

com valor em R$ e quantidade de pontos adquiridos. 

 Histórico de transações:  mantém um extrato com todas as 

movimentações de pontos (entrada/saída), incluindo origem (compra, uso 

em consulta, cancelamento). 

 Consulta de saldo:  exibe saldo atual e histórico tabular com descrição, 

tipo (ENTRADA/SAÍDA), data e valor. 4. MS Consulta/Agendamento 

Esse Microsserviço é o núcleo funcional do sistema, responsável por permitir o 

cadastro, busca e gerenciamento das consultas oferecidas e das interações dos 

pacientes com elas. 

Principais responsabilidades: 

 Cadastro de consulta:  permite que funcionários registrem novas 

consultas informando especialidade, médico, data/hora, valor e número 

de vagas. A consulta começa no estado DISPONÍVEL. 

 Busca por consulta:  pacientes podem filtrar por especialidade ou médico 

e visualizar as opções futuras. 

 Agendamento:  o paciente agenda uma consulta, utilizando pontos e, se 

necessário, complementando com pagamento em dinheiro. O

agendamento é salvo com status CRIADO e recebe um código único. 

 Check-in do paciente:  se a consulta ocorrer em até 48h, o paciente pode 

registrar sua presença com antecedência (CHECK-IN). 

 Confirmar comparecimento:  o funcionário confirma o comparecimento 

do paciente, mudando o estado para COMPARECEU. 

 Realização da consulta:  ao final, o funcionário registra a consulta como 

REALIZADA e atualiza o status dos pacientes para REALIZADO ou 

FALTOU. 

 Cancelamento:  o paciente pode cancelar o agendamento (com retorno 

dos pontos), ou o funcionário pode cancelar a consulta caso não haja ao 

menos 50% das vagas preenchidas. 

Tecnologias e Padrões 

 API RESTful (Richardson Nível 2) 

 API Gateway com Node.js 

 Back-end com Spring Boot (Java/Kotlin) 

 PostgreSQL para todos os MS 

 Front-end com Angular 13+ / React / Vue 

 JWT para autenticação 

 DTOs para tráfego de dados 

 Interface com Bootstrap/Tailwind/Material 

 Containers Docker para cada MS, banco e gateway  Máscaras, validações e padrões brasileiros (datas, valores) 

Principais Padrões Arquiteturais 

1. Microsserviços (Microservices Architecture) 

Cada domínio do sistema é separado em serviços independentes (ex: 

Autenticação, Paciente, Consulta), que se comunicam entre si via HTTP. Isso 

favorece manutenção, escalabilidade e separação de responsabilidades. 

2. API Gateway 

Um ponto único de entrada (Node.js) recebe todas as requisições do front-end e 

encaminha para o microsserviço apropriado. Permite centralizar autenticação, 

controle de rotas e segurança. 

3. RESTful APIs (Modelo de Maturidade de Richardson - Nível 2) 

Todas as comunicações seguem o padrão REST, com uso adequado de verbos 

HTTP (GET, POST, PUT, DELETE) e URIs representando recursos. As respostas 

utilizam JSON como formato de dados. 

4. JWT (JSON Web Token) 

Os usuários se autenticam e recebem um token assinado digitalmente. Esse 

token é incluído nas requisições subsequentes para garantir acesso autorizado 

às funcionalidades protegidas. 

5. DTO (Data Transfer Object) 

Os objetos trafegados entre front-end e back-end são instâncias simples 

contendo apenas os dados necessários, evitando exposição de entidades 

persistentes diretamente (boas práticas de segurança e manutenibilidade). 

6. Database per Service 

Cada microsserviço possui seu próprio banco de dados PostgreSQL, garantindo 

autonomia, isolamento de dados e consistência dentro do escopo do serviço. 

7. Docker 

Cada componente do sistema é empacotado em um contêiner Docker separado. 

Isso garante portabilidade e padronização do ambiente entre desenvolvimento 

e apresentação. Dados Pré-Cadastrados 

Os seguintes dados devem estar pré-cadastrados na sua base de dados, com 

estes dados em específico (o nome das colunas e localização pode variar 

conforme sua modelagem de dados): 

1.  MS Autenticação (Usuários iniciais) 

Campo  Valor exemplo 

Nome  Funcionário Padrão 

CPF  90769281001 

E-mail  func_pre@hospital.com 

Senha  TADS (criptografada via SHA256+salt) 

Tipo  FUNCIONÁRIO 

Este usuário pode ser usado para realizar os primeiros cadastros de consulta e 

testar o sistema administrativo. 

2.  MS Paciente (Sem necessidade de pré-cadastro, já que pacientes fazem 

autocadastro. Contudo, é possível incluir um exemplo para testes.) 

Campo  Valor exemplo 

Nome  Maria da Silva 

CPF  12345678900 

E-mail  maria.silva@teste.com 

Endereço  Rua A, 123 - Centro - Curitiba 

CEP  80000-000 

Pontos  100 

3.  MS Consulta/Agendamento 

Especialidades Médicas 

Código  Nome CARD  Cardiologia 

DERM  Dermatologia 

PED  Pediatria 

GINE  Ginecologia 

ORTO  Ortopedia 

Consultas disponíveis 

Código 

Consulta  Data/Hora  Especiali 

dade  Médico  Valor 

(R$)  Vagas 

CON001  2025-08-10 10:30  CARD  Dr. Paulo  300,00  5

CON002  2025-09-11 09:30  PED  Dra. Lúcia  250,00  4

CON003  2025-10-12 08:30  DERM  Dr. Carlos  200,00  3

Esses dados possibilitam que os estudantes testem rapidamente o processo de 

agendamento, cancelamento, check-in e realização. 

Funcionários (MS Consulta - gerenciamento) 

Campo  Valor exemplo 

Nome  Dr. Paulo Cardoso 

CPF  23456789012 

E-mail  dr.paulo@hospital.com 

Telefone  (41) 99999-0001 

Status  ATIVO 

Embora os dados de login do funcionário estejam no MS Autenticação, aqui 

ficam seus dados operacionais. 

Roteiro de Desenvolvimento (8 semanas) 

Semana 1: Ambiente e Planejamento 

 Leitura do projeto, divisão da equipe  Instalação de ferramentas e repositório Git 

 Docker + PostgreSQL básico 

Semana 2: API Gateway + MS Autenticação 

 JWT, criptografia de senhas, validação 

 Testes com Postman 

Semana 3: Front-end – login, cadastro, integração 

 Integração via Gateway 

 Máscaras, formulário, requisições HTTP 

Semana 4: MS Paciente + Extrato de Pontos 

 Registro de compra de pontos 

 Consulta de extrato e saldo 

Semana 5: MS Consulta + Cadastro (funcionário) 

 Consulta por especialidade, CRUD e filtros 

Semana 6: Agendamento + Cancelamento + Check-in 

 Seleção de consulta, uso de pontos, check-in 

Semana 7: Fluxo final (funcionário) 

 Confirmação, cancelamento, realização da consulta 

 Atualização de status dos agendamentos 

Semana 8: Integração, testes, dockerização 

 Docker Compose, vídeos, finalização visual e técnica NORMAS PARA DEFESA 

Diretrizes: 

 A defesa deve demorar uns 20 min por equipe. 

 Vocês devem trazer suas máquinas para rodar a aplicação. 

 No momento da defesa tudo deve estar no ar: Front, Back, BD, Containers, 

etc.  No momento da defesa o projeto de teste deve estar instalado e será 

executado na hora 

 Não serão aceitos projetos sem integração Front x Back, ou rodando com 

LocalStorage/json-server. 

 Não serão aceitos projetos "rodando" somente no Postman. 

 Não serão aceitos projetos sem a implementação dos microsserviços 

solicitados. 

REQUISITOS PARA ENTREGA/DEFESA (Sem isso não há defesa) 

 Aplicação de Teste instalada e executando. Ela será executada na hora da 

defesa; 

 Front-end implementado em Angular/React/Vue+Typescript e back-end 

em Spring Boot (Java ou Kotlin); 

 Sistemas usando arquitetura de microsserviços; 

 Front-end acessando somente o API Gateway via HTTP-REST; 

 Não usar Local Storage nem json-server para armazenar as informações 

do sistema; 

 Usar banco de dados distintos por microsserviço (ou schema-per-service); 

 Todos os requisitos implementados corretamente e de forma completa 

(arquitetura de MS solicitada etc.); 

 API Gateway básico implementado; 

 Sistemas devem possuir interface muito bem elaborada. (Não será 

permitida a entrega de sistemas em HTML puro ou com interface ruim). 

O QUE DEVE SER ENTREGUE 

Deve ser entregue em arquivo ZIP: 

 Todos os fontes do projeto; 

 Scripts de inicialização do banco de dados (criação e inserções); 

 Scripts para construção das imagens e execução do projeto; 

 Link para UM vídeo no Youtube (não listado) onde são mostrados os 

requisitos funcionais. 

!!!!! Cuidado para remover arquivos inúteis (executáveis, bibliotecas, 

diretório node_modules) antes da compactação <= VOU DESCONTAR DE 

QUEM ENTREGAR NODE_MODULES e ARQUIVOS COMPILADOS) SOBRE O VÍDEO COM OS REQUISITOS FUNCIONAIS 

 Link para um Vídeo contendo a apresentação de todos os requisitos 

funcionais implementados. 

 No vídeo deve aparecer - de forma clara - a identificação do requisito 

(Número e nome, conforme a especificação do trabalho) que está sendo 

testado e o teste efetivo de todos os aspectos do requisito. 

 O vídeo deve ter, no máximo, 20 minutos de duração. 

 Não há necessidade de todos os integrantes da equipe participarem do 

vídeo. 

 Só devem ser mostrados os requisitos funcionais que estão 

implementados integralmente (front-end e back-end). 

Mantenha o banco de dados aberto para mostrar que o requisito 

funcionou, como uma evidência do teste. 

SOBRE A DEFESA DOS NÃO-FUNCIONAIS 

 Defesa dos requisitos não-funcionais. 

 O sistema deve estar funcionando, todos os contêineres carregados. 

 Todos os fontes devem estar disponibilizados, bem como banco de dados 

e scripts. 

 O projeto de Testes deve estar instalado e funcionando. 

 Ele será executado na hora da defesa. 

 A nota será individual, por aluno, que deverá responder aos 

questionamentos do professor, bem como demonstrar fluência no 

código para explicá-lo, alterá-lo ou criar funcionalidades novas, no 

momento da defesa. 

 Cada aluno deve: 

o Explicar trechos do código 

o Justificar decisões técnicas 

o Realizar ajustes se solicitado Não serão aceitos: 

 Uso de json-server, LocalStorage ou sistemas com dados estáticos 

 Projetos rodando apenas no Postman 

 Sistemas sem integração completa entre front-end e microsserviços 

 Projetos sem autenticação JWT funcional
