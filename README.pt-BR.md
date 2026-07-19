# Cobryn

**Cobryn** é uma plataforma moderna de cobrança recorrente construída com **Java 25** e **Spring Boot 4**.

O objetivo do projeto é simular um sistema real de billing para SaaS, incluindo gerenciamento de clientes, ciclo de vida de assinaturas, geração de faturas, processamento de pagamentos simulados, webhooks, idempotência, jobs agendados de cobrança e notificações assíncronas.

> Este projeto está em desenvolvimento e tem como objetivo demonstrar práticas de engenharia backend usando uma stack Java moderna.

---

## Visão geral

O Cobryn permite que organizações gerenciem cobranças recorrentes para seus produtos SaaS.

Uma empresa pode criar planos, cadastrar clientes, iniciar assinaturas, gerar faturas, processar pagamentos e reagir a eventos de pagamento por meio de webhooks.

O projeto foi desenhado como um **monólito modular**, mantendo o deploy simples enquanto preserva limites claros entre os domínios da aplicação.

---

## Principais funcionalidades

### Escopo atual do MVP

- Registro e autenticação de usuários
- Login com JWT
- Gerenciamento de organização
- Gerenciamento de clientes
- Gerenciamento de planos
- Criação de assinaturas
- Geração de faturas
- Processamento de pagamento simulado
- Gerenciamento de status da assinatura

### Funcionalidades planejadas

- Controle de acesso baseado em papéis
- Isolamento de dados por organização
- Chaves de idempotência para operações de pagamento
- Webhooks simulados de provedor de pagamento
- Geração agendada de faturas
- Cancelamento automático de assinaturas vencidas
- Notificações assíncronas com RabbitMQ
- Cache com Redis
- Testes de integração com Testcontainers
- Documentação da API com OpenAPI/Swagger
- Pipeline de CI com GitHub Actions
- Ambiente local com Docker

---

## Stack utilizada

- **Java 25**
- **Spring Boot 4**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway**
- **Redis**
- **RabbitMQ**
- **Docker Compose**
- **JUnit 5**
- **Testcontainers**
- **OpenAPI/Swagger**
- **GitHub Actions**

---

## Domínio de negócio

O Cobryn modela um sistema simplificado de billing para SaaS.

### Entidades principais

- **Organization**: empresa que usa o Cobryn para gerenciar cobranças.
- **User**: pessoa que pertence a uma organização.
- **Customer**: cliente da organização.
- **Plan**: plano de preço recorrente.
- **Subscription**: assinatura ativa ou inativa de um cliente em um plano.
- **Invoice**: documento de cobrança gerado para uma assinatura.
- **Payment**: tentativa de pagamento relacionada a uma fatura.
- **Webhook Event**: evento externo recebido de um provedor de pagamento.

---

## Ciclo de vida da assinatura

Uma assinatura pode passar pelos seguintes status:

```text
PENDING -> ACTIVE -> PAST_DUE -> CANCELED
```

### Regras

- Uma assinatura começa como `PENDING`.
- Quando a primeira fatura é paga, a assinatura se torna `ACTIVE`.
- Se uma fatura ficar vencida, a assinatura se torna `PAST_DUE`.
- Se a fatura continuar sem pagamento após o período de tolerância, a assinatura se torna `CANCELED`.
- Uma assinatura cancelada não pode ser reativada diretamente.

---

## Ciclo de vida da fatura

Uma fatura pode passar pelos seguintes status:

```text
OPEN -> PAID
OPEN -> OVERDUE
OPEN -> CANCELED
```

### Regras

- Uma fatura é criada como `OPEN`.
- Um pagamento bem-sucedido altera o status da fatura para `PAID`.
- Uma fatura não paga após a data de vencimento se torna `OVERDUE`.
- Uma assinatura cancelada pode cancelar todas as faturas abertas relacionadas a ela.

---

## Fluxo de pagamento

O Cobryn usa um processador de pagamento simulado para representar o comportamento de um pagamento real.

Exemplo de fluxo:

```text
1. A organização cria um plano
2. A organização cadastra um cliente
3. O cliente inicia uma assinatura
4. O Cobryn gera uma fatura
5. O pagamento é processado pelo provedor de pagamento simulado
6. A fatura é marcada como PAID
7. A assinatura se torna ACTIVE
8. Um evento de notificação é publicado
```

---

## Visão geral da API

### Autenticação

```http
POST /api/auth/register
POST /api/auth/login
```

### Organizações

```http
GET /api/organizations/me
PATCH /api/organizations/me
```

### Clientes

```http
POST /api/customers
GET /api/customers
GET /api/customers/{id}
PATCH /api/customers/{id}
DELETE /api/customers/{id}
```

### Planos

```http
POST /api/plans
GET /api/plans
GET /api/plans/{id}
PATCH /api/plans/{id}
DELETE /api/plans/{id}
```

### Assinaturas

```http
POST /api/subscriptions
GET /api/subscriptions
GET /api/subscriptions/{id}
POST /api/subscriptions/{id}/cancel
```

### Faturas

```http
GET /api/invoices
GET /api/invoices/{id}
POST /api/invoices/{id}/pay
```

### Webhooks

```http
POST /api/webhooks/payment-provider
```

---

## Estrutura do projeto

```text
src/main/java/com/cobryn

├── auth
├── organizations
├── users
├── customers
├── plans
├── subscriptions
├── invoices
├── payments
├── webhooks
├── notifications
└── shared
    ├── config
    ├── exceptions
    ├── security
    ├── pagination
    └── events
```

---

## Arquitetura

O Cobryn segue uma arquitetura de **monólito modular**.

Cada módulo é responsável pela sua própria lógica de domínio e se comunica com outros módulos por meio de serviços de aplicação e eventos de domínio quando apropriado.

O principal objetivo é evitar a complexidade de um sistema distribuído cedo demais, mantendo o código preparado para uma possível extração futura em serviços separados.

### Decisões arquiteturais iniciais

- Usar monólito modular em vez de microsserviços.
- Manter regras de negócio nas camadas de serviço/domínio.
- Usar DTOs para entrada e saída da API.
- Evitar expor entidades JPA diretamente nos controllers.
- Usar Flyway para migrações do banco de dados.
- Usar Testcontainers para testes de integração.
- Usar eventos de domínio para fluxos de billing e notificações.

---

## Desenvolvimento local

### Requisitos

- Java 25
- Docker
- Docker Compose
- Maven

### Subindo a infraestrutura

```bash
docker compose up -d
```

### Rodando a aplicação

```bash
./mvnw spring-boot:run
```

### Rodando os testes

```bash
./mvnw test
```

---

## Variáveis de ambiente

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/cobryn
DATABASE_USERNAME=cobryn
DATABASE_PASSWORD=cobryn

JWT_SECRET=change-me
JWT_EXPIRATION_MINUTES=60

REDIS_HOST=localhost
REDIS_PORT=6379

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
```

---

## Estratégia de testes

O Cobryn tem como objetivo incluir:

- Testes unitários para regras de negócio
- Testes de integração para persistência e fluxos principais
- Testes de controller para comportamento da API
- Testes de segurança para rotas protegidas
- Testes com Testcontainers para funcionalidades dependentes do PostgreSQL

Exemplos de áreas de teste:

```text
- Criar uma assinatura gera a primeira fatura
- Pagar uma fatura ativa a assinatura
- Requisições duplicadas de pagamento não criam pagamentos duplicados
- Usuários não podem acessar recursos de outra organização
- Faturas vencidas alteram o status da assinatura
```

---

## Roadmap

### Fase 1 — MVP principal

- [x] Configuração do projeto com Java 25 e Spring Boot 4
- [x] Configuração do PostgreSQL e Flyway
- [ ] Autenticação com JWT
- [ ] Modelo de organização e usuário
- [ ] CRUD de clientes
- [ ] CRUD de planos
- [ ] Criação de assinaturas
- [ ] Geração de faturas
- [ ] Processamento de pagamento simulado

### Fase 2 — Comportamento real de billing

- [ ] Regras de ciclo de vida da assinatura
- [ ] Transições de status da fatura
- [ ] Simulação de falha de pagamento
- [ ] Chaves de idempotência
- [ ] Processamento de webhooks
- [ ] Jobs agendados de cobrança

### Fase 3 — Funcionalidades com cara de produção

- [ ] Controle de acesso baseado em papéis
- [ ] Testes de isolamento por organização
- [ ] Eventos de notificação com RabbitMQ
- [ ] Cache com Redis
- [ ] Documentação com OpenAPI
- [ ] Ambiente com Docker Compose
- [ ] Pipeline com GitHub Actions

### Fase 4 — Polimento

- [ ] Melhorar respostas de erro
- [ ] Melhorar validações de requisição
- [ ] Adicionar exemplos de uso da API
- [ ] Adicionar collection do Postman ou Insomnia
- [ ] Adicionar guia de deploy
- [ ] Adicionar diagrama de arquitetura

---

## Objetivos

Este projeto foi criado para praticar e demonstrar:

- Arquitetura backend com Java e Spring Boot
- Regras de negócio reais
- Design seguro de APIs
- Modelagem de banco de dados
- Controle transacional
- Processamento assíncrono
- Testes com infraestrutura real
- Documentação clara de projeto
- Práticas backend orientadas a produção

---

## Licença

Este projeto está licenciado sob a licença MIT.
