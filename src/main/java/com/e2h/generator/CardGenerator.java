package com.e2h.generator;

import com.e2h.request.GenerationRequest;
import com.webfirmframework.wffweb.tag.html.AbstractHtml;
import com.webfirmframework.wffweb.tag.html.Body;
import com.webfirmframework.wffweb.tag.html.H5;
import com.webfirmframework.wffweb.tag.html.Html;
import com.webfirmframework.wffweb.tag.html.attribute.global.ClassAttribute;
import com.webfirmframework.wffweb.tag.html.attribute.global.Id;
import com.webfirmframework.wffweb.tag.html.attribute.global.Style;
import com.webfirmframework.wffweb.tag.html.formatting.B;
import com.webfirmframework.wffweb.tag.html.formatting.S;
import com.webfirmframework.wffweb.tag.html.lists.Li;
import com.webfirmframework.wffweb.tag.html.lists.Ol;
import com.webfirmframework.wffweb.tag.html.metainfo.Head;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div;
import com.webfirmframework.wffweb.tag.htmlwff.NoTag;

import java.util.ArrayList;
import java.util.Map;

public class CardGenerator implements GeneratorStrategy {

    @Override
    public Html generate(GenerationRequest request) {

        Html document = HtmlGeneratorUtility.makeStarterRoot();
        Head head = HtmlGeneratorUtility.makeStarterHead(document, request);
        Body body = HtmlGeneratorUtility.makeStarterBody(document);
        document.appendChildren(head, body);

        Div cardWrapper = buildCardWrapper(body);
        body.appendChild(cardWrapper);

        ArrayList<AbstractHtml> cards = buildCardList(cardWrapper, request);
        cardWrapper.appendChildren(cards);

        return document;
    }

    private Div buildCardWrapper(Body body) {
        ClassAttribute cardWrapperAttributes = new ClassAttribute(
                "h-100", "w-100", "p-4",
                "row","row-cols-1","row-cols-xs-1","row-cols-sm-1",
                "row-cols-md-2","row-cols-lg-3","row-cols-xl-4");
        Div cardWrapper = new Div(body, new Id("cardWrapper"), cardWrapperAttributes);
        return cardWrapper;
    }

    private ArrayList<AbstractHtml> buildCardList(AbstractHtml parent, GenerationRequest request) {
        ArrayList<AbstractHtml> array = new ArrayList<>();
        ArrayList<Map<String, Object>> data = request.getData();
        for (int row = 0; row < data.size(); row++) {
            Div card = buildCard(parent, request, row);
            array.add(card);
        }
        return array;
    }

    private Div buildCard(AbstractHtml parent, GenerationRequest request, int row) {

        ArrayList<String> columns = request.getConfig().getColumns();

        String titleColumn = columns.get(0);

        Div cardBody = new Div(parent, new ClassAttribute("card-body"));

        ClassAttribute cardHeaderAttributes = new ClassAttribute("card-header", "d-flex", "align-items-center", "justify-content-center");
        Style cardHeaderStyle = new Style("height: 80px; background-color: #99ffc5;");
        Div cardHeader = new Div(cardBody, cardHeaderAttributes, cardHeaderStyle);

        ClassAttribute titleAttributes = new ClassAttribute("card-title", "font-weight-normal", "flex-grow-1", "center");
        H5 title = new H5(cardHeader, titleAttributes);
        B bold = new B(title);
        AbstractHtml insideBold = HtmlGeneratorUtility.makeDataTag(bold, request, row, titleColumn);

        bold.addInnerHtml(insideBold);
        title.appendChild(bold);
        cardHeader.appendChildren(title);

        ClassAttribute orderedListAttributes = new ClassAttribute("list-group", "list-group-flush", "order-1");
        Ol orderedList = new Ol(cardBody, orderedListAttributes);

        ArrayList<AbstractHtml> lis = new ArrayList<>();
        ClassAttribute liAttribute = new ClassAttribute("list-group-item");
        for (int i = 1; i < columns.size(); i++) {
            String colName = columns.get(i);
            Li li = new Li(orderedList, liAttribute);
            AbstractHtml insideLi = new NoTag(null, colName+": ");
            AbstractHtml valueLi = HtmlGeneratorUtility.makeDataTag(li, request, row, colName);
            li.addInnerHtmls(insideLi, valueLi);
            lis.add(li);
        }
        orderedList.appendChildren(lis);
        return cardBody;
    }
}

