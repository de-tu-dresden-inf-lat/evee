package de.tu_dresden.inf.lat.proofs.interfaces;

import de.tu_dresden.inf.lat.proofs.data.exceptions.ParsingException;

import java.io.File;

public interface IProofParser<SENTENCE> {

    IProof<SENTENCE> fromFile(File file);
    IProof<SENTENCE> parseProof(String string) throws ParsingException;

}
