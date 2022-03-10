package uk.gov.homeoffice.digital.sas.jparest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.ast.SpelNodeImpl;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.homeoffice.digital.sas.demo.EntitiesApplication;
import uk.gov.homeoffice.digital.sas.demo.models.Record;
import uk.gov.homeoffice.digital.sas.demo.models.*;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= EntitiesApplication.class)
@WebAppConfiguration
public class ResourceApiControllerTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Test
    void shouldReturnAllEntitiesList(){
        Class<?> resource = Record.class;
        EntityUtils<?> entityUtils = new EntityUtils<>(resource, entityManager);
        ResourceApiController<?, ?> controller = new ResourceApiController<>(resource, entityManager, transactionManager, entityUtils);
        ApiResponse response = controller.list(null, Pageable.ofSize(100));

        assertThat(response).isNotNull();
        assertThat(response.getItems().size() == 30).isTrue();
    }

    @Test
    void shouldReturnEntityById(){
        Class<Artist> resource = Artist.class;
        EntityUtils<Artist> entityUtils = new EntityUtils<Artist>(resource, entityManager);
        ResourceApiController<Artist,Integer> controller = new ResourceApiController<Artist, Integer>(resource, entityManager, transactionManager, entityUtils);
        ResponseEntity<ApiResponse<Artist>> response = ( ResponseEntity<ApiResponse<Artist>>) controller.get(6);
        ApiResponse<Artist> apiResponse = response.getBody();
        Artist artist = apiResponse.getItems().get(0);

        assertThat(apiResponse.getItems().size() == 1).isTrue();
        assertThat(artist).isNotNull();
        assertThat(artist.getArtist_id()).isEqualTo(6);
        assertThat(artist.getProfile_id()).isEqualTo(6);
        assertThat(artist.getPerformance_name()).isEqualTo("Queen");
    }

    @Test
    void shouldSaveData() throws JsonProcessingException {
        String payload = "{\n" +
                "            \"profile_id\": 7,\n" +
                "            \"preferences\": \"My preferences for 7\",\n" +
                "            \"bio\": \"My Bio for 7\",\n" +
                "            \"phone_number\": \"07879 899107\",\n" +
                "            \"dob\": \"1979-01-01T00:00:00.000+00:00\",\n" +
                "            \"first_release\": \"1980-01-01T00:00:00.000+00:00\"\n" +
                "        }";

        Class<Profile> resource = Profile.class;
        EntityUtils<Profile> entityUtils = new EntityUtils<Profile>(resource, entityManager);
        ResourceApiController<Profile,String> controller = new ResourceApiController<Profile, String>(resource, entityManager, transactionManager, entityUtils);
        ResponseEntity<ApiResponse<Profile>> response = ( ResponseEntity<ApiResponse<Profile>>) controller.create(payload);
        ApiResponse<Profile> apiResponse = response.getBody();
        Profile profile = apiResponse.getItems().get(0);

        assertThat(apiResponse.getItems().size()).isEqualTo(1);
        assertThat(profile).isNotNull();
        assertThat(profile.getProfile_id()).isEqualTo(7);
        assertThat(profile.getPreferences()).isEqualTo("My preferences for 7");
        assertThat(profile.getBio()).isEqualTo("My Bio for 7");
        assertThat(profile.getPhone_number()).isEqualTo("07879 899107");
        assertThat(format.format(profile.getDob())).isEqualTo("1979-01-01");
        assertThat(format.format(profile.getFirst_release())).isEqualTo("1980-01-01");
    }

    @Test
    void shouldUpdateData() throws JsonProcessingException {

        String payload = "{\n" +
                "            \"session_id\": 1,\n" +
                "            \"session_name\": \"Keynote - The Golden Age of Software updated\",\n" +
                "            \"session_description\": \"Expires in 1 year\",\n" +
                "            \"session_length\": 60\n" +
                "        }";

        Class<Session> resource = Session.class;
        EntityUtils<Session> entityUtils = new EntityUtils<Session>(resource, entityManager);
        ResourceApiController<Session,Integer> controller = new ResourceApiController<Session, Integer>(resource, entityManager, transactionManager, entityUtils);
        ResponseEntity<ApiResponse<Session>> response = ( ResponseEntity<ApiResponse<Session>>) controller.update(1, payload);
        ApiResponse<Session> apiResponse = response.getBody();
        Session session = apiResponse.getItems().get(0);

        assertThat(apiResponse.getItems().size()).isEqualTo(1);
        assertThat(session).isNotNull();
        assertThat(session.getSession_id()).isEqualTo(1);
        assertThat(session.getSession_name()).isEqualTo("Keynote - The Golden Age of Software updated");
        assertThat(session.getSession_description()).isEqualTo("Expires in 1 year");
        assertThat(session.getSession_length()).isEqualTo(60);
    }

    @Test
    @Transactional
    void shouldDeleteData() {
        ResourceApiController<Concert, Integer> controller = getResourceApiController();
        controller.delete(1);

        ApiResponse response = controller.list(null, Pageable.ofSize(20));

        List<Concert> concerts = response.getItems();

        assertThat(concerts).isNotNull();
        assertThat(concerts.size()).isEqualTo(1);
        concerts.stream().forEach( concert -> {
            assertThat(concert.getConcert_id()).isNotEqualTo(1);
        });
    }

    @Test
    void shouldGetRelatedEntities(){
        ResourceApiController<Concert, Integer> controller = getResourceApiController();

        SpelExpressionParser expressionParser = new SpelExpressionParser();
        SpelExpression expression = expressionParser.parseRaw("artist_id==1");

        ApiResponse apiResponse = controller.getRelated(1, "artists", expression, Pageable.ofSize(100));
        Artist artist = (Artist) apiResponse.getItems().get(0);
        assertThat(apiResponse.getItems().size()).isEqualTo(1);
        assertThat(artist.getArtist_id()).isEqualTo(1);
        assertThat(artist.getProfile_id()).isEqualTo(1);
        assertThat(artist.getPerformance_name()).isEqualTo("Beautiful South");
    }

    @Test
    void shouldAddRelatedEntities() throws NoSuchFieldException, IllegalAccessException {
        ResourceApiController<Concert, Integer> controller = getResourceApiController();

        ResponseEntity response = controller.addRelated(1, "artists", new Object[]{3});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDeleteRelatedEntities() throws NoSuchFieldException, IllegalAccessException {
        ResourceApiController<Concert, Integer> controller = getResourceApiController();

        ResponseEntity response = controller.deleteRelated(2, "artists", new Object[]{6});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ResourceApiController<Concert, Integer> getResourceApiController() {
        Class<Concert> resource = Concert.class;
        EntityUtils<Concert> entityUtils = new EntityUtils<Concert>(resource, entityManager);
        return new ResourceApiController<Concert, Integer>(resource, entityManager, transactionManager, entityUtils);
    }
}