package org.gym.crm.dao.impl;

import lombok.RequiredArgsConstructor;
import org.gym.crm.config.TransactionManager;
import org.gym.crm.dao.TraineeDao;
import org.gym.crm.model.Trainee;
import org.gym.crm.util.Validator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TraineeDaoImpl implements TraineeDao {
    private final TransactionManager transactionManager;

    public Trainee save(Trainee trainee) {
        Validator.validateNotNull(trainee, "Trainee");

        transactionManager.performWithinTx(manager -> manager.persist(trainee));

        return trainee;
    }

    public Trainee update(Trainee trainee) {
        Validator.validateId(trainee.getId());

        transactionManager.performWithinTx(manager -> manager.merge(trainee));

        return trainee;
    }

    public void delete(Long id) {
        Validator.validateId(id);

        Trainee trainee = transactionManager.performReturningWithinTx(manager -> manager.find(Trainee.class, id));
        if (trainee != null) {
            transactionManager.performWithinTx(manager -> manager.remove(trainee));
        }
    }

    public Optional<Trainee> findById(Long id) {
        Validator.validateId(id);

        return transactionManager.performReturningWithinTx(manager ->
                Optional.ofNullable(manager.find(Trainee.class, id)));
    }

    public List<Trainee> findAll() {
        return transactionManager.performReturningWithinTx(manager -> manager
                .createQuery("from Trainee", Trainee.class)
                .getResultList()
        );
    }
}
