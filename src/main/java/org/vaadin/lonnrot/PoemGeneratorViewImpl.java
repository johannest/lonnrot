package org.vaadin.lonnrot;

import com.vaadin.ui.Notification;
import org.vaadin.PoemGeneratorView;
import org.vaadin.lonnrot.dl4j.LonnrotModel;

import java.io.IOException;

public class PoemGeneratorViewImpl extends PoemGeneratorView {

    private LonnrotModel model;

    public PoemGeneratorViewImpl() {
        model = new LonnrotModel();
        try {
            model.loadModel();
        } catch (IOException e) {
            Notification.show("Failed to load model: training model");
            model.trainModel();
        }

        generateButton.addClickListener(e -> {
            String initialSeq = firstWordsField.getValue();
            int length = seqLengthField.getValue().intValue();
            String[] samples = model.sampleCharactersFromNetwork(initialSeq, length, 1);
            poemField.setValue(samples[0]);
        });

        trainMoreButton.addClickListener(e-> {
            model.trainMore();
        });
    }

}
