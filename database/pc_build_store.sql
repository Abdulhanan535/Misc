SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `bills`;
DROP TABLE IF EXISTS `gpu_options`;
DROP TABLE IF EXISTS `build_parts`;
DROP TABLE IF EXISTS `builds`;
DROP TABLE IF EXISTS `parts`;
DROP TABLE IF EXISTS `categories`;

CREATE TABLE `categories` (
  `category_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `parts` (
  `part_id` int(11) NOT NULL AUTO_INCREMENT,
  `category_id` int(11) NOT NULL,
  `brand` varchar(50) NOT NULL,
  `name` varchar(150) NOT NULL,
  `price` int(11) NOT NULL,
  `performance_score` int(11) NOT NULL,
  `socket_type` varchar(20) DEFAULT NULL,
  `ddr_generation` varchar(10) DEFAULT NULL,
  `core_count` int(11) DEFAULT NULL,
  `clock_speed` varchar(30) DEFAULT NULL,
  `vram` varchar(20) DEFAULT NULL,
  `memory_speed` varchar(30) DEFAULT NULL,
  `capacity` varchar(30) DEFAULT NULL,
  `read_speed` varchar(30) DEFAULT NULL,
  `wattage` int(11) DEFAULT NULL,
  `efficiency` varchar(20) DEFAULT NULL,
  `image_path` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`part_id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `parts_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `builds` (
  `build_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `total_price` int(11) NOT NULL DEFAULT 0,
  `total_score` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`build_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `build_parts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `build_id` int(11) NOT NULL,
  `category_id` int(11) NOT NULL,
  `part_id` int(11) NOT NULL,
  `price_at_add` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `build_category` (`build_id`, `category_id`),
  KEY `category_id` (`category_id`),
  KEY `part_id` (`part_id`),
  CONSTRAINT `build_parts_ibfk_1` FOREIGN KEY (`build_id`) REFERENCES `builds` (`build_id`) ON DELETE CASCADE,
  CONSTRAINT `build_parts_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`),
  CONSTRAINT `build_parts_ibfk_3` FOREIGN KEY (`part_id`) REFERENCES `parts` (`part_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `gpu_options` (
  `gpu_option_id` int(11) NOT NULL AUTO_INCREMENT,
  `gpu_part_id` int(11) NOT NULL,
  `for_budget` int(11) NOT NULL,
  `price_increase` int(11) NOT NULL,
  `performance_increase` int(11) NOT NULL,
  PRIMARY KEY (`gpu_option_id`),
  KEY `gpu_part_id` (`gpu_part_id`),
  CONSTRAINT `gpu_options_ibfk_1` FOREIGN KEY (`gpu_part_id`) REFERENCES `parts` (`part_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `bills` (
  `bill_id` int(11) NOT NULL AUTO_INCREMENT,
  `build_id` int(11) NOT NULL,
  `final_price` int(11) NOT NULL,
  `final_score` int(11) NOT NULL,
  `purchase_date` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`bill_id`),
  KEY `build_id` (`build_id`),
  CONSTRAINT `bills_ibfk_1` FOREIGN KEY (`build_id`) REFERENCES `builds` (`build_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================================
-- SEED DATA
-- ============================================================

INSERT INTO `categories` (`name`) VALUES ('CPU'), ('GPU'), ('RAM'), ('Storage'), ('PSU');

-- CPU (category_id = 1): socket_type, ddr_generation, core_count, clock_speed
INSERT INTO `parts` (`category_id`, `brand`, `name`, `price`, `performance_score`, `socket_type`, `ddr_generation`, `core_count`, `clock_speed`, `image_path`) VALUES
(1, 'Intel',    'Core i3-12100F',          22000,  55, 'LGA1700', 'DDR4', 4,  '3.3 GHz', 'https://zahcomputers.pk/wp-content/uploads/2023/01/Intel-Core-i3-12100F-12th-Gen-3.3-GHz-Quad-Core-LGA-1700-Processor-Tray-Price-in-Pakistan.jpg'),
(1, 'Intel',    'Core i5-13400F',          45000,  72, 'LGA1700', 'DDR5', 10, '2.5 GHz', 'https://static.webx.pk/files/87161/Images/3-czone.com.pk-1540-16376-090724101007-1540-16413-1207240728-87161-2462552-021025053011.webp'),
(1, 'Intel',    'Core i5-13600K',          62000,  80, 'LGA1700', 'DDR5', 14, '3.5 GHz', 'https://zahcomputers.pk/wp-content/uploads/2022/10/Intel-Core-i5-13600K-3.5-GHz-14-Core-LGA-1700-Processor-Price-in-Pakistan.jpg'),
(1, 'Intel',    'Core i7-13700K',          95000,  88, 'LGA1700', 'DDR5', 16, '3.4 GHz', 'https://zahcomputers.pk/wp-content/uploads/2022/10/Intel-Core-i7-13700K-3.4-GHz-16-Core-LGA-1700-Processor-Price-in-Pakistan.jpg'),
(1, 'Intel',    'Core i9-13900K',         135000,  95, 'LGA1700', 'DDR5', 24, '3.0 GHz', 'https://zahcomputers.pk/wp-content/uploads/2025/06/Intel-Core-i9-13900K-3-GHz-24-Core-LGA-1700-Processor-Tray-Price-in-Pakistan.jpg'),
(1, 'AMD',      'Ryzen 5 5600X',           28000,  65, 'AM4',     'DDR4', 6,  '3.7 GHz', 'https://static.webx.pk/files/87161/Images/7-czone.com.pk-1540-13906-201022080817-87161-2462570-021025053316.webp'),
(1, 'AMD',      'Ryzen 5 7600',            38000,  73, 'AM5',     'DDR5', 6,  '3.8 GHz', 'https://zahcomputers.pk/wp-content/uploads/2025/02/AMD-Ryzen-5-7600-Desktop-Processor-Tray-Pack-Price-in-Pakistan.jpg'),
(1, 'AMD',      'Ryzen 7 7700X',           65000,  85, 'AM5',     'DDR5', 8,  '4.5 GHz', 'https://zahcomputers.pk/wp-content/uploads/2023/09/AMD-Ryzen-7-7700X-4.5GHz-Socket-AM5-Tray-01.jpg');

-- GPU (category_id = 2): vram
INSERT INTO `parts` (`category_id`, `brand`, `name`, `price`, `performance_score`, `vram`, `image_path`) VALUES
(2, 'Nvidia',   'RTX 3050',                55000,  60, '8 GB',  'https://static.webx.pk/files/87161/Images/czone.com.pk-27-1540-19066-050925074531-87161-2461124-021025104106.webp'),
(2, 'Nvidia',   'RTX 3060',                75000,  70, '12 GB', 'https://zahcomputers.pk/wp-content/uploads/2022/06/EVGA-GeForce-RTX-3060-XC-BLACK-GAMING-12G-P5-3655-KR-12GB-GDDR6-Dual-Fan-Graphic-Card-Price-in-Pakistan.jpg'),
(2, 'Nvidia',   'RTX 4060',                95000,  78, '8 GB',  'https://zahcomputers.pk/wp-content/uploads/2024/05/ASUS-Dual-GeForce-RTX%E2%84%A2-4060-8GB-Lowest-Price-in-Pakistan.jpg'),
(2, 'Nvidia',   'RTX 4060 Ti',            120000,  84, '16 GB', 'https://zahcomputers.pk/wp-content/uploads/2024/12/ASUS-Dual-GeForce-RTX-4060-Ti-EVO-OC-16GB-Edition-Gaming-Graphics-Card.jpg'),
(2, 'Nvidia',   'RTX 4070',               165000,  90, '12 GB', 'https://zahcomputers.pk/wp-content/uploads/2024/08/ASUS-Dual-GeForce-RTX-4070-EVO-OC-Edition-12GB-GDDR6X-Graphics-Card-Lowest-Price-in-Pakistan.jpg'),
(2, 'Nvidia',   'RTX 4080',               280000,  95, '16 GB', 'https://zahcomputers.pk/wp-content/uploads/2022/11/ZOTAC-GAMING-GeForce-RTX-4080-16GB-Trinity-OC-NVIDIA-Ada-Lovelace-Streaming-Multiprocessors-Graphics-Card-Price-in-Pakistan.jpg'),
(2, 'Nvidia',   'RTX 4090',               450000,  98, '24 GB', 'https://zahcomputers.pk/wp-content/uploads/2023/06/ASUS-TUF-Gaming-GeForce-RTX-4090-OC-Edition-24GB-GDDR6X-Gaming-Graphics-Card-Price-in-Pakistan.jpg'),
(2, 'AMD',      'RX 6600',                 55000,  62, '8 GB',  'https://zahcomputers.pk/wp-content/uploads/2022/06/SAPPHIRE-PULSE-Radeon-RX-6600-8GB-GDDR6-PCI-Express-4.0-ATX-Video-Card-Price-in-Pakistan-.jpg'),
(2, 'AMD',      'RX 7600',                 70000,  72, '8 GB',  'https://zahcomputers.pk/wp-content/uploads/2025/04/SAPPHIRE-PULSE-Radeon-RX-7600-8GB-GDDR6-PCI-Express-4.0-x8-ATX-Graphics-Card-Refurbished-Price-in-Pakistan.jpg');

-- RAM (category_id = 3): ddr_generation, memory_speed, capacity
INSERT INTO `parts` (`category_id`, `brand`, `name`, `price`, `performance_score`, `ddr_generation`, `memory_speed`, `capacity`, `image_path`) VALUES
(3, 'Corsair',  'Vengeance LPX 16GB DDR4',      6000,  45, 'DDR4', '3200 MHz', '16 GB', 'https://zahcomputers.pk/wp-content/uploads/2021/12/CORSAIR-Vengeance-LPX-16GB-2-x-8GB-288-Pin-DDR4-SDRAM-DDR4-3200-PC4-25600-Intel-XMP-2.0-Desktop-Memory-Price-in-Pakistan-ZahComputers.jpg'),
(3, 'Corsair',  'Vengeance LPX 32GB DDR4',     11000,  55, 'DDR4', '3200 MHz', '32 GB', 'https://zahcomputers.pk/wp-content/uploads/2023/05/CORSAIR-Vengeance-LPX-DDR4-Desktop-Memory-Kit-Price-in-Pakistan.jpg'),
(3, 'G.Skill',  'Trident Z5 16GB DDR5',        12000,  60, 'DDR5', '5600 MHz', '16 GB', NULL),
(3, 'G.Skill',  'Trident Z5 32GB DDR5',        20000,  72, 'DDR5', '6000 MHz', '32 GB', 'https://zahcomputers.pk/wp-content/uploads/2025/06/G.SKILL-Trident-Z5-RGB-Series-32GB-2-x-16GB-288-Pin-PC-RAM-DDR5-6000-PC5-48000-Desktop-Memory-Price-in-Pakistan.jpg'),
(3, 'Kingston', 'Fury Beast 16GB DDR5',        10000,  58, 'DDR5', '5200 MHz', '16 GB', 'https://zahcomputers.pk/wp-content/uploads/2025/11/Kingston-FURY-Beast-16GB-288-Pin-PC-RAM-DDR5-5600-PC5-44800-Desktop-Memory-Price-in-Pakistan.jpg'),
(3, 'Kingston', 'Fury Beast 32GB DDR5',        18000,  68, 'DDR5', '5600 MHz', '32 GB', NULL),
(3, 'Corsair',  'Dominator Platinum 64GB DDR5', 35000, 85, 'DDR5', '6200 MHz', '64 GB', 'https://zahcomputers.pk/wp-content/uploads/2025/05/CORSAIR-VENGEANCE-RGB-DDR5-64GB-2x32GB-DDR5-6000MHz-CL30-AMD-EXPO-Intel-XMP-iCUE-Compatible-Computer-Memory-Price-in-Pakistan.jpg');

-- Storage (category_id = 4): capacity, read_speed
INSERT INTO `parts` (`category_id`, `brand`, `name`, `price`, `performance_score`, `capacity`, `read_speed`, `image_path`) VALUES
(4, 'Samsung',  '970 EVO Plus 256GB',    10000,  50, '256 GB', '3,500 MB/s', 'https://zahcomputers.pk/wp-content/uploads/2022/01/Samsung-250GB-970-EVO-Plus-NVMe-M.2-Internal-SSD-Price-in-Pakistan-ZahComputers.jpg'),
(4, 'Samsung',  '970 EVO Plus 512GB',    16000,  60, '512 GB', '3,500 MB/s', 'https://zahcomputers.pk/wp-content/uploads/2022/01/Samsung-500GB-970-EVO-Plus-NVMe-M.2-Internal-SSD-Price-in-Pakistan-ZahComputers.jpg'),
(4, 'Samsung',  '980 Pro 1TB',           28000,  78, '1 TB',   '7,000 MB/s', 'https://zahcomputers.pk/wp-content/uploads/2022/01/Samsung-1TB-980-PRO-PCIe-4.0-x4-M.2-Internal-SSD-Price-in-Pakistan-ZahComputers.jpg'),
(4, 'WD',       'Black SN770 1TB',       22000,  72, '1 TB',   '5,150 MB/s', NULL),
(4, 'WD',       'Black SN850X 2TB',      42000,  88, '2 TB',   '7,300 MB/s', NULL),
(4, 'Kingston', 'NV2 1TB',               15000,  62, '1 TB',   '3,500 MB/s', NULL),
(4, 'Crucial',  'P3 Plus 2TB',           35000,  75, '2 TB',   '5,000 MB/s', NULL);

-- PSU (category_id = 5): wattage, efficiency
INSERT INTO `parts` (`category_id`, `brand`, `name`, `price`, `performance_score`, `wattage`, `efficiency`, `image_path`) VALUES
(5, 'Corsair',  'CV450',                  13000,  55, 450, '80+ Bronze',   NULL),
(5, 'Corsair',  'CX550',                  18000,  65, 550, '80+ Bronze',   'https://zahcomputers.pk/wp-content/uploads/2024/02/Corsair-CX-Series-CX550-550-Watt-80-PLUS-Bronze-ATX-Power-Supply.jpg'),
(5, 'Corsair',  'RM750',                  28000,  78, 750, '80+ Gold',     NULL),
(5, 'Corsair',  'RM850x',                 35000,  85, 850, '80+ Gold',     'https://zahcomputers.pk/wp-content/uploads/2022/03/Corsair-RMx-Series%E2%84%A2-RM850x-Price-in-Pakistan-ZahComputers.jpg'),
(5, 'EVGA',     'SuperNOVA 650 G7',       22000,  70, 650, '80+ Gold',     NULL),
(5, 'Seasonic', 'Focus GX-1000',          55000,  92, 1000,'80+ Gold',     NULL),
(5, 'be quiet!', 'Dark Power Pro 1200W',  75000,  98, 1200,'80+ Platinum', NULL);

-- Sample builds
INSERT INTO `builds` (`name`, `total_price`, `total_score`) VALUES
('Intel Mid-Range Gaming', 266000, 335),
('AMD Workstation', 408000, 421);

-- Build 1: Intel Mid-Range Gaming
INSERT INTO `build_parts` (`build_id`, `category_id`, `part_id`, `price_at_add`) VALUES
(1, 1, 2, 45000),   -- Intel Core i5-13400F
(1, 2, 11, 95000),  -- RTX 4060
(1, 3, 4, 20000),   -- G.Skill Trident Z5 32GB DDR5
(1, 4, 3, 28000),   -- Samsung 980 Pro 1TB
(1, 5, 4, 35000);   -- Corsair RM850x

-- Build 2: AMD Workstation
INSERT INTO `build_parts` (`build_id`, `category_id`, `part_id`, `price_at_add`) VALUES
(2, 1, 8, 65000),   -- Ryzen 7 7700X
(2, 2, 13, 165000), -- RTX 4070
(2, 3, 7, 35000),   -- Corsair Dominator 64GB DDR5
(2, 4, 5, 42000),   -- WD Black SN850X 2TB
(2, 5, 7, 75000);   -- be quiet! Dark Power 1200W

-- GPU upgrade options
INSERT INTO `gpu_options` (`gpu_part_id`, `for_budget`, `price_increase`, `performance_increase`) VALUES
(12, 266000, 25000, 6),   -- RTX 4060 Ti for mid-range builds
(13, 266000, 70000, 12),  -- RTX 4070 for mid-range builds
(14, 408000, 115000, 5),  -- RTX 4080 for workstation builds
(15, 408000, 285000, 8);  -- RTX 4090 for workstation builds

-- Sample bill
INSERT INTO `bills` (`build_id`, `final_price`, `final_score`) VALUES
(1, 266000, 335);

SET FOREIGN_KEY_CHECKS = 1;
