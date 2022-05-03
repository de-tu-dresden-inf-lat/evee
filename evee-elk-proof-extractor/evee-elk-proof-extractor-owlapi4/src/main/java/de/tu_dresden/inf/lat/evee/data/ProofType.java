package de.tu_dresden.inf.lat.evee.data;

/**
 * 
 * @author Christian Alrabbaa
 *
 */
public enum ProofType {
	MinimalTreeSize, CondensedMinimalTreeSize, MinimalDepth, CondensedMinimalDepth, MinimalWeightedTreeSize,
	CondensedMinimalWeightedTreeSize, TreeUnravellingOFMinimalSizeGraph, MinimalSizeGraph;

	public static ProofType getTypeValue(String str) {
		if (str.toLowerCase().equals("mintreesize"))
			return ProofType.MinimalTreeSize;

		if (str.toLowerCase().equals("conmintreesize"))
			return ProofType.CondensedMinimalTreeSize;

		if (str.toLowerCase().equals("mindepth"))
			return ProofType.MinimalDepth;

		if (str.toLowerCase().equals("conmindepth"))
			return ProofType.CondensedMinimalDepth;

		if (str.toLowerCase().equals("minwtreesize"))
			return ProofType.MinimalWeightedTreeSize;

		if (str.toLowerCase().equals("conminwtreesize"))
			return ProofType.CondensedMinimalWeightedTreeSize;

		if (str.toLowerCase().equals("unrgraph"))
			return ProofType.TreeUnravellingOFMinimalSizeGraph;

		if (str.toLowerCase().equals("mingraph"))
			return ProofType.MinimalSizeGraph;

		throw new IllegalArgumentException("No TreeProofType value for \"" + str + "\"");
	}
}
