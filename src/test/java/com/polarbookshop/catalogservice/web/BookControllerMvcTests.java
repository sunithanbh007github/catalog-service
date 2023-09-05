package com.polarbookshop.catalogservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polarbookshop.catalogservice.config.SecurityConfig;
import com.polarbookshop.catalogservice.domain.Book;
import com.polarbookshop.catalogservice.domain.BookNotFoundException;
import com.polarbookshop.catalogservice.domain.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class) //Imports the application’s security configuration
public class BookControllerMvcTests {

    private static final String ROLE_EMPLOYEE = "ROLE_employee";
    private static final String ROLE_CUSTOMER = "ROLE_customer";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;


    @MockBean
    private BookService bookService;

    @MockBean
    JwtDecoder jwtDecoder; //Mocks the JwtDecoder so that the application doesn’t try to call Keycloak and get the public keys
                            // for decoding the Access Token

    @Test
    void whenGetBookExistingAndAuthenticatedThenShouldReturn200() throws Exception {
        var isbn = "7373731394";
        var expectedBook = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
        given(bookService.viewBookDetails(isbn)).willReturn(expectedBook);
        mockMvc
                .perform(get("/books/" + isbn)
                        .with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void whenGetBookExistingAndNotAuthenticatedThenShouldReturn200() throws Exception {
        var isbn = "7373731394";
        var expectedBook = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
        given(bookService.viewBookDetails(isbn)).willReturn(expectedBook);
        mockMvc
                .perform(get("/books/" + isbn))
                .andExpect(status().isOk());
    }

    @Test
    void whenGetBookNotExistingAndAuthenticatedThenShouldReturn404() throws Exception {
        String isbn = "73737313940";
        given(bookService.viewBookDetails(isbn)).willThrow(BookNotFoundException.class);
        mockMvc
                .perform(get("/books/" + isbn)
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetBookNotExistingAndNotAuthenticatedThenShouldReturn404() throws Exception {
        var isbn = "7373731394";
        given(bookService.viewBookDetails(isbn)).willThrow(BookNotFoundException.class);
        mockMvc
                .perform(get("/books/" + isbn))
                .andExpect(status().isNotFound());
    }


    @Test
    void whenDeleteBookWithEmployeeRoleThenShouldReturn204() throws Exception {
        var isbn = "7373731394";
        mockMvc
                .perform(MockMvcRequestBuilders.delete("/books/" + isbn)
                        //Mutates the HTTP request with a mock JWT-formatted Access Token for a user with the “employee” role
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_employee"))))
                        .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void whenDeleteBookWithCustomerRoleThenShouldReturn403() throws Exception {
        var isbn = "7373731394";
        mockMvc
                .perform(delete("/books/" + isbn)
                        .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CUSTOMER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenDeleteBookNotAuthenticatedThenShouldReturn401() throws Exception {
        var isbn = "7373731394";
        mockMvc
                .perform(delete("/books/" + isbn))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenPostBookWithEmployeeRoleThenShouldReturn201() throws Exception {
        var isbn = "7373731394";
        var bookToCreate = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
        given(bookService.addBookToCatalog(bookToCreate)).willReturn(bookToCreate);
        mockMvc
                .perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookToCreate))
                        .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE))))
                .andExpect(status().isCreated());
    }

    @Test
    void whenPostBookWithCustomerRoleThenShouldReturn403() throws Exception {
        var isbn = "7373731394";
        var bookToCreate = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
        given(bookService.addBookToCatalog(bookToCreate)).willReturn(bookToCreate);
        mockMvc
                .perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookToCreate))
                        .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CUSTOMER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenPostBookAndNotAuthenticatedThenShouldReturn403() throws Exception {
        var isbn = "7373731394";
        var bookToCreate = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
        mockMvc
                .perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookToCreate)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void whenPutBookWithEmployeeRoleThenShouldReturn200() throws Exception {
        var isbn = "7373731394";
        var bookToCreate = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
        given(bookService.addBookToCatalog(bookToCreate)).willReturn(bookToCreate);
        mockMvc
                .perform(put("/books/" + isbn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookToCreate))
                        .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE))))
                .andExpect(status().isOk());
    }

    @Test
    void whenPutBookWithCustomerRoleThenShouldReturn403() throws Exception {
        var isbn = "7373731394";
        var bookToCreate = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
        given(bookService.addBookToCatalog(bookToCreate)).willReturn(bookToCreate);
        mockMvc
                .perform(put("/books/" + isbn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookToCreate))
                        .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CUSTOMER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenPutBookAndNotAuthenticatedThenShouldReturn401() throws Exception {
        var isbn = "7373731394";
        var bookToCreate = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
        mockMvc
                .perform(put("/books/" + isbn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookToCreate)))
                .andExpect(status().isUnauthorized());
    }
}
