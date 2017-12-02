package org.vaadin.lonnrot;

import com.vaadin.router.PageTitle;
import com.vaadin.router.Route;
import com.vaadin.ui.common.HtmlImport;
import com.vaadin.ui.html.Div;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Poem Generator")
@HtmlImport("frontend://styles.html")
public class PoemGeneratorView extends Div {



}
