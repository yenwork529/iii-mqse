package org.iii.esd;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
public class ApplicationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void testLoadContext(){
        Assertions.assertThat(context).isNotNull();

        Application application = context.getBean(Application.class);
        Assertions.assertThat(application).isNotNull();
    }
}
