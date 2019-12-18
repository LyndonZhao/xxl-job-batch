package pn.lyndon.batch.tasklet;

import java.util.List;

import javax.sql.DataSource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import pn.lyndon.batch.user.dao.UserDao;
import pn.lyndon.batch.user.entity.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyTasklet implements Tasklet {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserDao userDao;

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        User user = new User();
        user.setId(1);
        List<User> users = userDao.selectUser(user);
        for (User user1 : users) {
            System.out.printf("MyTasklet.execute get user data: %s", user1);
        }
        return RepeatStatus.FINISHED;
    }

}
