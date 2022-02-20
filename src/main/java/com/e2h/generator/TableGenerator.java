package com.e2h.generator;

import com.e2h.request.GenerationRequest;
import com.webfirmframework.wffweb.tag.html.AbstractHtml;
import com.webfirmframework.wffweb.tag.html.Body;
import com.webfirmframework.wffweb.tag.html.Html;
import com.webfirmframework.wffweb.tag.html.attribute.Scope;
import com.webfirmframework.wffweb.tag.html.attribute.global.ClassAttribute;
import com.webfirmframework.wffweb.tag.html.attribute.global.Id;
import com.webfirmframework.wffweb.tag.html.attribute.global.Style;
import com.webfirmframework.wffweb.tag.html.metainfo.Head;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div;
import com.webfirmframework.wffweb.tag.html.tables.*;
import com.webfirmframework.wffweb.tag.htmlwff.NoTag;

import java.util.ArrayList;
import java.util.Map;

public class TableGenerator implements GeneratorStrategy {

    @Override
    public Html generate(GenerationRequest request) {

        Html document = HtmlGeneratorUtility.makeStarterRoot();
        Head head = HtmlGeneratorUtility.makeStarterHead(document, request);
        Body body = HtmlGeneratorUtility.makeStarterBody(document);
        document.appendChildren(head, body);

        Div tableWrapper = buildTableWrapper(body);
        body.appendChild(tableWrapper);

        Table table = buildTable(tableWrapper, request);
        tableWrapper.appendChild(table);

        return document;
    }

    private Div buildTableWrapper(Body body){
        ClassAttribute tableWrapperClasses = new ClassAttribute("table-responsive", "fluid", "border", "border-1", "p-4");
        Style maxHeight = new Style("max-height: 100vh;");
        Div tableWrapper = new Div(body, new Id("tableWrapper"), tableWrapperClasses, maxHeight);
        return tableWrapper;
    }

    private Table buildTable(AbstractHtml parent, GenerationRequest request) {
        Table table = new Table(parent, new ClassAttribute("table"));
        THead tHead = buildTHead(table, request);
        TBody tBody = buildTableBody(table, request);
        table.appendChildren(tHead, tBody);
        return table;
    }

    private THead buildTHead(Table table, GenerationRequest request) {
        THead tHead = new THead(table);

        Tr rowHeader = new Tr(tHead);
        tHead.appendChild(rowHeader);

        if(request.getConfig().isAddRowCounter()){
            Scope scope = new Scope("col");
            Th rowId = new Th(rowHeader, scope);
            rowId.addInnerHtml(new NoTag(null, "ID"));
            rowHeader.appendChild(rowId);
        }

        ArrayList<String> columns = request.getConfig().getColumns();

        ArrayList<AbstractHtml> ths = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i);
            Th th = new Th(rowHeader, new Scope("col"));
            th.addInnerHtml(new NoTag(null, colName));
            ths.add(th);
        }
        rowHeader.appendChildren(ths);
        return tHead;
    }

    private TBody buildTableBody(Table table, GenerationRequest request) {
        TBody tBody = new TBody(table);

        ArrayList<Map<String, Object>> data = request.getData();

        ArrayList<AbstractHtml> trs = new ArrayList<>();
        for (int row = 0; row < data.size(); row++) {
            Tr tr = buildTr(tBody, request, row);
            trs.add(tr);
        }
        tBody.appendChildren(trs);
        return tBody;
    }

    public Tr buildTr(AbstractHtml parent, GenerationRequest request, int row) {
        Tr tr = new Tr(parent);

        if(request.getConfig().isAddRowCounter()){
            Scope scope = new Scope("row");
            Th rowId = new Th(tr, scope);
            rowId.addInnerHtml(new NoTag(null, String.valueOf(row)));
            tr.appendChild(rowId);
        }

        ArrayList<AbstractHtml> tds = new ArrayList<>();
        ArrayList<String> columns = request.getConfig().getColumns();

        for (int i = 0; i < columns.size(); i++) {
            Td td = new Td(tr);
            String colName = columns.get(i);
            td.addInnerHtml(HtmlGeneratorUtility.makeDataTag(td, request, row, colName));
            tds.add(td);
        }

        tr.appendChildren(tds);
        return tr;
    }

}
