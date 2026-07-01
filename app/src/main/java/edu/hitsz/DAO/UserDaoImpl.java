package edu.hitsz.DAO;

import android.content.Context;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserDaoImpl implements UserDao {
    private static final Comparator<User> SCORE_DESC_COMPARATOR =
            (first, second) -> Integer.compare(second.getScore(), first.getScore());

    private final List<User> userList = new ArrayList<>();
    private final Context appContext;
    private final String fileName;

    public UserDaoImpl(Context context, String filename) {
        this.appContext = context.getApplicationContext();
        this.fileName = filename;
        userList.addAll(loadSavedUsers());
    }

    @SuppressWarnings("unchecked")
    private List<User> loadSavedUsers() {
        try (ObjectInputStream input =
                     new ObjectInputStream(appContext.openFileInput(fileName))) {
            Object savedObject = input.readObject();
            if (savedObject instanceof List<?>) {
                return new ArrayList<>((List<User>) savedObject);
            }
        } catch (FileNotFoundException ignored) {
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public void addData(User user) throws IOException, ClassNotFoundException {
        if (user == null) {
            return;
        }
        userList.add(user);
        storage(new ArrayList<>(userList));
    }

    public void storage(List<User> userList) throws IOException {
        List<User> sortedUsers = new ArrayList<>(userList);
        Collections.sort(sortedUsers, SCORE_DESC_COMPARATOR);
        this.userList.clear();
        this.userList.addAll(sortedUsers);
        try (ObjectOutputStream output =
                     new ObjectOutputStream(appContext.openFileOutput(fileName, Context.MODE_PRIVATE))) {
            output.writeObject(sortedUsers);
        }
    }

    @Override
    public void deleteData(int num) {
        if (num < 0 || num >= userList.size()) {
            return;
        }
        userList.remove(num);
        Collections.sort(userList, SCORE_DESC_COMPARATOR);
    }

    @Override
    public List<User> getAllData() {
        return new ArrayList<>(userList);
    }
}
