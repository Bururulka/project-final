package com.javarush.jira.profile.internal.web;

import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.common.BaseHandler;
import com.javarush.jira.common.util.JsonUtil;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileMapper;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
class ProfileRestControllerTest extends AbstractControllerTest {
    private static final String REST_URL_PROFILE = BaseHandler.REST_URL + "/profile";
    @SpyBean

    private ProfileRepository profileRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProfileMapper profileMapper;
    private ProfileRestController profileRestController;

    private final WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        profileMapper = Mockito.mock(ProfileMapper.class);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @WithAnonymousUser
    @DisplayName(value = "Un Auth User")
    public void testUnAuthUser() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL_PROFILE))
                .andExpect(status().isUnauthorized());
    }

//    @Test
//    @DisplayName(value = "Auth User")
//    @WithUserDetails(value = "user@gmail.com")
//    public void testAuthUser() throws Exception {
//        perform(MockMvcRequestBuilders.get(REST_URL_PROFILE)
//                .contentType("application/json;charset=UTF-8"))
//                .andExpect(status().isOk());
//    }

    @Test
    @DisplayName(value = "Error on database")
    @WithUserDetails(value = "user@gmail.com")
    public void testErrorOnDatabase() throws Exception {
        when(profileRepository.getOrCreate(1L)).thenThrow(new RuntimeException());

        mockMvc.perform(MockMvcRequestBuilders.get(REST_URL_PROFILE))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName(value = "get Auth User")
    @WithUserDetails(value = "user@gmail.com")
    public void testGetAuthUser() throws  Exception {
        ProfileTo profileTo = ProfileTestData.USER_PROFILE_TO;

        MvcResult result = perform(MockMvcRequestBuilders.get(REST_URL_PROFILE).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        ProfileTo profileToResult = JsonUtil.readValue(result.getResponse().getContentAsString(), ProfileTo.class);
        assertEquals(profileToResult, profileTo);
    }
}