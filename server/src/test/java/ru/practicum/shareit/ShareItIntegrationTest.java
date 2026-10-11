package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.config.HeaderConstants;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShareItIntegrationTest {
    private static final String HEADER = HeaderConstants.X_SHARER_USER_ID;
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final long UNKNOWN_ID = 999999L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    // ---------- пользователи ----------

    @Test
    void usersFlow() throws Exception {
        String email = "flow" + COUNTER.incrementAndGet() + "@mail.com";
        long id = createUserWithEmail(email);
        long other = createUser();

        patchJson("/users/" + id, null, "{\"name\":\"new\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new"));
        patchJson("/users/" + id, null, "{\"email\":\"" + email + "\"}")
                .andExpect(status().isOk());
        patchJson("/users/" + other, null, "{\"email\":\"" + email + "\"}")
                .andExpect(status().isConflict());
        patchJson("/users/" + UNKNOWN_ID, null, "{\"name\":\"x\"}")
                .andExpect(status().isNotFound());

        getAs("/users/" + id, null).andExpect(status().isOk());
        getAs("/users", null).andExpect(status().isOk());
        getAs("/users/" + UNKNOWN_ID, null).andExpect(status().isNotFound());

        postJson("/users", null, "{\"name\":\"dup\",\"email\":\"" + email + "\"}")
                .andExpect(status().isConflict());
        postJson("/users", null, "{\"name\":\"bad\",\"email\":\"bad-email\"}")
                .andExpect(status().isBadRequest());
        postJson("/users", null, "{\"name\":\"empty\",\"email\":\"\"}")
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/users/" + id)).andExpect(status().isOk());
        mockMvc.perform(delete("/users/" + id)).andExpect(status().isNotFound());
        getAs("/users/" + id, null).andExpect(status().isNotFound());
    }

    // ---------- вещи ----------

    @Test
    void itemsFlow() throws Exception {
        long owner = createUser();
        long stranger = createUser();
        long itemId = createItem(owner, true, null);

        patchJson("/items/" + itemId, owner,
                "{\"name\":\"renamed\",\"description\":\"new description\",\"available\":false}")
                .andExpect(status().isOk());
        patchJson("/items/" + itemId, owner, "{\"available\":true}")
                .andExpect(status().isOk());
        patchJson("/items/" + itemId, stranger, "{\"name\":\"hack\"}")
                .andExpect(status().is4xxClientError());
        patchJson("/items/" + UNKNOWN_ID, owner, "{\"name\":\"x\"}")
                .andExpect(status().isNotFound());

        getAs("/items/" + itemId, owner).andExpect(status().isOk());
        getAs("/items/" + itemId, stranger).andExpect(status().isOk());
        getAs("/items/" + UNKNOWN_ID, owner).andExpect(status().isNotFound());
        getAs("/items", owner).andExpect(status().isOk());
        getAs("/items", UNKNOWN_ID).andExpect(status().isNotFound());

        getAs("/items/search?text=RENAMED", stranger)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        getAs("/items/search?text=", stranger)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        getAs("/items/search?text=zzzzqqqq", stranger)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        postJson("/items", owner, "{\"name\":\"\",\"description\":\"d\",\"available\":true}")
                .andExpect(status().isBadRequest());
        postJson("/items", owner, "{\"name\":\"n\",\"description\":\"\",\"available\":true}")
                .andExpect(status().isBadRequest());
        postJson("/items", owner, "{\"name\":\"n\",\"description\":\"d\"}")
                .andExpect(status().isBadRequest());
        postJson("/items", UNKNOWN_ID, "{\"name\":\"n\",\"description\":\"d\",\"available\":true}")
                .andExpect(status().isNotFound());
    }

    // ---------- бронирования ----------

    @Test
    void bookingsFlow() throws Exception {
        long owner = createUser();
        long booker = createUser();
        long stranger = createUser();
        long itemId = createItem(owner, true, null);
        LocalDateTime now = LocalDateTime.now();

        long b1 = createBooking(booker, itemId, now.plusDays(1), now.plusDays(2));
        patchAs("/bookings/" + b1 + "?approved=true", owner)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        patchAs("/bookings/" + b1 + "?approved=true", booker)
                .andExpect(status().is4xxClientError());
        patchAs("/bookings/" + UNKNOWN_ID + "?approved=true", owner)
                .andExpect(status().isNotFound());

        long b2 = createBooking(booker, itemId, now.plusDays(5), now.plusDays(6));
        patchAs("/bookings/" + b2 + "?approved=false", owner)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
        createBooking(booker, itemId, now.plusDays(10), now.plusDays(11));

        getAs("/bookings/" + b1, booker).andExpect(status().isOk());
        getAs("/bookings/" + b1, owner).andExpect(status().isOk());
        getAs("/bookings/" + b1, stranger).andExpect(status().is4xxClientError());
        getAs("/bookings/" + UNKNOWN_ID, booker).andExpect(status().isNotFound());

        for (String state : new String[]{"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"}) {
            getAs("/bookings?state=" + state, booker).andExpect(status().isOk());
            getAs("/bookings/owner?state=" + state, owner).andExpect(status().isOk());
        }
        getAs("/bookings", booker).andExpect(status().isOk());
        getAs("/bookings/owner", owner).andExpect(status().isOk());
        getAs("/bookings", UNKNOWN_ID).andExpect(status().isNotFound());
        getAs("/bookings/owner", UNKNOWN_ID).andExpect(status().isNotFound());
        // неизвестный state: только проходим по обработчику ошибок
        getAs("/bookings?state=UNKNOWN", booker).andReturn();
    }

    @Test
    void bookingValidation() throws Exception {
        long owner = createUser();
        long booker = createUser();
        long itemId = createItem(owner, true, null);
        long unavailableId = createItem(owner, false, null);
        LocalDateTime now = LocalDateTime.now();

        postBooking(owner, itemId, now.plusDays(1), now.plusDays(2))
                .andExpect(status().is4xxClientError());
        postBooking(booker, unavailableId, now.plusDays(1), now.plusDays(2))
                .andExpect(status().is4xxClientError());
        postBooking(booker, itemId, now.plusDays(2), now.plusDays(1))
                .andExpect(status().is4xxClientError());
        postBooking(booker, itemId, now.minusDays(1), now.plusDays(1))
                .andExpect(status().is4xxClientError());
        postJson("/bookings", booker, "{\"itemId\":" + itemId + "}")
                .andExpect(status().is4xxClientError());
        postBooking(booker, UNKNOWN_ID, now.plusDays(1), now.plusDays(2))
                .andExpect(status().isNotFound());
        postBooking(UNKNOWN_ID, itemId, now.plusDays(1), now.plusDays(2))
                .andExpect(status().isNotFound());

        long approved = createBooking(booker, itemId, now.plusDays(3), now.plusDays(5));
        patchAs("/bookings/" + approved + "?approved=true", owner).andExpect(status().isOk());
        postBooking(booker, itemId, now.plusDays(4), now.plusDays(6))
                .andExpect(status().is4xxClientError());
    }

    // ---------- комментарии ----------

    @Test
    void commentsFlow() throws Exception {
        long owner = createUser();
        long booker = createUser();
        long stranger = createUser();
        long itemId = createItem(owner, true, null);
        LocalDateTime now = LocalDateTime.now();

        postJson("/items/" + itemId + "/comment", booker, "{\"text\":\"too early\"}")
                .andExpect(status().is4xxClientError());

        Booking past = new Booking();
        past.setStart(now.minusDays(3));
        past.setEnd(now.minusDays(2));
        past.setStatus(BookingStatus.APPROVED);
        past.setItem(itemRepository.findById(itemId).orElseThrow());
        past.setBooker(userRepository.findById(booker).orElseThrow());
        bookingRepository.save(past);

        long next = createBooking(booker, itemId, now.plusDays(1), now.plusDays(2));
        patchAs("/bookings/" + next + "?approved=true", owner).andExpect(status().isOk());

        postJson("/items/" + itemId + "/comment", booker, "{\"text\":\"great\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("great"));
        postJson("/items/" + itemId + "/comment", stranger, "{\"text\":\"fake\"}")
                .andExpect(status().is4xxClientError());
        postJson("/items/" + UNKNOWN_ID + "/comment", booker, "{\"text\":\"x\"}")
                .andExpect(status().isNotFound());
        postJson("/items/" + itemId + "/comment", UNKNOWN_ID, "{\"text\":\"x\"}")
                .andExpect(status().isNotFound());

        getAs("/items/" + itemId, owner)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.lastBooking").exists())
                .andExpect(jsonPath("$.nextBooking").exists());
        getAs("/items", owner).andExpect(status().isOk());
        getAs("/items/" + itemId, booker).andExpect(status().isOk());
    }

    // ---------- запросы вещей ----------

    @Test
    void requestsFlow() throws Exception {
        long requestor = createUser();
        long helper = createUser();

        String response = postJson("/requests", requestor, "{\"description\":\"need a drill\"}")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long requestId = objectMapper.readTree(response).get("id").asLong();

        createItem(helper, true, requestId);

        getAs("/requests", requestor)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].items.length()").value(1));
        getAs("/requests/all", helper).andExpect(status().isOk());
        getAs("/requests/" + requestId, helper)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));
        getAs("/requests/" + UNKNOWN_ID, requestor).andExpect(status().isNotFound());
        getAs("/requests", UNKNOWN_ID).andExpect(status().isNotFound());
        getAs("/requests/all", UNKNOWN_ID).andExpect(status().isNotFound());
        postJson("/requests", UNKNOWN_ID, "{\"description\":\"x\"}")
                .andExpect(status().isNotFound());
        postJson("/items", helper,
                "{\"name\":\"n\",\"description\":\"d\",\"available\":true,\"requestId\":" + UNKNOWN_ID + "}")
                .andExpect(status().isNotFound());
    }

    // ---------- вспомогательные методы ----------

    private long createUser() throws Exception {
        return createUserWithEmail("user" + COUNTER.incrementAndGet() + "@mail.com");
    }

    private long createUserWithEmail(String email) throws Exception {
        String body = "{\"name\":\"user\",\"email\":\"" + email + "\"}";
        return idOf(postJson("/users", null, body).andExpect(status().isOk()));
    }

    private long createItem(long ownerId, boolean available, Long requestId) throws Exception {
        String body = "{\"name\":\"drill" + COUNTER.incrementAndGet() + "\","
                + "\"description\":\"powerful drill\",\"available\":" + available
                + (requestId != null ? ",\"requestId\":" + requestId : "") + "}";
        return idOf(postJson("/items", ownerId, body).andExpect(status().isOk()));
    }

    private ResultActions postBooking(long bookerId, long itemId, LocalDateTime start, LocalDateTime end)
            throws Exception {
        String body = "{\"itemId\":" + itemId + ",\"start\":\"" + start + "\",\"end\":\"" + end + "\"}";
        return postJson("/bookings", bookerId, body);
    }

    private long createBooking(long bookerId, long itemId, LocalDateTime start, LocalDateTime end)
            throws Exception {
        return idOf(postBooking(bookerId, itemId, start, end).andExpect(status().isOk()));
    }

    private long idOf(ResultActions result) throws Exception {
        String json = result.andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    private ResultActions postJson(String url, Long userId, String body) throws Exception {
        MockHttpServletRequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON).content(body);
        if (userId != null) {
            request.header(HEADER, userId);
        }
        return mockMvc.perform(request);
    }

    private ResultActions patchJson(String url, Long userId, String body) throws Exception {
        MockHttpServletRequestBuilder request = patch(url)
                .contentType(MediaType.APPLICATION_JSON).content(body);
        if (userId != null) {
            request.header(HEADER, userId);
        }
        return mockMvc.perform(request);
    }

    private ResultActions patchAs(String url, Long userId) throws Exception {
        return mockMvc.perform(patch(url).header(HEADER, userId));
    }

    private ResultActions getAs(String url, Long userId) throws Exception {
        MockHttpServletRequestBuilder request = get(url);
        if (userId != null) {
            request.header(HEADER, userId);
        }
        return mockMvc.perform(request);
    }
}