# Evee

When cloning the repository, clone it with all its submodules via:
git clone URL --recurse-submodules 

Update submodules to current state:
1. git submodule update --remote
2. cd into directory and use git pull origin main

Remove detached HEAD state:
cd into submodules manually and checkout the current main branch

Need to install FamePlus-library (found in subdirectory forgetting-based-proofs/forgetting-based-proofs-fame-owlapi4/lib) via the following maven command:
mvn install:install-file -Dfile="absolute path to FamePlus1-SNAPSHOT.jar from directory lib" -DgroupId=FamePlus -DartifactId=FamePlus -Dversion=1-SNAPSHOT -Dpackaging=jar

To build complete project (including owlapi5-versions), use:
mvn clean install -P !owlapi4,complete