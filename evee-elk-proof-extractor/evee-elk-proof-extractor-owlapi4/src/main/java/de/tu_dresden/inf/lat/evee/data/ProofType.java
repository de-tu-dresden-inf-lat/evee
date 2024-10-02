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
		if (str.equalsIgnoreCase("MinimalTreeSize"))
			return ProofType.MinimalTreeSize;

		if (str.equalsIgnoreCase("CondensedMinimalTreeSize"))
			return ProofType.CondensedMinimalTreeSize;

		if (str.equalsIgnoreCase("MinimalDepth"))
			return ProofType.MinimalDepth;

		if (str.equalsIgnoreCase("CondensedMinimalDepth"))
			return ProofType.CondensedMinimalDepth;

		if (str.equalsIgnoreCase("MinimalWeightedTreeSize"))
			return ProofType.MinimalWeightedTreeSize;

		if (str.equalsIgnoreCase("CondensedMinimalWeightedTreeSize"))
			return ProofType.CondensedMinimalWeightedTreeSize;

		if (str.equalsIgnoreCase("TreeUnravellingOfMinimalSizeGraph"))
			return ProofType.TreeUnravellingOfMinimalSizeGraph;

		if (str.equalsIgnoreCase("MinimalSizeGraph"))
			return ProofType.MinimalSizeGraph;

		throw new IllegalArgumentException("No TreeProofType value for \"" + str + "\"");
	}
}
