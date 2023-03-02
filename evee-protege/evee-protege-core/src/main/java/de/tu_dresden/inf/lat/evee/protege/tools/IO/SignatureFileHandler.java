package de.tu_dresden.inf.lat.evee.protege.tools.IO;

import de.tu_dresden.inf.lat.evee.protege.tools.ui.UIUtilities;
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

public class SignatureFileHandler {

    private boolean useSignature;
    private final Set<OWLEntity> signature;
    private final OWLEditorKit owlEditorKit;

    private final Logger logger = LoggerFactory.getLogger(SignatureFileHandler.class);

    public SignatureFileHandler(OWLEditorKit owlEditorKit){
        this.useSignature = false;
        this.owlEditorKit = owlEditorKit;
        this.signature = new HashSet<>();
    }

    public boolean getUseSignature(){
        return this.useSignature;
    }

    public Collection<OWLEntity> getSignature(){
        return this.signature;
    }

    public void setUseSignature(boolean useSignature){
        this.useSignature = useSignature;
    }

    public void setSignature(Collection<OWLEntity> signature){
        this.signature.clear();
        this.signature.addAll(signature);
    }

    /**
     * will only load those OWLEntities that are present in the signature of the currently active ontology
     */
    public void loadFile() throws IOException {
        this.logger.debug("Loading signature form file");
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showOpenDialog(ProtegeManager.getInstance()
                .getFrame(this.owlEditorKit.getWorkspace()));
        List<IRI> classes = new ArrayList<>();
        List<IRI> objectProperties = new ArrayList<>();
        List<IRI> individuals = new ArrayList<>();
        List<IRI> currentList = classes;
        boolean includeOWLThing = false;
        boolean includeOWLNothing = false;
        boolean includeOWLTopObjectProp = false;
        boolean includeOWLBotObjectProp = false;
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
                            logger.debug("reading useSignature = true");
                            this.useSignature = true;
                            break;
                        case FALSE:
                            logger.debug("loading useSignature = false");
                            this.useSignature = false;
                            break;
                        case CLASSES:
                            logger.debug("loading classes");
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
                        case OWL_BOTTOM_OBJECT_PROPERTY:
                            logger.debug("including owl:BottomObjectProperty");
                            includeOWLBotObjectProp = true;
                            break;
                        default:
                            currentList.add(IRI.create(line));
                            break;
                    }
                }
        } catch (IOException e){
                logger.error("Error when loading signature from file: ", e);
                UIUtilities.showError("Error: " + e, this.owlEditorKit);
                throw e;
            }
        }
        else{
            logger.debug("Loading aborted");
        }
//        if (classes.size() == 0 && objectProperties.size() == 0 && individuals.size() == 0){
//            return knownEntitySet;
//        }
        OWLOntology activeOntology = this.owlEditorKit.getOWLModelManager().getActiveOntology();
        activeOntology.getClassesInSignature(Imports.INCLUDED).forEach(owlClass -> {
            if (classes.contains(owlClass.getIRI())){
                this.signature.add(owlClass);
            }});
        activeOntology.getObjectPropertiesInSignature(Imports.INCLUDED).forEach(objectProperty -> {
            if (objectProperties.contains(objectProperty.getIRI())){
                this.signature.add(objectProperty);
            }});
        activeOntology.getIndividualsInSignature(Imports.INCLUDED).forEach(individual -> {
            if (individuals.contains(individual.getIRI())){
                this.signature.add(individual);
            }});
        OWLDataFactory dataFactory = this.owlEditorKit.getOWLModelManager().getOWLDataFactory();
        if (includeOWLThing){
            this.signature.add(dataFactory.getOWLThing());
        }
        if (includeOWLNothing){
            this.signature.add(dataFactory.getOWLNothing());
        }
        if (includeOWLTopObjectProp){
            this.signature.add(dataFactory.getOWLTopObjectProperty());
        }
        if (includeOWLBotObjectProp){
            this.signature.add(dataFactory.getOWLBottomObjectProperty());
        }
//        classes.forEach(iri ->
//                knownEntitySet.addAll(
//                        this.activeOntology.getEntitiesInSignature(
//                                iri)));
//        ontologyEntitySet.removeAll(knownEntitySet);
    }

    public void saveSignature() throws IOException {
        this.logger.debug("Saving signature to file");
        ArrayList<OWLEntity> classes = new ArrayList<>();
        ArrayList<OWLEntity> objectProperties = new ArrayList<>();
        ArrayList<OWLEntity> individuals = new ArrayList<>();
        this.signature.forEach(owlEntity -> {
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
        JFileChooser fileChooser = this.createFileChooser();
        int result = fileChooser.showSaveDialog(ProtegeManager.getInstance()
                .getFrame(this.owlEditorKit.getWorkspace()));
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            if (! FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("txt")) {
                file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + ".txt");
            }
            try (FileWriter fileWriter = new FileWriter(file);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)){
                bufferedWriter.write(SignatureEnum.USE_SIGNATURE + "\n");
                if (this.useSignature) {
                    bufferedWriter.write(SignatureEnum.TRUE + "\n");
                } else{
                    bufferedWriter.write(SignatureEnum.FALSE + "\n");
                }
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
                    } else if (entity.isBottomEntity()){
                        bufferedWriter.write(SignatureEnum.OWL_BOTTOM_OBJECT_PROPERTY + "\n");
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
                UIUtilities.showError("Error: " + e, this.owlEditorKit);
                throw e;
            }
        }
        else{
            logger.debug("Saving aborted");
        }
    }

    public JFileChooser createFileChooser(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(
                "txt files (*.txt)", "txt");
        fileChooser.setFileFilter(fileFilter);
        return fileChooser;
    }


}
