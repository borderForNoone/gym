package org.gym.crm.dao.impl;

import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.User;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeDaoImplTest {
    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    @SuppressWarnings("rawtypes")
    private Query query;

    @InjectMocks
    private TraineeDaoImpl traineeDao;

    private void stubSession() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void save_validTrainee_persistsAndReturns() {
        stubSession();
        Trainee trainee = buildTrainee(1L, "tom.tomas");

        Trainee result = traineeDao.save(trainee);

        verify(session).persist(trainee);
        assertThat(result).isSameAs(trainee);
    }

    @Test
    void save_nullTrainee_throwsException() {
        assertThatThrownBy(() -> traineeDao.save(null)).isInstanceOf(IllegalArgumentException.class);
        verify(session, never()).persist(any());
    }

    @Test
    void update_validTrainee_mergesAndReturns() {
        stubSession();
        Trainee trainee = buildTrainee(1L, "tom.tomas");
        when(session.merge(trainee)).thenReturn(trainee);

        Trainee result = traineeDao.update(trainee);

        verify(session).merge(trainee);
        assertThat(result).isSameAs(trainee);
    }

    @Test
    void update_traineeWithNullId_throwsException() {
        Trainee trainee = buildTrainee(null, "tom.tomas");

        assertThatThrownBy(() -> traineeDao.update(trainee)).isInstanceOf(IllegalArgumentException.class);
        verify(session, never()).merge(any());
    }

    @Test
    void delete_managedTrainee_removesDirectly() {
        stubSession();
        Trainee trainee = buildTrainee(1L, "tom.tomas");
        when(session.contains(trainee)).thenReturn(true);

        traineeDao.delete(trainee);

        verify(session, never()).merge(any());
        verify(session).remove(trainee);
    }

    @Test
    void delete_detachedTrainee_mergesThenRemoves() {
        stubSession();
        Trainee trainee = buildTrainee(1L, "tom.tomas");
        Trainee managed = buildTrainee(1L, "tom.tomas");
        when(session.contains(trainee)).thenReturn(false);
        when(session.merge(trainee)).thenReturn(managed);

        traineeDao.delete(trainee);

        verify(session).merge(trainee);
        verify(session).remove(managed);
    }

    @Test
    void delete_nullTrainee_throwsException() {
        assertThatThrownBy(() -> traineeDao.delete(null)).isInstanceOf(IllegalArgumentException.class);
        verify(session, never()).remove(any());
    }

    @Test
    void delete_traineeWithNullId_throwsException() {
        Trainee trainee = buildTrainee(null, "tom.tomas");

        assertThatThrownBy(() -> traineeDao.delete(trainee)).isInstanceOf(IllegalArgumentException.class);
        verify(session, never()).remove(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByUsername_existingUsername_returnsTrainee() {
        stubSession();
        Trainee trainee = buildTrainee(1L, "tom.tomas");
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(trainee));

        Optional<Trainee> result = traineeDao.findByUsername("tom.tomas");

        assertThat(result).isPresent().contains(trainee);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByUsername_nonExistingUsername_returnsEmpty() {
        stubSession();
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.empty());

        Optional<Trainee> result = traineeDao.findByUsername("ghost");

        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_blankUsername_throwsException() {
        assertThatThrownBy(() -> traineeDao.findByUsername("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findByUsername_nullUsername_throwsException() {
        assertThatThrownBy(() -> traineeDao.findByUsername(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void existsByUsername_exists_returnsTrue() {
        stubSession();
        when(session.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1L);

        assertThat(traineeDao.existsByUsername("tom.tomas")).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void existsByUsername_notExists_returnsFalse() {
        stubSession();
        when(session.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(0L);

        assertThat(traineeDao.existsByUsername("ghost")).isFalse();
    }

    @Test
    void existsByUsername_blankUsername_throwsException() {
        assertThatThrownBy(() -> traineeDao.existsByUsername("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void existsByUsername_nullUsername_throwsException() {
        assertThatThrownBy(() -> traineeDao.existsByUsername(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteByUsername_existingUser_removesTrainee() {
        stubSession();
        Trainee trainee = buildTrainee(1L, "tom.tomas");
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(trainee));

        traineeDao.deleteByUsername("tom.tomas");

        verify(session).remove(trainee);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteByUsername_nonExistingUser_doesNothing() {
        stubSession();
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.empty());

        traineeDao.deleteByUsername("ghost");

        verify(session, never()).remove(any());
    }

    @Test
    void deleteByUsername_blankUsername_throwsException() {
        assertThatThrownBy(() -> traineeDao.deleteByUsername("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findUnassignedTrainers_validUsername_returnsList() {
        stubSession();
        Trainer trainer = buildTrainer(10L, "trainer.one");
        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(trainer));

        List<Trainer> result = traineeDao.findUnassignedTrainers("tom.tomas");

        assertThat(result).containsExactly(trainer);
    }

    @Test
    void findUnassignedTrainers_blankUsername_throwsException() {
        assertThatThrownBy(() -> traineeDao.findUnassignedTrainers("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateTrainersList_validData_updatesCollection() {
        stubSession();
        Trainer trainerArg = buildTrainer(10L, "trainer.one");
        Trainer managedTrainer = buildTrainer(10L, "trainer.one");
        Trainee trainee = buildTraineeWithTrainer(1L, "tom.tomas", buildTrainer(99L, "old.trainer"));

        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(trainee));
        when(session.find(Trainer.class, 10L)).thenReturn(managedTrainer);

        traineeDao.updateTrainersList("tom.tomas", List.of(trainerArg));

        assertThat(trainee.getTrainers()).containsExactly(managedTrainer);
        verify(session).merge(trainee);
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateTrainersList_trainerNotInDb_skipsNullTrainers() {
        stubSession();
        Trainer trainerArg = buildTrainer(10L, "trainer.one");
        Trainee trainee = buildTrainee(1L, "tom.tomas");

        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(trainee));
        when(session.find(Trainer.class, 10L)).thenReturn(null);

        traineeDao.updateTrainersList("tom.tomas", List.of(trainerArg));

        assertThat(trainee.getTrainers()).isEmpty();
        verify(session).merge(trainee);
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateTrainersList_traineeNotFound_throwsException() {
        stubSession();
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.empty());

        assertThatThrownBy(() -> traineeDao.updateTrainersList("ghost", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    void updateTrainersList_blankUsername_throwsException() {
        assertThatThrownBy(() -> traineeDao.updateTrainersList("  ", List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateTrainersList_nullTrainers_throwsException() {
        assertThatThrownBy(() -> traineeDao.updateTrainersList("tom.tomas", null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAllByUsernames_validList_returnsTrainers() {
        stubSession();
        Trainer trainer = buildTrainer(10L, "trainer.one");
        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(trainer));

        List<Trainer> result = traineeDao.findAllByUsernames(List.of("trainer.one"));

        assertThat(result).containsExactly(trainer);
    }

    @Test
    void findAllByUsernames_emptyList_returnsEmptyWithoutQuery() {
        List<Trainer> result = traineeDao.findAllByUsernames(List.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(sessionFactory, session);
    }

    @Test
    void findAllByUsernames_nullList_throwsException() {
        assertThatThrownBy(() -> traineeDao.findAllByUsernames(null)).isInstanceOf(IllegalArgumentException.class);
    }

    private User buildUser(String username) {
        return User.builder().username(username).build();
    }

    private Trainee buildTrainee(Long id, String username) {
        Trainee trainee = Trainee.builder().user(buildUser(username)).build();
        setId(trainee, id);

        return trainee;
    }

    private Trainee buildTraineeWithTrainer(Long id, String username, Trainer trainer) {
        Trainee trainee = buildTrainee(id, username);
        trainee.getTrainers().add(trainer);

        return trainee;
    }

    private Trainer buildTrainer(Long id, String username) {
        Trainer trainer = Trainer.builder().user(buildUser(username)).build();
        setId(trainer, id);

        return trainer;
    }

    private void setId(Object entity, Long id) {
        if (id == null) {
            return;
        }
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
                throw new RuntimeException("Cannot set id on " + entity.getClass(), e);
            }
        }
        throw new RuntimeException("Field 'id' not found in hierarchy of " + entity.getClass());
    }
}