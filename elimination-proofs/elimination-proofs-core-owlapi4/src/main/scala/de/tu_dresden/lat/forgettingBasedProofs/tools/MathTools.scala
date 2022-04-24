package de.tu_dresden.inf.lat.forgettingBasedProofs.tools

object MathTools {

  def factorial(n: Int) : Int = n match {
    case 0 => 1
    case 1 => 1
    case n if n>1 => n*factorial(n-1)
  }

}
