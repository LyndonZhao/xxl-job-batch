package pn.lyndon.batch.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import pn.lyndon.batch.user.entity.User;

/**
 * Description:
 *
 * @author Administrator
 * @create 2019-12-01 12:29
 */
public class UserRowMapper  implements RowMapper<User> {

    /**
     * rs一条结果集，rowNum代表当前行
     */
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("age"));
    }

}