# EVEE - <u>e</u>xplaining <u>v</u>ery <u>e</u>xpressive DL <u>e</u>ntailments
This is the github-repository of *Evee*, a library for explaining very expressive DL entailments.
In this README, you will find general information on Evee (see the section "About Evee"), information on how to install Evee and the requirements you need for that (see the sections "Requirements" and "Installation") as well as information on how to use Evee (see the section "Using Evee").
For any questions, please contact [Stefan Borgwardt](https://github.com/stefborg) or [Patrick Koopmann](https://github.com/PKoopmann).

## About Evee
Evee was developed at TU Dresden (todo: link to the department?) and first published at DL 2022, the 35th International Workshop on Description Logics, which took place from 7. August 2022 to 10 August 2022 in Haifa, Israel (todo: link to the paper and/or conference).

Although logic-based ontology languages offer the inherent possibility of explaining the process of deriving implicit knowledge, explaining complex Description Logic (DL) entailments to users is still a challenging task.
So far, the ontology editor [Protégé](https://protege.stanford.edu/) supports only (black-box) justifications and (glass-box) proofs for lightweight OWL 2 EL ontologies via the proof facilities of the reasoner [Elk](https://github.com/liveontologies/elk-reasoner).
Here we present Evee, a new library for computing proofs that includes proof generation algorithms for DLs up to ALCH. It includes a recently developed technique for computing black-box *elimination proofs* as well as a glass-box technique based on the resolution calculus of *Lethe*, a tool for computing uniform interpolants.
Additionally, it provides methods to optimize proofs that are generated via other methods according to various measures of proof quality. The Evee library is used by two frontends: a collection of Protégé plugins of the same name and the standalone tool *Evonne* (todo: link?), which supports more varied ways of displaying and interacting with proofs.

## Requirements:
Evee requires the following software, which is included as a git-submodule:
- The library [lat-scala-dl-tools](https://github.com/de-tu-dresden-inf-lat/lat-scala-dl-tools)

The Evee-Protégé-plugins require the following other Protégé-plugins:
- The [protege-proof-explanation](https://github.com/liveontologies/protege-proof-explanation) plugin
- The [proof utility library](https://github.com/liveontologies/puli) PULi

These plugins need to be downloaded and installed into Protégé in order to use the Evee-Protégé-plugins.

Any scala code of this repository was written in scala version 2.12.6.
The Protégé-plugins were developed for and tested on Protégé version 5.5.0.

Note that Evee relies on the libraries FamePlus and Lethe (consisting of lethe-core, lethe-owlapi4 and lethe-owlapi4).
All these libraries have been pre-installed to a maven-repository in the directory [lib](todo: add link).
If you only want to use the Evee-plugins for Protégé, no further steps are required.
However, if you want to sue Evee anywhere else (e.g. as part of your own project), these libraries are required for Evee to function.
You can find them in these directories:
- [Fame](todo: add link)
- [Lethe-core](todo: add link)
- [Lethe-owlapi4](todo: add link)
- [Lethe-owlapi5](todo: add link)
Todo: Add reference to Fame-creators!

## Installation:
To clone this repository, use `git clone https://github.com/de-tu-dresden-inf-lat/evee --recurse-submodules`.
This will clone the repository for Evee as well as the submodule lat-scala-dl-tools, which is required by Evee.

Evee was developed to work with the owlapi versions 4 and 5.
However, as Protégé itself runs on the owlapi version 4, the Progégé-plugins can only be created for the owlapi 4 as well.

For easy compilation via maven, several profiles have been created:
- owlapi4: Compiles all modules except for the Protégé-plugins, the owlapi version 4 will be used.
- owlapi5: Compiles all modules except for the Protégé-plugins, the owlapi version 5 will be used.
- protege: Compiles the Protégé-plugins using the owlapi version 4.
- complete: Compiles all modules including the Protégé-plugins. All modules (except the Protégé plugins) will be compiled using both the owlapi versions 4 and 5.

For example, to compile everything you need to try Evee with Protégé use: `mvn clean install -P protege`.

All compiled jar files will be found in the directory XYZ.
For easy use, we have precompiled the Protégé-plugins in the directory [relese](todo: add link).

To install the Protégé-plugins to Protégé, simply copy the jar-files in the directory XYZ to the directory *plugins* of your Protégé-installation.

## Using Evee
