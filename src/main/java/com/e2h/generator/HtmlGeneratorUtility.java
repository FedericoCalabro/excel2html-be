package com.e2h.generator;

import com.e2h.request.GenerationRequest;
import com.e2h.util.DataManipulationUtility;
import com.webfirmframework.wffweb.tag.html.AbstractHtml;
import com.webfirmframework.wffweb.tag.html.Body;
import com.webfirmframework.wffweb.tag.html.Html;
import com.webfirmframework.wffweb.tag.html.TitleTag;
import com.webfirmframework.wffweb.tag.html.attribute.*;
import com.webfirmframework.wffweb.tag.html.attribute.global.ClassAttribute;
import com.webfirmframework.wffweb.tag.html.attribute.global.Style;
import com.webfirmframework.wffweb.tag.html.attribute.global.Style;
import com.webfirmframework.wffweb.tag.html.formatting.S;
import com.webfirmframework.wffweb.tag.html.html5.attribute.Content;
import com.webfirmframework.wffweb.tag.html.links.A;
import com.webfirmframework.wffweb.tag.html.links.Link;
import com.webfirmframework.wffweb.tag.html.metainfo.Head;
import com.webfirmframework.wffweb.tag.html.metainfo.Meta;
import com.webfirmframework.wffweb.tag.html.programming.Script;
import com.webfirmframework.wffweb.tag.htmlwff.NoTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlGeneratorUtility {

    private final static String BOOTSTRAP_CSS_HREF = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css";
    private final static String BOOTSTRAP_CSS_REL = "stylesheet";
    private final static String BOOTSTRAP_JS_SRC = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js";
    private final static String META_CHARSET = "utf-8";
    private final static String META_RESPONSIVE_NAME = "viewport";
    private final static String META_RESPONSIVE_CONTENT = "width=device-width, initial-scale=1";

    private final static String CHART_JS_SRC = "https://cdn.jsdelivr.net/npm/chart.js";
    private final static String HAMMER_JS_SRC = "https://cdn.jsdelivr.net/npm/hammerjs@2.0.8";
    private final static String CHARTZOOM_JS_SRC = "https://cdnjs.cloudflare.com/ajax/libs/chartjs-plugin-zoom/1.2.0/chartjs-plugin-zoom.min.js";

    public static void includeChartJS(Head head){
        Src src = new Src(CHART_JS_SRC);
        Script script = new Script(head, src);

        Src src2 = new Src(HAMMER_JS_SRC);
        Script script2 = new Script(head, src2);

        Src src3 = new Src(CHARTZOOM_JS_SRC);
        Script script3 = new Script(head, src3);

        head.appendChildren(script, script2, script3);
    }

    private static void includeMetaCharset(Head head) {
        Charset charset = new Charset(META_CHARSET);
        Meta meta = new Meta(head, charset);
        head.appendChild(meta);
    }

    private static void includeMetaResponsive(Head head) {
        Name name = new Name(META_RESPONSIVE_NAME);
        Content content = new Content(META_RESPONSIVE_CONTENT);
        Meta meta = new Meta(head, name, content);
        head.appendChild(meta);
    }

    private static void includeBootstrapCSS(Head head) {
        Href href = new Href(BOOTSTRAP_CSS_HREF);
        Rel rel = new Rel(BOOTSTRAP_CSS_REL);
        Link link = new Link(head, href, rel);
        head.appendChild(link);
    }

    private static void includeTitle(Head head, GenerationRequest request) {
        String titleConfig = request.getConfig().getName() != null ? request.getConfig().getName() : "DATA-VIEW";
        TitleTag title = new TitleTag(head);
        title.addInnerHtml(new NoTag(null, titleConfig));
        head.appendChild(title);
    }

    public static Head makeStarterHead(Html document, GenerationRequest request) {
        Head head = new Head(document);
        includeMetaCharset(head);
        includeMetaResponsive(head);
        includeBootstrapCSS(head);
        includeTitle(head, request);
        return head;
    }

    private static void includeBootstrapJS(Body body){
        Src src = new Src(BOOTSTRAP_JS_SRC);
        Script script = new Script(body, src);
        body.appendChild(script);
    }

    public static void finalizeDocument(Html document) {
        document.setPrependDocType(true);
        Body body = (Body) document.getChildAt(2);
        includeBootstrapJS(body);
    }

    public static Body makeStarterBody(Html document){
        ClassAttribute bodyAttributes = new ClassAttribute("w-100", "h-100", "d-flex", "flex-column");
        Body body = new Body(document, bodyAttributes);
        return body;
    }

    public static Html makeStarterRoot(){
        ClassAttribute htmlAttributes = new ClassAttribute("w-100", "h-100");
        Html document = new Html(null, htmlAttributes);
        return  document;
    }

    public static AbstractHtml makeDataTag(AbstractHtml parent, GenerationRequest request, int row, String col){
        String colValueString = DataManipulationUtility.extractColValue(request, request.getData().get(row), col);

        AbstractHtml TAG = new NoTag(parent);
        List<AbstractHtml> insideTag = new ArrayList<>();

        String regex = "((http|https)://)(www.)?"
        + "[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]"
        + "{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(colValueString);

        List<String> splitByRegex = Arrays.stream(colValueString.split(regex)).toList();

        int i = 0;
        while(matcher.find()){
            if(splitByRegex.size() > i){
                AbstractHtml tag = new NoTag(TAG);
                tag.addInnerHtml(new NoTag(null, splitByRegex.get(i)));
                insideTag.add(tag);
            }

            A tag2 = new A(TAG);
            String href = matcher.group(0);
            tag2.addAttributes(new Href(href));
            tag2.addInnerHtml(new NoTag(null, DataManipulationUtility.getColNameFromRowValue(request, row, href)));
            insideTag.add(tag2);

            i++;
        }
        if(i == 0){
            AbstractHtml tag = new NoTag(TAG);
            tag.addInnerHtml(new NoTag(null, colValueString));
            insideTag.add(tag);
        }else if(i < splitByRegex.size()){
            for (int j = i; j < splitByRegex.size(); j++) {
                AbstractHtml tag = new NoTag(TAG);
                tag.addInnerHtml(new NoTag(null, splitByRegex.get(i)));
                insideTag.add(tag);
            }
        }

        TAG.appendChildren(insideTag);
        return TAG;

    }

    public static String getBGStyleFromColPos(GenerationRequest request, int pos) {
        ArrayList<String> themeColor = request.getConfig().getThemeColor();
        String bg = "#ffffff";
        if(themeColor.size() > pos) {
            String temp = themeColor.get(pos);
            if (temp != null) bg = temp;
        }
        return "background-color:"+bg+";";

    }
    public static String getTextStyleFromColPos(GenerationRequest request, int pos) {
        ArrayList<String> textColor = request.getConfig().getTextColor();
        String c = "#000000";
        if(textColor.size() > pos){
            String temp = textColor.get(pos);
            if(temp != null) c = temp;
        }
        return "color:"+c+";";
    }
    public static Style getTextAndBgStyleForData(GenerationRequest request, int pos){
        return new Style(getTextStyleFromColPos(request, pos) + getBGStyleFromColPos(request, pos));
    }

}
