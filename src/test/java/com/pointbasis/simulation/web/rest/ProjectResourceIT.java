package com.pointbasis.simulation.web.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pointbasis.simulation.IntegrationTest;
import com.pointbasis.simulation.domain.Authority;
import com.pointbasis.simulation.domain.Project;
import com.pointbasis.simulation.domain.User;
import com.pointbasis.simulation.repository.ProjectRepository;
import com.pointbasis.simulation.repository.UserRepository;
import com.pointbasis.simulation.security.AuthoritiesConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.USER)
@IntegrationTest
public class ProjectResourceIT {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MockMvc restProjectMockMVC;

    private final ObjectMapper mapper = new ObjectMapper();

    private MvcResult result;

    private final String endpoint = "/api/projects/";

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
        userRepository.save(user);
    }

    @Test
    void testCreateProjectWithName() throws Exception {
        Project project = new Project();
        project.setName("Test");

        result = restProjectMockMVC
            .perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(project))
            )
            .andExpect(status().isCreated())
            .andReturn();

        String string = result.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(string, Map.class);

        Optional<Project> updatedProject = projectRepository.findById(map.get("id").toString());
        updatedProject.ifPresent(value -> assertThat(project.getName().equals(value.getName())));

    }

    @Test
    void testCreateProjectWithoutName() throws Exception {
        Project project = new Project();

        restProjectMockMVC
            .perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(project))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateProject() throws Exception {
        Project project = new Project();
        project.setName("Test");

        result = restProjectMockMVC
            .perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(project))
            )
            .andExpect(status().isCreated())
            .andReturn();

        String string = result.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(string, Map.class);


        Optional<Project> updatedProject = projectRepository.findById(map.get("id").toString());
        updatedProject.get().setName("Name is changed");

        restProjectMockMVC
            .perform(
                put(endpoint + map.get("id").toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedProject.get()))
            )
            .andExpect(status().isOk())
            .andReturn();

        projectRepository.findById(map.get("id").toString()).ifPresent(
            value -> assertThat(updatedProject.get().getName().equals(value.getName()))
        );
    }

    @Test
    void testGetAllProjectsByUser() throws Exception {
        Project project = new Project();
        project.setName("Project 1");

        restProjectMockMVC
            .perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(project))
            )
            .andExpect(status().isCreated());

        project.setName("Project 2");
        restProjectMockMVC
            .perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(project))
            )
            .andExpect(status().isCreated());

        result = restProjectMockMVC
            .perform(
                get(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andReturn();

        String string = result.getResponse().getContentAsString();

        List<Project> list = mapper.readValue(string, new TypeReference<List<Project>>() {
        });

        assertThat(list.size() == 2);
        assertThat(list.get(0).getName().equals("Project 1"));
        assertThat(list.get(1).getName().equals("Project 2"));
    }

    @Test
    public void testGetProjectById() throws Exception {
        Project project = new Project();
        project.setName("Project 1");

        result = restProjectMockMVC
            .perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(project))
            )
            .andExpect(status().isCreated())
            .andReturn();

        result = restProjectMockMVC
            .perform(
                get(endpoint + mapper.readValue(result.getResponse().getContentAsString(), Project.class).getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andReturn();


        assertThat(project.equals(mapper.readValue(result.getResponse().getContentAsString(), Project.class)));
    }

    @Test
    public void testDeleteProjectById() throws Exception {
        Project project = new Project();
        project.setName("Project");

        result = restProjectMockMVC
            .perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(project))
            )
            .andExpect(status().isCreated())
            .andReturn();

        assertThat(mapper.readValue(result.getResponse().getContentAsString(), Project.class).getName().equals("Project"));

        String projectId = mapper.readValue(result.getResponse().getContentAsString(), Project.class).getId();

        result = restProjectMockMVC
            .perform(
                delete(endpoint + "/" + projectId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent())
            .andReturn();

        result = restProjectMockMVC
            .perform(
                get(endpoint + "/" + projectId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andReturn();

        assertThat(result.getResponse().getContentAsString() == null);

    }

    @Test
    public void testDeleteProjectByWrongId() throws Exception {
        Project project = new Project();
        project.setName("Project");


        restProjectMockMVC
            .perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(project))
            )
            .andExpect(status().isCreated());

        result = restProjectMockMVC
            .perform(
                get(endpoint + "/123")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound())
            .andReturn();

    }

}
