# EVEE - <u>ev</u>incing <u>e</u>xpressive DL <u>e</u>ntailments
This is the github-repository of *Evee*, a tool collection for explaining very expressive DL entailments and missing entailments.
For general information on Evee, please refer to the section "About Evee".
If you just want to try Evee with the Ontology Editor [Protégé](https://protege.stanford.edu/ "https://protege.stanford.edu/"), please refer to the section "Using the Evee plugins".
Information on how to install Evee and use it as a library in your own projects can be found in the section "Installing and using Evee".

For questions or feedback, you can contact Stefan Borgwardt ([stefan.borgwardt@tu-dresden.de](mailto:stefan.borgwardt@tu-dresden.de)) or Patrick Koopmann ([p.k.koopmann@vu.nl](mailto:p.k.koopmann@vu.nl)).

## About Evee
Evee was developed at the [Chair of Automata Theory at TU Dresden](https://tu-dresden.de/ing/informatik/thi/lat "https://tu-dresden.de/ing/informatik/thi/lat"). Version 0.1 is described in the paper

C. Alrabbaa, S. Borgwardt, T. Friese, P. Koopmann, J. Méndez, A. Popovic: **On the eve of true explainability for OWL ontologies: Description logic proofs with Evee and Evonne**, in: O. Arieli, M. Homola, J. C. Jung, M. Mugnier (Eds.), Proceedings of the 35th International Workshop on Description Logics (DL), volume 3263 of CEUR Workshop Proceedings, CEUR-WS.org, 2022. https://ceur-ws.org/Vol-3263/paper-2.pdf.

![Evee plugin showing a proof in Protégé](https://user-images.githubusercontent.com/8749392/183616469-05452593-ae9b-496a-a55d-4fda6d122f2a.png)

## Using the Evee plugins
1. Install [Protégé](https://protege.stanford.edu/ "https://protege.stanford.edu/"). Evee was developed for and tested with Protégé version 5.5.0.
2. Install both the [protege-proof-explanation](https://github.com/liveontologies/protege-proof-explanation "https://github.com/liveontologies/protege-proof-explanation") plugin and the [proof utility library](https://github.com/liveontologies/puli "https://github.com/liveontologies/puli") PULi.
4. Copy the .jar files from the directory "release" of this repository to the directory "plugins" of your local Protégé installation.

Note that the Evee plugins require the OWL Reasoner [HermiT](http://www.hermit-reasoner.com/index.html "http://www.hermit-reasoner.com/index.html"), which is already included as a plugin in Protégé version 5.5.0.

The Connection-Minimal Abduction solver utilizing [CAPI](https://lat.inf.tu-dresden.de/~koopmann/CAPI/ "https://lat.inf.tu-dresden.de/~koopmann/CAPI/") requires the installation of [SPASS](https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover "https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover").
For Linux and macOS, please refer to [CAPI](https://lat.inf.tu-dresden.de/~koopmann/CAPI/ "https://lat.inf.tu-dresden.de/~koopmann/CAPI/") for further information on how to install SPASS.
For Windows, please refer to [SPASS](https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover/download "https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover/download").
On Windows, the Connection-Minimal Abduction solver was developed and tested with SPASS 3.5.
When using the Connection-Minimal Abduction solver for the first time, you will be asked to enter the path to the SPASS executable.
For Linux and macOS, this refers to the file named *SPASS*.
For Windows, this refers to the file *SPASS.exe*.

## Compiling and using Evee

### Requirements
Evee requires the following software, which is included as a git-submodule:
- The library [lat-scala-dl-tools](https://github.com/de-tu-dresden-inf-lat/lat-scala-dl-tools "https://github.com/de-tu-dresden-inf-lat/lat-scala-dl-tools")

The Evee-Protégé plugins require the following other Protégé plugins:
- The [protege-proof-explanation](https://github.com/liveontologies/protege-proof-explanation "https://github.com/liveontologies/protege-proof-explanation") plugin
- The [proof utility library](https://github.com/liveontologies/puli "https://github.com/liveontologies/puli") PULi
- The OWL Reasoner [HermiT](http://www.hermit-reasoner.com/index.html "http://www.hermit-reasoner.com/index.html"), which is already included as a plugin in Protégé version 5.5.0.

These plugins also need to be installed into Protégé in order to use the Evee Protégé plugins.

### Installation

#### Getting the latest stable version of Evee
1. If you have never downloaded this repository, use `git clone https://github.com/de-tu-dresden-inf-lat/evee --recurse-submodules`.
   If you have already cloned this repository in the past, use `git pull origin main` from the root directory.
   These commands will create/update your local repository of Evee (including the submodule [lat-scala-dl-tools](https://github.com/de-tu-dresden-inf-lat/lat-scala-dl-tools "https://github.com/de-tu-dresden-inf-lat/lat-scala-dl-tools")) to the latest commit.

2. Currently, the latest stable version of Evee is tagged as `v0.2` (you can find previous stable versions by checking the *Releases* on GitHub).
   To check out the commit of this version, use `git checkout tags/v0.2`.
   This command will set your local Evee-repository to the commit of this release.
   However, this does **not** change the submodule [lat-scala-dl-tools](https://github.com/de-tu-dresden-inf-lat/lat-scala-dl-tools "https://github.com/de-tu-dresden-inf-lat/lat-scala-dl-tools") to the commit associated with the stable release.

3. Use `git submodule update` to set the submodule [lat-scala-dl-tools](https://github.com/de-tu-dresden-inf-lat/lat-scala-dl-tools "https://github.com/de-tu-dresden-inf-lat/lat-scala-dl-tools") to the commit specified in the stable release you have just checked out.

#### Compiling Evee with Maven

Evee was developed to work with the OWL API versions 4 and 5.
For easy compilation, we have created several Maven profiles:
- owlapi4: This will compile all submodules of Evee except the Evee Protégé plugins. The resulting .jar files will have the OWL API version 4 as a dependency.
- owlapi5: This will compile all submodules of Evee except the Evee Protégé plugins. The resulting .jar files will have the OWL API version 5 as a dependency.
- protege: This will compile all submodules of Evee including the Evee Protégé plugins. As Protégé itself uses the OWL API version 4, every compiled .jar  file will have the OWL API version 4 as a dependency.
- complete: This will compile all submodules of Evee. Every library except for evee-elimination-proofs-fame and the Protégé plugins will be compiled in 2 versions, one using the OWL API version 4, the other using the OWL API version 5.

The standard profile is "complete", which can be used via the command `mvn clean install` from the root directory.
If you want to use any of the other profiles, use the command `mvn clean install -P !complete,profileName` instead, where *profileName* is one of the other 3 mentioned above.

For easy reuse of Evee as a library, use [evee-libs-owlapi4](evee-libs/evee-libs-owlapi4/pom.xml) or [evee-libs-owlapi5](evee-libs/evee-libs-owlapi5/pom.xml) as a dependency, depending on the version of the OWL API that you need.
These libraries contain all submodules of Evee except for the Evee Protégé plugins.

### Technical notes

Evee was developed for and tested with Java version 8.

Any Scala code of this repository was written for Scala version 2.12.6.

The Protégé plugins were developed for and tested with Protégé version 5.5.0.

On Windows, the Connection-Minimal Abduction solver was developed and tested with SPASS 3.5 for Windows.

Evee internally relies on the libraries FamePlus, Lethe (consisting of lethe-core, lethe-owlapi4, lethe-owlapi5, lethe-abduction, lethe-abduction-owlapi4 and lethe-abduction-owlapi5) and CAPI.
All these libraries have been pre-installed to a maven-repository in the directory [lib](lib).
If you only want to use the Evee plugins for Protégé, no further steps are required.
However, if you want to use Evee anywhere else (e.g. as part of your own project), Lethe, Fame and CAPI are required for some functionalities of Evee.
In this case, please declare the directory [lib](lib) as a repository in your pom like this:

```xml
<repository>
  <name>localRepository</name>
  <id>localRepository</id>
  <url>file:path/to/lib</url>
</repository>
```

In this declaration, `path/to/lib` should be replaced by the path to the directory [lib](lib).

## Disclaimer

FamePlus, CAPI and SPASS, all libraries and programs that Evee relies on internally, were *NOT* created by the developers of Evee.

For further information on Fame, please see [http://www.cs.man.ac.uk/~schmidt/sf-fame/](http://www.cs.man.ac.uk/~schmidt/sf-fame/ "http://www.cs.man.ac.uk/~schmidt/sf-fame/").

For further information on CAPI, please see [https://lat.inf.tu-dresden.de/~koopmann/CAPI/](https://lat.inf.tu-dresden.de/~koopmann/CAPI/ "https://lat.inf.tu-dresden.de/~koopmann/CAPI/").

For further information on SPASS, please see [https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover](https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover "https://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/classic-spass-theorem-prover").

Lethe has been developed by Patrick Koopmann; see [https://lat.inf.tu-dresden.de/~koopmann/LETHE/](https://lat.inf.tu-dresden.de/~koopmann/LETHE/ "https://lat.inf.tu-dresden.de/~koopmann/LETHE/").
