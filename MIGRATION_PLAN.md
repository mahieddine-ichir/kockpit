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

## Décisions — VALIDÉES le 2026-06-11 (recommandations acceptées)

- [x] **D1** — Spring AI **2.0.0-RC2** (bump vers GA avant toute release Central).
- [x] **D2** — Bump opensearch-java 3.0.0→3.9.0 et opensearch-rest-high-level-client 3.3.2→3.7.0.
- [x] **D3** — Smoke tests `@SpringBootTest` de chargement de contexte par application/profil.
- [x] **D4** — Correction du chemin Dockerfile du stage Docker azure-pipelines.yml.
- [x] **D5** — springdoc 2.8.13 → 3.0.3.

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

### Commit 1 — `build: parent Boot 4.1.0, Java 21, version 2.0.0-SNAPSHOT` — commit: fb3e8175 (docs : 16d2a935)
- [x] Créer la branche `migration/sb4-spring-ai2`
- [x] pom racine : parent `spring-boot-starter-parent:4.1.0`, `java.version=21`, version `2.0.0-SNAPSHOT` (tous les modules suivent)
- [x] `spring-boot-properties-migrator` en scope runtime sur les applications (TEMPORAIRE — retrait tracké en fin de plan)
- [x] Dé-pinner les versions bloquantes : B1 (rules-starter), B2 spring-aop (rules-registry), B10 (httpclient5/httpcore5)
- [x] Supprimer `maven.compiler.source/target=21` redondants (`kockpit-rules-engine-parent/pom.xml:15-16`) + bloc dependencyManagement 3.4.3 commenté
- [x] Supprimer le pin `maven-compiler-plugin:3.8.1` (`kockpit-ai-mcp-server/pom.xml`)
- [x] Aligner lombok 1.18.34→`${lombok.version}` et junit-jupiter 5.8.1→managé (rules-maven-plugin)
- [x] Vérifier : `mvn clean install -Dmaven.test.skip=true` sous JDK 21 — compile jusqu'à features-audit-module-web (cassure Framework 7 `ContentCachingRequestWrapper(request, int)` découverte, traitée au commit 2). Deps testcontainers mortes supprimées (kinesis-s3, artefacts renommés en TC 2.0.5). NB : JDK local 25 → Temurin 21.0.11 installé (`~/Library/Java/JavaVirtualMachines/temurin-21.0.11`), builds avec `JAVA_HOME` pointé dessus.

### Commit 2 — `build: starters fins Boot 4.x (webmvc, kafka, jackson...)` — commit: c5b54e48
- [x] B3 : `spring-boot-starter-web` → `spring-boot-starter-webmvc` (9 poms — NB : les alias dépréciés existent encore sur Central en 4.1.0, mais politique = noms 4.x uniquement)
- [x] B2 : `spring-boot-starter-json` (rules-registry) → deps Jackson 2 explicites + `jakarta.annotation-api` (perdue avec le starter)
- [x] B4 : deps `spring-boot-restclient` + `spring-boot-webclient` (optional) sur features-audit-module-httpexchange + imports
- [x] B5 : dep `spring-boot-kafka` + import `org.springframework.boot.kafka.autoconfigure.KafkaProperties` (audit-notification-kafka, audit-stream-starter-kafka). NB : `buildConsumerProperties()` est SANS argument en 4.1 (vérifié javap) — pas de variante SslBundles.
- [x] features-audit-module-web : `FilterRegistrationBean` reste dans `org.springframework.boot.web.servlet` (jar core spring-boot) — aucun changement nécessaire
- [x] B6 : springdoc → 3.0.3 (backend-application ; le plugin maven springdoc est commenté — rien à faire)
- [x] Cassures Framework 7 découvertes au build : `ContentCachingRequestWrapper(request, Integer.MAX_VALUE)` (AuditFilter) ; `HttpHeaders.entrySet()` → `headerSet()` (httpexchange ×2)
- [x] Jackson 2 explicite là où starter-web le fournissait : search-opensearch, dashboard, sample-all, ai-mcp-server
- [x] Vérifier : `mvn clean install -Dmaven.test.skip=true` vert sur tout le réacteur

### Commit 3 — `feat(kockpit-rules): compat Spring Framework 7` — commit: 2e87bc60
- [x] **`spring-icomponent:1.0.8` VALIDÉ sous Spring 7** : `mvn -f kockpit-rules/pom.xml clean verify` vert — 13 tests dont ApiTest 2/2 (bout-en-bout `@Flow` + MockMvc). Pas de STOP.
- [x] rules-app-sample : B8 (`org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` + starter-test → `spring-boot-starter-webmvc-test`)
- [x] rules-maven-plugin : deps test inutilisées `spring-context`/`kockpit-rules-starter` supprimées (plugin découplé de Boot) ; génération mustache OK (tests du plugin verts)
- [x] `mvn -f kockpit-rules/pom.xml clean verify` (attention : `-pl kockpit-rules -am` ne construit PAS les enfants de l'agrégateur)

### Commit 4 — `feat: jackson 3 applications / coexistence jackson 2 clients` — commit: 7838da41
- [x] B7 : `tools.jackson` dans dynaconfig-service-application + heartbeat-service-backend (injection ObjectMapper) + deps `tools.jackson.core:jackson-databind`
- [x] B9 : sample-all FeatureFlagService → dep jackson-databind 2 explicite (mapper local conservé en Jackson 2)
- [x] kockpit-ai AuditReportHelper + DTOs : deps explicites jackson-databind 2 + jsr310 (Jackson 2 conservé — flux OpenSearch)
- [x] storageaccount : dep jackson-databind 2 explicite ajoutée (était transitive azure-core)
- [x] Mappers OpenSearch conservés en Jackson 2 isolé — documenté dans MIGRATION_NOTES.md
- [x] opensearch-java 3.0.0 → 3.9.0 ; opensearch-rest-high-level-client 3.3.2 → 3.7.0 (D2)
- [x] Réacteur vert (`install -Dmaven.test.skip=true`)

### Commit 5 — `feat: starters maison conformes Boot 4` — commit: e89d4a0f
- [x] `@AutoConfiguration` sur les 9 auto-configs en `@Configuration` simple (ou sans annotation) ; appel inter-bean `kafkaTemplate→producerFactory` remplacé par injection (proxyBeanMethods=false)
- [x] `AutoConfiguration.imports` : mécanisme inchangé, aucun spring.factories dans le repo ✅
- [x] README racine + kockpit-rules : mention « kockpit 2.x = Spring Boot 4 uniquement, Java 21 minimum »
- [x] Deps manquantes : jackson-databind (storageaccount, commit 4), spring-boot-autoconfigure (audit-annotation), micrometer StringUtils → `org.springframework.util.StringUtils::hasLength` (s3, legacy, manifest-s3, starter-kinesis)

### Commit 6 — `test: migration annotations test Boot 4` — commit: 7607f5ab
- [x] B8 features-audit-module-web : `XB3TraceIdFilterITTest` nouvel import + dep `spring-boot-webmvc-test`
- [x] Aucun @MockBean/@SpyBean dans le repo ✅
- [x] `RuleEngineAuditTest` JUnit 4 → Jupiter + dep junit-jupiter (audit-rules-impl)
- [x] SerdesTest ×2 : réécrits en tests unitaires du mapper Jackson 2 RÉEL (config KafkaStreamAutoConfiguration) et RÉACTIVÉS
- [x] D3 : smoke tests `@SpringBootTest contextLoads()` × 5 applications (backend en variante filesystem via deps test ; audit-stream avec `kockpit.audit.trace.enabled=true`)
- [x] **`mvn clean verify` racine : VERT — 45 tests, 0 échec, 0 erreur**

### Commit 7 — `chore: config applicative Boot 4 (probes)` — commit: 71debc5c
- [ ] Démarrage réel de CHAQUE application × CHAQUE profil → zéro warning properties-migrator (→ Phase 3)
- [x] `spring.kafka.retry.topic.backoff.random` : non utilisée ✅ ; clés `spring.kafka.*` et `server.servlet.context-path` inchangées (vérifié changelogs 4.0/4.1)
- [x] `management.endpoint.health.probes.enabled=true` supprimé (défaut Boot 4) — backend, audit-stream (+ yml de test)
- [x] Probes par défaut : terraform `/actuator/health` compatible ; ports management 8090/8091 conservés → MIGRATION_NOTES.md
- [ ] `spring.main.allow-bean-definition-overriding=true` (backend-application) : tester sans en Phase 3, sinon consigner

### Commit 8 — `ci: maven.test.skip, image corretto 21` — commit: 854f93cd
- [x] Dockerfile mcp-server : corretto 22 → 21 (les 4 autres déjà en 21 ✅)
- [x] 16 occurrences actives `-DskipTests`(+`-DskipITs`) → `-Dmaven.test.skip=true` (GH Actions + azure-pipelines) ; fichier mort kockpit-audit/.github/worflows non touché (consigné)
- [x] CI déjà JDK 21 partout ✅
- [x] D4 : chemin Dockerfile du stage Docker azure-pipelines.yml corrigé

### Commit 9 — `feat(kockpit-ai): Spring AI 2.0.0-RC2` (EN DERNIER) — commit: ebf68d5c
- [x] BOM `spring-ai-bom` 1.1.3 → 2.0.0-RC2 (D1) ; propriété morte 1.0.1 supprimée ; doublon lombok nettoyé
- [x] Starter `spring-ai-starter-mcp-server-webmvc` : coordonnées et API inchangées — aucun changement de code
- [x] `mvn -f kockpit-ai verify` : VERT (9 tests dont smoke de contexte)
- [ ] Validation des arguments d'outils MCP (défaut 2.0) : tester avec un client MCP réel en Phase 3
- [ ] AVANT toute release Central : bump RC2 → GA (tracké, AUCUNE publication avant)

### Fin de phase 2
- [x] CLAUDE.md mis à jour (Boot 4.1, Spring AI 2, Java 21, conventions starters)
- [x] README + release notes : mention de rupture « kockpit 2.x : Spring Boot 4 uniquement, Java 21 minimum » ; impact plugin Maven (JDK 21 requis chez les consommateurs)

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
