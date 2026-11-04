package de.tu_dresden.inf.lat.evee.concreteDomains.data.exceptions

/**
 * A constraint or the concrete domain of an ontology cannot be read; the message says what is wrong and where. It is
 * unchecked, so that the reasoner factory can pass it on as it is (Protégé shows its message).
 */
class ParsingException(message: String) extends RuntimeException(message) {

}
