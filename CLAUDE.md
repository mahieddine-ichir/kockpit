# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

Kockpit is a multi-module Java/Spring Boot project with React frontend components. The project is organized into the following main modules:

- **kockpit-backends**: Backend services and APIs
  - `kockpit-backend-application`: Main Spring Boot application (port 8080)
  - `kockpit-backend-service-*`: Service layer implementations (storage, search, dashboard)
  - `kockpit-backend-authentication-basic`: Basic authentication module
- **kockpit-rules**: Rules engine with web-based designer
  - `kockpit-rules-engine-parent`: Core rules engine implementation
  - `kockpit-rules-designer`: React/Vite frontend for rule design (BPMN-based)
  - `kockpit-rules-maven-plugin`: Maven plugin for DSL generation
- **kockpit-features**: Feature modules
  - `kockpit-features-dynaconfig`: Dynamic configuration
  - `kockpit-features-heartbeat`: Health monitoring
  - `kockpit-features-featureflipping`: Feature toggle management
- **kockpit-audit**: Audit and streaming components
  - Stream applications for Kafka and Kinesis
  - Console UI for audit visualization
- **kockpit-samples**: Sample applications and examples
- **kockpit-core**: Core libraries and shared components

## Build and Development Commands

### Maven (Java Backend)
```bash
# Build entire project
mvn clean install

# Build specific module
mvn clean install -pl kockpit-backends/kockpit-backend-application

# Run backend application (default OpenSearch profile)
mvn spring-boot:run -pl kockpit-backends/kockpit-backend-application

# Run with Azure profile
mvn spring-boot:run -pl kockpit-backends/kockpit-backend-application -Pazure

# Run with filesystem profile
mvn spring-boot:run -pl kockpit-backends/kockpit-backend-application -Pfilesystem

# Run tests
mvn test
```

### Node.js/React (Rules Designer Frontend)
```bash
# Navigate to rules designer
cd kockpit-rules/kockpit-rules-designer

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Run linting
npm run lint

# Start Storybook for component development
npm run storybook
```

### Docker Infrastructure
```bash
# Start OpenSearch and dashboards
docker-compose -f docker/docker-compose.yml up -d

# OpenSearch available at: http://localhost:9200
# OpenSearch Dashboards at: http://localhost:5601
```

## Architecture Overview

### Backend Architecture
- **Spring Boot 4.1** (Spring Framework 7, Jakarta EE 11) with **Java 21** (build and runtime — JDK 21 required, not 25)
- **Spring AI 2.x** for the MCP server (`kockpit-ai`) — bump RC → GA required before any Maven Central release
- **Modular design** with separate service layers for storage, search, and dashboard
- **Multiple storage backends**: Azure, filesystem, OpenSearch
- **Authentication**: Basic authentication with extensible design
- **Profile-based configuration**: Different profiles for different deployment scenarios

### Spring Boot 4 conventions (kockpit 2.x)
- **Fine-grained starters only**: `spring-boot-starter-webmvc` (never the deprecated `spring-boot-starter-web` alias), `spring-boot-starter-webmvc-test` for MockMvc tests (`spring-boot-starter-test` alone is not enough). Relocated modules in use: `spring-boot-kafka` (KafkaProperties), `spring-boot-restclient`, `spring-boot-webclient`.
- **Jackson coexistence**: applications serialize web payloads with **Jackson 3** (`tools.jackson`, the Boot-autoconfigured ObjectMapper bean); **Jackson 2** (`com.fasterxml`) is kept ONLY for OpenSearch client mappers, always as locally-instantiated ObjectMappers with explicit `jackson-databind` deps (versions managed by Boot's `jackson-2-bom`). Never inject the Boot ObjectMapper bean as `com.fasterxml...ObjectMapper`.
- **In-house auto-configurations**: registered via `META-INF/spring/...AutoConfiguration.imports`, annotated `@AutoConfiguration` (implies `proxyBeanMethods=false` — no inter-@Bean method calls).
- **Versioning**: kockpit 2.x = Spring Boot 4 / Java 21 only (breaking change); Boot 3.x consumers stay on kockpit 1.x. The `kockpit-rules-maven-plugin` 2.x requires JDK 21 on consumer projects.
- **CI**: use `-Dmaven.test.skip=true` (not `-DskipTests`) to skip tests in image/deploy builds — Boot 4's plugin only honors `maven.test.skip` for test AOT.

### Frontend Architecture
- **React 18** with TypeScript and Vite build system
- **BPMN.js integration** for visual rule design
- **Tailwind CSS** for styling with Radix UI components
- **Storybook** for component development and documentation
- **FlexLayout** for advanced layout management

### Rules Engine
- **Visual designer** using BPMN notation for business rules
- **JSON-based rule definitions** with schema validation
- **Maven plugin** for code generation from rule definitions
- **Multiple execution contexts** with extensible action framework

### Data Flow
1. Rules are designed visually in the React frontend
2. Rules are exported as JSON following the kockpit rules schema
3. Maven plugin generates Java code from rule definitions
4. Backend executes rules using the generated classes
5. Audit events are streamed to Kafka/Kinesis for analysis

## Development Notes

### Running the Full Stack
1. Start OpenSearch: `docker-compose -f docker/docker-compose.yml up -d`
2. Build backend: `mvn clean install`
3. Start backend: `mvn spring-boot:run -pl kockpit-backends/kockpit-backend-application`
4. Start rules designer: `cd kockpit-rules/kockpit-rules-designer && npm run dev`

### Testing Strategy
- **Backend**: Standard Maven test lifecycle with Spring Boot Test
- **Frontend**: Component testing with Storybook
- **Integration**: Docker-compose based integration testing

### Configuration Profiles
- **opensearch** (default): Uses OpenSearch for search and storage
- **azure**: Uses Azure storage services
- **filesystem**: Uses local filesystem storage