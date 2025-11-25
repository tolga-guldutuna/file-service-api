-- ============================
--  Introduction Data (H2)
-- ============================

-- Users
INSERT INTO users (id, email, password) VALUES
                                            (1, 'admin@dosyavelisi.com',  'Admin123!'),
                                            (2, 'tolga@dosyavelisi.com',  'Tolga123!'),
                                            (3, 'deneme@dosyavelisi.com', 'Demo123!');

-- Example Files
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
    is_temp
) VALUES
      (
          1,
          '11111111-1111-1111-1111-111111111111',
          2,
          'Kimlik_Fotokopisi.pdf',
          '11111111-1111-1111-1111-111111111111.pdf',
          'PDF',
          'application/pdf',
          24576,
          '2025/11/25/pdf/K/11111111-1111-1111-1111-111111111111.pdf',
          FALSE
      ),
      (
          2,
          '22222222-2222-2222-2222-222222222222',
          2,
          'Maas_Bordrosu_Ekim_2025.xlsx',
          '22222222-2222-2222-2222-222222222222.xlsx',
          'XLSX',
          'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          32768,
          '2025/11/25/xlsx/M/22222222-2222-2222-2222-222222222222.xlsx',
          FALSE
      ),
      (
          3,
          '33333333-3333-3333-3333-333333333333',
          3,
          'Rezervasyon_Onayi.png',
          '33333333-3333-3333-3333-333333333333.png',
          'PNG',
          'image/png',
          16384,
          '2025/11/25/png/R/33333333-3333-3333-3333-333333333333.png',
          TRUE
      );
