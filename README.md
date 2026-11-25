# LembreteZap

Aplicação desktop em **Java 21 (Maven + Swing + JDBC/MySQL)** para gerenciamento de compromissos pessoais e envio de lembretes via WhatsApp, estruturada em camadas com o padrão comportamental **GoF Command** e instrumentação completa com **`java.util.logging`**.

> Trabalho dedicado à disciplina de **Programação Orientada a Objetos II (POO II)**.

---

## Pré-requisitos

- **Java JDK 21** (`maven.compiler.release` configurado para `21` no `pom.xml`)
- **Apache Maven 3.9+**
- **MySQL 8+** (conector configurado: `com.mysql:mysql-connector-j` `8.4.0`)
- Navegador web padrão configurado no sistema operacional (utilizado por `Desktop.getDesktop().browse(URI)` para abrir links `https://wa.me/...`)

---

## Estrutura do Projeto

```text
LembreteZap/
├── pom.xml                                  # Configuração Maven (Java 21, MySQL Connector/J, JUnit 5, exec-maven-plugin)
├── sql/
│   └── schema.sql                           # DDL do banco/tabela `compromisso` + 3 registros de exemplo
├── logs/
│   └── lembretezap.log                      # Arquivo de log gerado em tempo de execução
└── src/
    ├── main/
    │   ├── resources/
    │   │   ├── db.properties.example        # Modelo de configuração JDBC
    │   │   └── db.properties                # Configuração local do banco (ignorado no Git)
    │   └── java/br/edu/poo2/lembretezap/
    │       ├── Main.java                    # Bootstrap: inicializa logs, Look and Feel e abre a GUI na EDT
    │       ├── model/                       # Compromisso, SituacaoCompromisso, FiltroCompromisso
    │       ├── dao/                         # ConnectionFactory, CompromissoDAO, CompromissoJdbcDAO
    │       ├── service/                     # WhatsAppService (montagem de mensagem/URL wa.me e abertura no navegador)
    │       ├── command/                     # Command, CommandInvoker e os 4 comandos GoF
    │       ├── logging/                     # LogConfig, MyFormatter, DebugLogFilter
    │       └── view/                        # TelaPrincipalFrame, CompromissoTableModel, CompromissoCellRenderer
    └── test/java/br/edu/poo2/lembretezap/   # Testes unitários JUnit 5 (command, dao, logging, service, view)
```

---

## 1. Configuração do Banco de Dados (`sql/schema.sql`)

Execute o script [`sql/schema.sql`](sql/schema.sql) no seu servidor MySQL:

```bash
mysql -u root -p < sql/schema.sql
```

Ou abra o arquivo no **MySQL Workbench** / cliente SQL de sua preferência e execute todo o conteúdo.

O script realiza as seguintes ações:
- Cria o banco `lembretezap` (`utf8mb4` / `utf8mb4_unicode_ci`), se ainda não existir;
- Cria a tabela `compromisso` com as colunas `codigo` (`INT AUTO_INCREMENT PRIMARY KEY`), `descricao`, `data_compromisso`, `horario`, `nome_contato`, `telefone` e `situacao` (`ENUM('PENDENTE', 'CONCLUIDO')`);
- Insere **3 registros iniciais** de exemplo (1 pendente atrasado, 1 pendente futuro e 1 concluído).

---

## 2. Configuração do `db.properties`

As credenciais JDBC são lidas do classpath a partir de `src/main/resources/db.properties` pela classe `ConnectionFactory` (`src/main/java/br/edu/poo2/lembretezap/dao/ConnectionFactory.java`).

1. Caso ainda não exista localmente, copie o modelo [`src/main/resources/db.properties.example`](src/main/resources/db.properties.example) para `src/main/resources/db.properties`:

```powershell
Copy-Item src/main/resources/db.properties.example src/main/resources/db.properties
```

2. Ajuste URL, usuário e senha conforme o seu ambiente MySQL:

```properties
db.url=jdbc:mysql://localhost:3306/lembretezap?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo
db.user=root
db.password=sua_senha_aqui
```

> **Nota:** `db.properties` está listado no `.gitignore` para evitar versionar credenciais locais.

---

## 3. Como Executar a Aplicação e os Testes

### Executar a aplicação (GUI Swing)

Na raiz do projeto (`LembreteZap/`), execute:

```bash
mvn compile exec:java
```

*(O plugin `exec-maven-plugin` no `pom.xml` já aponta `mainClass` para `br.edu.poo2.lembretezap.Main`.)*

### Executar os testes unitários

```bash
mvn test
```

> **Importante:** Feche qualquer instância aberta do **LembreteZap** antes de rodar `mvn test`. Enquanto a aplicação está em execução, o `FileHandler` mantém o arquivo de trava `logs/lembretezap.log.lck` ativo; se um teste rodar em paralelo, o `java.util.logging` redirecionará a saída para `logs/lembretezap.log.1`, fazendo o teste `LogConfigTest` falhar ao verificar `logs/lembretezap.log`.

---

## 4. Localização dos Logs (`logs/lembretezap.log`)

Ao iniciar a aplicação via `Main.main`, o método `LogConfig.init()` cria automaticamente o diretório `logs/` (na raiz do projeto) e grava os registros em:

```text
logs/lembretezap.log
```

Cada registro segue em uma única linha no formato definido por `MyFormatter`:

```text
threadID::level::sourceClassName::sourceMethodName::date::message
```

Exemplo real gerado pela aplicação:

```text
1::FINE::br.edu.poo2.lembretezap.logging.LogConfig::init::Tue Nov 24 22:04:56 BRT 2026::Subsistema de logging inicializado com sucesso.
1::FINE::br.edu.poo2.lembretezap.command.CommandInvoker::executar::Tue Nov 24 22:04:56 BRT 2026::Iniciando execução do comando: CadastrarCompromissoCommand
```

---

## 5. Roteiro Rápido para o Vídeo PiP (`java.util.logging`)

Abaixo estão os pontos exatos do código para apresentar os 5 conceitos do pacote `java.util.logging` e a granularidade de debug:

| Conceito `java.util.logging` | Classe / Arquivo | O que mostrar e explicar |
| :--- | :--- | :--- |
| **Logger** | [`LogConfig.java`](src/main/java/br/edu/poo2/lembretezap/logging/LogConfig.java#L21-L45) e [`Main.java`](src/main/java/br/edu/poo2/lembretezap/Main.java#L20-L27) | `Logger.getLogger("br.edu.poo2.lembretezap")` atua como logger raiz com `Level.ALL` e `setUseParentHandlers(false)`. Os loggers de cada classe (`Main`, `CompromissoJdbcDAO`, `CommandInvoker`, `WhatsAppService`, `TelaPrincipalFrame`) herdam automaticamente essa configuração pela hierarquia de nomes de pacotes. |
| **Handler** | [`LogConfig.java`](src/main/java/br/edu/poo2/lembretezap/logging/LogConfig.java#L50-L74) | `init()` e `configureHandler(...)` anexam dois handlers ao logger raiz: um `FileHandler("logs/lembretezap.log", true)` (persistência em arquivo em modo *append*) e um `ConsoleHandler` (saída no console), ambos com `Level.ALL` e encoding UTF-8. |
| **LogRecord** | [`MyFormatter.java`](src/main/java/br/edu/poo2/lembretezap/logging/MyFormatter.java#L15-L22) e [`CommandInvoker.java`](src/main/java/br/edu/poo2/lembretezap/command/CommandInvoker.java#L27-L32) | Objeto criado pelo `Logger` contendo metadados do evento (`threadID`, `level`, `sourceClassName`, `sourceMethodName`, `millis` e `message`). As classes usam `LOGGER.logp(Level, CLASS_NAME, method, ...)` para preencher classe e método de origem com precisão no `LogRecord`. |
| **Filter** | [`DebugLogFilter.java`](src/main/java/br/edu/poo2/lembretezap/logging/DebugLogFilter.java#L12-L27) | Implementa `Filter#isLoggable(LogRecord)`: aceita registros cujo `loggerName` comece com `br.edu.poo2.lembretezap` ou cujo nível seja `>= WARNING`, evitando poluição de logs internos de bibliotecas Swing/AWT/JDBC nos níveis mais verbosos. |
| **Formatter** | [`MyFormatter.java`](src/main/java/br/edu/poo2/lembretezap/logging/MyFormatter.java#L11-L23) | Estende `Formatter` e sobrescreve `format(LogRecord)` gerando `threadID::level::sourceClassName::sourceMethodName::new Date(millis)::message\n`. |

### Granularidade dos Níveis de Log na Aplicação

- **`FINE`** (ações de alto nível e orquestração):
  - Inicialização da aplicação ([`Main.java`](src/main/java/br/edu/poo2/lembretezap/Main.java#L27));
  - Início e conclusão de comandos no [`CommandInvoker.java`](src/main/java/br/edu/poo2/lembretezap/command/CommandInvoker.java#L27-L32);
  - Confirmação/cancelamento de envio em [`PrepararLembreteWhatsAppCommand.java`](src/main/java/br/edu/poo2/lembretezap/command/PrepararLembreteWhatsAppCommand.java#L46-L53);
  - Cliques de botões, seleção na tabela, aplicação de filtros e abertura/fechamento da janela em `TelaPrincipalFrame.java`.
- **`FINER`** (rastreamento de fluxo — entrada e saída de métodos):
  - Registros `"ENTRY"` e `"RETURN"` em [`ConnectionFactory.java`](src/main/java/br/edu/poo2/lembretezap/dao/ConnectionFactory.java#L36-L63), [`CompromissoJdbcDAO.java`](src/main/java/br/edu/poo2/lembretezap/dao/CompromissoJdbcDAO.java#L72-L113), [`WhatsAppService.java`](src/main/java/br/edu/poo2/lembretezap/service/WhatsAppService.java#L52-L110), nos 4 comandos e em `TelaPrincipalFrame.carregarCompromissos`.
- **`FINEST`** (detalhes internos de debug):
  - URL/usuário JDBC em `ConnectionFactory.java`;
  - Queries SQL, parâmetros vinculados, contagem de linhas e códigos gerados em `CompromissoJdbcDAO.java`;
  - Telefone normalizado, mensagem montada e URL `https://wa.me/...` gerada em [`WhatsAppService.java`](src/main/java/br/edu/poo2/lembretezap/service/WhatsAppService.java#L84-L108);
  - Look and Feel aplicado e eventos detalhados de janela/UI.
- **`WARNING` / `SEVERE`** (falhas e alertas):
  - Falha ao aplicar Look and Feel do sistema (`WARNING` em `Main.java`);
  - Erros de conexão/SQL no DAO, falhas de execução no `CommandInvoker` e erros ao abrir URI/navegador no `WhatsAppService` (`SEVERE`).
