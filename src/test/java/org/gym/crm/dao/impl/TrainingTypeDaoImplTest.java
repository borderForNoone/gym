package org.gym.crm.dao.impl;

import org.gym.crm.model.TrainingType;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeDaoImplTest {
    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    @SuppressWarnings("rawtypes")
    private Query query;

    @InjectMocks
    private TrainingTypeDaoImpl trainingTypeDao;

    private void stubSession() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTrainingTypeName_existing_returnsOptional() {
        stubSession();

        TrainingType type = mock(TrainingType.class);

        when(session.createQuery(anyString(), eq(TrainingType.class))).thenReturn(query);
        when(query.setParameter(eq("name"), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(type));

        Optional<TrainingType> result = trainingTypeDao.findByTrainingTypeName("Strength");

        assertThat(result).isPresent().contains(type);

        verify(session).createQuery(anyString(), eq(TrainingType.class));
        verify(query).setParameter("name", "Strength");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTrainingTypeName_notFound_returnsEmpty() {
        stubSession();

        when(session.createQuery(anyString(), eq(TrainingType.class))).thenReturn(query);
        when(query.setParameter(eq("name"), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        Optional<TrainingType> result = trainingTypeDao.findByTrainingTypeName("Unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void findByTrainingTypeName_blank_throwsException() {
        assertThatThrownBy(() -> trainingTypeDao.findByTrainingTypeName(" ")).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(sessionFactory, session);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_returnsList() {
        stubSession();

        TrainingType type1 = mock(TrainingType.class);
        TrainingType type2 = mock(TrainingType.class);

        when(session.createQuery(anyString(), eq(TrainingType.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(type1, type2));

        List<TrainingType> result = trainingTypeDao.findAll();

        assertThat(result).containsExactly(type1, type2);
        verify(session).createQuery("FROM TrainingType", TrainingType.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_empty_returnsEmptyList() {
        stubSession();

        when(session.createQuery(anyString(), eq(TrainingType.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        List<TrainingType> result = trainingTypeDao.findAll();

        assertThat(result).isEmpty();
    }
}