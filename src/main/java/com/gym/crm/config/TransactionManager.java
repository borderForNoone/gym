package com.gym.crm.config;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.Consumer;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class TransactionManager {
    private final SessionFactory sessionFactory;

    public <T> T performReturningWithinTx(Function<Session, T> action) {
        Transaction transaction = null;
        Session session = sessionFactory.openSession();

        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));

        try {
            transaction = session.beginTransaction();
            T result = action.apply(session);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(sessionFactory);
            session.close();
        }
    }

    public void performWithinTx(Consumer<Session> action) {
        Transaction transaction = null;
        Session session = sessionFactory.openSession();

        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));

        try {
            transaction = session.beginTransaction();
            action.accept(session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(sessionFactory);
            session.close();
        }
    }
}