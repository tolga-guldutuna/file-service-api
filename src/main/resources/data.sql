------------------------------------------------------------
-- ROLES
------------------------------------------------------------
INSERT INTO roles (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_ADMIN');
INSERT INTO roles (id, name) VALUES (3, 'ROLE_AUDITOR');

------------------------------------------------------------
-- USERS
-- Note: password_hash values are dummy placeholders.
-- They are NOT intended for real login.
-- Real users should be created via registration or a
-- CommandLineRunner that uses the configured PasswordEncoder.
------------------------------------------------------------

INSERT INTO users (
    id,
    email,
    password_hash,
    full_name,
    is_active,
    created_at,
    updated_at
) VALUES
      (
          1,
          'admin@example.com',
          'DUMMY_HASH_ADMIN',
          'System Administrator',
          TRUE,
          CURRENT_TIMESTAMP,
          CURRENT_TIMESTAMP
      ),
      (
          2,
          'user.alice@example.com',
          'DUMMY_HASH_ALICE',
          'Alice Carter',
          TRUE,
          CURRENT_TIMESTAMP,
          CURRENT_TIMESTAMP
      ),
      (
          3,
          'user.bob@example.com',
          'DUMMY_HASH_BOB',
          'Bob Miller',
          TRUE,
          CURRENT_TIMESTAMP,
          CURRENT_TIMESTAMP
      ),
      (
          4,
          'auditor@example.com',
          'DUMMY_HASH_AUDITOR',
          'Audit Viewer',
          TRUE,
          CURRENT_TIMESTAMP,
          CURRENT_TIMESTAMP
      );

------------------------------------------------------------
-- USER_ROLES
------------------------------------------------------------
-- admin: USER + ADMIN
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2);

-- alice: USER
INSERT INTO user_roles (user_id, role_id) VALUES (2, 1);

-- bob: USER
INSERT INTO user_roles (user_id, role_id) VALUES (3, 1);

-- auditor: USER + AUDITOR (read-only style)
INSERT INTO user_roles (user_id, role_id) VALUES (4, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (4, 3);

------------------------------------------------------------
-- FILES
-- Notes:
--  * public_id uses H2 RANDOM_UUID() to generate sample UUIDs.
--  * storage_path values reflect the planned folder strategy:
--      YYYY/MM/DD/<typeFolder>/<firstLetter>/<storedFileName>
--  * sha256_hash values are dummy hex strings.
------------------------------------------------------------

INSERT INTO files (
    id,
    public_id,
    owner_id,
    original_name,
    stored_name,
    extension,
    content_type,
    size_bytes,
    storage_path,
    sha256_hash,
    is_temp,
    expires_at,
    created_at,
    updated_at,
    deleted_at
) VALUES
-- 1) Admin – PDF report (active, normal file)
(
    1,
    RANDOM_UUID(),
    1, -- admin
    'Quarterly_Report_Q4_2024.pdf',
    'q4-2024-report-admin.pdf',
    'PDF',
    'application/pdf',
    845_312, -- ~825 KB
    '2024/12/31/pdf/Q/q4-2024-report-admin.pdf',
    'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
    FALSE,
    NULL,
    TIMESTAMP '2024-12-31 10:30:00',
    TIMESTAMP '2024-12-31 10:30:00',
    NULL
),

-- 2) Alice – product image (PNG)
(
    2,
    RANDOM_UUID(),
    2, -- alice
    'product_image_homepage.png',
    'product-home-hero.png',
    'PNG',
    'image/png',
    392_158, -- ~383 KB
    '2025/01/10/image/P/product-home-hero.png',
    'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
    FALSE,
    NULL,
    TIMESTAMP '2025-01-10 09:15:00',
    TIMESTAMP '2025-01-10 09:15:00',
    NULL
),

-- 3) Alice – meeting notes in DOCX
(
    3,
    RANDOM_UUID(),
    2, -- alice
    'Meeting_Notes_Project_X.docx',
    'meeting-notes-project-x.docx',
    'DOCX',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    128_764, -- ~126 KB
    '2025/02/05/office/M/meeting-notes-project-x.docx',
    'cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc',
    FALSE,
    NULL,
    TIMESTAMP '2025-02-05 14:42:00',
    TIMESTAMP '2025-02-05 14:42:00',
    NULL
),

-- 4) Bob – financial spreadsheet in XLSX
(
    4,
    RANDOM_UUID(),
    3, -- bob
    'Financial_Model_v3.xlsx',
    'financial-model-v3.xlsx',
    'XLSX',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    256_101, -- ~250 KB
    '2025/02/20/office/F/financial-model-v3.xlsx',
    'dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd',
    FALSE,
    NULL,
    TIMESTAMP '2025-02-20 11:03:00',
    TIMESTAMP '2025-02-20 11:03:00',
    NULL
),

-- 5) Alice – temporary PDF export (expires in the future)
(
    5,
    RANDOM_UUID(),
    2, -- alice
    'Temporary_Export_2025-Report.pdf',
    'temp-export-2025-report.pdf',
    'PDF',
    'application/pdf',
    512_000, -- 500 KB
    '2025/03/01/pdf/T/temp-export-2025-report.pdf',
    'eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee',
    TRUE,
    TIMESTAMP '2025-12-31 23:59:59',
    TIMESTAMP '2025-03-01 08:00:00',
    TIMESTAMP '2025-03-01 08:00:00',
    NULL
),

-- 6) Admin – soft deleted JPEG avatar
(
    6,
    RANDOM_UUID(),
    1, -- admin
    'old_profile_avatar.jpg',
    'admin-avatar-old.jpg',
    'JPG',
    'image/jpeg',
    96_240, -- ~94 KB
    '2024/10/15/image/O/admin-avatar-old.jpg',
    'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
    FALSE,
    NULL,
    TIMESTAMP '2024-10-15 16:20:00',
    TIMESTAMP '2024-11-01 09:00:00',
    TIMESTAMP '2024-11-01 09:05:00' -- soft delete
);
