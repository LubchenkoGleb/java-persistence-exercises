package ua.procamp.dao;

import ua.procamp.exception.AccountDaoException;
import ua.procamp.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void save(Account account) {
        executeVoid((entityManager) -> entityManager.persist(account));
    }

    @Override
    public Account findById(Long id) {
        return executeWithReturn(entityManager -> entityManager.find(Account.class, id));
    }

    @Override
    public Account findByEmail(String email) {
        return executeWithReturn(entityManager -> entityManager
                .createQuery("select a from Account a where a.email = :email", Account.class)
                .setParameter("email", email)
                .getSingleResult());
    }

    @Override
    public List<Account> findAll() {
        return executeWithReturn(entityManager -> entityManager
                .createQuery("select a from Account a", Account.class)
                .getResultList());
    }

    @Override
    public void update(Account account) {
        executeVoid(entityManager -> {
            entityManager.merge(account);
        });
    }

    @Override
    public void remove(Account account) {
        executeVoid(entityManager -> {
            Account merged = entityManager.merge(account);
            entityManager.remove(merged);
        });
    }

    private void executeVoid(Consumer<EntityManager> entityManagerConsumer) {
        EntityManager entityManager = emf.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManagerConsumer.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException(e.getMessage(), e);
        } finally {
            entityManager.close();
        }
    }

    private <T> T executeWithReturn(Function<EntityManager, T> entityManagerFunction) {
        EntityManager entityManager = emf.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            return entityManagerFunction.apply(entityManager);
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException(e.getMessage(), e);
        } finally {
            entityManager.close();
        }
    }
}

