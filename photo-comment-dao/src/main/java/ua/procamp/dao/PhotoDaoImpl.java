package ua.procamp.dao;

import ua.procamp.exception.PhotoCommentDaoException;
import ua.procamp.model.Photo;
import ua.procamp.model.PhotoComment;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Please note that you should not use auto-commit mode for your implementation.
 */
public class PhotoDaoImpl implements PhotoDao {
    private EntityManagerFactory entityManagerFactory;

    public PhotoDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void save(Photo photo) {
        voidEntityManagerHelper(entityManager -> entityManager.persist(photo));
    }

    @Override
    public Photo findById(long id) {
        return returningEntityManagerHelper(entityManager -> entityManager.find(Photo.class, id));
    }

    @Override
    public List<Photo> findAll() {
        return returningEntityManagerHelper(entityManager -> entityManager
                .createQuery("select p from Photo p", Photo.class).getResultList());
    }

    @Override
    public void remove(Photo photo) {
        voidEntityManagerHelper(entityManager -> {
            Photo mergedPhoto = entityManager.merge(photo);
            entityManager.remove(mergedPhoto);
        });
    }

    @Override
    public void addComment(long photoId, String comment) {
        voidEntityManagerHelper(entityManager -> {
            Photo photo = entityManager.find(Photo.class, photoId);
            PhotoComment photoComment = new PhotoComment(comment);
            photo.addComment(photoComment);
        });
    }

    private void voidEntityManagerHelper(Consumer<EntityManager> entityManagerConsumer) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            entityManagerConsumer.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new PhotoCommentDaoException("Unable to perform query", e);
        } finally {
            entityManager.close();
        }
    }

    private <T> T returningEntityManagerHelper(Function<EntityManager, T> entityManagerConsumer) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            T result = entityManagerConsumer.apply(entityManager);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new PhotoCommentDaoException("Unable to perform query", e);
        } finally {
            entityManager.close();
        }
    }
}
