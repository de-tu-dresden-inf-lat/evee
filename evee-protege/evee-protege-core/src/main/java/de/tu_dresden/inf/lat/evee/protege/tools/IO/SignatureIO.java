package de.tu_dresden.inf.lat.evee.protege.tools.IO;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.Util;
import org.apache.commons.io.FilenameUtils;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.*;

public class SignatureIO {

    private static final Logger logger = LoggerFactory.getLogger(SignatureIO.class);

    public static Collection<OWLEntity> loadSignature(OWLEditorKit owlEditorKit) throws IOException {
        logger.debug("Loading signature form file");
        Set<OWLEntity> knownEntitySet = new HashSet<>();
        JFileChooser fileChooser = createFileChooser();
        int result = fileChooser.showOpenDialog(ProtegeManager.getInstance()
                .getFrame(owlEditorKit.getWorkspace()));
        List<IRI> classes = new ArrayList<>();
        List<IRI> objectProperties = new ArrayList<>();
        List<IRI> individuals = new ArrayList<>();
        List<IRI> currentList = classes;
        boolean includeOWLThing = false;
        boolean includeOWLNothing = false;
        boolean includeOWLTopObjectProp = false;
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            try (FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader)){
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    SignatureEnum signatureEnum;
                    try{
                        signatureEnum = SignatureEnum.valueOf(line);
                    } catch (IllegalArgumentException e){
                        signatureEnum = SignatureEnum.DEFAULT;
                    }
                    switch (signatureEnum) {
                        case USE_SIGNATURE:
                            break;
                        case TRUE:
                            logger.debug("Reading UseSignature==TRUE, ignored for nonEntailment");
                            break;
                        case FALSE:
                            logger.debug("Reading UseSignature==FALSE, ignored for nonEntailment");
                            break;
                        case CLASSES:
                            logger.debug("Loading classes");
                            currentList = classes;
                            break;
                        case OBJECT_PROPERTIES:
                            logger.debug("loading object properties");
                            currentList = objectProperties;
                            break;
                        case INDIVIDUALS:
                            logger.debug("loading individuals");
                            currentList = individuals;
                            break;
                        case OWL_THING:
                            logger.debug("including owl:Thing");
                            includeOWLThing = true;
                            break;
                        case OWL_NOTHING:
                            logger.debug("including owl:Nothing");
                            includeOWLNothing = true;
                            break;
                        case OWL_TOP_OBJECT_PROPERTY:
                            logger.debug("including owl:TopObjectProperty");
                            includeOWLTopObjectProp = true;
                            break;
                        default:
                            currentList.add(IRI.create(line));
                            break;
                    }
                }
        } catch (IOException e){
                logger.error("Error when loading signature from file: ", e);
                Util.showError("Error: " + e, owlEditorKit);
                throw e;
            }
        }
        else{
            logger.debug("Loading aborted");
        }
//        if (classes.size() == 0 && objectProperties.size() == 0 && individuals.size() == 0){
//            return knownEntitySet;
//        }
        OWLOntology activeOntology = owlEditorKit.getOWLModelManager().getActiveOntology();
        activeOntology.getClassesInSignature(Imports.INCLUDED).forEach(owlClass -> {
            if (classes.contains(owlClass.getIRI())){
                knownEntitySet.add(owlClass);
            }});
        activeOntology.getObjectPropertiesInSignature(Imports.INCLUDED).forEach(objectProperty -> {
            if (objectProperties.contains(objectProperty.getIRI())){
                knownEntitySet.add(objectProperty);
            }});
        activeOntology.getIndividualsInSignature(Imports.INCLUDED).forEach(individual -> {
            if (individuals.contains(individual.getIRI())){
                knownEntitySet.add(individual);
            }});
        OWLDataFactory dataFactory = owlEditorKit.getOWLModelManager().getOWLDataFactory();
        if (includeOWLThing){
            knownEntitySet.add(dataFactory.getOWLThing());
        }
        if (includeOWLNothing){
            knownEntitySet.add(dataFactory.getOWLNothing());
        }
        if (includeOWLTopObjectProp){
            knownEntitySet.add(dataFactory.getOWLTopObjectProperty());
        }
//        classes.forEach(iri ->
//                knownEntitySet.addAll(
//                        this.activeOntology.getEntitiesInSignature(
//                                iri)));
//        ontologyEntitySet.removeAll(knownEntitySet);
        return knownEntitySet;
    }

    public static void saveSignature(OWLEditorKit owlEditorKit, Collection<OWLEntity> signature) throws IOException {
        logger.debug("Saving signature to file");
        ArrayList<OWLEntity> classes = new ArrayList<>();
        ArrayList<OWLEntity> objectProperties = new ArrayList<>();
        ArrayList<OWLEntity> individuals = new ArrayList<>();
        signature.forEach(owlEntity -> {
            if (owlEntity.isOWLClass()){
                classes.add(owlEntity);
            }
            else if (owlEntity.isOWLObjectProperty()){
                objectProperties.add(owlEntity);
            }
            else if (owlEntity.isOWLNamedIndividual()){
                individuals.add(owlEntity);
            }
        });
        JFileChooser fileChooser = createFileChooser();
        int result = fileChooser.showSaveDialog(ProtegeManager.getInstance()
                .getFrame(owlEditorKit.getWorkspace()));
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            if (! FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("txt")) {
                file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + ".txt");
            }
            try (FileWriter fileWriter = new FileWriter(file);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)){
                bufferedWriter.write(SignatureEnum.USE_SIGNATURE + "\n");
                bufferedWriter.write(SignatureEnum.TRUE + "\n"); // written to keep compatibility between proofSignature-File and nonEntailmentSignature-File
                bufferedWriter.write(SignatureEnum.CLASSES + "\n");
                for (OWLEntity entity : classes){
                    if (entity.isTopEntity()){
                        bufferedWriter.write(SignatureEnum.OWL_THING + "\n");
                    } else if (entity.isBottomEntity()){
                        bufferedWriter.write(SignatureEnum.OWL_NOTHING + "\n");
                    } else{
                        bufferedWriter.write(entity.getIRI() + "\n");
                    }
                }
                bufferedWriter.write(SignatureEnum.OBJECT_PROPERTIES + "\n");
                for (OWLEntity entity : objectProperties){
                    if (entity.isTopEntity()){
                        bufferedWriter.write(SignatureEnum.OWL_TOP_OBJECT_PROPERTY + "\n");
                    } else{
                        bufferedWriter.write(entity.getIRI() + "\n");
                    }
                }
                bufferedWriter.write(SignatureEnum.INDIVIDUALS + "\n");
                for (OWLEntity entity : individuals){
                    bufferedWriter.write(entity.getIRI() + "\n");
                }
            } catch (IOException e){
                logger.error("Error when saving signature to file: ", e);
                Util.showError("Error: " + e, owlEditorKit);
                throw e;
            }
        }
        else{
            logger.debug("Saving aborted");
        }
    }

    public static JFileChooser createFileChooser(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(
                "txt files (*.txt)", "txt");
        fileChooser.setFileFilter(fileFilter);
        return fileChooser;
    }


}
