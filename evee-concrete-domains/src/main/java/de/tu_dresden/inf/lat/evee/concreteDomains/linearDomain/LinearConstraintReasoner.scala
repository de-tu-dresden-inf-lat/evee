package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain

import de.tu_dresden.inf.lat.evee.concreteDomains.CDReasoner

@deprecated
class LinearConstraintReasoner extends CDReasoner[LinearConstraint[Double]] {

  override def consistent(predicates: Iterable[LinearConstraint[Double]]): Boolean = {
    println("Checking " + predicates + " for consistency")
    val result = if(predicates.isEmpty)
      return true
    else {
      val (matrix, constants) = MatrixConversions.toMatrixEquationSystem(predicates)
      MatrixTools.hasSolution(matrix, constants)
    }
    println("Consistency is: "+result)
    result
  }

  override def implies(premises: Iterable[LinearConstraint[Double]], conclusion: LinearConstraint[Double]): Boolean = {
    println("Checking whether "+premises)
    println("entails "+conclusion)

    if(premises.nonEmpty)
      println("Matrix1: "+MatrixConversions.toMatrix(premises))
      println("Matrix2: "+MatrixConversions.toMatrix(premises++Some(conclusion)))

    if(!consistent(premises)) // this check is not really necessary, as it is usually done anyway before checking an implication - maybe we should just assume this as a precondition on the first argument?
      return true
    else {
      // idea: see constraints as matrix
      // if adding the conclusion changes the rank, then the conclusion is not entailed, otherwise, it is
      val rankBefore = if(premises.isEmpty)
        0
      else
        MatrixTools.rank(MatrixConversions.toMatrix(premises))

      val rankAfter = MatrixTools.rank(MatrixConversions.toMatrix(premises ++ Some(conclusion)))

      println("Rank1: "+rankBefore)
      println("Rank2: "+rankAfter)

      return rankBefore==rankAfter
    }
  }
}
