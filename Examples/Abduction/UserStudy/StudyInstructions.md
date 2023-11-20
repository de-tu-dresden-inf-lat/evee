# General information

This user study is intended to test the usability of different methods for the task of explaining missing entailments in an ontology.
To this end, 3 different ontologies have been prepared:
1. A modified version of the famous pizza-ontology
2. An ontology for modeling animals
3. An ontology for modeling lectures

Each of these ontologies contains a number of "mistakes", which can be:
- Missing Disjointness-Axioms
- Missing SubClassOf-Axioms
- Wrong classification of concepts

Your goal is to fix these mistakes based on the comments of the following classes:
1. For the pizza ontology, see the subclasses of NamedPizza
2. For the animal ontology, see ...
3. For the lecture ontology, see the subclasses of SpecificLecture

If a comment lists a class that has not been derived, then this is a mistake that should be fixed.

# Rules

You should not use generic concepts to fix the mistakes.
That is, for a given concept *A* which should belong to some concept *B*, do not add the axiom "*A* SubClassOf *B*" to fix the mistake.
Instead, you should specify the "properties of *A*" in order to derive "*A* SubClassOf *B*".
For the individual ontologies, these "properties" are:
1. Subclasses of PizzaTopping for the pizza ontology
2. Subclasses of ... for the animal ontology 
3. Subclasses of Location, Professor, TeachingElement or TopicComplexity for the lecture ontology 

Note that in some cases you do not have to fix the descriptions of a NamedPizza/.../SpecificLecture, but rather the description of some other concept to make the desired entailments hold.
However, if a comment of some concept *A* states that "*A* SubClassOf *B*" should be entailed, you should NEVER change the description of *B*.

# Using Protégé and the Missing Entailment Explanation tab 

In case you accidentally close the tab labeled "Missing Entailment Explanation", you can redisplay it via the following menu: Window -> Tabs -> Missing Entailment Explanation


//TODO Tom: What other information should go here? Do we even need this section?