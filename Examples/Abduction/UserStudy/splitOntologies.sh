#! /bin/bash

java -cp study-tools-assembly-0.1.0-SNAPSHOT.jar SplitOntologies PizzaOntologyForStudy.owl http://www.co-ode.org/ontologies/pizza/pizza.owl#NamedPizza http://www.co-ode.org/ontologies/pizza#CategoryForAbduction http://www.co-ode.org/ontologies/pizza#CategoryForModels http://www.co-ode.org/ontologies/pizza#CategoryForWithout
