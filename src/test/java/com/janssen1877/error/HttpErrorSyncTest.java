package com.janssen1877.error;

import nl.copernicus.niklas.NiklasProperties;
import nl.copernicus.niklas.test.*;
import nl.copernicus.niklas.transformer.Header;
import org.junit.Test;

/**
 * @author Marco
 */
public class HttpErrorSyncTest extends FunctionalTestCase {

    @Test
    public void testProcessJDS() throws Exception {
        // initialise the BeanIOTransformer
        HttpErrorSync transformerInstance = getTransformerInstance(HttpErrorSync.class);
        Header hdr = new MockupHeader() {
            @Override
            public <R> R getProperty(String key) {
                if (key.equalsIgnoreCase(NiklasProperties.ERROR_MSG)) {
                    return (R) "er is iets mis gegaan";
                }
                return super.getProperty(key);
            }
        };
        String err = transformerInstance.process(hdr, "payload");
        System.out.println(Integer.toString(hdr.getProperty("http.status")));
        System.out.println(err);
    }

    @Test
    public void testProcessText() throws Exception {
        this.setComponentContext(new MockupComponentContext());
        this.getComponentContext().getProperties().put("outputMode", "text");
        // initialise the BeanIOTransformer
        HttpErrorSync transformerInstance = getTransformerInstance(HttpErrorSync.class);
        Header hdr = new MockupHeader() {
            @Override
            public <R> R getProperty(String key) {
                if (key.equalsIgnoreCase(NiklasProperties.ERROR_MSG)) {
                    return (R) "er is iets mis gegaan";
                }
                return super.getProperty(key);
            }
        };
        String err = transformerInstance.process(hdr, "payload");
        System.out.println(Integer.toString(hdr.getProperty("http.status")));
        System.out.println(err);
    }


}
