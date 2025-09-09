#! /usr/bin/python

import sys, random

if(len(sys.argv)!=2):
    print("Usage: ")
    print(" give as argument the number of participants of the study")
    print(" we will then generate configurations for that number")
    exit()

participantNumber = int(sys.argv[1])

ontologies = ["animal", "pizza", "university"]
studyParts = ["no_tool", "Fix ontology with tool support"]
studyPartIndices = [0,1]
studySubParts = ["abduction", "counterexample"]


def useOntology():
    global currentOnt
    currentOnt += 1
    return ontologies[currentOnt % 3]

def elements(studyPartIndex):
    if(studyPartIndex==0):
        return studyParts[0]+"-"+useOntology()
    elif(studyPartIndex==1):
        return studySubParts[0]+"-"+useOntology()+" / "+studySubParts[1]+"-"+useOntology()

def oneConfiguration():
    global currentOnt
    
    random.shuffle(ontologies)
    random.shuffle(studyPartIndices)
    random.shuffle(studySubParts)
    
    currentOnt = 0

    print(" / ".join([elements(part) for part in studyPartIndices]))

    


for i in range(0, participantNumber):
    oneConfiguration()
