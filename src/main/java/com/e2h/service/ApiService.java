package com.e2h.service;

import com.e2h.entity.GenerationEntity;
import com.e2h.generator.CardGenerator;
import com.e2h.generator.HtmlGeneratorUtility;
import com.e2h.generator.GeneratorStrategy;
import com.e2h.generator.TableGenerator;
import com.e2h.repository.GenerationRepository;
import com.e2h.request.GenerationRequest;
import com.e2h.util.DataManipulationUtility;
import com.webfirmframework.wffweb.tag.html.Html;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Service
public class ApiService {

    @Autowired
    GenerationRepository repository;

    GeneratorStrategy generator;

    public GenerationEntity preview (GenerationRequest request){
        try {
            String view = request.getConfig().getView();
            var data= request.getData();
            switch (view) {
                case "TABLE": generator = new TableGenerator(); break;
                case "CARD":  generator = new CardGenerator(); break;
                default: throw new RuntimeException("Bad view-mode configured!");
            }

            ArrayList<Map<String, Object>> rows = request.getData();
            rows.removeIf(row -> !DataManipulationUtility.matchCriterias(row, request));

            Html document = generator.generate(request);
            HtmlGeneratorUtility.finalizeDocument(document);

            String plainHTML = document.toHtmlString();
            GenerationEntity entity = new GenerationEntity();
            entity.setHtml(plainHTML);
            return repository.save(entity);

        }catch (NullPointerException e){
            throw new RuntimeException("Either data or config is not valid!");
        }
    }

    public GenerationEntity get(String id){
        return repository.getById(id);
    }

    public Resource download(String id) {
        GenerationEntity entity = repository.getById(id);
        return new ByteArrayResource(entity.getHtml().getBytes(StandardCharsets.UTF_8));
    }
}
