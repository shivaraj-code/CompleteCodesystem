package com.io.codesystem;

import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurationContext;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomLuceneAnalysisConfigurer implements LuceneAnalysisConfigurer {

    @Override
    public void configure(LuceneAnalysisConfigurationContext context) {

        context.analyzer("edgengram").custom()
                .tokenizer("standard")
                .tokenFilter("lowercase")
                .tokenFilter("edgeNGram")
                .param("minGramSize", "3")
                .param("maxGramSize", "15");
        
        context.analyzer("ndcngram").custom()
        .tokenizer("standard")
        .tokenFilter("lowercase")
        .tokenFilter("edgeNGram")
        .param("minGramSize", "4")
        .param("maxGramSize", "15");
       
        context.analyzer("stdquery").custom()
                .tokenizer("standard")
                .tokenFilter("lowercase");

    }

}

