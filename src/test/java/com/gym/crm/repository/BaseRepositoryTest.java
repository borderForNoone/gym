package com.gym.crm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
abstract class BaseRepositoryTest {
    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void cleanDatabase() throws Exception {
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {

            st.execute("SET REFERENTIAL_INTEGRITY FALSE");

            List<String> tables = new ArrayList<>();
            try (ResultSet rs = con.getMetaData().getTables(
                    null, "PUBLIC", "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String name = rs.getString("TABLE_NAME");
                    if (!name.startsWith("DATABASECHANGELOG")) {
                        tables.add(name);
                    }
                }
            }

            for (String table : tables) {
                st.execute("TRUNCATE TABLE \"" + table + "\"");
            }

            st.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }
}