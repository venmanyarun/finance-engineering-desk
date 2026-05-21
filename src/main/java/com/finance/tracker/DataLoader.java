package com.finance.tracker;

import com.finance.tracker.domain.*;
import com.finance.tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    
    @Autowired 
    @Qualifier("authJdbcTemplate")
    private JdbcTemplate authJdbcTemplate;

    @Autowired
    @Qualifier("financeJdbcTemplate")
    private JdbcTemplate financeJdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // Ensure at least one system operator exists in authdb
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            userRepository.save(admin);
        }
        
        // Link any orphan records in financedb to the admin user
        fixOrphanRecords();
    }

    private void fixOrphanRecords() {
        try {
            List<Long> adminIds = authJdbcTemplate.queryForList("SELECT id FROM users WHERE username = 'admin'", Long.class);
            if (!adminIds.isEmpty()) {
                Long adminId = adminIds.get(0);
                // Defensive: drop any CHECK constraints on recurring_obligations that might block new enum values
                try {
                    List<String> checks = financeJdbcTemplate.queryForList(
                        "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.CONSTRAINTS WHERE TABLE_NAME = 'RECURRING_OBLIGATIONS' AND CONSTRAINT_TYPE = 'CHECK'",
                        String.class);
                    for (String c : checks) {
                        try {
                            financeJdbcTemplate.execute("ALTER TABLE RECURRING_OBLIGATIONS DROP CONSTRAINT " + c);
                        } catch (Exception ex) {
                            // ignore individual drop failures
                        }
                    }
                } catch (Exception ex) {
                    // ignore if INFORMATION_SCHEMA or query not available
                }

                financeJdbcTemplate.update("UPDATE financial_accounts SET user_id = ? WHERE user_id IS NULL", adminId);
                financeJdbcTemplate.update("UPDATE income_sources SET user_id = ? WHERE user_id IS NULL", adminId);
                financeJdbcTemplate.update("UPDATE recurring_obligations SET user_id = ? WHERE user_id IS NULL", adminId);
                financeJdbcTemplate.update("UPDATE transactions SET user_id = ? WHERE user_id IS NULL", adminId);
            }
        } catch (Exception e) {
            // Ignore if tables don't exist in financedb yet
        }
    }
}