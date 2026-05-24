# PC Build Store

A PC part picker and configurator built with Java 17, Swing, and MariaDB.

## Features

- **Build Configurator** - Pick individual components (CPU, GPU, RAM, Storage, PSU) with compatibility filtering
- **GPU Upgrades** - Browse and apply GPU upgrades to saved builds
- **Billing** - Purchase builds and view receipt breakdowns
- **Dashboard** - Overview stats (total builds, revenue, avg score, bills)

## Setup

1. Start XAMPP MySQL
2. Import database: `mysql -u root < database/pc_build_store.sql`
3. Build: `ant compile`
4. Run: `ant run`

## Tech Stack

- Java 17 + Swing
- MariaDB 10.4 (XAMPP)
- JDBC (mysql-connector-j-9.7.0)
- NetBeans Ant build

## Database

6 tables: `categories`, `parts`, `builds`, `build_parts`, `gpu_options`, `bills`

Seed data: 38 parts across 5 categories, 2 sample builds, 1 sample bill.
