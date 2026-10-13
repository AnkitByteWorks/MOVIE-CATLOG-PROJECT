-- ====================================================================
-- Movie Tracker — MySQL Initialization Script
-- Executed automatically on the first boot of the MySQL container
-- ====================================================================

-- Create databases for each microservice
CREATE DATABASE IF NOT EXISTS movietracker_user
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS movietracker_watchlog
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS movietracker_notification
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privileges to application user
GRANT ALL PRIVILEGES ON movietracker_user.* TO 'movietracker'@'%';
GRANT ALL PRIVILEGES ON movietracker_watchlog.* TO 'movietracker'@'%';
GRANT ALL PRIVILEGES ON movietracker_notification.* TO 'movietracker'@'%';
FLUSH PRIVILEGES;

USE movietracker_user;
-- Schema managed by Flyway/JPA later; leave empty here.
