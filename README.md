# 🚗 Veículos API

API REST para gerenciamento de veículos desenvolvida como parte do processo seletivo da **Tinnova**.

## 📋 Sobre o Projeto

Sistema completo de CRUD de veículos com autenticação JWT, controle de acesso baseado em roles (USER/ADMIN), integração com APIs externas para conversão de moeda em tempo real e cache com Redis.

### ✨ Funcionalidades Principais

- ✅ Autenticação e autorização com JWT
- ✅ CRUD completo de veículos com soft delete
- ✅ Filtros e consultas customizadas (marca, ano, cor, range de preço)
- ✅ Conversão automática de preços BRL → USD em tempo real
- ✅ Cache de cotação do dólar com Redis
- ✅ Relatórios (veículos agrupados por marca)
- ✅ Paginação e ordenação
- ✅ Documentação interativa com Swagger
- ✅ Tratamento de erros padronizado
- ✅ Validação de dados com Bean Validation

---

## 🛠️ Tecnologias Utilizadas

### Core
- **Java 17**
- **Spring Boot 3.2.1**
- **Maven 3.x**

### Frameworks e Bibliotecas
- **Spring Data JPA** - Persistência de dados
- **Spring Security** - Autenticação e autorização
- **Spring Data Redis** - Cache distribuído
- **Spring WebFlux** - Cliente HTTP reativo para APIs externas
- **PostgreSQL** - Banco de dados principal
- **H2 Database** - Banco em memória para testes
- **JWT (jjwt 0.12.3)** - Tokens de autenticação
- **Lombok** - Redução de boilerplate
- **SpringDoc OpenAPI 2.3.0** - Documentação Swagger
- **JaCoCo 0.8.11** - Cobertura de testes

### APIs Externas
- **AwesomeAPI** - Cotação USD/BRL (principal)
- **Frankfurter API** - Cotação USD/BRL (fallback)

---

## 🏗️ Arquitetura
```
veiculos-api/
├── src/main/java/com/tinnova/veiculos/
│   ├── config/          # Configurações (Security, Swagger, Redis, WebClient)
│   ├── controller/      # Endpoints REST
│   ├── dto/             # Request/Response DTOs
│   ├── entity/          # Entidades JPA
│   ├── enums/           # Enumerações (Role)
│   ├── exception/       # Exceções customizadas e handlers
│   ├── repository/      # Repositórios JPA
│   ├── security/        # Filtros e utilitários JWT
│   └── service/         # Lógica de negócio
└── src/test/java/       # Testes unitários e de integração
```

---

## 🚀 Como Executar

### Pré-requisitos

- Java 17+
- Docker e Docker Compose (para PostgreSQL e Redis)
- Maven 3.x (ou use o wrapper `./mvnw`)

### 1️⃣ Clone o repositório
```bash
git clone https://github.com/seu-usuario/veiculos-api.git
cd veiculos-api
```

### 2️⃣ Suba os serviços (PostgreSQL + Redis)
```bash
docker-compose up -d
```

### 3️⃣ Execute a aplicação

**Usando Maven Wrapper (recomendado):**
```bash
./mvnw spring-boot:run
```

**Ou usando Maven instalado:**
```bash
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

---

## 📚 Documentação da API

### Swagger UI
Acesse a documentação interativa em:
```
http://localhost:8080/swagger-ui.html
```

### Usuários Pré-cadastrados

A aplicação cria automaticamente dois usuários para testes:

| Username | Password  | Role  | Permissões |
|----------|-----------|-------|------------|
| `admin`  | `admin123`| ADMIN | Acesso total (GET, POST, PUT, PATCH, DELETE) |
| `user`   | `user123` | USER  | Somente leitura (GET) |

## 🧪 Testes

### Executar todos os testes
```bash
./mvnw test
```

### Gerar relatório de cobertura
```bash
./mvnw clean test jacoco:report
```

O relatório será gerado em: `target/site/jacoco/index.html`

### 📊 Cobertura de Testes Atual

- **Cobertura Geral:** 91% ✅
- **Controllers:** 100%
- **Services:** 88%
- **Security:** 96%

**Nível atingido:** Sênior (requisito: ≥75%) 🏆

### Tipos de Testes Implementados

✅ **Testes Unitários**
- Controllers com mocks
- Services com validações de negócio
- Repositories com queries customizadas

✅ **Testes de Integração**
- Fluxo completo: autenticação → criação → consulta → atualização → remoção
- Cenários de erro (401, 403, 409)
- Validação de payloads

✅ **Testes de Segurança**
- Controle de acesso por role
- Validação de JWT
- Endpoints protegidos

---

## ⚙️ Configuração

### Variáveis de Ambiente

Você pode customizar a aplicação através do `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/veiculos_db
    username: postgres
    password: postgres
  
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: your-secret-key-here
  expiration: 3600000  # 1 hora

exchange:
  api:
    primary:
      url: https://economia.awesomeapi.com.br/json/last/USD-BRL
    fallback:
      url: https://api.frankfurter.app/latest?from=USD&to=BRL
```

---

## 🐳 Docker Compose

O projeto inclui um `docker-compose.yml` para facilitar o setup:
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: veiculos-postgres
    environment:
      POSTGRES_DB: veiculos_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    container_name: veiculos-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

---

## 📦 Build para Produção
```bash
./mvnw clean package -DskipTests
```

O JAR será gerado em: `target/veiculos-api-0.0.1-SNAPSHOT.jar`

### Executar o JAR
```bash
java -jar target/veiculos-api-0.0.1-SNAPSHOT.jar
```

---

## 🎯 Decisões Técnicas

### Cache com Redis
- A cotação do dólar é cacheada por 1 hora
- Reduz chamadas às APIs externas
- Melhora performance e disponibilidade

### Soft Delete
- Veículos não são removidos fisicamente do banco
- Mantém histórico e auditoria
- Campo `ativo` controla visibilidade

### Conversão de Moeda
- Preços são armazenados em USD no banco
- Conversão BRL → USD acontece no cadastro/atualização
- API primária com fallback automático

### Segurança
- JWT com expiração configurável
- Senhas criptografadas com BCrypt
- Controle granular por role (USER/ADMIN)

---

## 📝 Requisitos Atendidos

✅ Todos os endpoints especificados  
✅ Autenticação JWT com roles USER/ADMIN  
✅ Validação de dados com Bean Validation  
✅ Soft delete implementado  
✅ Paginação e ordenação  
✅ Filtros combinados (marca, ano, cor, preço)  
✅ Integração com APIs externas de câmbio  
✅ Cache com Redis  
✅ Documentação Swagger/OpenAPI  
✅ Testes automatizados (91% de cobertura)  
✅ Tratamento de erros padronizado  
✅ README completo com instruções

---

## 👨‍💻 Autor

**[Patrick da Silva Oliveira]**
- GitHub: [@patrick-oliveira-dev](https://github.com/patrick-oliveira-dev)
- LinkedIn: [Patrick Oliveira](https://www.linkedin.com/in/patrickoliveira-dev/)
- Email: patrick.oliveira.dev@gmail.com

---

## 📄 Licença

Este projeto foi desenvolvido como parte de um processo seletivo para a **Tinnova**.