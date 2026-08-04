# Cobryn

**Cobryn** é uma plataforma de cobrança recorrente construída com **Java 25** e **Spring Boot 4**.

O objetivo do projeto é simular um sistema real de billing para produtos SaaS, incluindo gerenciamento de organizações, usuários, clientes, planos, assinaturas, geração de faturas, processamento de pagamentos simulados, idempotência, webhooks, jobs de cobrança e processamento assíncrono orientado a eventos.

> Este projeto está em desenvolvimento e tem como objetivo demonstrar práticas de engenharia backend utilizando uma stack Java moderna e decisões arquiteturais próximas de sistemas reais.

---

## Visão geral

O Cobryn permite que organizações gerenciem suas próprias operações de cobrança recorrente.

Cada organização possui seu próprio contexto de negócio, incluindo:

* usuários;
* clientes;
* planos;
* assinaturas;
* faturas;
* pagamentos.

Uma organização pode cadastrar clientes, criar planos, iniciar assinaturas, gerar faturas e processar pagamentos.

O sistema foi projetado como um **monólito modular**, mantendo um único deploy enquanto preserva limites claros entre os diferentes domínios da aplicação.

Operações financeiras críticas são tratadas de forma síncrona e transacional, enquanto tarefas secundárias e independentes podem ser processadas de maneira assíncrona.

A intenção não é reproduzir todas as complexidades de plataformas como Stripe ou Paddle, mas implementar problemas encontrados em sistemas reais de billing:

* consistência transacional;
* isolamento entre organizações;
* idempotência;
* controle de estados;
* concorrência;
* processamento assíncrono;
* entrega confiável de eventos;
* retries;
* rastreabilidade;
* integração com provedores externos.

---

# Funcionalidades

## Escopo do MVP

* Gerenciamento de organizações
* Registro e autenticação de usuários
* Autenticação com JWT
* Gerenciamento de clientes
* Gerenciamento de planos
* Criação e gerenciamento de assinaturas
* Geração de faturas
* Processamento de pagamentos simulados
* Controle do ciclo de vida de assinaturas
* Controle do ciclo de vida de faturas
* Isolamento dos recursos por organização

## Funcionalidades planejadas

* Controle de acesso baseado em papéis
* Isolamento completo de dados por organização
* Chaves de idempotência para operações críticas
* Webhooks simulados de provedor de pagamento
* Processamento idempotente de webhooks
* Geração agendada de faturas
* Tratamento de pagamentos vencidos
* Recuperação automática de pagamentos
* Período de tolerância para inadimplência
* Cancelamento automático de assinaturas inadimplentes
* Eventos assíncronos com RabbitMQ
* Transactional Outbox
* Consumers idempotentes
* Retry com backoff
* Dead-letter queues
* Notificações assíncronas
* Entrega assíncrona de webhooks
* Processamento distribuído de jobs de cobrança
* Cache com Redis
* Testes de integração com Testcontainers
* Documentação da API com OpenAPI
* Pipeline de CI com GitHub Actions
* Observabilidade e métricas

---

# Stack

## Stack atual

* **Java 25**
* **Spring Boot 4**
* **Spring Data JPA**
* **PostgreSQL**
* **Flyway**
* **Docker Compose**
* **JUnit 5**
* **Maven**

## Tecnologias planejadas

* **Spring Security**
* **JWT**
* **Testcontainers**
* **RabbitMQ**
* **Redis**
* **OpenAPI / Swagger**
* **GitHub Actions**

---

# Domínio de negócio

O Cobryn modela um sistema simplificado de billing recorrente para SaaS.

A `Organization` representa a principal fronteira de negócio e de isolamento dos dados.

Recursos pertencentes a organizações diferentes nunca devem ser combinados dentro do mesmo fluxo de cobrança.

---

# Entidades principais

## Organization

Representa uma empresa que utiliza o Cobryn para gerenciar suas cobranças.

Uma organização possui seus próprios:

* usuários;
* clientes;
* planos;
* assinaturas;
* faturas;
* pagamentos.

Cada recurso de negócio pertence a uma única organização.

Uma organização não pode acessar, modificar ou utilizar recursos pertencentes a outra organização.

---

## User

Representa uma pessoa com acesso a uma organização.

Todo `User` pertence a uma `Organization`.

Usuários poderão possuir diferentes permissões dependendo do papel atribuído dentro da organização.

Exemplos futuros:

```text
OWNER
ADMIN
MEMBER
```

---

## Customer

Representa o cliente final de uma organização.

Todo `Customer` pertence obrigatoriamente a uma única `Organization`.

O cliente é uma entidade gerenciada pela organização e não necessariamente possui acesso direto ao Cobryn.

Um cliente de uma organização não pode ser utilizado em assinaturas pertencentes a outra organização.

---

## Plan

Representa uma oferta comercial recorrente criada por uma organização.

Todo `Plan` pertence obrigatoriamente a uma única `Organization`.

Um plano pode definir informações como:

* nome;
* descrição;
* valor;
* moeda;
* intervalo de cobrança;
* status.

Planos são isolados por organização.

Uma organização não pode visualizar, modificar ou utilizar planos pertencentes a outra organização.

Um plano somente pode ser utilizado em assinaturas de clientes pertencentes à mesma organização.

---

## Subscription

Representa o vínculo recorrente entre um `Customer` e um `Plan`.

Toda `Subscription` pertence a uma `Organization`.

O cliente e o plano relacionados à assinatura devem obrigatoriamente pertencer à mesma organização.

A seguinte relação deve sempre ser verdadeira:

```text
subscription.organizationId
    == customer.organizationId
    == plan.organizationId
```

O sistema deve rejeitar qualquer tentativa de criar uma assinatura relacionando recursos pertencentes a organizações diferentes.

A assinatura controla seu próprio ciclo de vida e determina quando novas cobranças devem ser geradas.

---

## Invoice

Representa uma cobrança gerada para uma assinatura.

Toda `Invoice` pertence à mesma organização de sua `Subscription`.

Uma fatura mantém o estado financeiro da cobrança e pode permanecer pendente mesmo após o cancelamento da assinatura que a originou.

Uma organização nunca pode acessar ou manipular faturas pertencentes a outra organização.

---

## Payment

Representa uma tentativa de pagamento associada a uma fatura.

Todo `Payment` pertence à mesma organização da `Invoice` relacionada.

Uma mesma fatura pode possuir múltiplas tentativas de pagamento.

---

## Webhook Event

Representa um evento recebido de um provedor externo.

Eventos recebidos devem ser processados de maneira idempotente para evitar efeitos duplicados.

Quando um evento estiver associado a um recurso do Cobryn, o processamento deve respeitar a organização proprietária daquele recurso.

---

# Relacionamento entre entidades

A organização funciona como a raiz de isolamento do sistema.

```text
Organization
    |
    +-- Users
    |
    +-- Customers
    |
    +-- Plans
    |
    +-- Subscriptions
    |       |
    |       +-- Customer
    |       |
    |       +-- Plan
    |
    +-- Invoices
    |       |
    |       +-- Subscription
    |
    +-- Payments
            |
            +-- Invoice
```

Uma `Subscription` conecta um `Customer` e um `Plan`, mas somente quando ambos pertencem à mesma organização.

A partir desse ponto, todo o restante do fluxo financeiro permanece dentro da mesma fronteira organizacional.

---

# Invariantes de organização

Algumas das invariantes centrais do Cobryn são:

```text
user.organizationId == organization.id

customer.organizationId == organization.id

plan.organizationId == organization.id

subscription.organizationId == customer.organizationId

subscription.organizationId == plan.organizationId

invoice.organizationId == subscription.organizationId

payment.organizationId == invoice.organizationId
```

Em termos de negócio:

> Nenhuma relação de negócio pode atravessar a fronteira de uma `Organization`.

O sistema deve impedir situações como:

```text
Customer da Organization A
        +
Plan da Organization B
        =
Subscription inválida
```

ou:

```text
User da Organization A
        ->
Invoice da Organization B
        =
acesso negado
```

---

# Ciclo de vida da assinatura

Uma assinatura pode passar pelos seguintes estados:

```text
PENDING -> ACTIVE -> PAST_DUE -> CANCELED
```

Também podem existir transições como:

```text
ACTIVE -> CANCELED

PAST_DUE -> ACTIVE
```

dependendo das regras de recuperação de pagamento adotadas pelo sistema.

## Regras

* Uma assinatura é inicialmente criada como `PENDING`.
* O cliente e o plano devem pertencer à mesma organização.
* Quando sua primeira cobrança é confirmada, a assinatura pode se tornar `ACTIVE`.
* Quando uma fatura relevante vence sem pagamento, a assinatura pode se tornar `PAST_DUE`.
* Um pagamento posterior pode recuperar uma assinatura `PAST_DUE`.
* Após determinado período de inadimplência, a assinatura pode ser cancelada.
* Uma assinatura também pode ser cancelada manualmente.
* Uma assinatura `CANCELED` não pode ser reativada diretamente.
* Quando necessário, uma nova contratação deve gerar uma nova assinatura.

---

# Ciclo de vida da fatura

Uma fatura pode passar pelos seguintes estados:

```text
OPEN -> PAID

OPEN -> OVERDUE

OPEN -> VOID

OVERDUE -> PAID

OVERDUE -> VOID
```

## Regras

* Uma fatura é criada como `OPEN`.
* Um pagamento confirmado altera seu estado para `PAID`.
* Uma fatura não paga após a data de vencimento se torna `OVERDUE`.
* Uma fatura pode ser invalidada e marcada como `VOID` quando a cobrança deixa de ser válida.
* O cancelamento de uma assinatura não implica automaticamente a invalidação de faturas já emitidas.
* Dívidas já geradas podem continuar pendentes mesmo após o encerramento da assinatura.
* A fatura sempre permanece associada à organização que originou a cobrança.

---

# Fluxo de assinatura e cobrança

Um fluxo simplificado pode funcionar da seguinte forma:

```text
1. Uma organização cria um plano

2. A mesma organização cadastra um cliente

3. Um usuário da organização cria uma assinatura
   vinculando o cliente ao plano

4. O Cobryn valida que Customer e Plan
   pertencem à mesma Organization

5. O Cobryn gera a primeira fatura

6. Uma tentativa de pagamento é criada

7. O pagamento é processado pelo provedor simulado

8. O pagamento é confirmado

9. A fatura é marcada como PAID

10. A assinatura se torna ACTIVE

11. Eventos podem ser produzidos para
    processamento secundário
```

Caso o `Customer` e o `Plan` pertençam a organizações diferentes, o fluxo deve ser interrompido antes da criação da assinatura.

---

# Arquitetura síncrona e assíncrona

O Cobryn diferencia operações que fazem parte do estado financeiro principal de operações secundárias que podem ser processadas posteriormente.

## Fluxos síncronos

Operações críticas para a consistência do billing devem ser concluídas dentro da transação principal.

Exemplos:

* criação de organização;
* criação de cliente;
* criação de plano;
* criação de assinatura;
* validação de ownership;
* criação de fatura;
* criação e confirmação de pagamento;
* alteração do estado financeiro da fatura;
* alteração crítica do estado da assinatura.

Exemplo:

```text
HTTP Request
     |
     v
Application Service
     |
     +--> validate organization
     +--> validate invoice
     +--> process payment
     +--> persist payment
     +--> mark invoice as PAID
     +--> activate subscription
     |
     v
COMMIT
```

A requisição não deve depender de processamento secundário para que o estado financeiro principal se torne consistente.

---

## Fluxos assíncronos

Operações que não precisam bloquear a transação principal podem ser processadas posteriormente.

Exemplos:

* envio de notificações;
* envio de e-mails;
* entrega de webhooks de saída;
* processamento de métricas;
* auditoria complementar;
* integração com serviços externos;
* determinados jobs de billing;
* processamento de tarefas em lote.

Exemplo:

```text
PaymentSucceeded
        |
        v
RabbitMQ
   /       \
  v         v
Notification   Webhook
Consumer       Consumer
```

Uma falha em uma notificação não deve invalidar um pagamento já confirmado.

---

# Processamento assíncrono orientado a eventos

O Cobryn pretende utilizar eventos para desacoplar fluxos secundários do processamento principal.

Exemplos de eventos:

```text
PaymentSucceeded

PaymentFailed

InvoicePaid

InvoiceOverdue

SubscriptionActivated

SubscriptionCanceled
```

Esses eventos podem iniciar diferentes processos sem aumentar o acoplamento entre módulos.

Exemplo:

```text
PaymentSucceeded
        |
        +--> Notification
        |
        +--> Outbound Webhook
        |
        +--> Analytics
        |
        +--> Audit
```

Nem todo evento precisa ser enviado para um broker externo.

Eventos internos podem permanecer dentro da aplicação quando não houver necessidade de processamento assíncrono ou durável.

---

# RabbitMQ

RabbitMQ será utilizado para processamento assíncrono entre produtores e consumidores.

Exemplo:

```text
Application
     |
     v
RabbitMQ Exchange
     |
     +--> notification.queue
     |
     +--> webhook.queue
     |
     +--> billing.queue
```

Cada consumidor é responsável por uma função específica.

Essa abordagem permite:

* desacoplamento;
* processamento paralelo;
* retry;
* escalabilidade de consumers;
* isolamento de falhas;
* processamento posterior.

---

# Transactional Outbox

Publicar diretamente no RabbitMQ dentro da mesma operação que altera o banco pode gerar inconsistências.

Exemplo problemático:

```text
1. Payment salvo
2. Invoice marcada como PAID
3. Banco confirma COMMIT
4. Publicação no RabbitMQ falha
```

O estado financeiro foi alterado, porém o evento nunca foi entregue.

Para evitar esse problema, o Cobryn pretende utilizar o padrão **Transactional Outbox**.

## Fluxo

Dentro da mesma transação PostgreSQL:

```text
BEGIN

INSERT Payment

UPDATE Invoice -> PAID

UPDATE Subscription -> ACTIVE

INSERT OutboxEvent -> PaymentSucceeded

COMMIT
```

Após o commit:

```text
Outbox Publisher
       |
       v
Unpublished events
       |
       v
RabbitMQ
       |
       v
Consumers
```

O evento somente é marcado como publicado depois que sua publicação for concluída.

Isso reduz o risco de divergência entre o estado persistido e os eventos produzidos.

---

# Consumers idempotentes

Mensageria normalmente trabalha com entrega **at least once**.

Isso significa que uma mesma mensagem pode ser entregue mais de uma vez.

Por esse motivo, consumers devem ser preparados para processar mensagens duplicadas.

Exemplo:

```text
PaymentSucceeded
ID: evt_123
```

Se `evt_123` já tiver sido processado:

```text
receive evt_123
        |
        v
already processed?
        |
       YES
        |
        v
ignore safely
```

A duplicação de uma mensagem nunca deve gerar:

* duas notificações críticas;
* dois pagamentos;
* duas faturas;
* duas alterações financeiras;
* dois webhooks considerados diferentes quando representam o mesmo evento.

---

# Retry e Dead-Letter Queue

Falhas temporárias não devem necessariamente descartar uma tarefa.

Exemplo:

```text
WebhookDelivery
      |
      v
HTTP request
      |
    ERROR
      |
      v
Retry
```

O Cobryn pretende utilizar estratégias de retry com backoff.

Exemplo conceitual:

```text
1ª tentativa -> imediatamente

2ª tentativa -> após alguns segundos

3ª tentativa -> após alguns minutos

4ª tentativa -> intervalo maior
```

Após exceder o número máximo de tentativas, a mensagem pode ser enviada para uma **Dead-Letter Queue**.

```text
webhook.queue
     |
     X
     |
 retries exhausted
     |
     v
webhook.dlq
```

A DLQ permite preservar mensagens que não puderam ser processadas para análise ou reprocessamento posterior.

---

# Notificações assíncronas

Notificações não fazem parte da confirmação financeira da operação.

Exemplo:

```text
PaymentSucceeded
        |
        v
RabbitMQ
        |
        v
NotificationConsumer
        |
        +--> email
        +--> other channels
```

Se o envio da notificação falhar:

```text
Payment = SUCCEEDED

Invoice = PAID

Notification = retry
```

O pagamento continua confirmado.

---

# Webhooks de saída

Além de receber eventos de um provedor de pagamento, o Cobryn poderá futuramente permitir que sistemas externos recebam eventos relacionados ao billing.

Exemplo:

```text
SubscriptionCanceled
        |
        v
Outbox
        |
        v
RabbitMQ
        |
        v
Webhook Consumer
        |
        v
POST https://customer-system.example/webhooks
```

A entrega deve suportar:

* identificador único do evento;
* assinatura/autenticação;
* retry;
* backoff;
* idempotência;
* histórico de tentativas;
* dead-letter queue.

---

# Jobs de billing

Cobranças recorrentes precisam ser iniciadas automaticamente.

Um scheduler pode identificar assinaturas que precisam ser cobradas.

Uma abordagem simples seria:

```text
Scheduler
    |
    v
Find subscriptions due
    |
    v
Process sequentially
```

Para volumes maiores, o scheduler pode apenas distribuir trabalhos:

```text
Billing Scheduler
       |
       v
Find subscriptions due
       |
       +--> BillingRequested A
       +--> BillingRequested B
       +--> BillingRequested C
                    |
                    v
                 RabbitMQ
              /      |      \
             v       v       v
         Worker   Worker   Worker
```

Cada trabalho deve ser independente e idempotente.

Isso permite aumentar o número de consumers sem alterar o fluxo principal da aplicação.

---

# Recuperação de pagamentos

Falhas de pagamento podem iniciar um processo de recuperação.

Exemplo:

```text
PaymentFailed
      |
      v
Subscription -> PAST_DUE
      |
      +--> Notification
      |
      +--> Retry policy
```

Uma política futura poderia funcionar conceitualmente assim:

```text
Dia 0  -> tentativa inicial

Dia 1  -> nova tentativa

Dia 3  -> nova tentativa

Dia 7  -> última tentativa

Após período de tolerância
       -> cancelamento
```

Os valores reais devem ser definidos pelas regras de negócio e não precisam estar fixos na infraestrutura.

---

# Cobranças recorrentes

Após a ativação da assinatura, o sistema poderá gerar novas faturas de acordo com seu intervalo de cobrança.

```text
Subscription ACTIVE
        |
        v
Billing Scheduler
        |
        v
Billing Requested
        |
        v
Invoice OPEN
        |
        v
Payment Attempt
        |
   +----+----+
   |         |
SUCCESS    FAILURE
   |         |
   v         v
 PAID     OPEN/OVERDUE
   |         |
   v         v
 ACTIVE   PAST_DUE
```

Caso a cobrança permaneça sem pagamento além do período configurado de tolerância, a assinatura poderá ser cancelada.

Todas as entidades envolvidas permanecem associadas à mesma organização durante todo o fluxo.

---

# Idempotência

Operações financeiras não devem produzir efeitos duplicados.

O Cobryn pretende utilizar chaves de idempotência em operações críticas.

Exemplos:

```text
pagamentos

processamento de webhooks

jobs de cobrança

consumers assíncronos

operações financeiras repetíveis
```

Uma mesma operação repetida com a mesma identidade lógica deve produzir o mesmo resultado sem gerar efeitos financeiros duplicados.

A idempotência também deve respeitar o contexto da organização.

Uma chave utilizada por uma organização não deve interferir nas operações de outra.

---

# Webhooks recebidos

O Cobryn simulará a integração com um provedor externo de pagamentos.

Exemplos:

```text
payment.succeeded

payment.failed

payment.refunded
```

Fluxo esperado:

```text
Payment Provider
       |
       v
Webhook Endpoint
       |
       v
Validate authenticity
       |
       v
Check idempotency
       |
       v
Persist event
       |
       v
Resolve organization/resource
       |
       v
Update billing state
       |
       v
Produce internal event
```

Eventos já processados não devem produzir efeitos novamente.

---

# Visão geral da API

> Os endpoints abaixo representam a API planejada do sistema e podem mudar durante o desenvolvimento.

## Autenticação

```http
POST /api/auth/register
POST /api/auth/login
```

## Organizações

```http
POST /api/organizations
GET /api/organizations/{slug}
PATCH /api/organizations/{slug}
```

## Clientes

```http
POST /api/customers
GET /api/customers
GET /api/customers/{id}
PATCH /api/customers/{id}
POST /api/customers/{id}/deactivate
```

Todos os clientes retornados ou modificados devem pertencer à organização do usuário autenticado.

Clientes que já possuem histórico financeiro não devem ser removidos fisicamente apenas para representar uma desativação.

## Planos

```http
POST /api/plans
GET /api/plans
GET /api/plans/{id}
PATCH /api/plans/{id}
POST /api/plans/{id}/deactivate
```

Todo plano criado pertence à organização responsável por sua criação.

Usuários de uma organização não podem consultar ou utilizar planos de outra organização.

Planos utilizados por assinaturas existentes devem manter seu histórico mesmo quando deixam de aceitar novas assinaturas.

## Assinaturas

```http
POST /api/subscriptions
GET /api/subscriptions
GET /api/subscriptions/{id}
POST /api/subscriptions/{id}/cancel
```

Ao criar uma assinatura, o sistema deve validar que:

```text
Customer.organizationId == Plan.organizationId
```

e que ambos pertencem à organização responsável pela operação.

## Faturas

```http
GET /api/invoices
GET /api/invoices/{id}
POST /api/invoices/{id}/pay
POST /api/invoices/{id}/void
```

## Webhooks

```http
POST /api/webhooks/payment-provider
```

---

# Arquitetura

O Cobryn segue uma arquitetura de **monólito modular**.

Cada módulo representa uma área funcional do sistema e encapsula suas próprias responsabilidades.

A aplicação continua sendo implantada como uma única unidade, porém seus módulos mantêm limites explícitos.

Essa abordagem permite evitar a complexidade operacional de microsserviços sem transformar o projeto em um monólito fortemente acoplado.

O uso de mensageria não altera essa decisão arquitetural.

RabbitMQ é utilizado como mecanismo de processamento assíncrono, e não como justificativa para dividir prematuramente o sistema em microsserviços.

---

# Visão arquitetural

```text
                         Cobryn

                   HTTP / REST API
                         |
                         v
                Application Services
                         |
                 synchronous core
                         |
                         v
                    PostgreSQL
        ┌────────────────────────────────┐
        │ Organizations                  │
        │ Customers                      │
        │ Plans                          │
        │ Subscriptions                  │
        │ Invoices                       │
        │ Payments                       │
        │ Outbox Events                  │
        └───────────────┬────────────────┘
                        |
                        v
                 Outbox Publisher
                        |
                        v
                     RabbitMQ
              /           |           \
             v            v            v
      Notifications   Webhooks     Billing Jobs
        Consumer       Consumer       Consumer
```

---

# Estrutura do projeto

A estrutura geral segue o formato:

```text
src/main/java/com/cobryn

├── auth
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── web
│
├── organization
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── web
│
├── user
├── customer
├── plan
├── subscription
├── invoice
├── payment
├── webhook
├── notification
│
└── shared
    ├── config
    ├── exception
    ├── security
    ├── pagination
    └── event
```

Nem todos os módulos precisam possuir exatamente as mesmas pastas.

A estrutura interna depende das responsabilidades de cada domínio.

---

# Responsabilidades das camadas

## Domain

Contém principalmente:

* entidades;
* value objects;
* invariantes;
* regras de negócio;
* estados;
* comportamento do domínio;
* eventos de domínio quando apropriado.

Exemplos:

```text
Subscription.cancel()

Invoice.markAsPaid()

Organization.changeName()
```

O domínio não deve depender de HTTP, controllers, RabbitMQ ou outros detalhes externos.

---

## Application

Responsável por:

* casos de uso;
* coordenação de operações;
* fronteiras transacionais;
* autorização contextual;
* comunicação entre módulos;
* publicação lógica de eventos;
* contratos necessários pela aplicação.

Exemplo:

```text
CreateSubscription
        |
        +--> resolve organization
        |
        +--> load customer
        |
        +--> load plan
        |
        +--> validate ownership
        |
        +--> create subscription
        |
        +--> persist
```

---

## Infrastructure

Responsável pelos detalhes técnicos:

* JPA;
* PostgreSQL;
* repositories;
* RabbitMQ;
* Redis;
* Outbox Publisher;
* schedulers;
* integrações externas;
* provedores de pagamento.

---

## Web

Responsável pela interface HTTP:

* controllers;
* request DTOs;
* response DTOs;
* validação de entrada;
* autenticação;
* mapeamento de erros HTTP.

Entidades internas não devem ser expostas diretamente pela API.

---

# Comunicação entre módulos

Módulos devem evitar acessar detalhes internos uns dos outros.

A comunicação pode ocorrer por:

```text
Application services

Contracts

Application events

Domain events

Integration events
```

dependendo do fluxo.

Eventos de domínio representam fatos ocorridos no domínio.

Mensagens do RabbitMQ são representações utilizadas pela infraestrutura para transporte assíncrono.

Portanto:

```text
Domain Event != RabbitMQ Message
```

Nem todo evento precisa sair da aplicação.

---

# Concorrência

O Cobryn deve considerar cenários em que duas operações aconteçam simultaneamente.

Exemplos:

* duas tentativas de pagar a mesma fatura;
* dois webhooks representando o mesmo evento;
* dois workers tentando cobrar a mesma assinatura;
* dois requests usando a mesma chave de idempotência;
* duas execuções tentando atualizar o mesmo estado financeiro.

Dependendo do caso, podem ser utilizadas estratégias como:

* constraints no banco;
* locking;
* optimistic concurrency;
* idempotência;
* transações;
* consumidores idempotentes.

A solução deve ser escolhida de acordo com a invariante que precisa ser protegida.

---

# Virtual Threads

Como o projeto utiliza Java 25, Virtual Threads podem ser exploradas em operações predominantemente I/O-bound.

Elas podem ser úteis em cenários como:

* chamadas HTTP externas;
* processamento concorrente de integrações;
* entrega de webhooks;
* tarefas independentes de I/O.

Virtual Threads e RabbitMQ resolvem problemas diferentes.

```text
Virtual Threads
    ->
concorrência de execução

RabbitMQ
    ->
desacoplamento, durabilidade
e processamento assíncrono
```

O uso de Virtual Threads não substitui mensageria, transactional outbox ou idempotência.

---

# Multi-tenancy e isolamento entre organizações

O Cobryn utiliza um modelo de **multi-tenancy lógico**.

Todas as organizações utilizam a mesma aplicação e infraestrutura, porém seus dados permanecem logicamente isolados.

```text
Organization A

├── Customers
├── Plans
├── Subscriptions
├── Invoices
└── Payments


Organization B

├── Customers
├── Plans
├── Subscriptions
├── Invoices
└── Payments
```

Nenhum relacionamento pode atravessar essas fronteiras.

São válidas:

```text
Customer A + Plan A -> Subscription A

Customer B + Plan B -> Subscription B
```

São inválidas:

```text
Customer A + Plan B -> X

Customer B + Plan A -> X
```

---

# Segurança de ownership

Não basta verificar se um recurso existe.

Também é necessário verificar se ele pertence à organização responsável pela operação.

Exemplo conceitual:

```text
findPlanById(planId)
```

não garante isolamento por si só.

A consulta ou validação deve considerar também a organização:

```text
findPlanByIdAndOrganization(planId, organizationId)
```

ou utilizar outra estratégia que preserve a mesma garantia.

O princípio se aplica a:

* customers;
* plans;
* subscriptions;
* invoices;
* payments.

---

# Desenvolvimento local

## Requisitos

* Java 25
* Docker
* Docker Compose

O projeto utiliza Maven Wrapper, portanto uma instalação global do Maven não é obrigatória.

## Subindo a infraestrutura

```bash
docker compose up -d
```

## Rodando a aplicação

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Rodando os testes

Linux/macOS:

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

---

# Variáveis de ambiente

Exemplo de configuração local:

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

Valores utilizados apenas futuramente podem permanecer ausentes enquanto suas respectivas funcionalidades ainda não estiverem implementadas.

Segredos reais nunca devem ser versionados no repositório.

Uma configuração pública deve utilizar apenas valores de exemplo, preferencialmente em:

```text
.env.example
```

---

# Estratégia de testes

O Cobryn pretende combinar diferentes níveis de teste.

## Testes unitários

Responsáveis principalmente pelas regras e invariantes de domínio.

Exemplos:

```text
- assinatura cancelada não pode ser cancelada novamente

- invoice paga não pode voltar para OPEN

- organização não aceita nome inválido

- transições inválidas de estado são rejeitadas
```

---

## Testes de integração

Validam a interação entre aplicação, persistência e infraestrutura.

Exemplos:

```text
- criação de organização persiste corretamente

- slug de organização deve ser único

- transação é revertida em caso de erro

- customer não pode ser associado a plan de outra organização
```

---

## Testes de API

Validam:

* contratos HTTP;
* validações;
* status codes;
* serialização;
* autenticação;
* autorização;
* tratamento de erros.

---

## Testes de isolamento

O isolamento entre organizações deve possuir cobertura explícita.

Exemplos:

```text
- usuário da Organization A não pode consultar Customer da Organization B

- usuário da Organization A não pode consultar Plan da Organization B

- usuário da Organization A não pode cancelar Subscription da Organization B

- Customer da Organization A não pode utilizar Plan da Organization B

- Invoice da Organization B não pode ser paga por uma operação da Organization A
```

---

## Testes de assincronia

Fluxos assíncronos também devem possuir cobertura própria.

Exemplos:

```text
- evento de outbox é salvo na mesma transação da operação

- rollback financeiro também remove o evento de outbox

- evento não publicado permanece disponível para nova tentativa

- mensagem duplicada não produz efeitos duplicados

- consumer falho realiza retry

- mensagem excedendo retries vai para DLQ

- falha de notificação não altera estado do pagamento

- worker duplicado não gera duas faturas para o mesmo ciclo
```

---

## Testcontainers

Quando introduzido no projeto, Testcontainers será utilizado para validar comportamento dependente de infraestrutura real, especialmente:

* PostgreSQL;
* RabbitMQ;
* Redis.

A intenção é evitar depender exclusivamente de doubles ou bancos em memória em cenários nos quais o comportamento da infraestrutura faz parte do que está sendo testado.

---

# Roadmap

## Fase 1 — Fundação

* [x] Configuração do projeto com Java 25 e Spring Boot 4
* [x] PostgreSQL
* [x] Flyway
* [x] Docker Compose
* [x] Estrutura inicial do monólito modular
* [x] Domínio inicial de organização
* [ ] Domínio de usuário
* [ ] Autenticação com JWT

---

## Fase 2 — Core de billing

* [ ] Clientes
* [ ] Planos vinculados à organização
* [ ] Assinaturas
* [ ] Validação de ownership entre Customer e Plan
* [ ] Faturas
* [ ] Pagamentos simulados
* [ ] Ciclo de vida das assinaturas
* [ ] Ciclo de vida das faturas

---

## Fase 3 — Robustez financeira

* [ ] Chaves de idempotência
* [ ] Simulação de falha de pagamento
* [ ] Recuperação de pagamento
* [ ] Processamento de webhooks
* [ ] Idempotência de webhooks
* [ ] Jobs agendados de cobrança
* [ ] Controle de concorrência em operações críticas

---

## Fase 4 — Multi-tenancy e segurança

* [ ] Isolamento completo por organização
* [ ] Validação de ownership em todos os recursos
* [ ] Testes de isolamento entre organizações
* [ ] Controle de acesso baseado em papéis
* [ ] Testes de segurança
* [ ] Endurecimento das configurações de autenticação

---

## Fase 5 — Processamento assíncrono

* [ ] Eventos de aplicação
* [ ] RabbitMQ
* [ ] Transactional Outbox
* [ ] Outbox Publisher
* [ ] Consumers assíncronos
* [ ] Consumers idempotentes
* [ ] Retry com backoff
* [ ] Dead-letter queues
* [ ] Notificações assíncronas
* [ ] Entrega assíncrona de webhooks
* [ ] Distribuição de jobs de cobrança
* [ ] Testes de integração da mensageria
* [ ] Avaliar uso de Virtual Threads em workloads I/O-bound

---

## Fase 6 — Infraestrutura e observabilidade

* [ ] Redis
* [ ] Testcontainers
* [ ] OpenAPI / Swagger
* [ ] Pipeline com GitHub Actions
* [ ] Logging estruturado
* [ ] Métricas
* [ ] Health checks
* [ ] Métricas de filas e consumers
* [ ] Observabilidade de mensagens em DLQ

---

## Fase 7 — Polimento

* [ ] Padronizar respostas de erro
* [ ] Melhorar validações de requisição
* [ ] Adicionar exemplos de uso da API
* [ ] Adicionar collection do Postman ou Insomnia
* [ ] Adicionar documentação arquitetural
* [ ] Adicionar diagrama de arquitetura
* [ ] Adicionar guia de deploy

---

# Objetivos técnicos

Este projeto foi criado para praticar e demonstrar:

* arquitetura backend com Java e Spring Boot;
* monólito modular;
* modelagem de domínios;
* APIs REST;
* design seguro de APIs;
* modelagem relacional;
* controle transacional;
* concorrência;
* idempotência;
* processamento assíncrono;
* arquitetura orientada a eventos;
* mensageria com RabbitMQ;
* Transactional Outbox;
* consumers idempotentes;
* retries e dead-letter queues;
* processamento de jobs;
* integração com sistemas externos;
* multi-tenancy;
* isolamento de dados;
* autorização baseada em ownership;
* testes com infraestrutura real;
* documentação técnica;
* práticas backend orientadas a produção.

---

# Escopo

O Cobryn é um projeto educacional e de portfólio.

Ele não pretende substituir uma plataforma de pagamentos real e não processa transações financeiras reais.

O processador de pagamento utilizado pelo projeto será simulado para permitir a implementação dos mesmos tipos de problemas arquiteturais encontrados em integrações financeiras reais.

O foco principal do projeto está nas decisões de engenharia envolvidas na construção de um sistema de billing confiável, incluindo:

* consistência;
* isolamento;
* idempotência;
* concorrência;
* evolução de estados;
* entrega confiável de eventos;
* tolerância a falhas;
* processamento assíncrono.

---

# Licença

Este projeto está licenciado sob a licença MIT.
