#! /usr/bin/python

import random

ontologies = ["Animal Ontology", "Pizza Ontology", "University Ontology"]
studyParts = ["Fix ontology without tool support", "Fix ontology with tool support"]
studyPartIndices = [0,1]
studySubParts = ["Fix ontology using abduction", "Fix ontology using counter examples"]

random.shuffle(ontologies)
random.shuffle(studyPartIndices)
random.shuffle(studySubParts)

currentOnt = 0

def useOntology():
    global currentOnt
    print("     using "+ontologies[currentOnt])
    currentOnt+=1
    
def printElementsFor(studyPartIndex):
    if(studyPartIndex==0):
        useOntology()
    elif(studyPartIndex==1):
        print(" - "+studySubParts[0])
        useOntology()
        print(" - "+studySubParts[1])
        useOntology()

print("Steps in this study")
print("1. "+studyParts[studyPartIndices[0]])
printElementsFor(studyPartIndices[0])
print("2. "+studyParts[studyPartIndices[1]])
printElementsFor(studyPartIndices[1])
