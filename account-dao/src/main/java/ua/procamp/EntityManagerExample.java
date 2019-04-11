package ua.procamp;

import ua.procamp.model.Account;
import ua.procamp.model.Card;
import ua.procamp.util.EntityManagerUtil;
import ua.procamp.util.TestDataGenerator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerExample {

    public static void main(String[] args) {

        EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("SingleAccountEntityPostgres");

        EntityManagerUtil entityManagerUtil = new EntityManagerUtil(entityManagerFactory);

        entityManagerUtil.performWithinTx(entityManager -> {
            Account account = TestDataGenerator.generateAccount();
            entityManager.persist(account);

            Card card1 = new Card();
            card1.setName("mono");
//            card1.setHolder(account);
            entityManager.persist(card1);


            Card card2 = new Card();
            card2.setName("privat");
//            card2.setHolder(account);
            entityManager.persist(card2);
        });

        entityManagerFactory.close();
    }
}
