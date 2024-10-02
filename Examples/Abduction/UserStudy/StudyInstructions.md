# General information

This user study is intended to test the usability of different methods for the task of explaining missing entailments in an ontology.
To this end, 3 different ontologies have been prepared:
1. A modified version of the famous pizza ontology
2. An ontology about animals
3. An ontology about lectures

Each of these ontologies contains a number of "mistakes", which can be:
- Missing disjointness axioms
- Missing subclass axioms
- Wrong classification of concepts

Your goal is to fix these mistakes based on the comments of the following classes:
1. For the pizza ontology, see the subclasses of NamedPizza
2. For the animal ontology, see the subclasses of Abduction and Counterexample
3. For the lecture ontology, see the subclasses of SpecificLecture

By "comment", we mean the comment annotation that can be found on the top in the description of the class.
If a comment lists a class that *has not been derived by the reasoner* (HermiT), then this is a mistake that should be fixed.


# Rules

To fix the mistake that a given class *A* should be a subclass of *B*, you should not add the axiom "*A* SubClassOf *B*".
Instead, you should add more complex class expressions in an appropriate way, or add subclass relations to other classes than *A*. 
When adding new class expressions as subclass or equivalent class under the given class *A* in question (that is, not on another class), you have to follow the following restrictions:

1. For subclasses of NamedPizza, class expressions should only refer to the specific toppings of that pizza.
2. For animals, class expressions should only refer to subclasses of BodyPart (e.g. hasBodyPart some CircularMuscles) or Animal.
3. In the lecture ontology, you should only use subclasses of Location, Professor, TeachingElement or TopicComplexity.

Of course, definitions can also use object properties and should also fit the intended meaning of the class.

In some cases, you do not have to fix the definitions of a NamedPizza/Animal/SpecificLecture, but rather the definition of some other class to make the desired entailments hold.
However, if a comment of some class *A* states that "*A* SubClassOf *B*" should be entailed, you should *never* change the description of *B*.

# Using Protégé and the Missing Entailment Explanation tab 

In case you accidentally close the tab labeled "Missing Entailment Explanation", you can redisplay it via the following menu: Window -> Tabs -> Missing Entailment Explanation
