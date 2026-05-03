package org.gym.crm.dao;

import jakarta.transaction.Transactional;
import org.gym.crm.config.TestAppConfig;
import org.gym.crm.dao.impl.TraineeDaoImpl;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestAppConfig.class})
@ActiveProfiles("test")
@Transactional
public class TraineeDAOImplTest {
    @Autowired
    private TraineeDaoImpl dao;

    @Test
    void save_shouldGenerateId_afterPersist() {
        Trainee saved = dao.save(buildTrainee());

        assertNotNull(saved.getUserId());
    }

    @Test
    void save_shouldMakeTraineeFindable_afterPersist() {
        Trainee saved = dao.save(buildTrainee());

        assertTrue(dao.findById(saved.getUserId()).isPresent());
    }

    @Test
    void findById_shouldReturnCorrectlyMappedFields() {
        Trainee saved = dao.save(buildTrainee());

        Trainee found = dao.findById(saved.getUserId()).orElseThrow();

        assertEquals(LocalDate.of(2000, 1, 1), found.getDateOfBirth());
        assertEquals("123 Main St", found.getAddress());
        assertNotNull(found.getUser());
        assertEquals("Callum", found.getUser().getFirstName());
        assertEquals("Whitfield", found.getUser().getLastName());
        assertEquals(saved.getUser().getUsername(), found.getUser().getUsername());
        assertEquals("password", found.getUser().getPassword());
        assertTrue(found.getUser().getIsActive());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<Trainee> found = dao.findById(999L);

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllPersistedTrainees() {
        dao.save(buildTrainee());
        dao.save(buildTrainee("Jane", "Foster", "Jane.Foster"));

        List<Trainee> actual = dao.findAll();

        assertEquals(2, actual.size());
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoneExist() {
        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    void update_shouldPersistChanges_whenReloaded() {
        Trainee saved = dao.save(buildTrainee());

        dao.update(saved.toBuilder().address("New Address").build());

        Trainee actual = dao.findById(saved.getUserId()).orElseThrow();
        assertEquals("New Address", actual.getAddress());
    }

    @Test
    void update_shouldNotAffectOtherFields() {
        Trainee saved = dao.save(buildTrainee());
        String expectedUsername = saved.getUser().getUsername();

        dao.update(saved.toBuilder().address("New Address").build());

        Trainee actual = dao.findById(saved.getUserId()).orElseThrow();
        assertEquals(LocalDate.of(2000, 1, 1), actual.getDateOfBirth());
        assertEquals(expectedUsername, actual.getUser().getUsername());
    }

    @Test
    void delete_shouldRemoveTrainee_fromDatabase() {
        Trainee saved = dao.save(buildTrainee());
        Long id = saved.getUserId();

        dao.delete(id);

        assertTrue(dao.findById(id).isEmpty());
    }

    @Test
    void delete_shouldThrowException_whenNotExists() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> dao.delete(999L)
        );

        assertEquals("Trainee not found with id: 999", ex.getMessage());
    }

    @Test
    void delete_shouldNotAffectOtherTrainees() {
        Trainee first = dao.save(buildTrainee());
        Trainee second = dao.save(buildTrainee("Jane", "Doe", "Jane.Doe"));

        dao.delete(first.getUserId());

        assertTrue(dao.findById(second.getUserId()).isPresent());
    }

    private Trainee buildTrainee() {
        return buildTrainee("Callum", "Whitfield", "Callum.Whitfield");
    }

    private Trainee buildTrainee(String firstName, String lastName, String username) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username + System.nanoTime())
                .password("password")
                .isActive(true)
                .build();

        return Trainee.builder()
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("123 Main St")
                .user(user)
                .build();
    }
}
