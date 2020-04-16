package org.example;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("/testJdbcCrudContext.xml")
public class TestCrudJooq extends AbstractTestCrud {

}
