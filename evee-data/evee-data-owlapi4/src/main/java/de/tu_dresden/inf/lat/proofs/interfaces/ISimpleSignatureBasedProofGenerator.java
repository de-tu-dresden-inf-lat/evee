package de.tu_dresden.inf.lat.proofs.interfaces;

import java.util.Collection;

public interface ISimpleSignatureBasedProofGenerator<SIGNATURE,SYMBOL,SENTENCE,THEORY>
        extends ISimpleProofGenerator<SYMBOL,SENTENCE,THEORY>, ISignatureBasedProofGenerator<SIGNATURE,SENTENCE,THEORY> {

    void setSignature(Collection<SIGNATURE> knownSignature);
}
