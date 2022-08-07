package ch.wipfli.microstreamclientplus.web.helper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class JavaScriptListener<T> extends AbstractResource {

    private boolean disableCache = false;

    public JavaScriptListener() {

        final ResourceReference resourceReference = new ResourceReference(getPath()) {
            @Override
            public IResource getResource() {
                return JavaScriptListener.this;
            }
        };
        WebApplication.get().mountResource(getPath(), resourceReference);
    }

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        ResourceResponse resourceResponse = new ResourceResponse();
        resourceResponse.setContentType("text/json");
        resourceResponse.setTextEncoding("utf-8");

        if(disableCache) {
            resourceResponse.disableCaching();
            super.configureCache(resourceResponse, attributes);
        }

        resourceResponse.setWriteCallback(new AbstractResource.WriteCallback() {

            @Override
            public void writeData(IResource.Attributes attributes) throws IOException {
                final OutputStream outputStream = attributes.getResponse().getOutputStream();
                try (Writer writer = new OutputStreamWriter(outputStream)) {

                    final String response = new ObjectMapper().writeValueAsString(listener(attributes.getParameters().getAllNamed()));
                    writer.write(response);
                }
            }
        });
        return resourceResponse;
    }

    public void setDisableCache(boolean disableCache) {
        this.disableCache = disableCache;
    }

    public abstract String getPath();

    protected abstract T listener(List<INamedParameters.NamedPair> parameter);
}
