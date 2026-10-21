# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Evee is a Java/Scala library plus a set of Protégé plugins for explaining OWL entailments (proofs) and missing entailments (abduction, counterexamples). Target: Java 11, Scala 2.12, Protégé 5.6.x.

## Build and test

```bash
mvn clean install                       # full build (default profile "complete"); this is what CI runs
mvn -pl evee-data install               # one module (or run `mvn install` inside the module dir)
mvn -pl evee-elk-proof-extractor -am install   # one module plus the sibling modules it depends on
```

**Tests are skipped by default**: `skipTests=true` is set in the root POM and again in some module POMs. Pass `-DskipTests=false` to run them:

```bash
mvn -pl evee-data test -DskipTests=false -Dtest=JsonProofReadingAndWritingTests
mvn -pl <module> -am test -DskipTests=false -Dtest=<Class> -Dsurefire.failIfNoSpecifiedTests=false
```

Modules depend on their siblings' `${revision}` SNAPSHOT artifacts. With `-pl` and no `-am`, Maven resolves those from `~/.m2`, so upstream changes must be installed first.

To run every test, use `mvn clean install -DskipTests=false`, not `mvn test`. Add `-Dmaven.test.failure.ignore=true` to keep going after failures. `mvn clean test` fails across the reactor because `evee-protege-nemo-proof-extractor` uses `maven-dependency-plugin:unpack` on the `evee-nemo-proof-extractor` jar, which errors with "Artifact has not been packaged yet" (MDEP-98) when the build stops before `package`. Without `clean` it can pass by accident, because it reuses an old jar in `target/`.

If compilation breaks for no obvious reason, delete old local installs of `capi` and `lethe-*` from `~/.m2`.

- The version is the `revision` property in the root `pom.xml`. `flatten-maven-plugin` resolves it and writes `.flattened-pom.xml` files, which are generated and gitignored.
- Other profiles: `-Pplugin` builds only the Protégé plugins and the release zip, `-PelExplicator` builds a small EL subset, and `-PsonartypeDeploy` signs and publishes to Maven Central (the release workflow uses it). The `protege` profile lists module paths that no longer exist, so don't use it.
- The release zip ends up at `evee-protege-release/target/evee_protege_v_<revision>-evee-protege-release-<revision>.zip`. It holds the plugin jars that go into Protégé's `plugins/` directory.

### Scala compilation

Every module containing Scala, and also `evee-data` (which is pure Java), is compiled by `sbt-compiler-maven-plugin` (sbt 0.13), which handles both Java and Scala sources. In `evee-elimination-proofs` and `evee-concrete-domains` the standard `maven-compiler-plugin` is turned off. `evee-concrete-domains` keeps its `.scala` files under `src/main/java`. Scala artifacts use the `_2.12` suffix (e.g. `evee-elimination-proofs-core_2.12`).

## Architecture

There are two layers with **different OWL API versions**:

1. **Library modules** use OWL API 5 (`owl5version`) and are published to Maven Central under `io.github.de-tu-dresden-inf-lat`. `evee-libs` gathers them into one dependency, but its POM lists them by hand, so a new library module has to be added there too.
2. **Protégé plugins** (`evee-protege/*`) are OSGi bundles (`packaging=bundle`, `maven-bundle-plugin`) built against OWL API 4.5.x, which Protégé supplies at runtime. Plugin POMs exclude `owlapi-distribution` from the library dependencies they pull in.

So library code is compiled against OWL API 5 but **runs on OWL API 4 inside Protégé**. Any library code a plugin uses may only call OWL API methods whose signatures are identical in both versions.

### Core abstractions (`evee-data`)

Everything else builds on the generic interfaces in `evee-data`:
- Proofs: `IProofGenerator<SENTENCE, ONTOLOGY>` and `ISignatureBasedProofGenerator`, which produce an `IProof` made of `IInference`s. Both are Jackson-serializable to JSON (`proofs/json`).
- Missing entailments: `INonEntailmentExplainer<Observation, Symbol, Ontology, Explanation>`, specialised as `IOWLAbductionSolver`, `IOWLCounterexampleGenerator` and `IOWLModelGenerator`.
- Shared tooling: proof minimisation (`MinimalProofExtractor`, `MinimalHypergraphProofExtractor`), generator decorators such as `CachingProofGenerator`, and proof quality evaluators and measures (`proofs/tools/evaluators`, `proofs/tools/measures`).
- The Jackson version is pinned to 2.9.8 on purpose, because OWL API 5 depends on it indirectly. Don't upgrade it.

### Library modules

- Proof generators:
  - `evee-elimination-proofs`: Scala, forgetting-based proofs, with a `core` module plus FAME and LETHE backends.
  - `evee-lethe-proof-extractor`: proofs from LETHE.
  - `evee-elk-proof-extractor`: ELK proofs minimised for size, depth or weighted size.
  - `evee-nemo-proof-extractor`: shells out to an external `nemo` binary from `NemoReasoner`.
  - `evee-concrete-domains`: EL with concrete domains (linear, difference and multiplication constraints; `ELKCDReasoner`, `OWLCDReasonerFactory`). This is the current work on the `concreteDomains` branch. An ontology picks its domain through the annotation `http://tu-dresden.de/inf/lat/evee/cd#concreteDomain`, with value `QDiff`, `QMult` or `QLinear`. Each constraint is a class annotated with `...cd#constraint`, whose value is the constraint string (see `CDAnnotationNames`).
- Non-entailment explainers:
  - `evee-relevant-counterexample`: `counterExampleProvider` and `model-datastructures`. Note the root package is `de.tu_dresden.inf.lat.*`, not `...lat.evee`.
  - `evee-small-model-generator`.

### Protégé plugin wiring

- `evee-protege-core` holds the shared base classes and UI. Each other `evee-protege-*` module is a thin plugin that wraps one library generator.
- **Proof plugins** extend `AbstractEveeProofService` and wrap the generator in an `AbstractEveeDynamicProofAdapter` subclass. They register with the `org.liveontologies.protege.explanation.proof.service` extension point, which comes from the external protege-proof-explanation plugin. Their preferences UIs extend `AbstractEveeProofPreferencesUI`, which takes an `AbstractEveeProofPreferencesManager`, and register with `org.liveontologies.protege.explanation.proof.preferences`.
- **Non-entailment plugins** implement `INonEntailmentExplanationService` and register with Evee's own extension point `de.tu_dresden.inf.lat.evee.nonEntailment_explanation_service`, which `evee-protege-core`'s `plugin.xml` declares. Their preferences UIs go through `...nonEntailment_preferences`. Keep the core's `bundleName` set to `de.tu_dresden.inf.lat.evee`, because extension point IDs are formed as `<Bundle-SymbolicName>.<id>`, and core hard-codes that name in `NonEntailmentExplanationPlugin.PLUGIN_ID` and `NonEntailmentPreferencesPanelPlugin.PLUGIN_ID`.
- Protégé calls `hasProof` (proof services) and `supportsExplanation` (non-entailment services) very often from the UI, so they must stay cheap. `hasProof` only checks whether the service is enabled in the preferences. The real work happens in `getProof` or `computeExplanation`, on a separate thread.
- Each plugin lists its extensions in `src/main/resources/plugin.xml`, with fully qualified class names and Maven resource filtering on. When you rename or move a service class, update `plugin.xml` too.
- OSGi packaging: `evee-protege-core` exports the `evee-data` packages (`general.*`, `proofs.*`, `nonEntailment.interfaces`), `dltools` and Jackson, and the plugins import those. Each plugin embeds its own library module's packages through `Private-Package`. If you add a package to a library module, add it to the `Private-Package` of every plugin that embeds that module, or to the core's `Export-Package` if it belongs to `evee-data`. Otherwise the plugin compiles fine, but at Protégé startup the log shows `BundleException: Unable to resolve … missing requirement … osgi.wiring.package=<pkg>`. Fixing these is often trial and error; `resolution:=optional` and `-split-package:=merge-last` are the usual tools.
- Adding a plugin means adding it to `evee-protege/pom.xml` `<modules>` and, to ship it, to the `<dependencies>` in `evee-protege-release/pom.xml`; the zip contains only the plugins listed there. Module directory names don't always match artifactIds: `evee-protege-counterexample-EL/` builds `evee-protege-counterexample-small-model`.

## External tools and dependencies

- **SPASS** must be installed for the CAPI abduction plugin (`evee-protege-abduction-capi`). The plugin asks the user for the executable path the first time it runs.
- **Nemo** must be installed for the Nemo proof generators, which were tested with Nemo 0.8.0.
- **FamePlus, CAPI and LETHE** come in as Maven dependencies. LETHE's version is set by `LETHE.Version` in the root POM.
- The untracked `LETHE-0.8/` and `lat-scala-dl-tools/` directories are separate git checkouts of upstream dependencies. They are not part of the Maven reactor; the build uses the published versions set by `LETHE.Version` and `LatScalaDlTools.Version`.
