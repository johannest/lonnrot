package org.vaadin.lonnrot;

import java.io.IOException;
import java.util.Arrays;

import org.vaadin.lonnrot.dl4j.LonnrotModel;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.router.RouterLayout;
import com.vaadin.router.event.AfterNavigationEvent;
import com.vaadin.router.event.AfterNavigationObserver;
import com.vaadin.ui.button.Button;
import com.vaadin.ui.combobox.ComboBox;
import com.vaadin.ui.html.Anchor;
import com.vaadin.ui.html.Div;
import com.vaadin.ui.html.H2;
import com.vaadin.ui.html.H3;
import com.vaadin.ui.html.Label;
import com.vaadin.ui.layout.FlexLayout;
import com.vaadin.ui.layout.HorizontalLayout;
import com.vaadin.ui.textfield.TextField;

public class MainLayout extends Div
        implements RouterLayout, AfterNavigationObserver {

    private LonnrotModel model;
    private TextArea resultTextArea;

    public MainLayout() {
        H2 title = new H2("Experimental deep learning (RNN) demonstration for generating traditional Finnish poems");
        title.setWidth("100%");

        H3 description1 = new H3("RNN teached with Kalevala, Kanteletar and other poems collected by Elias Lönnrot. ");
        H3 description2 = new H3("Kokeellinen deep learning Kalevala-runogeneraattori");
        description1.setWidth("100%");
        description2.setWidth("100%");

        HorizontalLayout inputLayout = new HorizontalLayout();
        inputLayout.setWidth("100%");
        inputLayout.addClassName("input-layout");
        inputLayout.setDefaultVerticalComponentAlignment(FlexLayout.Alignment.END);

        TextField firstWordsField = new TextField("Optional first words (ensimmäiset sanat) 0-160 chars");
        firstWordsField.setWidth("100%");
        firstWordsField.addClassName("first-words");

        ComboBox<Integer> seqLengthField = new ComboBox<>("Lenght (chars)");
        seqLengthField.addClassName("length-select");
        seqLengthField.setWidth("120px");
        seqLengthField.setDataProvider(new ListDataProvider<Integer>(Arrays.asList(100, 150, 200, 250, 500, 750)));
        seqLengthField.setValue(200);

        Button generateButton = new Button("Generate");
        generateButton.setWidth("150px");
        generateButton.addClassName("friendly");
        generateButton.addClickListener(e-> {
            String initialSeq = firstWordsField.getValue();
            int length = seqLengthField.getValue();
            String[] samples = model.sampleCharactersFromNetwork(initialSeq, length, 1);

            System.out.println(samples[0]);
            resultTextArea.setValue(samples[0]);
        });


        Label resultCaption = new Label("Genereated poem:");
        resultCaption.addClassName("caption");

        resultTextArea = new TextArea();
        resultTextArea.addClassName("result-text");
        resultTextArea.setReadonly(true);
        resultTextArea.setRows(5);

        inputLayout.add(firstWordsField, seqLengthField, generateButton);

        Anchor dl4jLink = new Anchor("https://deeplearning4j.org/", "DEEPLEARNING4j");

        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidth("100%");
        footerLayout.setAlignItems(FlexLayout.Alignment.END);
        footerLayout.add(dl4jLink);

        add(title, description1, description2, inputLayout, resultTextArea, footerLayout);
        addClassName("main-layout");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        model = new LonnrotModel();
        try {
            model.loadModel();
        } catch (IOException e) {
            e.printStackTrace();
            model.trainModel();
        }
    }
}
