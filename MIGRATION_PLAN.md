# MIGRATION_PLAN — Spring Boot 3.5.11 → 4.1.0, Spring AI 1.x → 2.0, Java 21

> Document VIVANT. Cocher chaque item `[x]` immédiatement après validation, avec le hash du commit.
> Branche d'exécution : `migration/sb4-spring-ai2`. Version cible : `2.0.0-SNAPSHOT`.
> Audit réalisé le 2026-06-11 (Phase 1, 8 subagents). Docs de référence : `docs/migration/`.

## Verdict de l'audit

**AUCUN BLOQUANT CRITIQUE.** Tous les points « bloquants à vérifier » sont résolus favorablement :

| Point | Verdict |
|---|---|
| Spring AI 2.0 vs Boot 4.1.0 | ✅ Compatible. RC1/RC2 sont construits contre **Boot 4.1.0-RC1** (pom officiel, issue spring-ai#5845 close en M5). Pas de repli Boot 4.0.x nécessaire. Dernière version au 11/06 : **2.0.0-RC2** (09/06/2026), pas de GA. Baseline Java de Spring AI 2 : 17 (Java 21 OK). |
| `spring-boot-jackson2` en 4.1 | ✅ **Existe en 4.1.0** (vérifié Maven Central), non déprécié en 4.1. `spring-boot-dependencies:4.1.0` gère `jackson-2-bom 2.21.4` ET `jackson-bom 3.1.4`. Coexistence Jackson 2/3 supportée (packages `com.fasterxml` vs `tools.jackson`, seul `jackson-annotations` partagé, conçu pour). |
| opensearch-java | ✅ avec action : 3.0.0 → **3.9.0** (support Jackson 3 ajouté, Jakarta OK, aligné httpclient5 **5.6** imposé par Boot 4.1 — les pins httpclient5 5.4.4 actuels sont à retirer). Jackson 2 reste le mapper par défaut → stratégie « mapper Jackson 2 isolé » conservée. |
| spring-kafka | ✅ avec action : version managée par Boot 4.1.0 = **4.1.0**. Cassures : `KafkaProperties` relocalisée (`org.springframework.boot.kafka.autoconfigure`, artefact `spring-boot-kafka`), `buildConsumerProperties()` sans argument supprimé, renommages converters Jackson (`JsonMessageConverter`→`JacksonJsonMessageConverter`...), `spring.kafka.retry.topic.backoff.random`→`.jitter`. Event Hub via Kafka/SASL_SSL : **non impacté**. |
| SDK AWS v2 (2.41.10) | ✅ Java 21 OK, pas de dépendance jakarta. SigV4 via `AwsSdk2Transport` d'opensearch-java (pas d'interceptor custom) : OK avec 3.9.0. |
| SDK Azure Storage (BOM 1.2.33) | ✅ Java 21 OK, Jackson 2 interne sans conflit avec Jackson 3. Bump BOM 1.3.7 possible mais NON requis → hors périmètre. |
| Plugin Maven rules | ✅ Aucune dépendance Boot en scope compile. Impact : le plugin exigera JDK 21 chez les consommateurs (à documenter). |
| Plugins profil `central` + lombok | ✅ Tous compatibles JDK 21 (lombok 1.18.42 = Java 11→25). CI déjà JDK 21 + Maven 3.9.8. |

**Risque transverse n°1 : couverture de tests quasi nulle.** ~45 modules, <20 classes de test au total ; les applications (backend, audit-stream, kinesis-s3, mcp-server, sample) ont 0 test actif (2 `SerdesTest` `@Disabled`). La validation reposera sur la compilation + Phase 3 runtime.

**Risque transverse n°2 : `org.thepavel:spring-icomponent:1.0.8`** (socle du mécanisme `@Flow` de kockpit-rules) — lib tierce ancienne, compat Spring Framework 7 non garantie, couverte par un seul test (`ApiTest` du sample). À tester tôt dans la Phase 2.

## Décisions en attente (à arbitrer AVANT Phase 2)

- [ ] **D1 — Spring AI RC1 ou RC2 ?** La décision actée dit RC1, mais RC2 est sortie le 09/06 (corrections post-RC1, toujours pré-GA). **Recommandation : RC2** (même politique : bump vers GA avant toute release Central).
- [ ] **D2 — Bump clients OpenSearch** : opensearch-java 3.0.0→3.9.0 et opensearch-rest-high-level-client 3.3.2→3.7.0 (dernière). Requis de fait par httpclient5 5.6 de Boot 4.1 (breaking change gzip). **Recommandation : oui** — c'est une dépendance « requise par la migration », pas du scope creep.
- [ ] **D3 — Smoke tests de contexte** : ajouter 1 test `@SpringBootTest` de chargement de contexte par application/profil (backend, audit-stream kafka+kinesis, kinesis-s3, mcp-server, sample). Seul filet automatisé possible vu la couverture. **Recommandation : oui** (petit, ciblé migration).
- [ ] **D4 — azure-pipelines.yml stage Docker** : référence un Dockerfile inexistant (`kockpit-audit/.../kockpit-audit-stream-application-kafka/Dockerfile`) — cassé AVANT migration. Corriger le chemin pendant l'étape docker-ci, ou seulement consigner ? **Recommandation : corriger** (c'est dans le périmètre CI de la Phase 2.8).
- [ ] **D5 — springdoc 2.8.13 → 3.0.3** (obligatoire pour Boot 4, backend-application). Pas vraiment une décision — confirmation que le bump est acté.

## Bloquants build/runtime identifiés (corrigés en Phase 2)

| # | Module | Cassure | Correctif |
|---|---|---|---|
| B1 | kockpit-rules-starter | pins `spring-context:6.2.5` + `spring-boot-autoconfigure:3.4.4` (artefact dissous dans `spring-boot` en 4.x) | dé-pinner, dep `spring-boot` |
| B2 | kockpit-rules-registry | pin `spring-aop:5.3.16` (Spring 5 !) ; `spring-boot-starter-json` supprimé en 4.x | dé-pinner ; deps Jackson 2 explicites |
| B3 | 9 poms | `spring-boot-starter-web` (alias supprimé en 4.1) : audit-openapi, manifest-service-backend, dashboard, search-opensearch, backend-application, audit-stream-application, sample-all, rules-app-sample, ai-mcp-server (+ scope test features-audit-module-web) | → `spring-boot-starter-webmvc` |
| B4 | features-audit-module-httpexchange | `RestClientCustomizer`/`RestTemplateCustomizer`/`WebClientCustomizer` relocalisés | imports `org.springframework.boot.restclient.*`/`...webclient.*` + deps `spring-boot-restclient`, `spring-boot-webclient` (optional) |
| B5 | audit-notification-kafka, audit-stream-starter-kafka | `KafkaProperties` relocalisée ; `buildConsumerProperties()` no-arg supprimé | import `org.springframework.boot.kafka.autoconfigure.KafkaProperties` + dep `spring-boot-kafka` ; `buildConsumerProperties(SslBundles)` |
| B6 | backend-application | springdoc 2.8.13 incompatible Boot 4 | → springdoc 3.0.3 (+ vérifier springdoc-openapi-maven-plugin) |
| B7 | dynaconfig-service-application, heartbeat-service-backend | injection du bean `com.fasterxml...ObjectMapper` → plus auto-configuré en Boot 4 (NoSuchBeanDefinition au runtime) | migrer vers `tools.jackson` (usages `convertValue` iso-API) |
| B8 | rules-app-sample, features-audit-module-web (tests) | `AutoConfigureMockMvc` déplacé (`org.springframework.boot.webmvc.test.autoconfigure`) | nouvel import + dep `spring-boot-starter-webmvc-test` |
| B9 | sample-all | `FeatureFlagService` : `new ObjectMapper()` Jackson 2 sans dep déclarée (venait de starter-web) | dep explicite jackson-databind 2 ou tools.jackson |
| B10 | audit-stream-application | pins httpclient5 5.4.4/httpcore5 5.3.4 vs 5.6 imposé par Boot 4.1 | retirer les pins, bump opensearch-java 3.9.0 |

---

## Phase 2 — Checklist d'exécution (ordre : libs → starters → applications, kockpit-ai en dernier)

### Commit 1 — `build: parent Boot 4.1.0, Java 21, version 2.0.0-SNAPSHOT` — commit: ______
- [ ] Créer la branche `migration/sb4-spring-ai2`
- [ ] pom racine : parent `spring-boot-starter-parent:4.1.0`, `java.version=21`, version `2.0.0-SNAPSHOT` (tous les modules suivent)
- [ ] `spring-boot-properties-migrator` en scope runtime sur les applications (TEMPORAIRE — retrait tracké en fin de plan)
- [ ] Dé-pinner les versions bloquantes : B1 (rules-starter), B2 spring-aop (rules-registry), B10 (httpclient5/httpcore5)
- [ ] Supprimer `maven.compiler.source/target=21` redondants (`kockpit-rules-engine-parent/pom.xml:15-16`) + bloc dependencyManagement 3.4.3 commenté
- [ ] Supprimer le pin `maven-compiler-plugin:3.8.1` (`kockpit-ai-mcp-server/pom.xml`)
- [ ] Aligner lombok 1.18.34→`${lombok.version}` et junit-jupiter 5.8.1→managé (rules-maven-plugin)
- [ ] Vérifier : `mvn -q clean compile` (échecs attendus = cassures listées, traitées aux commits suivants)

### Commit 2 — `build: starters fins Boot 4.x (webmvc, kafka, jackson...)` — commit: ______
- [ ] B3 : `spring-boot-starter-web` → `spring-boot-starter-webmvc` (9 poms, liste en B3)
- [ ] B2 : `spring-boot-starter-json` (rules-registry) → deps Jackson 2 explicites (managées par jackson-2-bom)
- [ ] B4 : deps `spring-boot-restclient` + `spring-boot-webclient` (optional) sur features-audit-module-httpexchange + imports
- [ ] B5 : dep `spring-boot-kafka` + import `KafkaProperties` relocalisé + `buildConsumerProperties(SslBundles)` (audit-notification-kafka, audit-stream-starter-kafka)
- [ ] features-audit-module-web : dep explicite `spring-boot-web-server` si `FilterRegistrationBean` n'arrive plus transitivement
- [ ] B6 : springdoc → 3.0.3 (backend-application, + plugin maven springdoc)
- [ ] Starters -test correspondants là où nécessaire (webmvc-test...) — `spring-boot-starter-test` seul ne suffit plus
- [ ] Vérifier : compilation des modules concernés

### Commit 3 — `feat(kockpit-rules): compat Spring Framework 7` — commit: ______
- [ ] Valider `spring-icomponent:1.0.8` sous Spring 7 (compiler + lancer `ApiTest` du sample) — **si KO : STOP, arbitrage** (pas d'alternative drop-in)
- [ ] rules-app-sample : B3 (starter-webmvc) + B8 (`AutoConfigureMockMvc` nouveau package + `spring-boot-starter-webmvc-test`)
- [ ] rules-maven-plugin : retirer deps test inutilisées `spring-context`/`kockpit-rules-starter` si confirmé ; vérifier la génération mustache (annotations Spring stables)
- [ ] `mvn -pl kockpit-rules -am verify`

### Commit 4 — `feat: jackson 3 applications / coexistence jackson 2 clients` — commit: ______
- [ ] B7 : `tools.jackson` dans dynaconfig-service-application + heartbeat-service-backend (injection ObjectMapper)
- [ ] B9 : sample-all FeatureFlagService
- [ ] kockpit-ai AuditReportHelper + DTOs : dep explicite jackson-databind 2 + jsr310 (ou tools.jackson — trancher au moment du code)
- [ ] Vérifier chaque module à `new ObjectMapper()` local : dep `jackson-databind` 2 EXPLICITE déclarée (storageaccount en manque déjà)
- [ ] Mappers OpenSearch (JacksonJsonpMapper Jackson 2, opensearch-objectMapper, audit-stream) : conservés en Jackson 2 isolé — documenter dans MIGRATION_NOTES.md
- [ ] opensearch-java → 3.9.0 ; opensearch-rest-high-level-client → 3.7.0 (D2)
- [ ] Aucun import `com.fasterxml.jackson.databind` ne subsiste dans du code servi par le mapper WEB des applications

### Commit 5 — `feat: starters maison conformes Boot 4` — commit: ______
- [ ] Annoter `@AutoConfiguration` les auto-configs en `@Configuration` simple : `AuditAnnotationConfig`, `OperationIdSetterInterceptorConfig`, 3 configs obfuscation, `HttpExchangeAutoConfiguration`, `WebAuditAutoConfiguration`, `FilterTraceIdConfiguration`, `KafkaAuditServiceAutoConfiguration`
- [ ] Vérifier tous les `AutoConfiguration.imports` (mécanisme inchangé en Boot 4, aucun spring.factories dans le repo ✅)
- [ ] README des modules starters : mention « kockpit 2.x = Spring Boot 4 uniquement, Java 21 minimum »
- [ ] Deps manquantes détectées : jackson-databind (storageaccount), micrometer StringUtils → `org.springframework.util.StringUtils` (s3, legacy, manifest-s3)

### Commit 6 — `test: migration annotations test Boot 4` — commit: ______
- [ ] B8 features-audit-module-web : `XB3TraceIdFilterITTest` nouvel import + `spring-boot-starter-webmvc-test` ; `spring-boot-starter-web` scope test → webmvc
- [ ] Aucun @MockBean/@SpyBean dans le repo ✅ (vérifié à l'audit) — re-vérifier par grep avant de cocher
- [ ] `RuleEngineAuditTest` JUnit 4 → Jupiter (audit-rules-impl) — junit:junit reste managé mais Boot 4 = JUnit 6
- [ ] SerdesTest ×2 `@Disabled` : réécrire (`@Autowired ObjectMapper` com.fasterxml → cassé en Boot 4) ou réactiver en tools.jackson — PAS de @Disabled pour « faire passer »
- [ ] D3 (si validé) : smoke tests de contexte par application/profil

### Commit 7 — `chore: config applicative Boot 4 (propriétés, probes)` — commit: ______
- [ ] Démarrage de CHAQUE application × CHAQUE profil (default, filesystem, azure, aws, kafka, kinesis, eventhub) → zéro warning properties-migrator ; renommages appliqués dans tous les application*.yml
- [ ] `spring.kafka.retry.topic.backoff.random` → `.jitter` si présent
- [ ] `management.endpoint.health.probes.enabled=true` : supprimer (défaut en Boot 4) — backend-application, audit-stream-application
- [ ] Probes actives par défaut : impact compose/terraform documenté (terraform utilise `/actuator/health` — compatible ; port management 8090/8091 à re-valider) → MIGRATION_NOTES.md
- [ ] `spring.main.allow-bean-definition-overriding=true` (backend-application) : tester sans, sinon consigner

### Commit 8 — `ci: JDK 21, maven.test.skip, image corretto 21` — commit: ______
- [ ] Dockerfile mcp-server : `amazoncorretto:22-al2023-headless` → `amazoncorretto:21-al2023-headless` (les 4 autres déjà en 21 ✅)
- [ ] 17 occurrences `-DskipTests` (15 GH Actions + azure-pipelines + 1 fichier mort) → `-Dmaven.test.skip=true` là où l'intention est « pas de tests » (builds d'image, deploy central)
- [ ] CI déjà JDK 21 partout ✅ — rien à changer côté setup-java
- [ ] D4 (si validé) : corriger le chemin Dockerfile du stage Docker azure-pipelines.yml
- [ ] Fichier mort `kockpit-audit/.github/worflows/maven-publish.yml` : consigner dans MIGRATION_NOTES.md (hors périmètre, suppression à proposer)

### Commit 9 — `feat(kockpit-ai): Spring AI 2.0.0-RC2` (EN DERNIER) — commit: ______
- [ ] BOM `spring-ai-bom` 1.1.3 → 2.0.0-RC2 (D1) ; supprimer la propriété morte `spring-ai.version=1.0.1` (module aws)
- [ ] Starter `spring-ai-starter-mcp-server-webmvc` : coordonnées INCHANGÉES en 2.0 (vérifié) — aucun changement de code attendu (`@Tool`/`@ToolParam`/`ToolCallbacks` inchangés, 7 propriétés `spring.ai.mcp.server.*` identiques)
- [ ] Doublon lombok dans kockpit-ai-mcp-server-aws/pom.xml : nettoyer
- [ ] Comportement 2.0 : validation des arguments d'outils activée par défaut côté serveur → tester avec un client MCP réel, consigner dans MIGRATION_NOTES.md
- [ ] AVANT toute release Central : bump RC2 → GA (tracké, AUCUNE publication avant)

### Fin de phase 2
- [ ] CLAUDE.md mis à jour (Boot 4.1, Spring AI 2, Java 21, conventions starters)
- [ ] README + release notes : mention de rupture « kockpit 2.x : Spring Boot 4 uniquement, Java 21 minimum » ; impact plugin Maven (JDK 21 requis chez les consommateurs)

## Phase 3 — Validation runtime
- [ ] `mvn clean verify` racine (sans profil central) → vert ; triage `mvn -pl <module> -am verify` si échec
- [ ] `docker compose` environnement local (OpenSearch + Kafka)
- [ ] backend (filesystem) : recherche d'audits aboutie contre OpenSearch
- [ ] audit-stream (kafka) : message consommé et indexé
- [ ] serveur MCP : `tools/list` répond
- [ ] Zéro erreur contexte Spring, zéro warning properties-migrator, zéro @Disabled de complaisance, zéro warning de dépréciation introduit
- [ ] **Retrait de `spring-boot-properties-migrator`** (ajouté au commit 1)
- [ ] Résultats + changements de comportement (Jackson 3, probes, Spring AI 2) → MIGRATION_NOTES.md

## Tableau de risque par groupe de modules

| Groupe | Risque | Raisons |
|---|---|---|
| kockpit-libs + kockpit-core | Faible (signing : moyen) | Libs Jackson-2-local, autoconfigs simples ; 0 test sur le point d'intégration SigV4 |
| kockpit-rules | **Élevé** | Pins Spring 5/6 bloquants, spring-boot-starter-json, spring-icomponent vs Spring 7 |
| kockpit-audit (+obfuscation) | Moyen | starter-web (openapi), dette Jackson 2, 9/13 modules sans test |
| kockpit-features | Moyen/Élevé | Relocalisations Boot 4 (restclient/webclient/kafka), injections ObjectMapper cassées au runtime |
| kockpit-backends + core-applications | Élevé | Applications = point de convergence ; springdoc, starter-web ×5, spring-kafka 4.1, 0 test actif |
| kockpit-ai | Modéré (revu à la baisse) | Bump BOM quasi suffisant ; vigilance httpclient5 5.6 / client OpenSearch ; 1 seul test |
| CI/Docker | Faible | Déjà JDK 21 ; -DskipTests ×17 ; 1 Dockerfile en 22 |
