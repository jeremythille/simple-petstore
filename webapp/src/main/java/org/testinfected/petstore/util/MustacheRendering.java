package org.testinfected.petstore.util;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import org.testinfected.support.util.Charsets;
import org.testinfected.support.RenderingEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

public class MustacheRendering implements RenderingEngine {

    private static final String TEMPLATE_EXTENSION = ".html";

    // We can't use DefaultMustacheFactory built-in resource loading for it always uses
    // the default platform encoding
    private final DefaultMustacheFactory mustacheFactory = new DefaultMustacheFactory() {
        public Reader getReader(String resourceName) {
            return read(resourceName);
        }
    };

    private final File base;

    public MustacheRendering(File base) {
        this.base = base;
    }

    public void render(Writer out, String view, Object context) {
        Mustache mustache = mustacheFactory.compile(templateFile(view));
        mustache.execute(out, context);
    }

    private Reader read(String resourceName) {
        File file = new File(base, resourceName);
        try {
            return new InputStreamReader(new FileInputStream(file), Charsets.UTF_8);
        } catch (FileNotFoundException e) {
            throw new MustacheException("Template not found: " + resourceName);
        }
    }

    private String templateFile(String templateName) {
        return templateName + TEMPLATE_EXTENSION;
    }
}