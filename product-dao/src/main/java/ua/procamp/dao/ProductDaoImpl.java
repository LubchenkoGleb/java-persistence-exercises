package ua.procamp.dao;

import ua.procamp.exception.DaoOperationException;
import ua.procamp.model.Product;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

public class ProductDaoImpl implements ProductDao {

    private static final String INSERT_QUERY = "INSERT INTO products(" +
            "name," +
            "producer," +
            "price," +
            "expiration_date) VALUES (?, ?, ?, ?);";

    private static final String FIND_ALL_QUERY = "SELECT * FROM products;";

    private static final String FIND_BY_ID = "SELECT * FROM products WHERE id = ?";

    private static final String UPDATE_QUERY = "UPDATE products SET " +
            "name = ?, " +
            "producer = ?, " +
            "price = ?, " +
            "expiration_date = ?" +
            "WHERE id = ?";

    private static final String DELETE_QUERY = "DELETE FROM products WHERE id = ?;";

    private DataSource dataSource;

    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {

        validateProductForSave(product);

        try (Connection connection = dataSource.getConnection()) {
            executeInsert(connection, product);
        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage(), e);
        }
    }

    private void validateProductForSave(Product product) {
        if (isNull(product) || isNull(product.getProducer())) {
            throw new DaoOperationException("Error saving product: " + product);
        }
    }

    private void executeInsert(Connection connection, Product product) throws SQLException {

        PreparedStatement statement = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
        setProductParams(product, statement);
        int updatedAmount = statement.executeUpdate();
        if (updatedAmount == 0) {
            throw new SQLException("Unable to insert to table");
        }

        ResultSet generatedKeys = statement.getGeneratedKeys();
        generatedKeys.next();

        product.setId(generatedKeys.getLong(1));
    }

    private void setProductParams(Product product, PreparedStatement statement) throws SQLException {
        statement.setString(1, product.getName());
        statement.setString(2, product.getProducer());
        statement.setBigDecimal(3, product.getPrice());
        statement.setDate(4, Date.valueOf(product.getExpirationDate()));
    }

    @Override
    public List<Product> findAll() {
        try (Connection connection = dataSource.getConnection()) {
            return executeFindAll(connection);
        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage());
        }
    }

    private List<Product> executeFindAll(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(FIND_ALL_QUERY);
        return this.parseResultSet(resultSet);
    }

    @Override
    public Product findOne(Long id) {
        try (Connection connection = dataSource.getConnection()) {
            return executeFindOne(connection, id);
        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage());
        }
    }

    private Product executeFindOne(Connection connection, Long id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(FIND_BY_ID);
        statement.setLong(1, id);
        ResultSet resultSet = statement.executeQuery();
        return this.parseResultSet(resultSet).stream().findAny()
                .orElseThrow(() -> new DaoOperationException(String.format("Product with id = %d does not exist", id)));
    }

    @Override
    public void update(Product product) {

        validateProduct(product);

        try (Connection connection = dataSource.getConnection()) {
            executeUpdate(connection, product);
        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage());
        }
    }

    private void validateProduct(Product product) {
        if (isNull(product)) {
            throw new DaoOperationException("Product cannot be null");
        } else if (isNull(product.getId())) {
            throw new DaoOperationException("Product id cannot be null");
        }
    }

    private void executeUpdate(Connection connection, Product product) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_QUERY);
        setProductParams(product, preparedStatement);
        preparedStatement.setLong(5, product.getId());
        int updatedRows = preparedStatement.executeUpdate();
        if (updatedRows == 0) {
            throw new DaoOperationException(String.format("Product with id = %d does not exist", product.getId()));
        }
    }

    @Override
    public void remove(Product product) {

        validateProduct(product);

        try (Connection connection = dataSource.getConnection()) {
            executeDelete(connection, product);
        } catch (SQLException e) {
            throw new DaoOperationException(e.getMessage());
        }
    }

    private void executeDelete(Connection connection, Product product) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(DELETE_QUERY);
        preparedStatement.setLong(1, product.getId());
        int deletedRows = preparedStatement.executeUpdate();
        if (deletedRows == 0) {
            throw new DaoOperationException(String.format("Product with id = %d does not exist", product.getId()));
        }
    }

    private List<Product> parseResultSet(ResultSet resultSet) throws SQLException {
        List<Product> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(new Product(
                    resultSet.getLong(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getBigDecimal(4),
                    resultSet.getDate(5).toLocalDate(),
                    resultSet.getTimestamp(6).toLocalDateTime()
            ));
        }
        return result;
    }

}
