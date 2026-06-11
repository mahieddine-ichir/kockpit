# MIGRATION_NOTES — Spring Boot 4.1.0 / Spring AI 2.0 / Java 21

Notes de migration : changements de comportement observés, coexistences documentées, et améliorations repérées mais hors périmètre.

## Coexistence Jackson 2 / Jackson 3 (sera complété en Phase 2)

- Boot 4 bascule le mapper auto-configuré sur Jackson 3 (`tools.jackson`, jackson-bom 3.1.4). Le BOM 4.1.0 continue de gérer Jackson 2 via `jackson-2-bom 2.21.4` ; `spring-boot-jackson2` existe en 4.1.0 (non déprécié en 4.1).
- Stratégie retenue : **Jackson 3 pour la sérialisation web des applications ; Jackson 2 conservé UNIQUEMENT pour les clients OpenSearch** (`JacksonJsonpMapper` d'opensearch-java, mappers construits localement et passés au transport — jamais le bean Boot). Pas de conflit de classpath (packages `com.fasterxml.*` vs `tools.jackson.*`, seul `jackson-annotations` partagé, conçu pour les deux lignes).

## Changements imposés par la migration

- **kockpit-kinesis-s3-application** : deps `org.testcontainers:localstack` et `org.testcontainers:junit-jupiter` SUPPRIMÉES (et non juste signalées hors périmètre) — Boot 4.1 importe Testcontainers **2.0.5** dont les artefacts ont été renommés ; les anciens IDs n'ont plus de version managée et bloquaient la lecture du POM. Elles étaient mortes (aucun `src/test` dans le module).

## Changements de comportement

- Probes liveness/readiness actives par défaut en Boot 4 : `/actuator/health/liveness|readiness` apparaissent ; `/actuator/health` inchangé (terraform compatible tel quel). La propriété `management.endpoint.health.probes.enabled=true` (redondante) a été retirée des configs.
- Spring AI 2.0 : validation des arguments d'outils MCP activée par défaut côté serveur (un client envoyant des arguments non conformes reçoit `isError=true`).
- `ContentCachingRequestWrapper` (AuditFilter) : le constructeur sans limite a disparu en Framework 7 → `Integer.MAX_VALUE` utilisé (iso-comportement 6.x, cache non borné).
- `HttpHeaders` n'implémente plus `Map` en Framework 7 : `entrySet()` → `headerSet()` (même forme d'Entry, iso-comportement).
- `@AutoConfiguration` (proxyBeanMethods=false) sur les ex-`@Configuration` enregistrées : l'appel inter-bean `kafkaTemplate→producerFactory` (notification-kafka) a été remplacé par une injection — même instance qu'avant via le conteneur.
- SerdesTest ×2 : réécrits pour tester le mapper Jackson 2 RÉEL du chemin de consommation (ils étaient `@Disabled` et injectaient le bean Boot, devenu Jackson 3).

## Validation runtime — Phase 3 (2026-06-11)

Environnement : docker compose kafka+opensearch (OpenSearch 3.2.0, apache/kafka), JDK 21 (Temurin 21.0.11).

| Vérification | Résultat |
|---|---|
| `mvn clean verify` racine (sans profil central) | ✅ vert — 45 tests, 0 échec |
| backend-application (profil **filesystem**) | ✅ démarré en 1,5 s ; `GET /api/kockpit/local/audits/_search` → HTTP 200 avec résultats réels d'OpenSearch (sérialisation web Jackson 3 OK) |
| audit-stream-application (profil **kafka**) | ✅ démarré en 1,4 s ; message AuditReport envoyé sur le topic `audits` → consommé (spring-kafka 4.1) et indexé dans OpenSearch (opensearch-java 3.9.0 / httpclient5 5.6) ; index + template + alias auto-créés |
| mcp-server (Spring AI 2.0.0-RC2) | ✅ démarré ; `tools/list` JSON-RPC OK (4 tools) ; `tools/call search-audits-by-traceId` exécuté de bout en bout (`isError:false`, requête OpenSearch via RHLC 3.7.0) |
| Warnings properties-migrator | ✅ zéro sur les 3 applications → **spring-boot-properties-migrator retiré** des 5 poms en fin de phase |
| Erreurs de contexte Spring | ✅ zéro |
| `spring.main.allow-bean-definition-overriding` (backend) | Démarre AUSSI avec `false` en profil filesystem — propriété conservée (profils azure/aws non testés au runtime), suppression candidate hors périmètre |

Notes :
- Le `|-ERROR in ch.qos.logback.classic.PatternLayout - Empty or null pattern` au démarrage du mcp-server vient du `logging.pattern.console:` (vide) préexistant dans application.yaml — pas une régression.
- Profils non exercés au runtime : azure (Event Hub), aws (Kinesis/SigV4) — couverts par la compilation + analyse de compat (SASL inchangé, AwsSdk2Transport maintenu) ; à valider sur un environnement cible.
- Build local : JDK 21 requis (JDK 25 désactive l'annotation processing implicite → Lombok KO, et viole la décision « pas de JDK 25 »). Temurin 21.0.11 installé dans `~/Library/Java/JavaVirtualMachines/temurin-21.0.11`.

## Hors périmètre (améliorations repérées pendant l'audit — NE PAS traiter dans cette migration)

- **kockpit-audit-module-web / kockpit-audit-web-starter** : coquilles vides DEPRECATED (0 source) redirigeant vers kockpit-features-audit-module-web — candidates à suppression.
- **kockpit-obfuscation-impl** : deps `jackson-databind`/`jackson-dataformat-xml:2.18.2` apparemment inutilisées (code en DOM/XPath + json-path) ; `commons-beanutils-core:1.8.3` (2010) ; ressources de test orphelines (les tests Java ont disparu).
- **kockpit-rules-maven-plugin** : `maven-project:3.0-alpha-2` (artefact mort de 2008) à remplacer par maven-core ; sources de test générées non compilées (build-helper manquant).
- **kockpit-core-sdk** : dépendance `spring-boot-autoconfigure` inutile (aucun import Spring).
- **kockpit-aws-opensearch-request-signing** : shutdown via `Runtime.addShutdownHook` au lieu de `@Bean(destroyMethod)` ; `signingServiceName` non paramétré (défaut "es", KO pour OpenSearch Serverless "aoss") ; dep `software.amazon.awssdk:opensearch` (control-plane) utilisée seulement pour le transitif.
- **kockpit-audit-openapi** : `OperationIdSetterInterceptorConfig` sans `@ConditionalOnBean`/`@ConditionalOnWebApplication` — peut casser le démarrage d'apps non-web (fragilité préexistante).
- **kockpit-kinesis-s3-application** : deps testcontainers déclarées sans `src/test` ; `management.endpoints.web.exposure` sans starter web (aucun endpoint HTTP exposé — healthcheck HTTP impossible, déjà le cas en 3.5) ; `DefaultCredentialsProvider.create()` déprécié SDK AWS.
- **Fichier CI mort** : `kockpit-audit/.github/worflows/maven-publish.yml` (répertoire mal orthographié, profil `release` inexistant) — à supprimer.
- **azure-sdk-bom 1.2.33 → 1.3.7** : bump possible, non requis par la migration.
- **opensearch-rest-high-level-client** : déprécié upstream — migration vers opensearch-java à planifier (dashboard, search-opensearch, ai-mcp-server).
- **`spring.main.allow-bean-definition-overriding=true`** (backend-application) : smell masquant des conflits de beans.
- **Dockerfile rules-designer** : node:18 EOL (frontend, hors périmètre migration).
- **`@EnableScheduling` dans des libs auto-importées** (manifest-service, audit-api-impl) : discutable, comportement inchangé.
- **`org.springframework.lang.Nullable`** (dynaconfig-application ×2) : déprécié Framework 7 au profit de JSpecify — à migrer plus tard globalement.
- **Lombok sans scope provided** (notification-kafka, featureflipping-backend) ; **starter-test sans scope test** (httpexchange) — hygiène de poms.
