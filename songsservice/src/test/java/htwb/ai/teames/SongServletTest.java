package htwb.ai.teames;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import htwb.ai.teames.controller.SongServlet;
import htwb.ai.teames.model.Song;
import htwb.ai.teames.model.Songs;

public class SongServletTest {
	
    private SongServlet servlet;
    private MockServletConfig config;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private 	ObjectMapper objectMapper;
    
    @Before
    public void setUp() throws ServletException {
    		objectMapper = new ObjectMapper();
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void playingWithJackson() throws IOException {
	    
    	    // Read a JSON file and create song list:
    		InputStream input = this.getClass().getClassLoader().getResourceAsStream("testSongs.json");
	    
        List<Song> songList = (List<Song>) objectMapper.readValue(input, new TypeReference<List<Song>>(){});
	    
	    	// write a list of objects to a JSON-String with prettyPrinter
	    	String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(songList);
	    
	    	// write a list of objects to an outputStream in JSON format
	    	objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream("output.json"), songList);
    
	    	// Create a song and write to JSON
	    	Song song = new Song (null, "titleXX", "artistXX", "albumXX", 1999);
	    byte[] jsonBytes = objectMapper.writeValueAsBytes(song);
	    
	    // Read JSON from byte[] into Object
	    Song newSong = (Song) objectMapper.readValue(jsonBytes, Song.class);
	    assertEquals(song.getTitle(), newSong.getTitle());
	    assertEquals(song.getArtist(), newSong.getArtist());
    }
    
    @Test
    public void playingWithJAXB() throws IOException {     
        
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(Songs.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("testSongs.xml")) {
                Songs songs = (Songs) unmarshaller.unmarshal(is);
            }
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    //@Test
    public void initShouldLoadSongList() {
    }

	//@Test
    public void doGetShouldxxx() {
    }
    
    //@Test
    public void doPostShouldxxx() {      
    }
    
    private Songs readXMLToSongs(String filename) throws JAXBException, FileNotFoundException, IOException {
        JAXBContext context = JAXBContext.newInstance(Songs.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        try (InputStream is = new BufferedInputStream(new FileInputStream(filename))) {
            return (Songs) unmarshaller.unmarshal(is);
        }
    }
}
