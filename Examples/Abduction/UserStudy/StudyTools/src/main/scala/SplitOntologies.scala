import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{IRI, OWLAxiom}

import java.io.{File, FileOutputStream}

object SplitOntologies {

  def printUsageAndExit() = {
    println("Usage: ")
    println("  "+this.getClass.getName()+ " ONTOLOGY_FILE CLASS CATEGORY_1 CATEGORY_2 ...")
    println()
    println("Assumes that each CATEGORY_i is a direct subclass of CLASS.")
    println("and generates one ontology for each category, where the other categories with their subclasses are removed," +
      "and the subclasses of the respective CATEGORY become subclasses of CLASS.")
    System.exit(0)
  }

  def main(args: Array[String]) = {
    if(args.isEmpty)
      printUsageAndExit()

    val manager = OWLManager.createOWLOntologyManager()
    val ontology = manager.loadOntologyFromOntologyDocument(new File(args(0)))

    val factory = manager.getOWLDataFactory()

    val mainClass = factory.getOWLClass(IRI.create(args(1)))

    val categories = args.toSeq.drop(2).map(x => factory.getOWLClass(IRI.create(x)))

    val ignoreClass = factory.getOWLClass(IRI.create(mainClass.getIRI.getNamespace+"Ignore"))

    categories.foreach {c =>
      ontology.addAxiom(factory.getOWLSubClassOfAxiom(c,ignoreClass))
    }

    categories.foreach { category =>
      println("Category: "+category)
      val otherCategories = (Set() ++ categories) - category
      var added = Set[OWLAxiom]()
      var removed = Set[OWLAxiom]()

      var toRemove = factory.getOWLSubClassOfAxiom(category,mainClass)
      ontology.remove(toRemove)
      removed+=toRemove
      println("Remove: "+toRemove)

      otherCategories.foreach { category2 =>
        ontology.getSubClassAxiomsForSuperClass(category2).forEach { ax =>
          ontology.remove(ax)
          removed+=ax
          val toAdd = factory.getOWLSubClassOfAxiom(ax.getSubClass, ignoreClass)
          added+=toAdd
          ontology.add(toAdd)
        }
        var toRemove = factory.getOWLSubClassOfAxiom(category2,mainClass)
        println("Remove: "+toRemove)
        ontology.remove(toRemove)
        removed+=toRemove
      }


      ontology.getSubClassAxiomsForSuperClass(category).forEach { ax =>
        ontology.remove(ax)
        ontology.remove(factory.getOWLSubClassOfAxiom(ax.getSubClass,ignoreClass))
        removed += ax
        val toAdd = factory.getOWLSubClassOfAxiom(
          ax.getSubClass, mainClass
        )
        ontology.addAxiom(toAdd)
        println("Remove: "+ax)
        println("Add: "+toAdd)
        added += toAdd
      }
      // not sure why the following is needed
      //ontology.removeAxiom(factory.getOWLSubClassOfAxiom(mainClass,mainClass))

      manager.saveOntology(ontology, new FileOutputStream(new File(args(0)+"_"+category.getIRI.getShortForm()+".owl")))
      removed.foreach(ontology.addAxiom)
      added.foreach(ontology.removeAxiom)
    }



  }
}
