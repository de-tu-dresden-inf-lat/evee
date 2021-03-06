# EVEE - <u>ev</u>incing <u>e</u>xpressive DL <u>e</u>ntailments
This is the github-repository of *Evee*, a tool collection for explaining very expressive DL entailments.
For general information on Evee, please refer to the section "About Evee".
If you just want to try Evee with the Ontology Editor [Protégé](https://protege.stanford.edu/ "https://protege.stanford.edu/"), please refer to the section "Using the Evee plugins".
Information on how to install Evee and use it as a library in your own projects can be found in the section "Installing and using Evee".

For questions or feedback, you can contact Stefan Borgwardt (stefan.borgwardt@tu-dresden.de) or Patrick Koopmann (patrick.koopmann@tu-dresden.de).

## About Evee
Evee was developed at the [chair of automata theory at TU Dresden](https://tu-dresden.de/ing/informatik/thi/lat "https://tu-dresden.de/ing/informatik/thi/lat") and a system description has been submitted to [DL 2022](https://dai.fmph.uniba.sk/events/dl2022/ "https://dai.fmph.uniba.sk/events/dl2022/"), the 35th International Workshop on Description Logics, which takes place from 7. August 2022 to 10 August 2022 in Haifa, Israel.

Although logic-based ontology languages offer the inherent possibility of explaining the process of deriving implicit knowledge, explaining complex Description Logic (DL) entailments to users is still a challenging task.
So far, the ontology editor Protégé supports only (black-box) justifications and (glass-box) proofs for lightweight OWL 2 EL ontologies via the proof facilities of the reasoner [Elk](https://github.com/liveontologies/elk-reasoner "https://github.com/liveontologies/elk-reasoner").
Evee is a library for computing proofs that includes proof generation algorithms for DLs up to ALCH. It includes a recently developed technique for computing black-box *elimination proofs* as well as a glass-box technique based on the resolution calculus of *Lethe*, a tool for computing uniform interpolants.
Additionally, it provides methods to optimize proofs that are generated via other methods according to various measures of proof quality. The Evee library is used by two frontends: a collection of Protégé plugins of the same name and the standalone tool [*Evonne*](https://imld.de/en/research/research-projects/evonne/ "https://imld.de/en/research/research-projects/evonne/"), which supports more varied ways of displaying and interacting with proofs.

## Using the Evee plugins
1. Install [Protégé](https://protege.stanford.edu/ "https://protege.stanford.edu/"). Evee was developed for and tested with Protégé version 5.5.0.
2. Install both the [protege-proof-explanation](https://github.com/liveontologies/protege-proof-explanation "https://github.com/liveontologies/protege-proof-explanation") plugin and the [proof utility library](https://github.com/liveontologies/puli "https://github.com/liveontologies/puli") PULi.
3. Copy the .jar files from the directory "release" of this repository to the directory "plugins" of your local Protégé installation.

Note that the Evee plugins require the OWL Reasoner [HermiT](http://www.hermit-reasoner.com/index.html "http://www.hermit-reasoner.com/index.html"), which is already included as a plugin in Protégé version 5.5.0.

## Installing and using Evee

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
1. If you have never downloaded this repository, please use `git clone https://github.com/de-tu-dresden-inf-lat/evee --recurse-submodules`.
If you have already cloned this repository in the past, please use `git pull origin main` from the root directory.
These commands will create/update your local repository of Evee (including the submodule [lat-scala-dl-tools](https://github.com/de-tu-dresden-inf-lat/lat-scala-dl-tools "https://github.com/de-tu-dresden-inf-lat/lat-scala-dl-tools")) to the latest commit.

2. Currently, the latest stable version of Evee is tagged as `v0.1` (you can find previous stable versions by checking the *Releases* on GitHub).
To check out the commit of this version, use `git checkout tags/v0.1`.
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

For easy reuse of Evee as a library, please use [evee-libs-owlapi4](evee-libs/evee-libs-owlapi4/pom.xml) or [evee-libs-owlapi5](evee-libs/evee-libs-owlapi5/pom.xml) as a dependency, depending on the version of the OWL API that you need.
These libraries aggregate all submodules of Evee except for the Evee Protégé plugins.

### Technical notes

Evee was developed for and tested with Java version 8.

Any Scala code of this repository was written for Scala version 2.12.6.

The Protégé plugins were developed for and tested with Protégé version 5.5.0.

Evee internally relies on the libraries FamePlus and Lethe (consisting of lethe-core, lethe-owlapi4 and lethe-owlapi5).
All these libraries have been pre-installed to a maven-repository in the directory [lib](lib).
If you only want to use the Evee plugins for Protégé, no further steps are required.
However, if you want to use Evee anywhere else (e.g. as part of your own project), Lethe and Fame are required for some functionalities of Evee.
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

FamePlus, a library that Evee relies on internally, was *NOT* created by the developers of Evee.
For further information on Fame, please see [http://www.cs.man.ac.uk/~schmidt/sf-fame/](http://www.cs.man.ac.uk/~schmidt/sf-fame/ "http://www.cs.man.ac.uk/~schmidt/sf-fame/").

Lethe has been developed by Patrick Koopmann; see [https://lat.inf.tu-dresden.de/~koopmann/LETHE/](https://lat.inf.tu-dresden.de/~koopmann/LETHE/ "https://lat.inf.tu-dresden.de/~koopmann/LETHE/").
