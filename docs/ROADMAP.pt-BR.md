# Roadmap do Cobryn

Este roadmap define as fases iniciais de desenvolvimento do Cobryn.

O objetivo é construir o projeto de forma incremental, começando por uma base backend sólida e evoluindo para uma plataforma de billing com características próximas de produção.

---

## Fase 1 — Fundação do projeto

Objetivo: criar a estrutura base da aplicação.

- [ ] Inicializar projeto com Spring Boot 4
- [ ] Configurar Java 25
- [ ] Configurar Maven
- [ ] Adicionar PostgreSQL com Docker Compose
- [ ] Adicionar Flyway
- [ ] Adicionar tratamento global de exceções
- [ ] Adicionar validação de requisições
- [ ] Adicionar endpoint básico de health check
- [ ] Adicionar documentação inicial no README

Resultado esperado:

```text
A aplicação roda localmente e se conecta ao PostgreSQL.
```

---

## Fase 2 — Autenticação e tenancy

Objetivo: dar suporte a usuários autenticados e propriedade de dados baseada em organização.

- [ ] Criar tabela de usuários
- [ ] Criar tabela de organizações
- [ ] Implementar registro de usuário
- [ ] Implementar login
- [ ] Gerar tokens JWT
- [ ] Proteger endpoints privados
- [ ] Associar usuário autenticado às requisições
- [ ] Garantir que usuários pertençam a organizações

Resultado esperado:

```text
Usuários conseguem se registrar, fazer login e acessar endpoints protegidos.
```

---

## Fase 3 — Recursos principais de billing

Objetivo: criar as principais entidades de cobrança.

- [ ] Criar módulo de clientes
- [ ] Criar módulo de planos
- [ ] Criar módulo de assinaturas
- [ ] Criar módulo de faturas
- [ ] Adicionar operações CRUD para clientes
- [ ] Adicionar operações CRUD para planos
- [ ] Permitir criação de assinaturas
- [ ] Gerar a primeira fatura quando uma assinatura for criada

Resultado esperado:

```text
Uma organização consegue criar clientes, planos, assinaturas e faturas.
```

---

## Fase 4 — Fluxo de pagamento

Objetivo: simular processamento de pagamento.

- [ ] Criar módulo de pagamentos
- [ ] Adicionar provedor de pagamento simulado
- [ ] Permitir pagamento de uma fatura
- [ ] Marcar fatura como paga após pagamento bem-sucedido
- [ ] Ativar assinatura após o primeiro pagamento bem-sucedido
- [ ] Simular falha de pagamento
- [ ] Armazenar tentativas de pagamento

Resultado esperado:

```text
Faturas podem ser pagas por meio de um provedor de pagamento simulado.
```

---

## Fase 5 — Idempotência e webhooks

Objetivo: tornar as operações de pagamento mais seguras e realistas.

- [ ] Adicionar suporte a `Idempotency-Key`
- [ ] Prevenir processamento duplicado de pagamentos
- [ ] Criar endpoint de webhook
- [ ] Simular eventos do provedor de pagamento
- [ ] Armazenar eventos de webhook processados
- [ ] Prevenir processamento duplicado de webhooks

Resultado esperado:

```text
Operações de pagamento e eventos de webhook ficam protegidos contra requisições duplicadas.
```

---

## Fase 6 — Jobs agendados de cobrança

Objetivo: dar suporte ao comportamento de cobrança recorrente.

- [ ] Gerar faturas recorrentes
- [ ] Detectar faturas vencidas
- [ ] Mover assinaturas para PAST_DUE
- [ ] Cancelar assinaturas após o período de tolerância
- [ ] Adicionar jobs agendados
- [ ] Adicionar testes para transições do ciclo de vida da assinatura

Resultado esperado:

```text
O Cobryn consegue gerenciar geração recorrente de faturas e assinaturas vencidas.
```

---

## Fase 7 — Notificações assíncronas

Objetivo: adicionar comportamento assíncrono orientado a eventos.

- [ ] Adicionar RabbitMQ ao Docker Compose
- [ ] Publicar evento quando uma fatura for paga
- [ ] Publicar evento quando um pagamento falhar
- [ ] Publicar evento quando uma assinatura for cancelada
- [ ] Consumir eventos no módulo de notificações
- [ ] Simular envio de e-mails

Resultado esperado:

```text
Eventos de billing disparam notificações assíncronas.
```

---

## Fase 8 — Testes e qualidade

Objetivo: tornar o projeto confiável e pronto para portfólio.

- [ ] Adicionar testes unitários para regras de domínio
- [ ] Adicionar testes de integração com Testcontainers
- [ ] Adicionar testes de controller
- [ ] Adicionar testes de segurança
- [ ] Adicionar testes de isolamento por organização
- [ ] Adicionar pipeline com GitHub Actions
- [ ] Adicionar badge de cobertura de testes, se desejado

Resultado esperado:

```text
O projeto possui testes automatizados e validação em CI.
```

---

## Fase 9 — Documentação e polimento

Objetivo: tornar o projeto fácil de entender e apresentar.

- [ ] Adicionar OpenAPI/Swagger
- [ ] Adicionar exemplos de uso da API
- [ ] Adicionar collection do Postman ou Insomnia
- [ ] Adicionar diagrama de arquitetura
- [ ] Adicionar diagrama do banco de dados
- [ ] Adicionar guia de setup local
- [ ] Adicionar guia de deploy
- [ ] Melhorar README com screenshots

Resultado esperado:

```text
O repositório fica profissional e fácil de avaliar.
```
