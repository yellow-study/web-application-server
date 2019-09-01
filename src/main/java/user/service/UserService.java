package user.service;

import db.DataBase;
import model.User;

import java.util.Collection;
import java.util.Map;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 1.
 */
public class UserService {
    public void addUser(Map<String, String> params) {
        User user = User.builder()
                .userId(params.get("userId"))
                .password(params.get("password"))
                .name(params.get("name"))
                .email(params.get("email")).build();
        DataBase.addUser(user);
    }

    public User findUserById(Map<String, String> params) {
        String userId = params.get("userId");
        if(userId == null) {
            return null;
        }
        return DataBase.findUserById(userId);
    }

    public Collection<User> findAll() {
        return DataBase.findAll();
    }

}
