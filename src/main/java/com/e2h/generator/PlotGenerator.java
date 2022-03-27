package com.e2h.generator;

import com.e2h.request.GenerationRequest;
import com.e2h.request.PlotConfig;
import com.e2h.util.DataManipulationUtility;
import com.webfirmframework.wffweb.tag.html.Body;
import com.webfirmframework.wffweb.tag.html.Html;
import com.webfirmframework.wffweb.tag.html.attribute.global.Id;
import com.webfirmframework.wffweb.tag.html.html5.images.Canvas;
import com.webfirmframework.wffweb.tag.html.metainfo.Head;
import com.webfirmframework.wffweb.tag.html.programming.Script;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.StyleTag;
import com.webfirmframework.wffweb.tag.htmlwff.NoTag;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


public class PlotGenerator implements GeneratorStrategy {
    @Override
    public Html generate(GenerationRequest request) {
        Html document = HtmlGeneratorUtility.makeStarterRoot();

        StyleTag styleTag = new StyleTag(document);

        Head head = HtmlGeneratorUtility.makeStarterHead(document, request);
        buildHeadScriptConfig(head, request);

        Body body = HtmlGeneratorUtility.makeStarterBody(document);
        document.appendChildren(styleTag, head, body);

        Div chartWrapper = new Div(body);
        Canvas chart = new Canvas(chartWrapper, new Id("myChart"));

        body.appendChild(chartWrapper);
        HtmlGeneratorUtility.includeChartJS(head);
        addBodyScript(body);

        return document;
    }

    private void addBodyScript(Body body) {
        Script script = new Script(body);
        NoTag scriptSelf = new NoTag(script, "const myChart = new Chart(" +
                "document.getElementById('myChart')," +
                "config" +
                ");");
        script.addInnerHtml(scriptSelf);
        body.appendChild(script);
    }

    private void buildHeadScriptConfig(Head head, GenerationRequest request) {

        Script script = new Script(head);
        StringBuilder config = new StringBuilder();

        PlotConfig plotConfig = request.getConfig().getPlotConfig();
        Map<String, ArrayList<Double>> groupedRows = DataManipulationUtility.getMapFromBlockedTarget(request, plotConfig.getBlockedCol(), plotConfig.getTargetCol());

        config.append("const data = {\n");
        config.append("datasets: [" + buildDataSetObjsAndLabels(plotConfig, groupedRows) + "],\n");
        config.append("labels : [" + buildLabels(plotConfig, groupedRows) + "]};\n");
        config.append("const config = {" + buildConfig(request, plotConfig) + "};\n");


        NoTag scriptSelf = new NoTag(script, config.toString());
        script.addInnerHtml(scriptSelf);
        head.appendChild(script);
    }

    private String buildLabels(PlotConfig plotConfig, Map<String, ArrayList<Double>> map) {
        String type = plotConfig.getType();
        StringBuilder returnValue = new StringBuilder();

        if (type.equals("CACTUS")) {
            int maxLen = -1;
            for (Map.Entry<String, ArrayList<Double>> entry : map.entrySet()) {
                ArrayList<Double> valueList = entry.getValue();
                if (valueList.size() > maxLen) {
                    maxLen = valueList.size();
                }
            }
            for (int i = 0; i < maxLen; i++)
                returnValue.append("'',");

        } else if (type.equals("SCATTER")) {
            String labelX = plotConfig.getLabelX();
            ArrayList<Double> valueList = map.get(labelX);
            if (valueList == null) throw new RuntimeException("Label X: " + labelX + " not found");
            valueList.stream().forEach(d -> returnValue.append(d + ","));
        }

        return returnValue.toString();
    }

    private String buildDataSetObjsAndLabels(PlotConfig plotConfig, Map<String, ArrayList<Double>> map) {
        StringBuilder returnValue = new StringBuilder();

        int index = 0;
        String type = plotConfig.getType();
        if (type.equals("CACTUS")) {
            for (Map.Entry<String, ArrayList<Double>> entry : map.entrySet()) {
                returnValue.append("\n{");
                returnValue.append("data: [");
                entry.getValue().stream().sorted().forEach(d -> returnValue.append(d + ","));
                returnValue.append("],");
                returnValue.append(getDSColors(plotConfig, index));
                returnValue.append(getPointStyle(plotConfig, index));
                returnValue.append("label: '" + entry.getKey() + "',");
                returnValue.append("},\n");
                index++;
            }
        } else if (type.equals("SCATTER")) {
            String labelY = plotConfig.getLabelY();
            String labelX = plotConfig.getLabelX();
            ArrayList<Double> valueY = map.get(labelY);
            if (valueY == null) throw new RuntimeException("Label Y: " + labelY + " not found");
            returnValue.append("\n{");
            returnValue.append("data: [");
            valueY.stream().forEach(d -> returnValue.append(d + ","));
            returnValue.append("],");
            returnValue.append(getDSColors(plotConfig, index));
            returnValue.append(getPointStyle(plotConfig, index));
            returnValue.append("label: '" + plotConfig.getLabelX() + " - " + labelY + "',");
            returnValue.append("},\n");
            Double maxX = Collections.max(map.get(labelX));
            Double maxY = Collections.max(valueY);
            returnValue.append(
                    "\n{" +
                            "data:[" +
                            "{x: 0, y: 0}, {x: " + maxX + ", y: " + maxY + "}" +
                            "]," +
                            "type: 'line'," +
                            "radius: 0," +
                            "backgroundColor: 'rgba(255, 0, 0, 0.6)'," +
                            "label: 'label to remove'" +
                            "}\n");
        }

        return returnValue.toString();
    }

    private String getDSColors(PlotConfig plotConfig, int index) {
        StringBuilder returnValue = new StringBuilder();
        ArrayList<String> colors = plotConfig.getColors();
        Random random = new Random();
        Color color = index >= colors.size() ? new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)) : Color.decode(colors.get(index));
        String colorString = color.getRed() + ", " + color.getGreen() + ", " + color.getBlue();
        String borderColor = "borderColor: 'rgba(" + colorString + ", 1.0)'";
        String backgroundColor = "backgroundColor: 'rgba(" + colorString + ", 0.6)'";
        returnValue.append(borderColor + "," + backgroundColor + ",");
        return returnValue.toString();
    }

    private String getPointStyle(PlotConfig plotConfig, int index) {
        ArrayList<String> styles = plotConfig.getStyles();
        String style = index >= styles.size() ? "circle" : styles.get(index);
        return "pointStyle: '" + style + "',radius: 4.5,";
    }

    private String buildConfig(GenerationRequest request, PlotConfig plotConfig) {
        String type = plotConfig.getType();
        StringBuilder returnValue = new StringBuilder();

        if (type.equals("CACTUS")) {
            returnValue.append("type: 'line',");
        } else if (type.equals("SCATTER")) {
            returnValue.append("type: 'scatter',");
        }

        returnValue.append("data: data,");
        returnValue.append("options: {" + buildOptions(request, plotConfig) + "},");

        return returnValue.toString();
    }

    private String buildOptions(GenerationRequest request, PlotConfig plotConfig) {
        String type = plotConfig.getType();
        StringBuilder returnValue = new StringBuilder();

        String zoom = plotConfig.isZoomable() ? "zoom: " + buildZoomConfig() + "," : "";

        returnValue.append(
                "plugins: {" +
                        "legend: {" +
                        "labels: {" +
                        "filter: function(item, chart) {" +
                        "return !item.text.includes('label to remove');}," +
                        "font:{size: 14}" +
                        "}" +
                        "}," +
                        zoom +
                        "title: {" +
                        "font:{size:20}," +
                        "align: 'center'," +
                        "display: true," +
                        "text: " + "'" + type.toUpperCase() + " PLOT'," +
                        "padding: { top: 10,bottom: 20}" +
                        "}" +
                        "},");

        returnValue.append("scales: {");

        returnValue.append(
                "x: {" +
                        "title: {" +
                        "display: true," +
                        "text: '" + plotConfig.getLabelXValue() + "'," +
                        "font:{size:15}" +
                        "}" +
                        "},");

        returnValue.append(
                "y: {" +
                        "title: {" +
                        "display: true," +
                        "text: '" + plotConfig.getLabelYValue() + "'," +
                        "font:{size:15}" +
                        "}" +
                        "},");

        returnValue.append("},");

        return returnValue.toString();
    }

    private String buildZoomConfig() {
        return "{" +
                "  pan: {" +
                "    enabled: true," +
                "    mode: 'xy'," +
                "  }," +
                "  zoom: {" +
                "    wheel: {" +
                "      enabled: true," +
                "    }," +
                "    pinch: {" +
                "      enabled: true" +
                "    }," +
                "    mode: 'xy'," +
                "    onZoomComplete({chart}) {" +
                "      chart.update('none');" +
                "    }" +
                "  }" +
                "}";
    }
}
