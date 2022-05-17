--
-- db2 -td"@" -f p2.sql
--
CONNECT TO CS157A@
--
--
DROP PROCEDURE P2.CUST_CRT@
DROP PROCEDURE P2.CUST_LOGIN@
DROP PROCEDURE P2.ACCT_OPN@
DROP PROCEDURE P2.ACCT_CLS@
DROP PROCEDURE P2.ACCT_DEP@
DROP PROCEDURE P2.ACCT_WTH@
DROP PROCEDURE P2.ACCT_TRX@
DROP PROCEDURE P2.ADD_INTEREST@
--
--
CREATE PROCEDURE P2.CUST_CRT
(IN p_name CHAR(15), IN p_gender CHAR(1), IN p_age INTEGER, IN p_pin INTEGER, OUT id INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    IF p_gender != 'M' AND p_gender != 'F' THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Gender';
    ELSEIF p_age <= 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Age';
    ELSEIF p_pin < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Pin';
    ELSE
      SET p_pin = p2.encrypt(p_pin);
      INSERT INTO p2.customer(name, gender, age, pin) values(p_name, p_gender, p_age, p_pin);
      SET id = (SELECT MAX(id) from p2.customer where name = p_name and gender = p_gender and age = p_age and pin = p_pin);
      SET sql_code = 0;
    END IF;
END@

CREATE PROCEDURE P2.CUST_LOGIN
(IN p_id INTEGER, IN p_pin INTEGER, OUT valid INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    DECLARE pin INTEGER;
    SET pin = p2.decrypt((SELECT pin from p2.customer where id = p_id));
    IF p_pin = pin THEN
      SET valid = 1;
      SET sql_code = 0;
    ELSE
      SET valid = 0;
      SET sql_code = -100;
      SET err_msg = 'Incorrect ID or Pin';
    END IF;

END@

CREATE PROCEDURE P2.ACCT_OPN
(IN p_id INTEGER, IN p_balance INTEGER, IN p_type CHAR(1), OUT number INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN 
    IF p_balance < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Balance';
    ELSEIF (SELECT count(*) From p2.customer where id = p_id) = 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Customer ID';
    ELSEIF p_type != 'C' AND p_type != 'S' THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Account Type';
    ELSE
      INSERT INTO p2.account(id, balance, type, status) values(p_id, p_balance, p_type, 'A');
      SET number = (SELECT MAX(number) from p2.account where id = p_id and balance = p_balance and type = p_type and status = 'A');
      SET sql_code = 0;
    END IF;
      

END@

CREATE PROCEDURE P2.ACCT_CLS
(IN p_number INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
  IF (SELECT count(*) From p2.account where number = p_number) = 0 THEN
    SET sql_code = -100;
    SET err_msg = 'Invalid Account Number';
  ELSEIF (SELECT status From p2.account where number = p_number) != 'A' THEN
    SET sql_code = -100;
    SET err_msg = 'Invalid Account Status';
  ELSE
    UPDATE p2.account set status = 'I', balance = 0 where number = p_number;
    SET sql_code = 0;
  END IF;

END@

CREATE PROCEDURE P2.ACCT_DEP
(IN p_number INTEGER, IN p_amount INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    IF p_amount < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Amount';
    ELSEIF (SELECT count(*) From p2.account where number = p_number) = 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Account Number';
    ELSEIF (SELECT status from p2.account where number = p_number) != 'A' THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Account Status';
    ELSE
      UPDATE p2.account set balance = balance + p_amount where number = p_number;
      SET sql_code = 0;
    END IF;

END@


CREATE PROCEDURE P2.ACCT_WTH
(IN p_number INTEGER, IN p_amount INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    IF p_amount < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Amount';
    ELSEIF (SELECT status from p2.account where number = p_number) != 'A' THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Account Status';
    ELSEIF (SELECT balance from p2.account where number = p_number) < p_amount THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Amount: Not Enough Balance'; 
    ELSE
      UPDATE p2.account set balance = balance - p_amount where number = p_number;
      SET sql_code = 0;
    END IF;

END@

CREATE PROCEDURE P2.ACCT_TRX
(IN p_source INTEGER, IN p_destination INTEGER, IN p_amount INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
  DECLARE code INTEGER;
  DECLARE err CHAR(100);
    IF p_amount < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Amount';
    ELSEIF (SELECT status from p2.account where number = p_source) != 'A' THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Source Account';
    ELSEIF (SELECT status from p2.account where number = p_destination) != 'A' THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Destination Account';
    ELSE
      CALL P2.ACCT_WTH(p_source,p_amount,code,err);
      IF code = 0 THEN
      CALL P2.ACCT_DEP(p_destination,p_amount,code,err);
      END IF;
      SET sql_code = code;
      SET err_msg = err;
    END IF;

END@

CREATE PROCEDURE P2.ADD_INTEREST
(IN p_savingsrate FLOAT, IN p_checkingrate FLOAT, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
     IF p_savingsrate < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Saving Rate';
    ELSEIF p_checkingrate < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Checking Rate';
    ELSE
     UPDATE p2.account set balance = balance + (balance*p_savingsrate) where type = 'S' and status = 'A';
     UPDATE p2.account set balance = balance + (balance*p_checkingrate) where type = 'C' and status = 'A';
     SET sql_code = 0;
    END IF;

END@
--
TERMINATE@
--
--
