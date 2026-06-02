-- Migration: add image_path column to parts table
-- Run this if you already have an existing pc_build_store database.

USE pc_build_store;

ALTER TABLE `parts`
  ADD COLUMN `image_path` varchar(500) DEFAULT NULL AFTER `efficiency`;

-- Optional: seed two sample image URLs to demonstrate the feature.
-- Source: czone.com.pk and zahcomputers.pk product pages.
UPDATE `parts` SET `image_path` = 'https://static.webx.pk/files/87161/Images/3-czone.com.pk-1540-16376-090724101007-1540-16413-1207240728-87161-2462552-021025053011.webp'
  WHERE `name` = 'Core i5-13400F';

UPDATE `parts` SET `image_path` = 'https://zahcomputers.pk/wp-content/uploads/2023/09/AMD-Ryzen-7-7700X-4.5GHz-Socket-AM5-Tray-01.jpg'
  WHERE `name` = 'Ryzen 7 7700X';
