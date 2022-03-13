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
import java.util.Map;
import java.util.Random;


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

        config.append("\nconst labels = [" + buildLabels(plotConfig, groupedRows) + "];");
        config.append("\nconst data = {");
        config.append("\nlabels : labels,");
        config.append("\ndatasets: [" + buildDataSetObjs(plotConfig, groupedRows) + "]\n};");
        config.append("\nconst config = {\n" + buildConfig(request, plotConfig) + "\n};");


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

    private String buildDataSetObjs(PlotConfig plotConfig, Map<String, ArrayList<Double>> map) {
        StringBuilder returnValue = new StringBuilder();

        int index = 0;
        String type = plotConfig.getType();
        if (type.equals("CACTUS")) {
            for (Map.Entry<String, ArrayList<Double>> entry : map.entrySet()) {
                returnValue.append("{\n");
                returnValue.append("data: [");
                entry.getValue().stream().sorted().forEach(d -> returnValue.append(d + ","));
                returnValue.append("],\n");
                returnValue.append(getDSColors(plotConfig, index));
                returnValue.append(getPointStyle(plotConfig, index));
                returnValue.append("\nlabel: '" + entry.getKey() + "',");
                returnValue.append("\n},");
                index++;
            }
        } else if (type.equals("SCATTER")) {
            String labelY = plotConfig.getLabelY();
            ArrayList<Double> valueY = map.get(labelY);
            if (valueY == null) throw new RuntimeException("Label Y: " + labelY + " not found");
            returnValue.append("{\n");
            returnValue.append("data: [");
            valueY.stream().sorted().forEach(d -> returnValue.append(d + ","));
            returnValue.append("],");
            returnValue.append(getDSColors(plotConfig, index));
            returnValue.append(getPointStyle(plotConfig, index));
            returnValue.append("label: '" + plotConfig.getLabelX() + " - " + labelY + "',");
            returnValue.append("}\n");
        }

        return returnValue.toString();
    }

    private String getDSColors(PlotConfig plotConfig, int index){
        StringBuilder returnValue = new StringBuilder();
        ArrayList<String> colors = plotConfig.getColors();
        Random random = new Random();
        Color color = index >= colors.size() ? new Color(random.nextInt(256),random.nextInt(256),random.nextInt(256)) : Color.decode(colors.get(index));
        String colorString = color.getRed() + ", " + color.getGreen() + ", " + color.getBlue();
        String borderColor = "borderColor: 'rgba(" + colorString + ", 1.0)'";
        String backgroundColor = "backgroundColor: 'rgba(" + colorString + ", 0.6)'";
        returnValue.append(borderColor + ",\n" + backgroundColor + ",\n");
        return returnValue.toString();
    }

    private String getPointStyle(PlotConfig plotConfig, int index){
        ArrayList<String> styles = plotConfig.getStyles();
        String style = index >= styles.size() ? "circle" : styles.get(index);
        return "pointStyle: '" + style + "',";
    }

    private String buildConfig(GenerationRequest request, PlotConfig plotConfig) {
        String type = plotConfig.getType();
        StringBuilder returnValue = new StringBuilder();

        if (type.equals("CACTUS")) {
            returnValue.append("\ntype: 'line',");
        } else if (type.equals("SCATTER")) {
            returnValue.append("\ntype: 'scatter',");
        }

        returnValue.append("\ndata: data,");
        returnValue.append("\noptions: {" + buildOptions(request, plotConfig) + "},");

        return returnValue.toString();
    }

    private String buildOptions(GenerationRequest request, PlotConfig plotConfig) {
        String type = plotConfig.getType();
        StringBuilder returnValue = new StringBuilder();

        returnValue.append(
                "plugins: {\n" +
                    "zoom: "+ buildZoomConfig() +",\n" +
                    "title: {\n" +
                        "align: 'center',\n" +
                        "display: true,\n" +
                        "text: " + "'" + type.toUpperCase() + " PLOT',\n" +
                        "padding: {\n top: 10,\nbottom: 20}" +
                    "\n}" +
                "},\n");

        returnValue.append("scales: {\n");

        returnValue.append(
                            "x: {\n" +
                                "title: {\n" +
                                    "display: true,\n" +
                                    "text: '"+ plotConfig.getLabelXValue()+"'\n" +
                                "}\n" +
                            "},\n");

        returnValue.append(
                            "y: {\n" +
                                "title: {\n" +
                                    "display: true,\n" +
                                    "text: '"+ plotConfig.getLabelYValue()+"'\n" +
                                "}\n" +
                            "},\n");

        returnValue.append("},\n");

        return returnValue.toString();
    }

    private String buildZoomConfig(){
        return "{\n" +
                "  pan: {\n" +
                "    enabled: true,\n" +
                "    mode: 'xy',\n" +
                "  },\n" +
                "  zoom: {\n" +
                "    wheel: {\n" +
                "      enabled: true,\n" +
                "    },\n" +
                "    pinch: {\n" +
                "      enabled: true\n" +
                "    },\n" +
                "    mode: 'xy',\n" +
                "    onZoomComplete({chart}) {\n" +
                "      chart.update('none');\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
    }
}
