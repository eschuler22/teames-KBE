package htwb.ai.teames.access;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AccessCounterServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;
    private String inFile = null;
    private Access access;

    @Override
    public void init(ServletConfig servletConfig)  {
        inFile = servletConfig.getInitParameter("accessFile");
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream input;
        try {
            input = this.getClass().getClassLoader()
                    .getResourceAsStream(inFile);
            access = (Access) objectMapper.readValue(input, Access.class);
        } catch (Exception e) {
            access = new Access();
        }
        System.out.println("Last time accessed: " 
         + access.getLastAccessTime() + "" + "total times accessed: "
         + access.getAccessCounter());
    }

    @Override
    public void doPost(HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(access);
            access.setLastAccessTime(System.currentTimeMillis());
            access.setAccessCounter(access.getAccessCounter() + 1);
            out.println(json);
        }
    }

    @Override
    public void destroy() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValue(new FileOutputStream(inFile), access);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
