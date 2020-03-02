package com.app.blooddonation.models;

import com.app.blooddonation.models.User;

import java.util.ArrayList;
import java.util.List;

public class DemoUsers {
    List<User> demoUserList = new ArrayList<>();

    public DemoUsers() {
        User user = new User();
        /*user = new User("Ranveer","Singh","r_sing","+915689789545","500600","B+",12.839497, 77.650826);
        demoUserList.add(user);
        user = new User("Ranvijay","Singh","rn_sing","+915689789545","500600","AB+",12.839493, 77.660826);
        demoUserList.add(user);
        user = new User("Juhi","Chawla","j_c","+915689789545","500600","B-",12.839490, 77.650826);
        demoUserList.add(user);
        user = new User("Amitabh","Bachhan","a_b","+915689789545","500600","O+",12.839487, 77.650846);
        demoUserList.add(user);
        user = new User("Koena","Mitra","k_m","+915689789545","500600","O-",12.836497, 77.650726);*/
        demoUserList.add(user);
    }

    public List<User> getDemoUserList() {
        return demoUserList;
    }
}
