package com.e2h.generator;

import com.e2h.request.GenerationRequest;
import com.e2h.util.DataManipulationUtility;
import com.webfirmframework.wffweb.tag.html.AbstractHtml;
import com.webfirmframework.wffweb.tag.html.Body;
import com.webfirmframework.wffweb.tag.html.Html;
import com.webfirmframework.wffweb.tag.html.TitleTag;
import com.webfirmframework.wffweb.tag.html.attribute.*;
import com.webfirmframework.wffweb.tag.html.attribute.global.ClassAttribute;
import com.webfirmframework.wffweb.tag.html.html5.attribute.Content;
import com.webfirmframework.wffweb.tag.html.links.A;
import com.webfirmframework.wffweb.tag.html.links.Link;
import com.webfirmframework.wffweb.tag.html.metainfo.Head;
import com.webfirmframework.wffweb.tag.html.metainfo.Meta;
import com.webfirmframework.wffweb.tag.html.programming.Script;
import com.webfirmframework.wffweb.tag.htmlwff.NoTag;

import java.util.Map;

public class HtmlGeneratorUtility {

    private final static String BOOTSTRAP_CSS_HREF = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css";
    private final static String BOOTSTRAP_CSS_REL = "stylesheet";
    private final static String BOOTSTRAP_JS_SRC = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js";
    private final static String META_CHARSET = "utf-8";
    private final static String META_RESPONSIVE_NAME = "viewport";
    private final static String META_RESPONSIVE_CONTENT = "width=device-width, initial-scale=1";

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
        Body body = (Body) document.getChildAt(1);
        includeBootstrapJS(body);
    }

    public static Body makeStarterBody(Html document){
        ClassAttribute bodyAttributes = new ClassAttribute(
                "w-100", "h-100", "d-flex",
                "flex-column", "align-content-center", "justify-content-center");
        Body body = new Body(document, bodyAttributes);
        return body;
    }

    public static Html makeStarterRoot(){
        ClassAttribute htmlAttributes = new ClassAttribute("w-100", "h-100");
        Html document = new Html(null, htmlAttributes);
        return  document;
    }

    public static AbstractHtml makeDataTag(AbstractHtml parent, GenerationRequest request, int row, String col){
        Map<String, Object> rowData = request.getData().get(row);

        Object colValueObject = rowData.get(col);
        String colValueString = colValueObject == null ? "#" : colValueObject.toString();

        AbstractHtml tag = new NoTag(parent);

        boolean isLink = DataManipulationUtility.isLink(request, col);
        if(isLink){
            tag = new A(parent);
            tag.addAttributes(new Href(colValueString));
            String linkName = DataManipulationUtility.getLinkName(request, col);
            tag.addInnerHtml(new NoTag(null, linkName));
        }else{
            tag.addInnerHtml(new NoTag(null, colValueString));
        }

        return tag;
    }

}
