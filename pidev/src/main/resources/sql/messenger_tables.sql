-- Tables pour la messagerie WellBalance
-- À exécuter dans phpMyAdmin

-- Table des conversations entre patients et docteurs
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `doctor_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `type` varchar(50) DEFAULT 'normal' COMMENT 'normal, urgence, question, rendez-vous',
  `sujet` varchar(255) DEFAULT NULL,
  `is_typing_patient` tinyint(1) NOT NULL DEFAULT 0,
  `is_typing_doctor` tinyint(1) NOT NULL DEFAULT 0,
  `is_group` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `doctor_id` (`doctor_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `conversation_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `conversation_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table des messages
CREATE TABLE IF NOT EXISTS `message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `conversation_id` int(11) NOT NULL,
  `sender_id` int(11) NOT NULL,
  `content` text NOT NULL,
  `attachment` varchar(255) DEFAULT NULL COMMENT 'nom du fichier joint',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `ai_analysis` text DEFAULT NULL,
  `parent_id` int(11) DEFAULT NULL COMMENT 'pour les réponses à des messages',
  `message_type` varchar(50) DEFAULT 'text' COMMENT 'text, audio, image, pdf',
  `audio_path` varchar(255) DEFAULT NULL,
  `reactions` text DEFAULT NULL COMMENT 'format: user1:emoji1,user2:emoji2',
  `original_content` text DEFAULT NULL COMMENT 'pour l historique des modifications',
  PRIMARY KEY (`id`),
  KEY `conversation_id` (`conversation_id`),
  KEY `sender_id` (`sender_id`),
  KEY `parent_id` (`parent_id`),
  CONSTRAINT `message_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`id`) ON DELETE CASCADE,
  CONSTRAINT `message_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `message_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `message` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
