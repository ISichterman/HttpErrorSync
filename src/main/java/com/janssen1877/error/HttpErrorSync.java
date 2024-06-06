package com.janssen1877.error;

import com.google.gson.Gson;
import nl.copernicus.niklas.NiklasProperties;
import nl.copernicus.niklas.transformer.*;
import nl.copernicus.niklas.transformer.context.ComponentContext;
import nl.copernicus.niklas.transformer.context.NiklasLogger;
import nl.copernicus.niklas.transformer.context.RoutingContext;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dverloop
 */
public class HttpErrorSync implements NiklasComponent<String, String>, NiklasLoggerAware, RoutingContextAware, ComponentContextAware {

    protected NiklasLogger log;
    private RoutingContext rc;
    private ComponentContext cc;
    private Set<String> Modes = Stream.of("json", "text").collect(Collectors.toSet());

    @Override
    public String process(Header header, String payload) throws NiklasComponentException, NiklasInterruptionException {
        String OutputMode = cc.getProperty("outputMode");
        if(OutputMode == null || ! Modes.contains(OutputMode)){
            OutputMode="json";
        }
        int Status = 400;
        //init the response map
        Map<String, String> response = new HashMap<>();
        response.put("title", "data has not been processed");
        response.put("type", "error");
        //check the error message
        if (header.getProperty(NiklasProperties.ERROR_MSG) != null) {
            //build error response if error message is found on the message
            String errorMessage = header.getProperty(NiklasProperties.ERROR_MSG);
            if (((String)header.getProperty(NiklasProperties.ERROR_TYPE)).contains("NiklasCarrierException")) {
                log.info("Attempting to translate error");
                try {
                    Header hdr = header.cloneHeader();
                    hdr.setProperty(NiklasProperties.SENDER_NAME,"HOLDING");
                    hdr.setProperty(NiklasProperties.RECIPIENT_NAME,"EUROPAKET");
                    hdr.setProperty(NiklasProperties.MSG_TYPE, "FLAT");
                    hdr.setProperty(NiklasProperties.DOC_TYPE, "ERROR");
                    hdr.setProperty(NiklasProperties.MSG_VERSION, "TRANSL");
                    byte[] errorBytes = rc.sendToAgreement(hdr, errorMessage);
                    errorMessage = new String(errorBytes);
                } catch (Exception e) {
                    log.error("Error when translating error message", e);
                }
            }
            response.put("description", errorMessage);

            if (errorMessage.equals("Error during detection with reason Detection found no results!")) {
                Status = 404;
            }
        } else if (header.getProperty("NIKLAS_ERR_REPORT") != null) {
            response.put("description", header.getProperty("NIKLAS_ERR_REPORT"));
        }

        //determine http error code
        header.setProperty("http.status", Status);
        //return the output
        if(OutputMode.equals("text")){
            return response.get("description");
        }

        //default output is json
        return new Gson().toJson(response);
    }

    @Override
    public void setLogger(NiklasLogger nl) {
        this.log = nl;
    }

    @Override
    public void setRoutingContext(RoutingContext routingContext) {
        this.rc = routingContext;
    }

    @Override
    public void setComponentContext(ComponentContext ctx) {
        this.cc = ctx;
    }
}
