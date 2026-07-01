package edu.hitsz.DAO;

import java.io.IOException;
import java.util.List;

public interface UserDao {
    void addData(User user) throws IOException, ClassNotFoundException;

    void deleteData(int num);

    void storage(List<User> userList) throws IOException;

    List<User> getAllData() throws IOException;
}
