package com.javarush.jira.profile.internal.web;

import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.common.BaseHandler;
import com.javarush.jira.common.util.JsonUtil;
import com.javarush.jira.login.AuthUser;
import com.javarush.jira.login.internal.web.UserTestData;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.javarush.jira.profile.internal.web.ProfileTestData.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileRestControllerTest extends AbstractControllerTest {
    private static final String REST_URL_PROFILE = BaseHandler.REST_URL + "/profile";

    @Autowired
    ProfileRepository profileRepository;
    @Autowired
    MockMvc mockMvc;

    @Test
    @WithUserDetails(value = UserTestData.USER_MAIL)
    public void getProfileSuccess() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(REST_URL_PROFILE);

        perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROFILE_MATCHER_TO.contentJson(USER_PROFILE_TO));
    }

    @Test
    public void getNonExistentProfile() throws Exception {
        int nonExistentId = 12345; // ID, которого нет в БД

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(REST_URL_PROFILE + "/{id}", nonExistentId)
                .with(user("admin").roles("ADMIN")); // Админ (имеет права на просмотр)

        perform(requestBuilder)
                .andExpect(status().isNotFound()); // Ожидаем 404
    }

    @Test
    public void getUnauthenticated() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(REST_URL_PROFILE)
                .with(anonymous()); // Имитация неавторизованного пользователя

        perform(requestBuilder)
                .andExpect(status().isUnauthorized()); // Ожидаем 401
    }

    @Test
    @WithUserDetails(value = UserTestData.USER_MAIL)
    public void updateProfileSuccess() throws Exception {
        long id = 1L;

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put(REST_URL_PROFILE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(JsonUtil.writeValue(ProfileTestData.getUpdatedTo(id)));

        perform(requestBuilder).andExpect(status().isNoContent());

        Profile expect = ProfileTestData.getUpdated(id);
        Profile actual = profileRepository.findById(id).orElseThrow();
        PROFILE_MATCHER.assertMatch(actual, expect);
    }

}

