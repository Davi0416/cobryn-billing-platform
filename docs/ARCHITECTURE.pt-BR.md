# Arquitetura do Cobryn

O Cobryn foi projetado como um **monólito modular**.

O sistema é dividido em módulos de negócio, cada um responsável por uma parte específica do domínio de billing. Isso mantém o código simples de executar e fazer deploy, ao mesmo tempo em que preserva limites claros entre as funcionalidades.

---

## Por que monólito modular?

Uma plataforma de billing possui vários domínios que poderiam, no futuro, se tornar serviços independentes, como pagamentos, faturas, assinaturas e notificações.

No entanto, começar diretamente com microsserviços adicionaria complexidade desnecessária nas fases iniciais do projeto.

A abordagem de monólito modular permite que o Cobryn:

- Mantenha o desenvolvimento local simples
- Evite complexidade prematura de sistemas distribuídos
- Preserve limites claros entre domínios
- Compartilhe um único banco de dados inicialmente
- Permita a extração futura de módulos para serviços separados

---

## Módulos principais

### Módulo de autenticação

Responsável por:

- Registro de usuários
- Login de usuários
- Hash de senhas
- Geração de JWT
- Filtros de autenticação

---

### Módulo de organizações

Responsável por:

- Criação de organizações
- Gerenciamento do perfil da organização
- Regras de propriedade por organização

Cada usuário pertence a uma organização, e a maioria dos recursos de negócio é vinculada a uma organização.

---

### Módulo de clientes

Responsável por:

- Criação de clientes
- Atualização de clientes
- Listagem de clientes
- Validação de propriedade do cliente

Clientes representam as pessoas ou empresas que serão cobradas por uma organização.

---

### Módulo de planos

Responsável por:

- Criação de planos
- Definição de preços
- Configuração do intervalo de cobrança
- Ativação e desativação de planos

Planos definem quanto um cliente deve ser cobrado e com qual frequência.

---

### Módulo de assinaturas

Responsável por:

- Criação de assinaturas
- Cancelamento de assinaturas
- Gerenciamento de status da assinatura
- Associação entre clientes e planos
- Reação a eventos de fatura e pagamento

Uma assinatura representa uma relação recorrente de cobrança entre um cliente e um plano.

---

### Módulo de faturas

Responsável por:

- Geração de faturas
- Datas de vencimento
- Transições de status da fatura
- Consulta de faturas
- Detecção de faturas vencidas

Faturas são geradas a partir de assinaturas e pagas por meio do fluxo de pagamento.

---

### Módulo de pagamentos

Responsável por:

- Processamento de pagamento simulado
- Tentativas de pagamento
- Status de pagamento
- Chaves de idempotência
- Simulação de provedor de pagamento

O módulo de pagamentos não se integra com um provedor real. Ele simula pagamentos bem-sucedidos e pagamentos com falha para fins de estudo e testes.

---

### Módulo de webhooks

Responsável por:

- Recebimento de eventos simulados do provedor de pagamento
- Validação do payload dos webhooks
- Prevenção de processamento duplicado
- Disparo de atualizações em faturas e assinaturas

---

### Módulo de notificações

Responsável por:

- Publicação de eventos de notificação
- Consumo de eventos relacionados a billing
- Simulação de envio de e-mails

Este módulo pode ser extraído futuramente para um serviço separado.

---

## Módulo compartilhado

O módulo compartilhado contém infraestrutura e preocupações transversais:

```text
shared
├── config
├── exceptions
├── security
├── pagination
├── validation
└── events
```

O módulo compartilhado não deve conter regras de negócio.

---

## Regra de dependência

Os módulos de negócio devem evitar acoplamento direto desnecessário.

Fluxo preferencial:

```text
Controller -> Application Service -> Domain Logic -> Repository
```

Para comunicação entre módulos, o Cobryn pode usar:

```text
Chamadas entre serviços de aplicação
Eventos de domínio
Eventos assíncronos com RabbitMQ
```

---

## Estratégia de banco de dados

O Cobryn usa PostgreSQL como banco de dados principal.

Alterações no banco são gerenciadas por migrações do Flyway.

Os arquivos de migração devem seguir este padrão:

```text
src/main/resources/db/migration
├── V1__create_users_table.sql
├── V2__create_organizations_table.sql
├── V3__create_customers_table.sql
```

---

## Estratégia de segurança

O Cobryn usa autenticação baseada em JWT.

Regras de segurança planejadas:

- Acesso público apenas para endpoints de autenticação
- Acesso protegido para todos os endpoints de negócio
- Todo usuário deve pertencer a uma organização
- Usuários só podem acessar recursos da própria organização
- Ações administrativas devem ser protegidas por papéis

---

## Estratégia de tratamento de erros

O Cobryn deve retornar erros de API consistentes.

Exemplo:

```json
{
  "timestamp": "2026-07-14T13:00:00Z",
  "status": 404,
  "error": "Resource not found",
  "message": "Customer not found",
  "path": "/api/customers/123"
}
```

Erros comuns:

- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `409 Conflict`
- `422 Unprocessable Entity`
- `500 Internal Server Error`

---

## Estratégia de testes

O Cobryn deve incluir múltiplos níveis de teste.

### Testes unitários

Usados para validar regras de negócio isoladas.

Exemplos:

- Transições de status da assinatura
- Cálculos de data de vencimento de fatura
- Mudanças de status de pagamento
- Comportamento de idempotência

### Testes de integração

Usados para validar fluxos com banco de dados.

Exemplos:

- Criar uma assinatura persiste a assinatura e a fatura
- Pagar uma fatura atualiza o status da fatura e da assinatura
- Isolamento por organização impede acesso a dados de outra organização

### Testes de API

Usados para validar comportamento dos controllers.

Exemplos:

- Requisição inválida retorna erro de validação
- Endpoint protegido exige autenticação
- Usuário autenticado consegue criar um cliente

---

## Candidatos à extração futura

Se o projeto crescer, estes módulos podem se tornar serviços separados:

```text
notifications-service
payments-service
billing-service
```

Por enquanto, mantê-los dentro da mesma aplicação evita complexidade operacional desnecessária.
