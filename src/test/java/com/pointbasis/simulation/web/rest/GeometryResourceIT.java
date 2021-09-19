package com.pointbasis.simulation.web.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pointbasis.simulation.IntegrationTest;
import com.pointbasis.simulation.domain.Authority;
import com.pointbasis.simulation.domain.Geometry;
import com.pointbasis.simulation.domain.Project;
import com.pointbasis.simulation.domain.User;
import com.pointbasis.simulation.repository.GeometryRepository;
import com.pointbasis.simulation.repository.ProjectRepository;
import com.pointbasis.simulation.repository.UserRepository;
import com.pointbasis.simulation.security.AuthoritiesConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.USER)
@IntegrationTest
public class GeometryResourceIT {

    private static final String DEFAULT_LOGIN = "johndoe";

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private GeometryRepository geometryRepository;

    @Autowired
    private MockMvc restGeometryMockMVC;

    private final ObjectMapper mapper = new ObjectMapper();

    private MvcResult result;

    private String userId, projectId;

    private String endpoint = "/api/project/";

    private Project project;

    private final String data =
        "solid ascii\n" +
            " facet normal 0.998757 -0.0498437 0\n" +
            "  outer loop\n" +
            "   vertex 0.995031 -0.0995678 -0.9\n" +
            "   vertex 0.995031 -0.0995678 -1\n" +
            "   vertex 1 0 -0.9\n" +
            "  endloop\n" +
            " endfacet\n" +
            "endsolid";

    @BeforeEach
    public void setup() {
        cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).clear();
        cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE).clear();
    }

    @BeforeEach
    public void initTest() {
        User user = UserResourceIT.initTestUser(userRepository);
        HashSet<Authority> authorities = new HashSet<>();
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.USER);
        authorities.add(authority);
        user.setAuthorities(authorities);
        user.setPassword("$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K");
        user.setLogin("user");
        userId = userRepository.save(user).getId();

        project = new Project();
        project.setName("Project");
        projectId = projectRepository.save(project).getId();
        endpoint += projectId + "/models/";
    }

    @Test
    public void testCreateModel() throws Exception {


        MockMultipartFile file = new MockMultipartFile(
            "object",
            "cylinder.stl",
            MediaType.MULTIPART_RELATED_VALUE,
            data.getBytes(StandardCharsets.UTF_8)
        );

        result = restGeometryMockMVC
            .perform(
                multipart(endpoint).file(file)
            )
            .andExpect(status().isCreated())
            .andReturn();

        Geometry geometry = mapper.readValue(result.getResponse().getContentAsString(), Geometry.class);

        assertThat(geometry.getName().equals("cylinder"));
        assertThat(geometry.getInputLink().get("stl").equals(userId + "/" + projectId + "/inputFiles/cylinder.stl"));
        assertThat(geometry.getInputLink().get("drc").equals(userId + "/" + projectId + "/inputFiles/cylinder.drc"));
    }

    @Test
    public void testGetModelById() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "object",
            "cylinder.stl",
            MediaType.MULTIPART_RELATED_VALUE,
            data.getBytes(StandardCharsets.UTF_8)
        );

        result = restGeometryMockMVC
            .perform(
                multipart(endpoint).file(file)
            )
            .andExpect(status().isCreated())
            .andReturn();

        Geometry model = mapper.readValue(result.getResponse().getContentAsString(), Geometry.class);

        result = restGeometryMockMVC
            .perform(
                get("/api/geometries/" + model.getId())
                .contentType(MediaType.APPLICATION_JSON)

            )
            .andExpect(status().isOk())
            .andReturn();

        assertThat(mapper.readValue(result.getResponse().getContentAsString(), Geometry.class).equals(model));
    }

    @Test
    public void testAllModelsByUser() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
            "object",
            "cylinder.stl",
            MediaType.MULTIPART_RELATED_VALUE,
            data.getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile file2 = new MockMultipartFile(
            "object",
            "747.stl",
            MediaType.MULTIPART_RELATED_VALUE,
            data.getBytes(StandardCharsets.UTF_8)
        );

        Geometry model1 = mapper.readValue(
            restGeometryMockMVC
            .perform(
                multipart(endpoint).file(file1)
            )
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse().getContentAsString(), Geometry.class
        );

        Geometry model2 = mapper.readValue(
            restGeometryMockMVC
                .perform(
                    multipart(endpoint).file(file2)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse().getContentAsString(), Geometry.class
        );

        List<Geometry> expectedList = new ArrayList<>();
        expectedList.add(model1);
        expectedList.add(model2);

        result = restGeometryMockMVC
            .perform(
                get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();

        List<Geometry> actualList = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<Geometry>>(){});

        assertThat(expectedList.equals(actualList));
        assertThat(!new ArrayList<Geometry>().equals(expectedList));
    }

    @Test
    public void testGetFileContent() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
            "object",
            "cylinder.stl",
            MediaType.MULTIPART_RELATED_VALUE,
            data.getBytes(StandardCharsets.UTF_8)
        );

        Geometry model = mapper.readValue(
            restGeometryMockMVC
            .perform(
                multipart(endpoint).file(file)
            )
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString(), Geometry.class
        );

        result = restGeometryMockMVC
            .perform(
                get("/api/project/" + projectId + "/model/" + model.getId() + "/getModel?format=drc")

            )
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getResponse().getContentAsString() != null);

    }

    @Test
    public void testDeleteModel() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "object",
            "cylinder.stl",
            MediaType.MULTIPART_RELATED_VALUE,
            data.getBytes(StandardCharsets.UTF_8)
        );

        Geometry model = mapper.readValue(
            restGeometryMockMVC
            .perform(
                multipart(endpoint).file(file)
            )
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString(), Geometry.class);

        restGeometryMockMVC
            .perform(
                delete("/api/models/" + model.getId())
            )
            .andExpect(status().isNoContent());
    }

}
