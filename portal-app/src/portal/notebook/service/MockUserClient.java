package portal.notebook.service;

import org.squonk.client.UserClient;
import org.squonk.core.user.User;

import javax.enterprise.inject.Alternative;
import java.io.Serializable;

/**
 * Created by timbo on 12/03/16.
 */
@Alternative
public class MockUserClient implements UserClient, Serializable {

    @Override
    public User getUser(String username) throws Exception {
        return new User(1l, username);
    }
}
