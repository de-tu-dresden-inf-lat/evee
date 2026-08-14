package de.tu_dresden.inf.lat.evee.concreteDomains.linearDomain

import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector, DecompositionSolver, LUDecomposition, RRQRDecomposition, SingularValueDecomposition, RealMatrix, RealVector}

@deprecated
object MatrixTools {

  val RANK_THRESHOLD = 1e-11
  val SINGULARITY_THRESHOLD = 1e-11

  def hasSolution(matrix: RealMatrix, constants: RealVector) = {
    // rank of coefficient matrix
    // rank of coefficient matrix with constant terms
    // equal: solution, inequal: unsatisfiable
    //val solver = new LUDecomposition(matrix).getSolver()
    //val solution = solver.solve(constants)
    val rank1 = rank(matrix)
    val rank2 = rank(addColumn(matrix,constants))

    rank1==rank2
  }

  def addColumn(matrix: RealMatrix, newColumn: RealVector) = {
    val combinedMatrix = new Array2DRowRealMatrix(matrix.getRowDimension, matrix.getColumnDimension+1)
    combinedMatrix.setSubMatrix(matrix.getData,0,0)
    combinedMatrix.setColumn(matrix.getColumnDimension, newColumn.toArray)
    combinedMatrix
  }

  def addRow(matrix: RealMatrix, newRow: RealVector) = {
    val combinedMatrix = new Array2DRowRealMatrix(matrix.getRowDimension+1, matrix.getColumnDimension)
    combinedMatrix.setSubMatrix(matrix.getData,0,0)
    combinedMatrix.setRow(matrix.getColumnDimension, newRow.toArray)
    combinedMatrix
  }

  def rank(matrix: RealMatrix): Integer =
//    new LUDecomposition(matrix, SINGULARITY_THRESHOLD)
//    new RRQRDecomposition(matrix).getRank(RANK_THRESHOLD)
    return new SingularValueDecomposition(matrix).getRank() // uses internally fixed thresholds

}
