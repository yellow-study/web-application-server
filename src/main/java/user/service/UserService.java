package user.service;

import db.DataBase;
import model.User;

import java.util.Collection;
import java.util.Map;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 1.
 */
public class UserService {
    public void addUser(User user) {
        DataBase.addUser(user);
    }

    public User findUserById(String userId) {
        if(userId == null) {
            return null;
        }
        return DataBase.findUserById(userId);
    }

    public Collection<User> findAll() {
        return DataBase.findAll();
    }

    public String getUserListView(Collection<User> users) {
        StringBuilder userTable = new StringBuilder("<table border='1'>");
        userTable.append("<thead>" +
                "<tr>" +
                "<th>" + "Id" + "</th>" +
                "<th>" + "Name" + "</th>" +
                "<th>" + "Email" + "</th>" +
                "</tr>" +
                "</thead></tbody>");
        for (User user : users) {
            userTable.append(
                    "<tr>" +
                            "<td>" + user.getUserId() + "</td>" +
                            "<td>" + user.getName() + "</td>" +
                            "<td>" + user.getPassword() + "</td>" +
                            "</tr>"
            );
        }
        userTable.append("</tbody></table>");

        return userTable.toString();
    }

}
