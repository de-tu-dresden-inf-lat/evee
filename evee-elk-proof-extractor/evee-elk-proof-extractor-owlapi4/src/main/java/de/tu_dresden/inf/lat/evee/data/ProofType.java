package de.tu_dresden.inf.lat.evee.data;

/**
 * 
 * @author Christian Alrabbaa
 *
 */
public enum ProofType {
	MinimalTreeSize, CondensedMinimalTreeSize, MinimalDepth, CondensedMinimalDepth, MinimalWeightedTreeSize,
	CondensedMinimalWeightedTreeSize, TreeUnravellingOfMinimalSizeGraph, MinimalSizeGraph;

	public static ProofType getTypeValue(String str) {
		if (str.equalsIgnoreCase(MinimalTreeSize.toString()))
			return ProofType.MinimalTreeSize;

		if (str.equalsIgnoreCase(CondensedMinimalTreeSize.toString()))
			return ProofType.CondensedMinimalTreeSize;

		if (str.equalsIgnoreCase(MinimalDepth.toString()))
			return ProofType.MinimalDepth;

		if (str.equalsIgnoreCase(CondensedMinimalDepth.toString()))
			return ProofType.CondensedMinimalDepth;

		if (str.equalsIgnoreCase(MinimalWeightedTreeSize.toString()))
			return ProofType.MinimalWeightedTreeSize;

		if (str.equalsIgnoreCase(CondensedMinimalWeightedTreeSize.toString()))
			return ProofType.CondensedMinimalWeightedTreeSize;

		if (str.equalsIgnoreCase(TreeUnravellingOfMinimalSizeGraph.toString()))
			return ProofType.TreeUnravellingOfMinimalSizeGraph;

		if (str.equalsIgnoreCase(MinimalSizeGraph.toString()))
			return ProofType.MinimalSizeGraph;

		throw new IllegalArgumentException("No TreeProofType value for \"" + str + "\"");
	}
}
