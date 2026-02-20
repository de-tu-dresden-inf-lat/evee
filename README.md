# EVEE - <u>ev</u>incing <u>e</u>xpressive DL <u>e</u>ntailments
This is the github-repository of *Evee*, a tool collection for explaining very expressive DL entailments and missing entailments.
For general information on Evee, please refer to the section "About Evee".
If you just want to try Evee with the Ontology Editor [Protégé](https://protege.stanford.edu/ "https://protege.stanford.edu/"), please refer to the section "Using the Evee plugins".
Information on how to install Evee and use it as a library in your own projects can be found in the section "Installing and using Evee".

For questions or feedback, you can contact Stefan Borgwardt ([stefan.borgwardt@tu-dresden.de](mailto:stefan.borgwardt@tu-dresden.de)) or Patrick Koopmann ([p.k.koopmann@vu.nl](mailto:p.k.koopmann@vu.nl)).

## About Evee
Evee is developed at the [Chair of Automata Theory at TU Dresden](https://tu-dresden.de/ing/informatik/thi/lat "https://tu-dresden.de/ing/informatik/thi/lat").
Evee is a Java library and collection of Protégé plugins that support ontology engineers by explaining reasons for (missing) entailments in OWL ontologies.
- Version 0.5 adds support for Protégé 5.6.0 and Java 11. It also introduces significantly easier to use build and release workflows.
- Version 0.4 introduces new proof generators for fragments of OWL 2 EL using the graph reasoning engine [Nemo](https://github.com/knowsys/nemo), which are described in the paper

  C. Alrabbaa, S. Borgwardt, P. Herrmann, M. Krötzsch: **The shape of EL proofs: A tale of three calculi**, in: P. Koopmann, Y. Ibáñez-García, (Eds.), Proceedings of the 38th International Workshop on Description Logics (DL), CEUR Workshop Proceedings, Opole, Poland, 2025. To appear.
  
  Extended version: [arXiv:abs/2507.21851](https://arxiv.org/abs/2507.21851)

  <img width="40%" height="40%" alt="protege_proof_textbook" src="https://github.com/user-attachments/assets/7d6180fb-c891-4705-986d-7db17290f680" />

- Version 0.3 improves the existing explanation methods and was used for a user study described in

  C. Alrabbaa, S. Borgwardt, T. Friese, A. Hirsch, N. Knieriemen, P. Koopmann, A. Kovtunova, A. Krüger, A. Popovič, I. Siahaan: **Explaining reasoning results for OWL ontologies with Evee**, in: P. Marquis, M. Ortiz, M. Pagnucco (Eds.), Proceedings of the 21st International Conference on Knowledge Representation and Reasoning (KR), pages 709–719, Hanoi, Vietnam, 2024. IJCAI. KR in the Wild track.
  [doi:10.24963/kr.2024/67](https://doi.org/10.24963/kr.2024/67)

- Version 0.2 introduces explanations for missing DL entailments, and is described in the paper

  C. Alrabbaa, S. Borgwardt, T. Friese, P. Koopmann, M. Kotlov: **Why not? Explaining missing entailments with Evee**, in: O. Kutz, C. Lutz, A. Ozaki (Eds.), Proceedings of the 36th International Workshop on Description Logics (DL), volume 3513 of CEUR Workshop Proceedings, CEUR-WS.org, 2023.
  https://ceur-ws.org/Vol-3515/paper-1.pdf.

  Extended version: [arXiv:abs/2308.07294](https://arxiv.org/abs/2308.07294)
  
  <div style="display: flex;">
    <a href="https://github.com/de-tu-dresden-inf-lat/evee/assets/98273877/bbd03678-1394-41de-8904-24231a47531c">
      <img src="https://github.com/de-tu-dresden-inf-lat/evee/assets/98273877/bbd03678-1394-41de-8904-24231a47531c" alt="AbductionLetheResult" width="50%" height="50%">
    </a>
    <a href="https://github.com/de-tu-dresden-inf-lat/evee/assets/98273877/d8089df4-8da0-4b63-ba7f-02d9254e23a7">
      <img src="https://github.com/de-tu-dresden-inf-lat/evee/assets/98273877/d8089df4-8da0-4b63-ba7f-02d9254e23a7" alt="small-model-spicy-american-flat" width="49%" height="50%">
    </a>
  </div>

- Version 0.1 supports various methods for generating proofs of DL entailments, and is described in the paper

  C. Alrabbaa, S. Borgwardt, T. Friese, P. Koopmann, J. Méndez, A. Popovic: **On the eve of true explainability for OWL ontologies: Description logic proofs with Evee and Evonne**, in: O. Arieli, M. Homola, J. C. Jung, M. Mugnier (Eds.), Proceedings of the 35th International Workshop on Description Logics (DL), volume 3263 of CEUR Workshop Proceedings, CEUR-WS.org, 2022.
  https://ceur-ws.org/Vol-3263/paper-2.pdf.

  Extended version: [arXiv:abs/2206.07711](https://arxiv.org/abs/2206.07711)
  
  <img src="https://user-images.githubusercontent.com/8749392/183616469-05452593-ae9b-496a-a55d-4fda6d122f2a.png" width="40%" height="40%">

## Using the Evee plugins
1. Install [Protégé](https://protege.stanford.edu/ "https://protege.stanford.edu/"). Evee was developed for and tested with Protégé version 5.6.5
2. Install both the [protege-proof-explanation](https://github.com/liveontologies/protege-proof-explanation "https://github.com/liveontologies/protege-proof-explanation") plugin [version 0.1.0](https://repo1.maven.org/maven2/org/liveontologies/protege-proof-explanation/0.1.0/) and the [proof utility library](https://github.com/liveontologies/puli "https://github.com/liveontologies/puli") PULi [version 0.1.0](https://repo1.maven.org/maven2/org/liveontologies/puli/0.1.0/).
3. Copy the evee-protege-*.jar files from the [github release page](https://github.com/de-tu-dresden-inf-lat/evee/releases) to the directory "plugins" of your local Protégé installation.
4. (Optional) Install [SPASS](https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover "https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover"), which is required for the Connection-Minimal Abduction solver utilizing [CAPI](https://lat.inf.tu-dresden.de/~koopmann/CAPI/ "https://lat.inf.tu-dresden.de/~koopmann/CAPI/").
For Linux and macOS, please refer to [the web page of CAPI](https://lat.inf.tu-dresden.de/~koopmann/CAPI/ "https://lat.inf.tu-dresden.de/~koopmann/CAPI/") for further information on how to install SPASS.
For Windows, please refer to [the web page of SPASS](https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover/download "https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover/download").
On Windows, the Connection-Minimal Abduction solver was developed and tested with SPASS 3.5.
When using the Connection-Minimal Abduction solver for the first time, you will be asked to enter the path to the SPASS executable, i.e. the file named *SPASS* or *SPASS.exe*.
5. (Optional) Install [Nemo](https://github.com/knowsys/nemo), which is required for the Nemo-based proof generators.

Some of the Evee plugins require the OWL Reasoner [HermiT](http://www.hermit-reasoner.com/index.html "http://www.hermit-reasoner.com/index.html"), which is already included as a plugin in Protégé.


## Compiling and using Evee

### Requirements

The Evee-Protégé plugins require the following other Protégé plugins:
- The [protege-proof-explanation](https://github.com/liveontologies/protege-proof-explanation "https://github.com/liveontologies/protege-proof-explanation") plugin
- The [proof utility library](https://github.com/liveontologies/puli "https://github.com/liveontologies/puli") PULi
- The OWL Reasoner [HermiT](http://www.hermit-reasoner.com/index.html "http://www.hermit-reasoner.com/index.html"), which is already included as a plugin in Protégé version 5.6.0.

These plugins also need to be installed into Protégé in order to use the Evee Protégé plugins.

### Installation

#### Getting the latest stable version of Evee
1. If you have never downloaded this repository, use `git clone https://github.com/de-tu-dresden-inf-lat/evee`.
   If you have already cloned this repository in the past, use `git pull origin main` from the root directory.
   These commands will create/update your local repository of Evee  to the latest commit.

2. Currently, the latest stable version of Evee is tagged as `v0.5` (you can find previous stable versions by checking the [Releases](https://github.com/de-tu-dresden-inf-lat/evee/releases) on GitHub).
   To check out the commit of this version, use `git checkout tags/v0.5`.
   This command will set your local Evee-repository to the commit of this release.

#### Compiling Evee with Maven

Evee was developed to work with the OWL API versions 4 and 5.
For easy compilation, we have created several Maven profiles:
- owlapi4: This will compile all submodules of Evee except the Evee Protégé plugins. The resulting .jar files will have the OWL API version 4 as a dependency.
- owlapi5: This will compile all submodules of Evee except the Evee Protégé plugins. The resulting .jar files will have the OWL API version 5 as a dependency.
- protege: This will compile all submodules of Evee including the Evee Protégé plugins. As Protégé itself uses the OWL API version 4, every compiled .jar  file will have the OWL API version 4 as a dependency.
- complete: This will compile all submodules of Evee. Every library except for evee-elimination-proofs-fame and evee-nemo-proof-extractor and the Protégé plugins will be compiled in 2 versions, one using the OWL API version 4, the other using the OWL API version 5.

The standard profile is "complete", which can be used via the command `mvn clean install` from the root directory.
If you want to use any of the other profiles, use the command `mvn clean install -P profileName` instead, where *profileName* is one of the other 3 mentioned above.

For easy reuse of Evee as a library, use [evee-libs-owlapi4](evee-libs/evee-libs-owlapi4/pom.xml) or [evee-libs-owlapi5](evee-libs/evee-libs-owlapi5/pom.xml) as a dependency, depending on the version of the OWL API that you need.
These libraries contain all submodules of Evee except for the Evee Protégé plugins.

### evee as library
All modules of evee except the Protégé plugins are published on Maven Central under the namespace `io.github.de-tu-dresden-inf-lat`. The artifact `evee-libs` depends on all published modules except `evee-protege-core`. To use it, simply add `evee-libs` as a dependency in your project's pom file:
```
<groupId>io.github.de-tu-dresden-inf-lat</groupId>
<artifactId>evee-libs</artifactId>
<version>0.5</version>
```
To use a single Evee module, add the respective dependency in your pom file,
e.g., for `evee-elk-proof-extractor-owlapi4`:
```
<groupId>io.github.de-tu-dresden-inf-lat</groupId>
<artifactId>evee-elk-proof-extractor-owlapi4</artifactId>
<version>0.5</version>
``` 

### Technical notes

Evee was developed for and tested with Java version 11.

Any Scala code of this repository was written for Scala version 2.12.6.

The Protégé plugins were developed for and tested with Protégé version 5.6.5.

The Nemo-based proof generators were tested with Nemo version 0.8.0.

On Windows, the Connection-Minimal Abduction solver was developed and tested with SPASS 3.5 for Windows.


## Disclaimer

FamePlus, CAPI, SPASS, and Nemo, all libraries and programs that Evee relies on internally, were *NOT* created by the developers of Evee.

For further information on Fame, please see [http://www.cs.man.ac.uk/~schmidt/sf-fame/](http://www.cs.man.ac.uk/~schmidt/sf-fame/ "http://www.cs.man.ac.uk/~schmidt/sf-fame/").

For further information on CAPI, please see [https://lat.inf.tu-dresden.de/~koopmann/CAPI/](https://lat.inf.tu-dresden.de/~koopmann/CAPI/ "https://lat.inf.tu-dresden.de/~koopmann/CAPI/").

For further information on SPASS, please see [https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover](https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover "https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover").

For further information on Nemo, please see [https://github.com/knowsys/nemo](https://github.com/knowsys/nemo).

Lethe has been developed by Patrick Koopmann; see [https://lat.inf.tu-dresden.de/~koopmann/LETHE/](https://lat.inf.tu-dresden.de/~koopmann/LETHE/ "https://lat.inf.tu-dresden.de/~koopmann/LETHE/").
