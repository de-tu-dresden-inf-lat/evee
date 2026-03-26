package de.tu_dresden.inf.lat.model.interfaces;

import java.io.File;

public interface IGenerator extends IData {

	static final String modelDirectory = "generatedCounterModel";
	static final String proofDirectory = "generatedProofs";
	static final String proverDirectory = "MetTelResources";

	static final String specificationsFile = modelDirectory + File.separator + "specs.s";
	static final String calculusExtensionFile = modelDirectory + File.separator + "tbox.s";
	static final String inputFile = modelDirectory + File.separator + "conclusion.input";

	static final String proverJarFile = proverDirectory + File.separator + NAME + ".jar";

	static final String modelFileExtension = ".model";
	static final String proofFileExtension = ".proof";
	static final String jsonFileExtension = ".json";
	static final String graphFileExtension = ".png";
	static final String graphMLFileExtension = ".xml";
	static final String textFileExtension = ".txt";

	static final String hyperGraphGMLFileExtension = ".hg" + graphMLFileExtension;
	static final String complexGraphGMLFileExtension = ".cg" + graphMLFileExtension;
	static final String treeGMLFileExtension = ".t" + graphMLFileExtension;
	static final String hyperTreeGMLFileExtension = ".ht" + graphMLFileExtension;

	static final String dlFileExtension = proofFileExtension + ".txt";

	static final String proofJsonFileExtension = proofFileExtension + jsonFileExtension;
	static final String modelJsonFileExtension = modelFileExtension + jsonFileExtension;

//	static final OWLClass classAlias = ObjectGenerator.getInstance().getNextConceptName();
}
