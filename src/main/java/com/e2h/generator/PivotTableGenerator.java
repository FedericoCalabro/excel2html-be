package com.e2h.generator;

import com.e2h.request.AggregationRow;
import com.e2h.request.GenerationRequest;
import com.e2h.util.DataManipulationUtility;
import com.webfirmframework.wffweb.tag.html.AbstractHtml;
import com.webfirmframework.wffweb.tag.html.Body;
import com.webfirmframework.wffweb.tag.html.Html;
import com.webfirmframework.wffweb.tag.html.attribute.Scope;
import com.webfirmframework.wffweb.tag.html.attribute.global.ClassAttribute;
import com.webfirmframework.wffweb.tag.html.metainfo.Head;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div;
import com.webfirmframework.wffweb.tag.html.tables.*;
import com.webfirmframework.wffweb.tag.htmlwff.NoTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PivotTableGenerator implements GeneratorStrategy{
    @Override
    public Html generate(GenerationRequest request) {
        Html document = HtmlGeneratorUtility.makeStarterRoot();
        Head head = HtmlGeneratorUtility.makeStarterHead(document, request);
        Body body = HtmlGeneratorUtility.makeStarterBody(document);
        document.appendChildren(head, body);

        Div tableListWrapper = buildTableListWrapper(body);
        body.appendChild(tableListWrapper);

        ArrayList<AbstractHtml> tables = buildTableList(tableListWrapper, request);
        tableListWrapper.appendChildren(tables);

        return document;
    }

    private Div buildTableListWrapper(AbstractHtml parent){
        ClassAttribute tableListWrapperClasses = new ClassAttribute("my-4 d-flex flex-col p-4");
        Div tableListWrapper = new Div(parent, tableListWrapperClasses);
        return tableListWrapper;
    }

    private ArrayList<AbstractHtml> buildTableList(AbstractHtml parent, GenerationRequest request){
        ArrayList<AbstractHtml> tables = new ArrayList<>();
        ArrayList<AggregationRow> aggrFuncs = request.getConfig().getAggregationRows();
        for (int row = 0; row < aggrFuncs.size(); row++){
            Div tableWrapper = buildTableWrapper(parent);
            HashMap<String, HashMap<String, Double>> mapToBuildTable = DataManipulationUtility.getGroupedAggrFuncMap(request, aggrFuncs.get(row));
            buildTable(tableWrapper, request, mapToBuildTable, row);
            tables.add(tableWrapper);
        }
        return tables;
    }

    private Div buildTableWrapper(AbstractHtml parent){
        ClassAttribute tableWrapperClasses = new ClassAttribute("table-responsive", "fluid", "border", "border-1");
        Div tableWrapper = new Div(parent, tableWrapperClasses);
        return tableWrapper;
    }

    private Table buildTable(AbstractHtml parent, GenerationRequest request, HashMap<String, HashMap<String, Double>> mapping, int row) {
        Table table = new Table(parent, new ClassAttribute("table"));
        THead tHead = buildTHead(table, request, mapping, row);
        TBody tBody = buildTableBody(table, request, mapping, row);
        table.appendChildren(tHead, tBody);
        return table;
    }

    private THead buildTHead(Table table, GenerationRequest request, HashMap<String, HashMap<String, Double>> mapping, int row) {
        THead tHead = new THead(table);

        Tr rowHeader = new Tr(tHead);
        tHead.appendChild(rowHeader);

        AggregationRow aggrRow = request.getConfig().getAggregationRows().get(row);
        String blocked = aggrRow.getBlockedCol();
        String target = aggrRow.getTargetCol();
        ArrayList<String> ops = aggrRow.getOp();

        ArrayList<String> columns = new ArrayList<>();
        columns.add("Blocked Column of " + blocked);
        for (int i = 0; i < ops.size(); i++)
            columns.add(ops.get(i).toUpperCase() + " of " + target);

        ArrayList<AbstractHtml> ths = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i);
            Th th = new Th(rowHeader, new Scope("col"), HtmlGeneratorUtility.getTextAndBgStyleForData(request, i));
            th.addInnerHtml(new NoTag(null, colName));
            ths.add(th);
        }
        rowHeader.appendChildren(ths);
        return tHead;
    }

    private TBody buildTableBody(Table table, GenerationRequest request, HashMap<String, HashMap<String, Double>> mapping, int row) {
        TBody tBody = new TBody(table);

        ArrayList<AbstractHtml> trs = new ArrayList<>();

        for (Map.Entry<String, HashMap<String, Double>> entry : mapping.entrySet()){
            Tr tr = buildTr(tBody, request, entry, row);
            trs.add(tr);
        }

        tBody.appendChildren(trs);
        return tBody;
    }

    public Tr buildTr(AbstractHtml parent, GenerationRequest request, Map.Entry<String, HashMap<String, Double>> entry, int row) {

        Tr tr = new Tr(parent);

        ArrayList<AbstractHtml> tds = new ArrayList<>();

        Td blockCol = new Td(tr);
        blockCol.addInnerHtml(new NoTag(null, entry.getKey()));
        tds.add(blockCol);

        ArrayList<String> columns = request.getConfig().getAggregationRows().get(row).getOp();

        for (int i = 0; i < columns.size(); i++) {
            String op = columns.get(i);
            Td td = new Td(tr);
            td.addInnerHtml(new NoTag(null, entry.getValue().get(op).toString()));
            tds.add(td);
        }

        tr.appendChildren(tds);
        return tr;
    }

}
