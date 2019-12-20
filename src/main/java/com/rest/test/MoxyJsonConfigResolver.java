package com.rest.test;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;


@Provider
public class MoxyJsonConfigResolver implements ContextResolver<MoxyJsonConfig> {

    private final MoxyJsonConfig config;

    public MoxyJsonConfigResolver() {
        final Map<String, String> namespacePrefixMapper = new HashMap<String, String>();
        namespacePrefixMapper.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        config = new MoxyJsonConfig()
                .setNamespacePrefixMapper(namespacePrefixMapper)
                .setNamespaceSeparator(':')
                .setAttributePrefix("")
                .setValueWrapper("value")
                .property(JAXBContextProperties.JSON_WRAPPER_AS_ARRAY_NAME, true)
                .setFormattedOutput(true)
                .setIncludeRoot(true)
                .setMarshalEmptyCollections(true);
    }

    @Override
    public MoxyJsonConfig getContext(Class<?> objectType) {
        return config;
    }
}
