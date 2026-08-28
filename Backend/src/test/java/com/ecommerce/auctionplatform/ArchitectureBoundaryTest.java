package com.ecommerce.auctionplatform;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureBoundaryTest {
    private static final Path SOURCE_ROOT = Path.of("src/main/java/com/ecommerce/auctionplatform");
    private static final Pattern PROJECT_IMPORT = Pattern.compile(
            "import\\s+com\\.ecommerce\\.auctionplatform\\.([^.]+)\\.([^;]+);"
    );

    @Test
    void domainMustBeFrameworkFreeAndBoundedByModule() throws IOException {
        List<String> violations = new ArrayList<>();

        for (Path source : javaSources()) {
            String normalized = source.toString().replace('\\', '/');
            if (!normalized.contains("/domain/")) {
                continue;
            }

            String content = Files.readString(source);
            if (content.contains("@Data")) {
                violations.add(source + " exposes blanket setters through Lombok @Data");
            }
            if (Pattern.compile("public\\s+void\\s+set[A-Z]").matcher(content).find()) {
                violations.add(source + " exposes a raw public setter instead of domain behavior");
            }
            if (content.contains("@NoArgsConstructor\n") || content.contains("@AllArgsConstructor\n")) {
                violations.add(source + " exposes a public Lombok constructor");
            }
            for (String forbidden : List.of(
                    "import jakarta.",
                    "import org.springframework.",
                    "import org.hibernate.",
                    "import com.fasterxml.jackson."
            )) {
                if (content.contains(forbidden)) {
                    violations.add(source + " imports " + forbidden.substring(7));
                }
            }

            String module = moduleOf(source);
            Matcher matcher = PROJECT_IMPORT.matcher(content);
            while (matcher.find()) {
                String importedModule = matcher.group(1);
                String importedPackage = matcher.group(2);
                boolean sameModule = module.equals(importedModule);
                boolean sharedDomain = importedModule.equals("shared") && importedPackage.startsWith("domain.");
                if (!sameModule && !sharedDomain) {
                    violations.add(source + " crosses domain boundary via " + matcher.group());
                }
            }
        }

        assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }

    @Test
    void boundedContextDependenciesMustFollowTheAcyclicGraph() throws IOException {
        Map<String, Set<String>> allowedDependencies = Map.of(
                "identity", Set.of(),
                "product", Set.of(),
                "auction", Set.of("identity", "product"),
                "payment", Set.of("identity", "product", "auction"),
                "notification", Set.of("auction", "payment"),
                "dispute", Set.of("identity", "product", "payment", "notification")
        );
        List<String> violations = new ArrayList<>();

        for (Path source : javaSources()) {
            String module = moduleOf(source);
            if (!allowedDependencies.containsKey(module)) {
                continue;
            }
            Matcher matcher = PROJECT_IMPORT.matcher(Files.readString(source));
            while (matcher.find()) {
                String importedModule = matcher.group(1);
                if (allowedDependencies.containsKey(importedModule)
                        && !module.equals(importedModule)
                        && !allowedDependencies.get(module).contains(importedModule)) {
                    violations.add(source + " violates module DAG via " + matcher.group());
                }
            }
        }

        assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }

    @Test
    void applicationMustNotDependOnPresentationOrInfrastructure() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path source : javaSources()) {
            String normalized = source.toString().replace('\\', '/');
            if (!normalized.contains("/application/")) {
                continue;
            }
            String content = Files.readString(source);
            if (content.contains(".presentation.")) {
                violations.add(source + " imports presentation");
            }
            if (content.contains(".infrastructure.")) {
                violations.add(source + " imports infrastructure");
            }
        }
        assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }

    @Test
    void applicationContractsMustBeFrameworkFreeAndOwnedByTheirContext() throws IOException {
        List<String> violations = new ArrayList<>();
        List<String> businessModules = List.of("auction", "dispute", "notification", "payment", "product", "identity");

        for (Path source : javaSources()) {
            String normalized = source.toString().replace('\\', '/');
            if (!normalized.contains("/application/")) {
                continue;
            }

            String content = Files.readString(source);
            for (String forbidden : List.of(
                    "import org.springframework.web.",
                    "import org.springframework.data.",
                    "import org.springframework.messaging.",
                    "import org.springframework.security.",
                    "import jakarta.",
                    "import com.fasterxml.jackson.",
                    "import org.hibernate.",
                    "import com.google.",
                    "import com.cloudinary.",
                    "import com.nimbusds.",
                    "import java.io.IOException",
                    "import java.text.ParseException"
            )) {
                if (content.contains(forbidden)) {
                    violations.add(source + " imports technical API " + forbidden.substring(7));
                }
            }

            String module = moduleOf(source);
            Matcher matcher = PROJECT_IMPORT.matcher(content);
            while (matcher.find()) {
                String importedModule = matcher.group(1);
                if (businessModules.contains(importedModule) && !module.equals(importedModule)) {
                    violations.add(source + " imports another bounded context: " + matcher.group());
                }
            }
        }

        assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }

    @Test
    void crossContextInfrastructureMustUsePublishedApplicationContracts() throws IOException {
        List<String> businessModules = List.of("auction", "dispute", "notification", "payment", "product", "identity");
        List<String> violations = new ArrayList<>();

        for (Path source : javaSources()) {
            String normalized = source.toString().replace('\\', '/');
            String module = moduleOf(source);
            if (!businessModules.contains(module) || !normalized.contains("/infrastructure/")) {
                continue;
            }
            Matcher matcher = PROJECT_IMPORT.matcher(Files.readString(source));
            while (matcher.find()) {
                String importedModule = matcher.group(1);
                String importedPackage = matcher.group(2);
                if (businessModules.contains(importedModule)
                        && !module.equals(importedModule)
                        && !importedPackage.startsWith("application.port.in.")
                        && !importedPackage.startsWith("application.event.")) {
                    violations.add(source + " bypasses the public contract of " + importedModule
                            + " via " + matcher.group());
                }
            }
        }

        assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }

    @Test
    void behavioralInfrastructureAdaptersMustNotOwnRepositoriesOrAggregates() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path source : javaSources()) {
            String normalized = source.toString().replace('\\', '/');
            boolean behavioralAdapter = normalized.contains("/infrastructure/adapter/")
                    || normalized.contains("/infrastructure/external/")
                    || normalized.contains("/infrastructure/messaging/");
            if (!behavioralAdapter) {
                continue;
            }
            String content = Files.readString(source);
            for (String forbidden : List.of(".domain.repository.", ".domain.model.",
                    ".infrastructure.persistence.repository.")) {
                if (content.contains(forbidden)) {
                    violations.add(source + " owns business persistence/orchestration via " + forbidden);
                }
            }
        }
        assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }

    @Test
    void presentationMustOnlyCallInputPortsNotServicesOrOutputAdapters() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path source : javaSources()) {
            String normalized = source.toString().replace('\\', '/');
            if (!normalized.contains("/presentation/")) {
                continue;
            }
            String content = Files.readString(source);
            for (String forbidden : List.of(
                    ".application.service.",
                    ".application.port.out.",
                    ".infrastructure."
            )) {
                if (content.contains(forbidden)) {
                    violations.add(source + " crosses presentation boundary via " + forbidden);
                }
            }
        }
        assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }

    @Test
    void restControllersMustExposePresentationOwnedResponseContracts() throws IOException {
        List<String> violations = new ArrayList<>();

        for (Path source : javaSources()) {
            String normalized = source.toString().replace('\\', '/');
            String content = Files.readString(source);

            if (normalized.contains("/presentation/rest/")
                    && content.contains(".application.dto.response.")) {
                violations.add(source + " exposes an application output model as an HTTP response");
            }

            if (normalized.contains("/presentation/dto/response/")) {
                for (String forbidden : List.of(
                        ".application.",
                        ".domain.",
                        ".infrastructure."
                )) {
                    if (content.contains(forbidden)) {
                        violations.add(source + " couples an HTTP response contract to " + forbidden);
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }

    @Test
    void jpaEntitiesMustLiveInInfrastructureAndAuthMustBeInsideIdentity() throws IOException {
        List<String> misplacedEntities = new ArrayList<>();
        List<String> publicJpaRepositories = new ArrayList<>();
        for (Path source : javaSources()) {
            String content = Files.readString(source);
            if (content.contains("@Entity")) {
                String normalized = source.toString().replace('\\', '/');
                if (!normalized.contains("/infrastructure/persistence/entity/")) {
                    misplacedEntities.add(source.toString());
                }
            }
            String normalized = source.toString().replace('\\', '/');
            if (normalized.contains("/infrastructure/persistence/repository/")
                    && source.getFileName().toString().endsWith("JpaRepository.java")
                    && Pattern.compile("public\\s+interface\\s+\\w+JpaRepository").matcher(content).find()) {
                publicJpaRepositories.add(source.toString());
            }
        }

        assertTrue(misplacedEntities.isEmpty(), "Misplaced JPA entities: " + misplacedEntities);
        assertTrue(publicJpaRepositories.isEmpty(),
                "Spring Data repositories must remain package-private: " + publicJpaRepositories);
        Path oldAuthModule = SOURCE_ROOT.resolve("auth");
        assertFalse(Files.exists(oldAuthModule),
                "The auth module must not exist; authentication belongs to the identity bounded context");
    }

    @Test
    void configMustOnlyContainConfigurationAndIntegrationMustBridgeApplicationContracts() throws IOException {
        List<String> violations = new ArrayList<>();
        Path configRoot = SOURCE_ROOT.resolve("config");
        Path obsoleteConfigIntegration = configRoot.resolve("integration");
        Path integrationRoot = SOURCE_ROOT.resolve("integration");

        assertFalse(Files.exists(obsoleteConfigIntegration),
                "Cross-context adapters must not live under config/integration");

        try (var paths = Files.list(configRoot)) {
            for (Path source : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                String content = Files.readString(source);
                if (!content.contains("@Configuration")) {
                    violations.add(source + " is behavior placed in config instead of technical configuration");
                }
                if (Pattern.compile("import\\s+com\\.ecommerce\\.auctionplatform\\."
                        + "(auction|dispute|notification|payment|product|identity)\\.").matcher(content).find()) {
                    violations.add(source + " composes business modules from the technical config package");
                }
            }
        }

        if (Files.exists(integrationRoot)) {
            try (var paths = Files.walk(integrationRoot)) {
                for (Path source : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                    String content = Files.readString(source);
                    for (String forbidden : List.of(
                            ".domain.",
                            ".infrastructure.",
                            ".presentation.",
                            ".application.service."
                    )) {
                        if (content.contains(forbidden)) {
                            violations.add(source + " bypasses an application contract via " + forbidden);
                        }
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }

    private List<Path> javaSources() throws IOException {
        try (var paths = Files.walk(SOURCE_ROOT)) {
            return paths.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }

    private String moduleOf(Path source) {
        return SOURCE_ROOT.relativize(source).getName(0).toString();
    }
}
