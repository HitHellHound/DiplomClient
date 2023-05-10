package org.diplom.client.dto;

import java.util.List;

public class RecognitionMessage extends Message {
    private List<String> embeddedFaceVectors;

    public List<String> getEmbeddedFaceVectors() {
        return embeddedFaceVectors;
    }

    public void setEmbeddedFaceVectors(List<String> embeddedFaceVectors) {
        this.embeddedFaceVectors = embeddedFaceVectors;
    }
}
