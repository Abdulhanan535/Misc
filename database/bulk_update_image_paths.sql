-- Bulk-update parts with real product image URLs fetched from
-- czone.com.pk and zahcomputers.pk on 2026-06-02.
--
-- Remaining parts without URLs (NULL) will fall back to the colored
-- category placeholder in the UI. Use the "Set Image..." dialog to
-- assign custom URLs for any that are wrong or missing.

USE pc_build_store;

-- CPU
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2023/01/Intel-Core-i3-12100F-12th-Gen-3.3-GHz-Quad-Core-LGA-1700-Processor-Tray-Price-in-Pakistan.jpg'   WHERE `name` = 'Core i3-12100F';
UPDATE `parts` SET `image_path` = 'https://static.webx.pk/files/87161/Images/3-czone.com.pk-1540-16376-090724101007-1540-16413-1207240728-87161-2462552-021025053011.webp'             WHERE `name` = 'Core i5-13400F';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2022/10/Intel-Core-i5-13600K-3.5-GHz-14-Core-LGA-1700-Processor-Price-in-Pakistan.jpg'                  WHERE `name` = 'Core i5-13600K';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2022/10/Intel-Core-i7-13700K-3.4-GHz-16-Core-LGA-1700-Processor-Price-in-Pakistan.jpg'                  WHERE `name` = 'Core i7-13700K';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2025/06/Intel-Core-i9-13900K-3-GHz-24-Core-LGA-1700-Processor-Tray-Price-in-Pakistan.jpg'             WHERE `name` = 'Core i9-13900K';
UPDATE `parts` SET `image_path` = 'https://static.webx.pk/files/87161/Images/7-czone.com.pk-1540-13906-201022080817-87161-2462570-021025053316.webp'                              WHERE `name` = 'Ryzen 5 5600X';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2025/02/AMD-Ryzen-5-7600-Desktop-Processor-Tray-Pack-Price-in-Pakistan.jpg'                          WHERE `name` = 'Ryzen 5 7600';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2023/09/AMD-Ryzen-7-7700X-4.5GHz-Socket-AM5-Tray-01.jpg'                                               WHERE `name` = 'Ryzen 7 7700X';

-- GPU
UPDATE `parts` SET `image_path` = 'https://static.webx.pk/files/87161/Images/czone.com.pk-27-1540-19066-050925074531-87161-2461124-021025104106.webp'                              WHERE `name` = 'RTX 3050';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2022/06/EVGA-GeForce-RTX-3060-XC-BLACK-GAMING-12G-P5-3655-KR-12GB-GDDR6-Dual-Fan-Graphic-Card-Price-in-Pakistan.jpg' WHERE `name` = 'RTX 3060';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2024/05/ASUS-Dual-GeForce-RTX%E2%84%A2-4060-8GB-Lowest-Price-in-Pakistan.jpg'                       WHERE `name` = 'RTX 4060';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2024/12/ASUS-Dual-GeForce-RTX-4060-Ti-EVO-OC-16GB-Edition-Gaming-Graphics-Card.jpg'                  WHERE `name` = 'RTX 4060 Ti';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2024/08/ASUS-Dual-GeForce-RTX-4070-EVO-OC-Edition-12GB-GDDR6X-Graphics-Card-Lowest-Price-in-Pakistan.jpg' WHERE `name` = 'RTX 4070';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2022/11/ZOTAC-GAMING-GeForce-RTX-4080-16GB-Trinity-OC-NVIDIA-Ada-Lovelace-Streaming-Multiprocessors-Graphics-Card-Price-in-Pakistan.jpg' WHERE `name` = 'RTX 4080';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2023/06/ASUS-TUF-Gaming-GeForce-RTX-4090-OC-Edition-24GB-GDDR6X-Gaming-Graphics-Card-Price-in-Pakistan.jpg' WHERE `name` = 'RTX 4090';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2022/06/SAPPHIRE-PULSE-Radeon-RX-6600-8GB-GDDR6-PCI-Express-4.0-ATX-Video-Card-Price-in-Pakistan-.jpg' WHERE `name` = 'RX 6600';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2025/04/SAPPHIRE-PULSE-Radeon-RX-7600-8GB-GDDR6-PCI-Express-4.0-x8-ATX-Graphics-Card-Refurbished-Price-in-Pakistan.jpg' WHERE `name` = 'RX 7600';

-- RAM (partial - 4 of 7)
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2021/12/CORSAIR-Vengeance-LPX-16GB-2-x-8GB-288-Pin-DDR4-SDRAM-DDR4-3200-PC4-25600-Intel-XMP-2.0-Desktop-Memory-Price-in-Pakistan-ZahComputers.jpg' WHERE `name` = 'Vengeance LPX 16GB DDR4';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2023/05/CORSAIR-Vengeance-LPX-DDR4-Desktop-Memory-Kit-Price-in-Pakistan.jpg' WHERE `name` = 'Vengeance LPX 32GB DDR4';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2025/06/G.SKILL-Trident-Z5-RGB-Series-32GB-2-x-16GB-288-Pin-PC-RAM-DDR5-6000-PC5-48000-Desktop-Memory-Price-in-Pakistan.jpg' WHERE `name` = 'Trident Z5 32GB DDR5';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2025/11/Kingston-FURY-Beast-16GB-288-Pin-PC-RAM-DDR5-5600-PC5-44800-Desktop-Memory-Price-in-Pakistan.jpg' WHERE `name` = 'Fury Beast 16GB DDR5';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2025/05/CORSAIR-VENGEANCE-RGB-DDR5-64GB-2x32GB-DDR5-6000MHz-CL30-AMD-EXPO-Intel-XMP-iCUE-Compatible-Computer-Memory-Price-in-Pakistan.jpg' WHERE `name` = 'Dominator Platinum 64GB DDR5';

-- Storage (partial - 3 of 7)
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2022/01/Samsung-250GB-970-EVO-Plus-NVMe-M.2-Internal-SSD-Price-in-Pakistan-ZahComputers.jpg' WHERE `name` = '970 EVO Plus 256GB';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2022/01/Samsung-500GB-970-EVO-Plus-NVMe-M.2-Internal-SSD-Price-in-Pakistan-ZahComputers.jpg' WHERE `name` = '970 EVO Plus 512GB';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2022/01/Samsung-1TB-980-PRO-PCIe-4.0-x4-M.2-Internal-SSD-Price-in-Pakistan-ZahComputers.jpg'   WHERE `name` = '980 Pro 1TB';

-- PSU (partial - 2 of 7)
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2024/02/Corsair-CX-Series-CX550-550-Watt-80-PLUS-Bronze-ATX-Power-Supply.jpg' WHERE `name` = 'CX550';
UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2022/03/Corsair-RMx-Series%E2%84%A2-RM850x-Price-in-Pakistan-ZahComputers.jpg'  WHERE `name` = 'RM850x';
