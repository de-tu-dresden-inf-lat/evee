package de.tu_dresden.inf.lat.counterExample;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter;
import de.tu_dresden.inf.lat.exceptions.EntityCheckerException;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.ElkModel;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

public class StatsMain {
	private static final Logger logger = Logger.getLogger(StatsMain.class);

	public static void main(String[] args) throws OWLOntologyCreationException, EntityCheckerException, IOException {
		Options options = new Options();

		Option ontologyPath = new Option("o", "ontology-path", true, "The path of the ontology");
		ontologyPath.setRequired(true);
		options.addOption(ontologyPath);

		Option conclusionAxiom = new Option("a", "conclusion-axiom", true,
				"An axiom that does not follow from the ontology ");
		conclusionAxiom.setRequired(false);
		options.addOption(conclusionAxiom);

		Option outFile = new Option("fn", "model-file-name", true, "The name of the output file");
		outFile.setRequired(false);
		options.addOption(outFile);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			logger.error(e.getMessage());
			formatter.printHelp("utility-name", options);

			System.exit(1);
		}

		String ontologyPathStr = cmd.getOptionValue("ontology-path");
		String conclusionAxiomStr = cmd.getOptionValue("conclusion-axiom");
		String outFileStr = cmd.getOptionValue("model-file-name");

		OWLOntology ontology = OWLManager.createOWLOntologyManager()
				.loadOntologyFromOntologyDocument(new File(ontologyPathStr));

		OWLAxiom axiom = null;
		OWLSubClassOfAxiom subClassOfAxiom = ToOWLTools.getInstance().getBotIsTopCI();

		if (cmd.hasOption("a")) {
			axiom = ToOWLTools.getInstance().getOWLAxiomFromStr(conclusionAxiomStr, ontology);

			if (axiom.getAxiomType() != AxiomType.SUBCLASS_OF) {
				logger.error("Only SubClassOf: axioms are supported!");
				System.exit(1);
			} else {
				subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
				logger.info("input axiom -> " + SimpleOWLFormatter.format(subClassOfAxiom));
			}
		}

		File modelFile;
		String outFileName = "model";
		if (cmd.hasOption("fn")) {
			modelFile = new File(outFileStr);
		} else
			modelFile = new File(outFileName + ".txt");

		ELKModelGenerator elkCounterModel = new ELKModelGenerator(ontology, subClassOfAxiom);

		long start = System.currentTimeMillis();

		Set<Element> rawModel = elkCounterModel.generateFullRawCanonicalModelElements();
		long endRow = System.currentTimeMillis() - start;

		ElkModel finalizedModel = new ElkModel(rawModel, elkCounterModel.getMapper());
		long endFinalized = System.currentTimeMillis() - start;

		assert rawModel.size() == finalizedModel.getFinalizedModelElements()
				.size() : "Error! this should not happen. please send me the ontology and the axiom you used!";

		int edgesCount = 0;
		for (Element e : finalizedModel.getFinalizedModelElements()) {
			for (Relation r : e.getRelations()) {
				if (r.isForward()) {
					edgesCount++;
				}
			}
		}

		logger.info("Total number of elements -> " + rawModel.size());
		logger.info("Total number of edges -> " + edgesCount);
		logger.info("Total time, raw model generation -> " + endRow);
		logger.info("Total time, finalised model generation -> " + endFinalized);

		ModelAsSetsFormatter.writeAsSets(finalizedModel.getFinalizedModelElements(), modelFile);

		CSVPrinter csvPrinter = createCSVFileStats(outFileName);
		csvPrinter.printRecord(rawModel.size(), edgesCount, endRow, endFinalized);
		csvPrinter.flush();

		logger.info("Done!");

	}

	private static CSVPrinter createCSVFileStats(String modelFileName) throws IOException {
		String[] HEADERS = { "Total Number of Elements", "Total Number of Edges", "Raw Model Total Time (ms)",
				"Finalized Model Total Time (ms)" };

		FileWriter out = new FileWriter("stats_" + modelFileName + ".csv");
		CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(HEADERS));

		return printer;
	}
}
