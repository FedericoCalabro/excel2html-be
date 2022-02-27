package com.e2h.generator;

import com.e2h.request.GenerationRequest;
import com.e2h.util.DataManipulationUtility;
import com.webfirmframework.wffweb.tag.html.Body;
import com.webfirmframework.wffweb.tag.html.Html;
import com.webfirmframework.wffweb.tag.html.metainfo.Head;

public class PivotTableGenerator implements GeneratorStrategy{
    @Override
    public Html generate(GenerationRequest request) {
        Html document = HtmlGeneratorUtility.makeStarterRoot();
        Head head = HtmlGeneratorUtility.makeStarterHead(document, request);
        Body body = HtmlGeneratorUtility.makeStarterBody(document);
        document.appendChildren(head, body);

//        Div cardWrapper = buildCardWrapper(body);
//        body.appendChild(cardWrapper);
//
//        ArrayList<AbstractHtml> cards = buildCardList(cardWrapper, request);
//        cardWrapper.appendChildren(cards);

        System.out.println(
                DataManipulationUtility.getGroupedAggrFuncMap(request, request.getConfig().getAggregationRows().get(0))
        );

        return document;
    }

}
