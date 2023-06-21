package de.tu_dresden.inf.lat.evee.proofs.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.tu_dresden.inf.lat.evee.general.data.exceptions.ParsingException;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IInference;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProof;
import de.tu_dresden.inf.lat.evee.proofs.interfaces.IProofParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonStringProofParser implements IProofParser<String> {

    private static class LazyHolder {
        static JsonStringProofParser instance = new JsonStringProofParser();
    }

    public static JsonStringProofParser getInstance() {
        return JsonStringProofParser.LazyHolder.instance;
    }



    @Override
    public IProof<String> fromFile(File file) {
        ObjectMapper mapper = getMapper();

        try {
            return mapper.readValue(file, proofType(mapper));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public IProof<String> parseProof(String string) throws ParsingException {

        ObjectMapper mapper = getMapper();

        try {
            return mapper.readValue(string, proofType(mapper));
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }


    private ObjectMapper getMapper(){
        abstract class IProofMixin {
            @JsonDeserialize(as = String.class) abstract String getFinalConclusion();
        }

        abstract class IInferenceMixin {
            @JsonDeserialize(as = String.class) abstract String getConclusion();
            @JsonDeserialize(contentAs = String.class) abstract List<String> getPremises();
        }
        ObjectMapper mapper = new ObjectMapper();

        mapper.addMixIn(IProof.class, IProofMixin.class);
        mapper.addMixIn(IInference.class, IInferenceMixin.class);

        return mapper;
    }

    private JavaType proofType(ObjectMapper mapper){
        return mapper.getTypeFactory().constructParametricType(IProof.class, String.class);
    }
}