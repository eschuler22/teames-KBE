package htwb.ai.TEAMNAME.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

@Path("/version")
public class GitVersionService {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getGITVersion() {
        String gitVersion = null;
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("git.properties")) {
            gitVersion = IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            gitVersion = "Could not load git.properties, check logs!";
            e.printStackTrace();
        }
            return Response.ok(gitVersion).build();
    }
}
