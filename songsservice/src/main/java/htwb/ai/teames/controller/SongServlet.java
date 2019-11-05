package htwb.ai.teames.controller;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import htwb.ai.teames.model.Song;
import htwb.ai.teames.model.Songs;

public class SongServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_XML = "application/xml";
    private static final String ACCEPT = "Accept";
    private static final String TEXT_PLAIN = "text/plain";

    private String songFilename = null;
    private String absoluteSongFilename = null;
    private Map<Integer, Song> songStore = null;
    private Integer currentID = null;

    // load songStore from JSON file and set currentID
    public void init(ServletConfig servletConfig) throws ServletException {
        songFilename = servletConfig.getInitParameter("songFile");
        absoluteSongFilename = this.getClass().getClassLoader().getResource(songFilename).getPath();
        songStore = new ConcurrentHashMap<>();
        List<Song> songList = new ArrayList<>();
        try {
            songList = readJSONToSongs(absoluteSongFilename);
            System.out.println("Read " + songList.size() + " titles from " + songFilename);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (JsonParseException e) {
            System.err.println(e.getMessage());
        } catch (JsonMappingException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } 
        for (Song song : songList) {
            songStore.put(song.getId(), song);
        }
        // Calculating currentID
        OptionalInt max = songStore.keySet().stream().mapToInt(Integer::intValue).max();
        currentID = max.orElse(0);
        System.out.println("currentID: " + currentID);
    }

    synchronized Integer getNextID() {
        currentID = currentID + 1;
        System.out.println("getNextID returns: " + currentID);
        return currentID;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<String> acceptHeaders = Collections.list((request.getHeaders(ACCEPT)));
        String contentType = determineContentTypeFromAcceptHeader(acceptHeaders);
        if (contentType == null) {
            response.sendError(406, "We only serve application/json or application/xml!");
            return;
        }

        response.setContentType(contentType);
        Songs songs = new Songs();
        Map<String, String[]> parameterMap = request.getParameterMap();
        
        if (parameterMap == null || parameterMap.isEmpty()) {
            // return all
            songs.setSongList(new ArrayList<Song>(songStore.values()));
        } else if (parameterMap.containsKey("songId")) {
            try {
                Integer id = Integer.parseInt(request.getParameter("songId"));
                Song song = songStore.get(id);
                if (song == null) {
                    response.sendError(404, id + " does not exist");
                    return;
                } else {
                    songs.getSong().add(song);
                }
            } catch (NumberFormatException nef) {
                response.sendError(400, "Could not parse id: " + nef.getMessage());
                return;
            }
        } else {
            response.sendError(400, "Please use ' or 'songId={SongId}'");
            return;
        }
 
        switch (contentType) {
        case APPLICATION_JSON:
            ObjectMapper objectMapper = new ObjectMapper();
            try (PrintWriter out = response.getWriter()) {
                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(songs.getSong());
                out.println(json);
            }
            break;

        case APPLICATION_XML:
            JAXBContext context;
            try {
                context = JAXBContext.newInstance(Songs.class);
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                try (PrintWriter out = response.getWriter()) {
                    marshaller.marshal(songs, out);
                }
            } catch (JAXBException e) {
                response.sendError(400, "Something went wrong! :-(");
                return;
            }
            break;
           
        default:
            response.sendError(400, "We should not be getting here!!");
            return;
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Song newSong = null;
        try {
            ServletInputStream inputStream = request.getInputStream();
            newSong = readSongFromInput(inputStream, request.getContentType());
            if (newSong == null) {
                response.sendError(400, "Something was wrong with the payload with content-type " + request.getContentType());
                return;
            }
        } catch (IOException e) {
            response.sendError(400, "Could not read payload: " + e.getMessage());
            return;
        }

        Integer songID = getNextID();
        newSong.setId(songID);
        songStore.put(songID, newSong);
        response.setContentType(TEXT_PLAIN);
        try (PrintWriter out = response.getWriter()) {
            out.println("Added song with title: " + newSong.getTitle() + " and id=" + newSong.getId() + "\n");
        }
    }

    @Override
    public void destroy() {
        System.out.println("In destroy - writing songs to " + absoluteSongFilename);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValue(new FileOutputStream(absoluteSongFilename), songStore.values());
        } catch (JsonGenerationException e) {
            System.err.println("In destroy: " + e.getMessage());
            e.printStackTrace();
        } catch (JsonMappingException e) {
            System.err.println("In destroy: " + e.getMessage());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println("In destroy: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("In destroy: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Song> readJSONToSongs(String filename) throws FileNotFoundException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream is = new BufferedInputStream(new FileInputStream(filename))) {
            return (List<Song>) objectMapper.readValue(is, new TypeReference<List<Song>>() {
            });
        }
    }

    private String determineContentTypeFromAcceptHeader(List<String> acceptHeaders) {

        if (acceptHeaders == null || acceptHeaders.isEmpty()) {
            return APPLICATION_JSON;
        }

        acceptHeaders = acceptHeaders.stream().map(String::toLowerCase).collect(Collectors.toList());

        if (acceptHeaders.contains(APPLICATION_JSON)) {
            return APPLICATION_JSON;
        }
//        if (acceptHeaders.contains(APPLICATION_XML)) {
//            return APPLICATION_XML;
//        }
        if (acceptHeaders.contains("*") || acceptHeaders.contains("*/*")) {
            return APPLICATION_JSON;
        }

        return null;
    }
    
    private Song readSongFromInput (InputStream inStream, String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return null;
        }
        Song song = null;
        switch (contentType.trim().toLowerCase()) {
        case APPLICATION_JSON:
            try {
                byte[] songBytes = IOUtils.toByteArray(inStream);
                ObjectMapper objectMapper = new ObjectMapper();
                song = (Song) objectMapper.readValue(songBytes, Song.class);
                return song;
            } catch (IOException e) {
                System.out.println("ERROR READING CLIENT's JSON INPUT");
                e.printStackTrace();
                return null;
            }
            
        case APPLICATION_XML:
            JAXBContext context;
            try {
                context = JAXBContext.newInstance(Songs.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                Songs songs = (Songs) unmarshaller.unmarshal(inStream);
                if (songs.getSong() != null && !songs.getSong().isEmpty()) {
                    song = songs.getSong().get(0);
                } else {
                    song = null;
                }
                return song;
            } catch (JAXBException e) {
                System.out.println("ERROR READING CLIENT's XML INPUT");
                e.printStackTrace();
                return null;
            }
           
        default:
            return song;
        }
        
    }

}
