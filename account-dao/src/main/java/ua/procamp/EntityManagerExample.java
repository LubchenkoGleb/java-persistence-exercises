package ua.procamp;

import ua.procamp.model.Account;
import ua.procamp.util.TestDataGenerator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerExample {
    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("SingleAccountEntityH2");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Account account = TestDataGenerator.generateAccount();
            System.out.println(account);
            entityManager.persist(account);
            System.out.println(account);

            Account foundAccount = entityManager.find(Account.class, account.getId());
            System.out.println(foundAccount);

            entityManager.remove(account);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            entityManager.close();
        }
        entityManagerFactory.close();
    }
}
