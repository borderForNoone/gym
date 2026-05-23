package org.gym.crm.dao.impl;

import org.gym.crm.model.Training;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.gym.crm.search.filter.TrainerTrainingFilter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingDaoImplTest {
    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    @SuppressWarnings("rawtypes")
    private Query query;

    @InjectMocks
    private TrainingDaoImpl trainingDao;

    private void stubSession() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void save_validTraining_persistsAndReturns() {
        stubSession();
        Training training = mock(Training.class);

        Training result = trainingDao.save(training);

        verify(session).persist(training);
        assertThat(result).isSameAs(training);
    }

    @Test
    void save_nullTraining_throwsException() {
        assertThatThrownBy(() -> trainingDao.save(null)).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(sessionFactory, session);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTraineeCriteria_validFilter_returnsList() {
        stubSession();
        Training training = mock(Training.class);
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder()
                .username("trainee.one")
                .fromDate(LocalDate.of(2024, 1, 1))
                .toDate(LocalDate.of(2024, 12, 31))
                .build();

        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(training));

        List<Training> result = trainingDao.findByTraineeCriteria(filter);

        assertThat(result).containsExactly(training);
        verify(session).createQuery(anyString(), eq(Training.class));
        verify(query).setParameter("username", "trainee.one");
        verify(query).setParameter("fromDate", filter.getFromDate());
        verify(query).setParameter("toDate", filter.getToDate());
    }

    @Test
    void findByTraineeCriteria_nullFilter_throwsException() {
        assertThatThrownBy(() -> trainingDao.findByTraineeCriteria(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTrainerCriteria_validFilter_returnsList() {
        stubSession();
        Training training = mock(Training.class);
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder()
                .username("trainer.one")
                .fromDate(LocalDate.of(2024, 1, 1))
                .toDate(LocalDate.of(2024, 12, 31))
                .build();

        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(training));

        List<Training> result = trainingDao.findByTrainerCriteria(filter);

        assertThat(result).containsExactly(training);
        verify(session).createQuery(anyString(), eq(Training.class));
        verify(query).setParameter("username", "trainer.one");
        verify(query).setParameter("fromDate", filter.getFromDate());
        verify(query).setParameter("toDate", filter.getToDate());
    }

    @Test
    void findByTrainerCriteria_nullFilter_throwsException() {
        assertThatThrownBy(() -> trainingDao.findByTrainerCriteria(null)).isInstanceOf(IllegalArgumentException.class);
    }
}