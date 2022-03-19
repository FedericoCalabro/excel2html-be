package com.e2h.generator;

import com.e2h.request.CardHeaderColorCriteria;
import com.e2h.request.CardHeaderConfig;
import com.e2h.request.GenerationRequest;
import com.e2h.util.DataManipulationUtility;
import com.webfirmframework.wffweb.tag.html.AbstractHtml;
import com.webfirmframework.wffweb.tag.html.Body;
import com.webfirmframework.wffweb.tag.html.H5;
import com.webfirmframework.wffweb.tag.html.Html;
import com.webfirmframework.wffweb.tag.html.attribute.ColorAttribute;
import com.webfirmframework.wffweb.tag.html.attribute.Href;
import com.webfirmframework.wffweb.tag.html.attribute.global.ClassAttribute;
import com.webfirmframework.wffweb.tag.html.attribute.global.Id;
import com.webfirmframework.wffweb.tag.html.attribute.global.Style;
import com.webfirmframework.wffweb.tag.html.formatting.B;
import com.webfirmframework.wffweb.tag.html.formatting.S;
import com.webfirmframework.wffweb.tag.html.links.A;
import com.webfirmframework.wffweb.tag.html.lists.Li;
import com.webfirmframework.wffweb.tag.html.lists.Ol;
import com.webfirmframework.wffweb.tag.html.metainfo.Head;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.StyleTag;
import com.webfirmframework.wffweb.tag.htmlwff.NoTag;
import org.aspectj.weaver.ast.Not;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CardGenerator implements GeneratorStrategy {

    @Override
    public Html generate(GenerationRequest request) {

        Html document = HtmlGeneratorUtility.makeStarterRoot();

        StyleTag styleTag = new StyleTag(document);
        styleTag.addInnerHtml(new NoTag(styleTag, ".card:hover{background-color:#f2f2f2;}"));

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
                "!h-100", "!w-100", "p-4",
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

    private String getHeaderColor(GenerationRequest request, int row){
        String leftColor = "background-color:";
        String rightColor = "ffffff;";

        Map<String, Object> rowData = request.getData().get(row);
        List<CardHeaderColorCriteria> criterias = request.getConfig().getCardHeaderConfig().getColorsCriteria();

        boolean found = false;
        for (int i = 0; i <criterias.size() ; i++) {
            CardHeaderColorCriteria criteria = criterias.get(i);
            if(DataManipulationUtility.matchesSingleCriteria(request, criteria, rowData)){
                rightColor = criteria.getColor() + ";";
                found = true;
                break;
            }
        }
        return found ? leftColor + rightColor : HtmlGeneratorUtility.getBGStyleFromColPos(request, 0);
    }

    private Div buildCard(AbstractHtml parent, GenerationRequest request, int row) {

        ArrayList<String> columns = request.getConfig().getColumns();
        CardHeaderConfig headerConfig = request.getConfig().getCardHeaderConfig();

        String titleColumn = columns.get(0);

        Div cardBody = new Div(parent, new ClassAttribute("p-4 card" )); //flexgrow per far crescere la card

        ClassAttribute cardHeaderAttributes = new ClassAttribute("card-header", "d-flex", "align-items-center", "justify-content-center");
        Style cardHeaderStyle = new Style("height: 80px !important;" + getHeaderColor(request, row) + HtmlGeneratorUtility.getTextStyleFromColPos(request, 0));
        Div cardHeader = new Div(cardBody, cardHeaderAttributes, cardHeaderStyle);

        ClassAttribute titleAttributes = new ClassAttribute( "font-weight-normal", "center");
        H5 title = new H5(cardHeader, titleAttributes);
        B bold = new B(title);
        AbstractHtml insideBold;

        if(request.getConfig().getCardHeaderConfig().isLink()){
            insideBold = new A(bold);
            String href = DataManipulationUtility.extractColValue(request, request.getData().get(row), headerConfig.getLinkValueColumn());
            insideBold.addAttributes(new Href(href));
            insideBold.addInnerHtml(new NoTag(null, DataManipulationUtility.extractColValue(request, request.getData().get(row), headerConfig.getLinkDisplayColumn())));
        }else{
            insideBold = HtmlGeneratorUtility.makeDataTag(bold, request, row, titleColumn);
        }

        bold.addInnerHtml(insideBold);
        title.appendChild(bold);
        cardHeader.appendChildren(title);

        ClassAttribute orderedListAttributes = new ClassAttribute("list-group", "list-group-flush", "order-1");
        Ol orderedList = new Ol(cardBody, orderedListAttributes);

        ArrayList<AbstractHtml> lis = new ArrayList<>();
        ClassAttribute liAttribute = new ClassAttribute("list-group-item");
        for (int i = 1; i < columns.size(); i++) {
            String colName = columns.get(i);
            Li li = new Li(orderedList, liAttribute, HtmlGeneratorUtility.getTextAndBgStyleForData(request, i));
            AbstractHtml insideLi = null;
            if(DataManipulationUtility.getColComposition(request, colName).size() == 1){
                insideLi = new NoTag(null, colName+": ");
            }
            AbstractHtml valueLi = HtmlGeneratorUtility.makeDataTag(li, request, row, colName);

            if (insideLi == null) {
                li.addInnerHtml(valueLi);
            } else {
                li.addInnerHtmls(insideLi, valueLi);
            }
            lis.add(li);
        }
        orderedList.appendChildren(lis);
        return cardBody;
    }
}

