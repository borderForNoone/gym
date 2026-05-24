package com.gym.crm.dao.impl;

import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerDaoImplTest {
    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    @SuppressWarnings("rawtypes")
    private Query query;

    @InjectMocks
    private TrainerDaoImpl trainerDao;

    private void stubSession() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void save_validTrainer_persistsAndReturns() {
        stubSession();
        Trainer trainer = buildTrainer(1L, "trainer.one");
        Trainer result = trainerDao.save(trainer);

        verify(session).persist(trainer);
        assertThat(result).isSameAs(trainer);
    }

    @Test
    void save_nullTrainer_throwsException() {
        assertThatThrownBy(() -> trainerDao.save(null)).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(sessionFactory, session);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByUsername_existing_returnsTrainer() {
        stubSession();
        Trainer trainer = buildTrainer(1L, "trainer.one");

        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(trainer));

        Optional<Trainer> result = trainerDao.findByUsername("trainer.one");

        assertThat(result).isPresent().contains(trainer);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByUsername_notExisting_returnsEmpty() {
        stubSession();

        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.empty());

        Optional<Trainer> result = trainerDao.findByUsername("ghost");

        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_blank_throwsException() {
        assertThatThrownBy(() -> trainerDao.findByUsername("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findNotAssignedToTrainee_valid_returnsList() {
        stubSession();

        Trainer trainer = buildTrainer(1L, "trainer.one");

        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(trainer));

        List<Trainer> result = trainerDao.findNotAssignedToTrainee("trainee.one");

        assertThat(result).containsExactly(trainer);
    }

    @Test
    void findNotAssignedToTrainee_blank_throwsException() {
        assertThatThrownBy(() -> trainerDao.findNotAssignedToTrainee(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void existsByUsername_true_returnsTrue() {
        stubSession();

        when(session.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1L);

        assertThat(trainerDao.existsByUsername("trainer.one")).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void existsByUsername_false_returnsFalse() {
        stubSession();

        when(session.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(0L);

        assertThat(trainerDao.existsByUsername("ghost")).isFalse();
    }

    @Test
    void existsByUsername_blank_throwsException() {
        assertThatThrownBy(() -> trainerDao.existsByUsername("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_valid_mergesAndReturns() {
        stubSession();
        Trainer trainer = buildTrainer(1L, "trainer.one");

        when(session.merge(trainer)).thenReturn(trainer);

        Trainer result = trainerDao.update(trainer);

        verify(session).merge(trainer);
        assertThat(result).isSameAs(trainer);
    }

    @Test
    void update_nullId_throwsException() {
        Trainer trainer = buildTrainer(null, "trainer.one");

        assertThatThrownBy(() -> trainerDao.update(trainer)).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(sessionFactory, session);
    }

    private Trainer buildTrainer(Long id, String username) {
        Trainer trainer = Trainer.builder().user(User.builder().username(username).build()).build();

        setId(trainer, id);

        return trainer;
    }

    private void setId(Object entity, Long id) {
        if (id == null) return;

        Class<?> entityClass = entity.getClass();

        while (entityClass != null) {
            try {
                var field = entityClass.getDeclaredField("id");
                field.setAccessible(true);
                field.set(entity, id);
                return;
            } catch (NoSuchFieldException e) {
                entityClass = entityClass.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        throw new RuntimeException("No id field found");
    }
}