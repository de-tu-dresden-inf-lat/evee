package de.tu_dresden.inf.lat.evee.eliminationProofs.tools

import com.typesafe.scalalogging.Logger
import de.tu_dresden.inf.lat.evee.general.interfaces.IProgressTracker

import scala.collection.immutable.Range


/**
 * Report on progress for search in trees.
 *
 * @param internalProgressTracker - the progress tracker to report to
 * @param searchDepth - the maximal search depth in the search
 * @param delta - remaining progress that should remain after the search tree is fully explored
 */
class SearchTreeProgressTracker(internalProgressTracker: IProgressTracker,
                                _searchDepth: Int,
                                delta: Int)  {

  val logger = Logger[SearchTreeProgressTracker]

  val MAX_DEPTH = 12 // beyond this depth we would get an overflow

  val searchDepth = Math.min(_searchDepth, MAX_DEPTH)

  /*for(i <- 1 to 13) {
    println (i, i*MathTools.factorial(i))
  }*/

   /**
   * We overapproximate the size of the search tree.
   * Let n be the signature size.
   * Then there are n! many paths in the search (each branching by the number of remaining symbols).
   * We multiply this by their maximal path length of n, obtaining n*n! as maximal progress.
   */
  val maxProgress = searchDepth*MathTools.factorial(searchDepth)


  internalProgressTracker.setMax( maxProgress + delta)

  /**
   * Increment the progress trackers appropriately assuming we are on the givenn level in the search tree.
   * Needs the previous increment before going down in the search tree.
   *
   * @param previous
   * @param level
   * @return
   */
  def increment(previous:Long, level: Int): Long = {

    /**
     * The increment of progress on this level is (n+1)*n!, with n=(a-b), a being the signature size and b the current
     * level. The search tree under the node on this level has n! paths, each having a length of n+1 (counting also the
     * current node)
     */

    if(level>=searchDepth )
      return previous

    val stepSize = (searchDepth-level+1)*MathTools.factorial(searchDepth-level)
    logger.trace("level: "+level+" of "+searchDepth)
    logger.trace("stepSize: "+stepSize)
    logger.trace("previous: "+previous)
    internalProgressTracker.setProgress(Math.min(previous+stepSize,maxProgress))
    internalProgressTracker.getProgress()
  }

}
