package com.book.manager;

import com.book.manager.entity.Announcement;
import com.book.manager.service.AnnouncementService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Announcement CRUD smoke test.
 *
 * Contract:
 * - save(insert) should assign id
 * - detail should return persisted row
 * - save(update) should update fields
 * - delete should remove row
 */
@SpringBootTest
class AnnouncementCrudSmokeTest {

    @Autowired
    private AnnouncementService announcementService;

    @Test
    void crud_smoke() {
        Announcement a = new Announcement();
        a.setTitle("test-ann");
        a.setContent("hello");
        a.setEnabled(1);
        a.setPinned(0);

        int inserted = announcementService.save(a, 1);
        Assertions.assertTrue(inserted > 0);
        Assertions.assertNotNull(a.getId());

        Announcement db = announcementService.detail(a.getId());
        Assertions.assertNotNull(db);
        Assertions.assertEquals("test-ann", db.getTitle());

        a.setTitle("test-ann-upd");
        int updated = announcementService.save(a, 1);
        Assertions.assertTrue(updated > 0);

        Announcement db2 = announcementService.detail(a.getId());
        Assertions.assertEquals("test-ann-upd", db2.getTitle());

        int deleted = announcementService.delete(a.getId());
        Assertions.assertTrue(deleted > 0);

        Announcement db3 = announcementService.detail(a.getId());
        Assertions.assertNull(db3);
    }
}
