package com.e2h.generator;

import com.e2h.request.GenerationRequest;
import com.webfirmframework.wffweb.tag.html.Html;

public interface GeneratorStrategy {
    Html generate(GenerationRequest request);
}
