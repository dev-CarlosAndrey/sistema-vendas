# Documentação Técnica — Sistema de Vendas

> API REST construída com **Java 21 + Spring Boot 3.5.15**, banco **H2 em memória**, Lombok e SpringDoc (Swagger).  
> Pacote raiz: `com.fpo.vendas` · Módulo Maven: `sistema-vendas/`

---

## Sumário

1. [Visão Geral da Aplicação](#1-visão-geral-da-aplicação)
2. [Stack e Dependências](#2-stack-e-dependências)
3. [Estrutura de Pastas Completa](#3-estrutura-de-pastas-completa)
4. [Camada de Configuração — `config/`](#4-camada-de-configuração--config)
5. [Camada de Modelo — `model/`](#5-camada-de-modelo--model)
6. [Camada de Repositório — `repository/`](#6-camada-de-repositório--repository)
7. [Camada de DTOs — `dto/`](#7-camada-de-dtos--dto)
8. [Camada de Mapeamento — `mapper/`](#8-camada-de-mapeamento--mapper)
9. [Camada de Serviço — `service/`](#9-camada-de-serviço--service)
10. [Camada de Estratégia — `strategy/`](#10-camada-de-estratégia--strategy)
11. [Camada de Exceções — `exception/`](#11-camada-de-exceções--exception)
12. [Camada de Controller — `controller/`](#12-camada-de-controller--controller)
13. [CRUD Completo por Entidade](#13-crud-completo-por-entidade)
14. [Fluxo de uma Requisição — Passo a Passo](#14-fluxo-de-uma-requisição--passo-a-passo)
15. [Banco de Dados e Tabelas](#15-banco-de-dados-e-tabelas)
16. [Princípios SOLID Aplicados](#16-princípios-solid-aplicados)
17. [Tratamento de Erros](#17-tratamento-de-erros)
18. [Configuração da Aplicação](#18-configuração-da-aplicação)

---

## 1. Visão Geral da Aplicação

O sistema é um **back-end de gerenciamento de vendas e estoque**. Ele permite:

- Cadastrar e gerenciar **clientes** (com validação de CPF matemático e unicidade de e-mail).
- Cadastrar e gerenciar **produtos** em dois subtipos: **Perecível** (com data de validade) e **Não Perecível** (com meses de garantia).
- Gerenciar o **estoque** de cada produto (criado automaticamente zerado quando um produto é cadastrado).
- Registrar **vendas**, que validam estoque, bloqueiam produtos vencidos e aplicam estratégias de desconto configuráveis.

A aplicação **não possui front-end**. Toda a interação é via JSON através dos endpoints REST, documentados automaticamente no Swagger.

---

## 2. Stack e Dependências

| Tecnologia | Versão | Papel |
|---|---|---|
| Java | 21 | Linguagem |
| Spring Boot | 3.5.15 | Framework principal |
| Spring Data JPA | (boot-managed) | Persistência e acesso ao banco |
| Spring Validation | (boot-managed) | Validação de entrada via anotações Bean Validation |
| H2 Database | (boot-managed) | Banco relacional em memória (dados reiniciam a cada startup) |
| Lombok | (boot-managed) | Geração de boilerplate (`@Getter`, `@Builder`, etc.) |
| SpringDoc OpenAPI | 2.7.0 | Geração automática do Swagger UI |

> **Banco em memória**: o `ddl-auto=create-drop` faz o Hibernate recriar todas as tabelas a cada inicialização. Os dados não persistem entre reinicializações.

---

## 3. Estrutura de Pastas Completa

```
sistema-vendas/
├── pom.xml                          ← Dependências Maven e configuração de build
├── src/
│   ├── main/
│   │   ├── java/com/fpo/vendas/
│   │   │   ├── VendasApplication.java       ← Ponto de entrada da aplicação (@SpringBootApplication)
│   │   │   │
│   │   │   ├── config/                      ← Configurações de infraestrutura
│   │   │   │   └── OpenApiConfig.java       ← Configura título, versão e contato do Swagger
│   │   │   │
│   │   │   ├── model/                       ← Entidades JPA (mapeadas para tabelas do banco)
│   │   │   │   ├── Client.java              ← Tabela: tb_client
│   │   │   │   ├── Product.java             ← Tabela: tb_product (classe abstrata, herança JOINED)
│   │   │   │   ├── PerishableProduct.java   ← Tabela: tb_perishable_product (herda Product)
│   │   │   │   ├── NonPerishableProduct.java← Tabela: tb_non_perishable_product (herda Product)
│   │   │   │   ├── Stock.java               ← Tabela: tb_stock (1:1 com Product)
│   │   │   │   ├── Sale.java                ← Tabela: tb_sale
│   │   │   │   └── SaleItem.java            ← Tabela: tb_sale_item (linha de item dentro de uma venda)
│   │   │   │
│   │   │   ├── repository/                  ← Interfaces de acesso ao banco (Spring Data JPA)
│   │   │   │   ├── ClientRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── StockRepository.java
│   │   │   │   └── SaleRepository.java
│   │   │   │
│   │   │   ├── dto/                         ← Objetos de Transferência de Dados (nunca expõe a entidade diretamente)
│   │   │   │   ├── request/                 ← O que o cliente envia na requisição (entrada)
│   │   │   │   │   ├── ClientRequest.java
│   │   │   │   │   ├── ProductRequest.java
│   │   │   │   │   ├── SaleRequest.java
│   │   │   │   │   ├── SaleItemRequest.java
│   │   │   │   │   └── StockUpdateRequest.java
│   │   │   │   ├── response/                ← O que a API devolve na resposta (saída)
│   │   │   │   │   ├── ClientResponse.java
│   │   │   │   │   ├── ProductResponse.java
│   │   │   │   │   ├── SaleResponse.java
│   │   │   │   │   ├── SaleItemResponse.java
│   │   │   │   │   └── StockResponse.java
│   │   │   │   └── validator/               ← Validações customizadas além das padrão do Bean Validation
│   │   │   │       ├── CPF.java             ← Anotação customizada @CPF
│   │   │   │       └── impl/
│   │   │   │           └── CPFValidator.java← Lógica real do cálculo dos dígitos verificadores do CPF
│   │   │   │
│   │   │   ├── mapper/                      ← Conversão entre Request/Response e Entidade
│   │   │   │   ├── ClientMapper.java        ← ClientRequest → Client; Client → ClientResponse
│   │   │   │   ├── ProductMapper.java       ← ProductRequest → Product (com decisão de subtipo); Product → ProductResponse
│   │   │   │   └── SaleMapper.java          ← Arquivo existe mas está vazio (mapeamento feito inline no SaleServiceImpl)
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── interfaces/              ← Contratos do que cada serviço deve fazer
│   │   │   │   │   ├── IClientService.java
│   │   │   │   │   ├── IProductService.java
│   │   │   │   │   ├── ISaleService.java
│   │   │   │   │   └── IStockService.java
│   │   │   │   └── impl/                    ← Implementações concretas com toda a lógica de negócio
│   │   │   │       ├── ClientServiceImpl.java
│   │   │   │       ├── ProductServiceImpl.java
│   │   │   │       ├── SaleServiceImpl.java
│   │   │   │       └── StockServiceImpl.java
│   │   │   │
│   │   │   ├── strategy/                    ← Padrão Strategy para cálculo de desconto nas vendas
│   │   │   │   ├── SalesCalculator.java     ← Interface: contrato que toda estratégia de desconto deve seguir
│   │   │   │   └── impl/
│   │   │   │       ├── NoDiscountCalculator.java         ← Sem desconto (bean name: "NO_DISCOUNT")
│   │   │   │       └── TenPercentDiscountCalculator.java ← 10% de desconto (bean name: "TEN_PERCENT")
│   │   │   │
│   │   │   └── exception/                   ← Exceções customizadas e handler global de erros HTTP
│   │   │       ├── BusinessException.java          ← Regra de negócio violada → HTTP 422
│   │   │       ├── InsufficientStockException.java  ← Estoque insuficiente → HTTP 422
│   │   │       ├── ResourceNotFoundException.java   ← Recurso não encontrado → HTTP 404
│   │   │       └── GlobalExceptionHandler.java      ← Intercepta todas as exceções e formata a resposta JSON de erro
│   │   │
│   │   └── resources/
│   │       └── application.properties       ← Configurações da porta, banco H2, JPA/Hibernate e Swagger
│   │
│   └── test/
│       └── java/com/fpo/vendas/
│           └── VendasApplicationTests.java  ← Teste padrão de contexto (verifica se a aplicação sobe)
```

---

## 4. Camada de Configuração — `config/`

### `OpenApiConfig.java`

Define as metainformações que aparecem na página do Swagger UI:

| Campo | Valor |
|---|---|
| Título | "Sistema de Vendas & Gerenciamento de Estoque API" |
| Versão | 1.0.0 |
| Descrição | Tecnologias e princípios usados |
| Contato | andreybezerra.info@gmail.com |

> Acesse o Swagger em: `http://localhost:8080/swagger-ui.html`  
> Acesse o JSON da spec em: `http://localhost:8080/api-docs`

---

## 5. Camada de Modelo — `model/`

As entidades são classes Java anotadas com `@Entity` que o Hibernate converte em tabelas SQL.

### `Client.java` → tabela `tb_client`

| Campo | Tipo Java | Coluna SQL | Regra |
|---|---|---|---|
| `id` | `Long` | `id` PK | Auto incremento |
| `name` | `String` | `name` | NOT NULL |
| `email` | `String` | `email` | NOT NULL, UNIQUE |
| `cpf` | `String` | `cpf` | NOT NULL, UNIQUE |
| `phone` | `String` | `phone` | Opcional |
| `registrationDate` | `LocalDateTime` | `registration_date` | Preenchido automaticamente no `@PrePersist`, não editável |

---

### `Product.java` → tabela `tb_product` (classe abstrata)

Usa herança `InheritanceType.JOINED`: a tabela `tb_product` guarda os campos comuns, e cada subtipo tem sua própria tabela complementar.

| Campo | Tipo Java | Regra |
|---|---|---|
| `id` | `Long` | PK, auto incremento |
| `name` | `String` | NOT NULL |
| `description` | `String` | Opcional |
| `price` | `BigDecimal` | NOT NULL, precisão 10,2 |
| `category` | `String` | NOT NULL |
| `stock` | `Stock` | Relacionamento `@OneToOne`, cascata `ALL` |

**Método abstrato:** `hasSalesRestriction()` — cada subtipo implementa sua própria regra de bloqueio de venda.

---

### `PerishableProduct.java` → tabela `tb_perishable_product`

Extende `Product`. Campo adicional:

| Campo | Tipo Java | Descrição |
|---|---|---|
| `expirationDate` | `LocalDate` | Data de validade |

`hasSalesRestriction()` retorna `true` se `expirationDate` já passou. Isso bloqueia a venda do produto.

---

### `NonPerishableProduct.java` → tabela `tb_non_perishable_product`

Extende `Product`. Campo adicional:

| Campo | Tipo Java | Descrição |
|---|---|---|
| `warrantyMonths` | `Integer` | Quantidade de meses de garantia |

`hasSalesRestriction()` sempre retorna `false` — produto não perecível nunca é bloqueado por validade.

---

### `Stock.java` → tabela `tb_stock`

Representa a quantidade de um produto em estoque. Cada produto tem exatamente um registro de estoque.

| Campo | Tipo Java | Regra |
|---|---|---|
| `id` | `Long` | PK, auto incremento |
| `product` | `Product` | FK para `tb_product`, `@OneToOne`, UNIQUE |
| `quantity` | `Integer` | NOT NULL, começa em 0 |

> O estoque é **criado automaticamente** com `quantity = 0` no momento em que um produto é cadastrado (`ProductServiceImpl.create()`). Não existe endpoint para criar estoque manualmente.

---

### `Sale.java` → tabela `tb_sale`

Representa o cabeçalho de uma venda.

| Campo | Tipo Java | Regra |
|---|---|---|
| `id` | `Long` | PK, auto incremento |
| `client` | `Client` | FK para `tb_client`, `@ManyToOne` |
| `saleDate` | `LocalDateTime` | Preenchido automaticamente no momento da venda |
| `totalValue` | `BigDecimal` | Calculado pela estratégia de desconto |
| `items` | `List<SaleItem>` | Itens da venda, `@OneToMany`, cascata `ALL` |

---

### `SaleItem.java` → tabela `tb_sale_item`

Representa uma linha dentro de uma venda (produto + quantidade + preço no momento da compra).

| Campo | Tipo Java | Regra |
|---|---|---|
| `id` | `Long` | PK, auto incremento |
| `sale` | `Sale` | FK para `tb_sale`, `@ManyToOne` |
| `product` | `Product` | FK para `tb_product`, `@ManyToOne` |
| `quantity` | `Integer` | NOT NULL |
| `unitPrice` | `BigDecimal` | Preço histórico no momento da venda (snapshot), NOT NULL |

> O `unitPrice` é salvo com o valor atual do produto para preservar o histórico. Se o preço do produto mudar depois, a venda antiga não é afetada.

---

## 6. Camada de Repositório — `repository/`

Todos os repositories estendem `JpaRepository<Entidade, Long>`, que já fornece automaticamente: `save()`, `findById()`, `findAll()`, `deleteById()`, `existsById()`, etc.

### `ClientRepository`

Além do padrão, declara métodos customizados (o Spring Data gera o SQL automaticamente pelo nome):

```java
boolean existsByEmail(String email);   // SELECT COUNT > 0 WHERE email = ?
boolean existsByCpf(String cpf);       // SELECT COUNT > 0 WHERE cpf = ?
Optional<Client> findByCpf(String cpf);// SELECT * WHERE cpf = ?
```

Usados no service para validar duplicidade antes de criar ou atualizar um cliente.

### `ProductRepository`

Apenas o padrão do `JpaRepository`. Nenhum método customizado necessário.

### `StockRepository`

```java
Optional<Stock> findByProductId(Long productId); // SELECT * WHERE product_id = ?
```

Usado para buscar o estoque de um produto pelo ID do produto (em vez do ID do estoque).

### `SaleRepository`

Apenas o padrão. O `findAll()` é suficiente para listar todas as vendas.

---

## 7. Camada de DTOs — `dto/`

DTOs são **Java Records** (imutáveis por padrão). Eles separam o contrato da API do modelo interno do banco.

### `dto/request/` — O que entra

#### `ClientRequest`

```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "cpf": "123.456.789-09",
  "phone": "11999999999"
}
```

| Campo | Validação |
|---|---|
| `name` | `@NotBlank` |
| `email` | `@NotBlank` + `@Email` |
| `cpf` | `@NotBlank` + `@CPF` (validação matemática customizada) |
| `phone` | Sem validação, campo opcional |

---

#### `ProductRequest`

```json
{
  "name": "Leite Integral",
  "description": "Leite pasteurizado 1L",
  "price": 5.99,
  "category": "Laticínios",
  "type": "PERISHABLE",
  "expirationDate": "2025-12-31",
  "warrantyMonths": null
}
```

| Campo | Validação | Observação |
|---|---|---|
| `name` | `@NotBlank` | |
| `price` | `@NotNull` + `@Positive` | Deve ser > 0 |
| `category` | `@NotBlank` | |
| `type` | `@NotBlank` | Deve ser `"PERISHABLE"` ou `"NON_PERISHABLE"` |
| `expirationDate` | Sem anotação | Obrigatório via regra de negócio se `type = PERISHABLE` |
| `warrantyMonths` | Sem anotação | Obrigatório via regra de negócio se `type = NON_PERISHABLE` |

---

#### `SaleRequest`

```json
{
  "clientId": 1,
  "discountType": "TEN_PERCENT",
  "items": [
    { "productId": 2, "quantity": 3 },
    { "productId": 5, "quantity": 1 }
  ]
}
```

| Campo | Validação |
|---|---|
| `clientId` | `@NotNull` |
| `discountType` | `@NotBlank` — valores válidos: `NO_DISCOUNT`, `TEN_PERCENT` |
| `items` | `@NotEmpty` + `@Valid` (valida cada item da lista) |

---

#### `SaleItemRequest`

| Campo | Validação |
|---|---|
| `productId` | `@NotNull` |
| `quantity` | `@NotNull` + `@Positive` (deve ser ≥ 1) |

---

#### `StockUpdateRequest`

```json
{ "quantity": 50 }
```

| Campo | Validação |
|---|---|
| `quantity` | `@NotNull` + `@PositiveOrZero` (pode ser 0, não pode ser negativo) |

---

### `dto/response/` — O que sai

#### `ClientResponse`

```json
{
  "id": 1,
  "name": "João Silva",
  "email": "joao@email.com",
  "cpf": "12345678909",
  "phone": "11999999999",
  "registrationDate": "2025-06-18T10:30:00"
}
```

---

#### `ProductResponse`

```json
{
  "id": 2,
  "name": "Leite Integral",
  "description": "Leite pasteurizado 1L",
  "price": 5.99,
  "category": "Laticínios",
  "type": "PERISHABLE",
  "expirationDate": "2025-12-31",
  "warrantyMonths": null
}
```

---

#### `SaleResponse`

```json
{
  "id": 1,
  "clientId": 1,
  "clientName": "João Silva",
  "saleDate": "2025-06-18T14:00:00",
  "totalValue": "53.91",
  "items": [
    {
      "productId": 2,
      "productName": "Leite Integral",
      "quantity": 3,
      "unitPrice": 5.99,
      "subtotal": 17.97
    }
  ]
}
```

---

#### `StockResponse`

```json
{
  "productId": 2,
  "productName": "Leite Integral",
  "quantity": 47
}
```

---

### `dto/validator/` — Validação Customizada de CPF

#### `CPF.java` — anotação customizada

```java
@CPF(message = "O CPF fornecido é matematicamente inválido.")
String cpf
```

Funciona como qualquer anotação Bean Validation (`@NotBlank`, `@Email`), integrada automaticamente ao pipeline de validação do Spring.

#### `CPFValidator.java` — lógica real

1. Remove pontos e traços (aceita CPF formatado ou só números).
2. Verifica se tem 11 dígitos e se não são todos iguais (ex: `111.111.111-11`).
3. Executa o **cálculo matemático dos dois dígitos verificadores** conforme o algoritmo da Receita Federal.
4. Retorna `false` se qualquer verificação falhar → Spring lança `MethodArgumentNotValidException` → resposta HTTP 400.

---

## 8. Camada de Mapeamento — `mapper/`

Os mappers são `@Component` do Spring, injetados nos serviços via `@RequiredArgsConstructor`.

### `ClientMapper`

| Método | Entrada | Saída | O que faz |
|---|---|---|---|
| `toEntity(request)` | `ClientRequest` | `Client` | Monta a entidade com os dados do request (sem `id` e `registrationDate`) |
| `toResponse(client)` | `Client` | `ClientResponse` | Converte a entidade salva em record de resposta |

---

### `ProductMapper`

| Método | Entrada | Saída | O que faz |
|---|---|---|---|
| `toEntity(request)` | `ProductRequest` | `Product` | Verifica o campo `type` e instancia `PerishableProduct` ou `NonPerishableProduct` com os campos corretos |
| `toResponse(entity)` | `Product` | `ProductResponse` | Usa `instanceof` para detectar o subtipo e preencher os campos específicos (`expirationDate` ou `warrantyMonths`) |

> O `ProductMapper` é o ponto central de **decisão de subtipo** — é ele que interpreta o campo `type` do request e cria a instância correta.

---

### `SaleMapper`

Existe como arquivo mas está **vazio**. O mapeamento da venda é feito inline no `SaleServiceImpl` pelo método privado `mapToResponse()`.

---

## 9. Camada de Serviço — `service/`

### Interfaces (`service/interfaces/`)

Cada serviço tem uma interface que define seu contrato. Os controllers dependem **sempre da interface**, nunca da implementação concreta. Isso segue o princípio da Inversão de Dependência (DIP do SOLID).

| Interface | Métodos |
|---|---|
| `IClientService` | `create`, `findById`, `findAll`, `update`, `delete` |
| `IProductService` | `create`, `findById`, `findAll`, `update`, `delete` |
| `ISaleService` | `executeSale`, `findAll` |
| `IStockService` | `findByProductId`, `updateQuantity`, `decrementStock`, `findAll` |

---

### `ClientServiceImpl`

| Método | O que faz |
|---|---|
| `create()` | Verifica duplicidade de CPF e e-mail antes de salvar. Lança `BusinessException` (422) se já existir. |
| `findById()` | Busca por ID. Lança `ResourceNotFoundException` (404) se não existir. |
| `findAll()` | Retorna todos os clientes mapeados para `ClientResponse`. |
| `update()` | Busca o cliente, verifica se o CPF novo pertence a outro cliente e atualiza todos os campos. |
| `delete()` | Verifica existência antes de deletar. Se o cliente tiver vendas vinculadas, o banco lança `DataIntegrityViolationException` (capturada pelo `GlobalExceptionHandler` como 409). |

---

### `ProductServiceImpl`

| Método | O que faz |
|---|---|
| `create()` | Valida campos obrigatórios por subtipo, cria o produto via mapper e **inicializa o estoque zerado** automaticamente (`Stock.quantity = 0`). |
| `findById()` | Busca por ID, lança 404 se não existir. |
| `findAll()` | Lista todos os produtos. |
| `update()` | Atualiza apenas os campos comuns (`name`, `description`, `price`, `category`). Não permite trocar o subtipo do produto. |
| `delete()` | Deleta o produto. O estoque é removido em cascata. Se houver itens de venda vinculados, o banco recusa com `DataIntegrityViolationException` (409). |

---

### `SaleServiceImpl`

O mais complexo do sistema. O método `executeSale()` orquestra todo o processo de venda em 6 etapas:

```
1. Valida se o cliente existe (ResourceNotFoundException 404 se não)
2. Valida se a estratégia de desconto existe (BusinessException 422 se não)
3. Cria o cabeçalho da Sale (sem total ainda)
4. Para cada item:
   a. Busca o produto (ResourceNotFoundException 404 se não)
   b. Chama stockService.decrementStock() que:
      - Verifica se produto tem restrição de venda (produto vencido → BusinessException 422)
      - Verifica se tem estoque suficiente (InsufficientStockException 422)
      - Decrementa a quantidade no banco
   c. Cria o SaleItem com o preço histórico do produto
5. Chama calculator.calculateTotal() com a estratégia escolhida
6. Salva a Sale (cascata salva os SaleItems automaticamente) e retorna SaleResponse
```

| Método | O que faz |
|---|---|
| `executeSale()` | Orquestra toda a lógica de venda (ver acima). `@Transactional` garante rollback se qualquer etapa falhar. |
| `findAll()` | Lista todas as vendas com seus itens. `@Transactional(readOnly = true)` para otimização. |

---

### `StockServiceImpl`

| Método | O que faz |
|---|---|
| `findByProductId()` | Busca o estoque pelo ID do produto. Lança 404 se não existir. |
| `updateQuantity()` | Atualiza a quantidade no estoque manualmente (usado para reabastecimento). |
| `decrementStock()` | **Método interno** (não exposto diretamente via HTTP). Chamado pelo `SaleServiceImpl` durante uma venda. Valida restrição de venda do produto (`hasSalesRestriction()`), verifica se há estoque suficiente e subtrai a quantidade. |
| `findAll()` | Lista o estoque de todos os produtos. |

---

## 10. Camada de Estratégia — `strategy/`

Implementa o **padrão de design Strategy** para o cálculo do total das vendas.

### `SalesCalculator` (interface)

```java
BigDecimal calculateTotal(List<SaleItem> items);
```

Qualquer nova estratégia de desconto só precisa implementar esta interface e ser anotada com `@Component("NOME")`.

### Como o Spring injeta a estratégia certa

O `SaleServiceImpl` recebe um `Map<String, SalesCalculator>` injetado automaticamente pelo Spring. O Spring preenche este mapa com todos os beans que implementam `SalesCalculator`, usando o nome do `@Component` como chave.

```java
// No SaleServiceImpl:
SalesCalculator calculator = calculatorStrategies.get(request.discountType().toUpperCase());
```

Se o `discountType` enviado na request não existir no mapa → `BusinessException` (422).

### `NoDiscountCalculator` — `@Component("NO_DISCOUNT")`

Soma simples: `Σ (unitPrice × quantity)` para cada item.

### `TenPercentDiscountCalculator` — `@Component("TEN_PERCENT")`

Calcula o subtotal igual ao acima, depois multiplica por `0.90` (aplica 10% de desconto).

> Para adicionar um novo desconto (ex: 20%), basta criar uma nova classe com `@Component("TWENTY_PERCENT")` — **zero alteração** no código existente.

---

## 11. Camada de Exceções — `exception/`

### Exceções customizadas

| Classe | Herda de | Quando lançar |
|---|---|---|
| `ResourceNotFoundException` | `RuntimeException` | Recurso não encontrado no banco (404) |
| `InsufficientStockException` | `RuntimeException` | Estoque insuficiente para a venda (422) |
| `BusinessException` | `RuntimeException` | Qualquer outra regra de negócio violada (422) |

Todas são **unchecked exceptions** (herdam de `RuntimeException`), então não precisam de `throws` na assinatura dos métodos.

---

### `GlobalExceptionHandler`

Anotado com `@RestControllerAdvice` — intercepta qualquer exceção lançada em qualquer controller da aplicação e converte para uma resposta JSON padronizada.

| Exceção capturada | HTTP Status | Campo `"error"` |
|---|---|---|
| `ResourceNotFoundException` | 404 | "Recurso não encontrado" |
| `InsufficientStockException` | 422 | "Estoque insuficiente" |
| `BusinessException` / `IllegalStateException` | 422 | "Regra de negócio violada" |
| `MethodArgumentNotValidException` | 400 | "Erro de validação nos campos enviados" |
| `HttpMessageNotReadableException` | 400 | "Erro na leitura da requisição" |
| `DataIntegrityViolationException` | 409 | "Conflito de Integridade no Banco de Dados" |

**Formato padrão de erro:**
```json
{
  "status": 422,
  "error": "Estoque insuficiente",
  "message": "O produto 'Leite Integral' possui apenas 2 unidade(s) em estoque.",
  "timestamp": "2025-06-18T14:30:00"
}
```

**Formato para erros de validação (400):**
```json
{
  "status": 400,
  "error": "Erro de validação nos campos enviados",
  "timestamp": "2025-06-18T14:30:00",
  "errors": {
    "cpf": "O CPF fornecido é matematicamente inválido.",
    "email": "O formato do e-mail é inválido."
  }
}
```

---

## 12. Camada de Controller — `controller/`

Os controllers são finos: apenas recebem a request HTTP, delegam para o service e devolvem `ResponseEntity`. Nenhuma lógica de negócio vive aqui.

Todos usam:
- `@RestController` — combina `@Controller` + `@ResponseBody`
- `@RequiredArgsConstructor` — injeta o service via construtor (Lombok)
- `@Valid` — ativa o Bean Validation nos parâmetros anotados

---

## 13. CRUD Completo por Entidade

### Cliente — `/api/clients`

| Operação | Método HTTP | Endpoint | Body | Retorno | Status |
|---|---|---|---|---|---|
| Criar | POST | `/api/clients` | `ClientRequest` | `ClientResponse` | 201 |
| Listar todos | GET | `/api/clients` | — | `List<ClientResponse>` | 200 |
| Buscar por ID | GET | `/api/clients/{id}` | — | `ClientResponse` | 200 |
| Atualizar | PUT | `/api/clients/{id}` | `ClientRequest` | `ClientResponse` | 200 |
| Deletar | DELETE | `/api/clients/{id}` | — | — | 204 |

---

### Produto — `/api/products`

| Operação | Método HTTP | Endpoint | Body | Retorno | Status |
|---|---|---|---|---|---|
| Criar | POST | `/api/products` | `ProductRequest` | `ProductResponse` | 201 |
| Listar todos | GET | `/api/products` | — | `List<ProductResponse>` | 200 |
| Buscar por ID | GET | `/api/products/{id}` | — | `ProductResponse` | 200 |
| Atualizar | PUT | `/api/products/{id}` | `ProductRequest` | `ProductResponse` | 200 |
| Deletar | DELETE | `/api/products/{id}` | — | — | 204 |

> Ao criar um produto, o estoque é criado automaticamente com `quantity = 0`.

---

### Venda — `/api/sales`

| Operação | Método HTTP | Endpoint | Body | Retorno | Status |
|---|---|---|---|---|---|
| Registrar venda | POST | `/api/sales` | `SaleRequest` | `SaleResponse` | 201 |
| Listar todas | GET | `/api/sales` | — | `List<SaleResponse>` | 200 |

> Vendas **não têm update nem delete** — são imutáveis por design do sistema.

---

### Estoque — `/api/stock`

| Operação | Método HTTP | Endpoint | Body | Retorno | Status |
|---|---|---|---|---|---|
| Listar todo o estoque | GET | `/api/stock` | — | `List<StockResponse>` | 200 |
| Buscar por produto | GET | `/api/stock/{productId}` | — | `StockResponse` | 200 |
| Atualizar quantidade | PUT | `/api/stock/{productId}` | `StockUpdateRequest` | `StockResponse` | 200 |

> Estoque **não tem create nem delete** — é gerenciado automaticamente pelo ciclo de vida do produto e das vendas.

---

## 14. Fluxo de uma Requisição — Passo a Passo

### Exemplo: registrar uma venda (`POST /api/sales`)

```
HTTP POST /api/sales
        │
        ▼
SaleController.create()
  │  Recebe SaleRequest, aplica @Valid
  │  @Valid falha? → MethodArgumentNotValidException → GlobalExceptionHandler → 400
        │
        ▼
SaleServiceImpl.executeSale()
  │
  ├─ 1. clientRepository.findById(clientId)
  │       Não existe? → ResourceNotFoundException → GlobalExceptionHandler → 404
  │
  ├─ 2. calculatorStrategies.get(discountType)
  │       Não existe? → BusinessException → GlobalExceptionHandler → 422
  │
  ├─ 3. Cria objeto Sale em memória (sem salvar ainda)
  │
  ├─ 4. Para cada item do request:
  │     ├─ productRepository.findById(productId)
  │     │     Não existe? → ResourceNotFoundException → 404
  │     │
  │     ├─ stockService.decrementStock(productId, quantity)
  │     │     ├─ stockRepository.findByProductId()
  │     │     ├─ product.hasSalesRestriction()
  │     │     │     true? → BusinessException → 422 (produto vencido)
  │     │     ├─ stock.quantity < quantity?
  │     │     │     true? → InsufficientStockException → 422
  │     │     └─ stock.quantity -= quantity → stockRepository.save()
  │     │
  │     └─ Cria SaleItem com preço histórico → sale.addItem(item)
  │
  ├─ 5. calculator.calculateTotal(sale.getItems())
  │       Aplica NO_DISCOUNT ou TEN_PERCENT
  │
  └─ 6. saleRepository.save(sale)
          Salva Sale + SaleItems em cascata no banco H2
          Retorna SaleResponse com todos os dados

        │
        ▼
ResponseEntity.status(201).body(saleResponse)
```

---

## 15. Banco de Dados e Tabelas

O Hibernate cria e destrói as tabelas automaticamente (`ddl-auto=create-drop`).

```
tb_client
├── id (PK)
├── name
├── email (UNIQUE)
├── cpf (UNIQUE)
├── phone
└── registration_date

tb_product (tabela base da hierarquia)
├── id (PK)
├── name
├── description
├── price
└── category

tb_perishable_product
├── id (PK, FK → tb_product.id)
└── expiration_date

tb_non_perishable_product
├── id (PK, FK → tb_product.id)
└── warranty_months

tb_stock
├── id (PK)
├── product_id (FK → tb_product.id, UNIQUE)
└── quantity

tb_sale
├── id (PK)
├── client_id (FK → tb_client.id)
├── sale_date
└── total_value

tb_sale_item
├── id (PK)
├── sale_id (FK → tb_sale.id)
├── product_id (FK → tb_product.id)
├── quantity
└── unit_price
```

> Acesse o console visual do H2 em: `http://localhost:8080/h2-console`  
> JDBC URL: `jdbc:h2:mem:vendasdb` · Usuário: `sa` · Senha: (vazia)

---

## 16. Princípios SOLID Aplicados

### S — Single Responsibility Principle
Cada classe tem uma única responsabilidade. O decremento de estoque **não vive no SaleService** — ele é delegado ao `StockService.decrementStock()`. O `SaleService` apenas orquestra.

### O — Open/Closed Principle
O sistema de descontos é aberto para extensão e fechado para modificação. Para adicionar `TWENTY_PERCENT`, basta criar uma nova classe com `@Component("TWENTY_PERCENT")` — nenhuma linha existente precisa ser tocada.

### L — Liskov Substitution Principle
`PerishableProduct` e `NonPerishableProduct` substituem `Product` sem quebrar o contrato. O método `hasSalesRestriction()` é chamado polimorficamente — o código do `StockService` não sabe qual subtipo está usando, só chama o método.

### I — Interface Segregation Principle
As interfaces de service (`IClientService`, `IProductService`, etc.) são específicas para cada domínio. Nenhum service é forçado a implementar métodos que não usa.

### D — Dependency Inversion Principle
Os controllers dependem das **interfaces** (`IClientService`), não das implementações (`ClientServiceImpl`). O Spring injeta a implementação concreta via DI.

---

## 17. Tratamento de Erros

O sistema nunca deixa uma exceção chegar ao cliente como um stack trace. Toda exceção é capturada pelo `GlobalExceptionHandler` e convertida em JSON estruturado.

**Regra de mapeamento:**

```
ResourceNotFoundException    → 404 Not Found
InsufficientStockException   → 422 Unprocessable Entity
BusinessException            → 422 Unprocessable Entity
IllegalStateException        → 422 Unprocessable Entity
MethodArgumentNotValidException → 400 Bad Request
HttpMessageNotReadableException → 400 Bad Request
DataIntegrityViolationException → 409 Conflict
```

---

## 18. Configuração da Aplicação

Arquivo: `src/main/resources/application.properties`

```properties
# Porta do servidor
server.port=8080

# Banco H2 em memória
spring.datasource.url=jdbc:h2:mem:vendasdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Console web do H2
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Hibernate: recria tabelas a cada startup
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Exibe SQL no terminal (útil para debug)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Swagger UI
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/api-docs
```

---

*Documento gerado com base na leitura completa do código-fonte em junho de 2026.*
