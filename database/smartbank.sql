-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 23, 2026 at 04:37 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `smartbank`
--

-- --------------------------------------------------------

--
-- Table structure for table `accounts`
--

CREATE TABLE `accounts` (
  `account_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `account_number` varchar(20) NOT NULL,
  `balance` decimal(12,2) NOT NULL DEFAULT 0.00,
  `account_type` varchar(30) DEFAULT 'Savings',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `accounts`
--

INSERT INTO `accounts` (`account_id`, `customer_id`, `account_number`, `balance`, `account_type`, `created_at`) VALUES
(1, 1, 'SB100001', 5011200.00, 'Savings', '2026-10-23 11:29:13');

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `customer_id` int(11) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`customer_id`, `full_name`, `email`, `phone`, `created_at`) VALUES
(1, 'Demo Customer', 'demo@smartbank.com', '9999999999', '2026-10-23 11:29:13');

-- --------------------------------------------------------

--
-- Table structure for table `transactions`
--

CREATE TABLE `transactions` (
  `transaction_id` int(11) NOT NULL,
  `account_id` int(11) NOT NULL,
  `recipient` varchar(100) DEFAULT NULL,
  `transaction_type` varchar(30) NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'Completed',
  `fraud_reason` varchar(255) DEFAULT NULL,
  `transaction_date` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `transactions`
--

INSERT INTO `transactions` (`transaction_id`, `account_id`, `recipient`, `transaction_type`, `amount`, `status`, `fraud_reason`, `transaction_date`) VALUES
(1, 1, 'Test Deposit', 'Deposit', 100.00, 'Completed', '', '2026-10-23 11:33:20'),
(2, 1, 'Java API Test', 'Deposit', 200.00, 'Completed', '', '2026-10-23 11:40:07'),
(3, 1, 'shital', 'Transfer', 4000.00, 'Completed', '', '2026-10-23 12:13:39'),
(4, 1, 'Cash deposit', 'Deposit', 1000.00, 'Completed', '', '2026-10-23 12:13:49'),
(5, 1, 'Cash withdrawal', 'Withdrawal', 2000.00, 'Completed', '', '2026-10-23 12:13:57'),
(6, 1, 'Cash withdrawal', 'Withdrawal', 1000.00, 'Completed', '', '2026-10-23 12:14:55'),
(7, 1, 'Test Deposit', 'Deposit', 100.00, 'Completed', '', '2026-10-23 12:31:16'),
(8, 1, 'shital', 'Transfer', 10000.00, 'Flagged for review', 'Large transfer', '2026-10-23 13:07:13'),
(9, 1, 'shital', 'Transfer', 5000.00, 'Flagged for review', 'Repeated transfer to the same recipient', '2026-10-23 13:07:22'),
(10, 1, 'Cash deposit', 'Deposit', 500.00, 'Completed', '', '2026-10-23 13:07:43'),
(11, 1, 'Cash withdrawal', 'Withdrawal', 100.00, 'Completed', '', '2026-10-23 13:07:49'),
(12, 1, 'Cash deposit', 'Deposit', 6000.00, 'Completed', '', '2026-10-23 13:09:20'),
(13, 1, 'Cash deposit', 'Deposit', 100.00, 'Completed', '', '2026-10-23 13:10:35'),
(14, 1, 'Cash withdrawal', 'Withdrawal', 100.00, 'Completed', '', '2026-10-23 13:12:37'),
(15, 1, 'shital', 'Transfer', 5000.00, 'Flagged for review', 'Repeated transfer to the same recipient', '2026-10-23 13:13:38'),
(16, 1, 'Cash deposit', 'Deposit', 200.00, 'Completed', '', '2026-10-23 13:44:44'),
(17, 1, 'Cash deposit', 'Deposit', 100.00, 'Completed', '', '2026-10-23 14:03:46'),
(18, 1, 'Cash withdrawal', 'Withdrawal', 100.00, 'Completed', '', '2026-10-23 14:03:53'),
(19, 1, 'shital', 'Transfer', 500.00, 'Completed', '', '2026-10-23 14:04:17'),
(20, 1, 'Cash deposit', 'Deposit', 5000000.00, 'Completed', '', '2026-10-23 14:27:56');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `accounts`
--
ALTER TABLE `accounts`
  ADD PRIMARY KEY (`account_id`),
  ADD UNIQUE KEY `account_number` (`account_number`),
  ADD KEY `customer_id` (`customer_id`);

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`customer_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `transactions`
--
ALTER TABLE `transactions`
  ADD PRIMARY KEY (`transaction_id`),
  ADD KEY `account_id` (`account_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `accounts`
--
ALTER TABLE `accounts`
  MODIFY `account_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `transactions`
--
ALTER TABLE `transactions`
  MODIFY `transaction_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `accounts`
--
ALTER TABLE `accounts`
  ADD CONSTRAINT `accounts_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`);

--
-- Constraints for table `transactions`
--
ALTER TABLE `transactions`
  ADD CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`account_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
