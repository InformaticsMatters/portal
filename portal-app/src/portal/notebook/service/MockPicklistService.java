package portal.notebook.service;

import portal.notebook.api.Strings;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("picklist")
@Produces(MediaType.APPLICATION_XML)
public class MockPicklistService {

    @Path("assayIdStrings")
    @GET
    public Strings assayIdStrings(@QueryParam("query") String query) {
        Strings strings = new Strings();
         for (int i = 0; i < 10; i++) {
             strings.add(query + i);
         }
        return strings;
    }



}
