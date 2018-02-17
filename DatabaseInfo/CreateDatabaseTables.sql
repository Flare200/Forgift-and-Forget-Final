CREATE TABLE friendList
(
  friendListID      INT         NULL,
  friendLoginString VARCHAR(32) NULL
)
  ENGINE = InnoDB;

CREATE INDEX friendList_userInfo_friendListID_fk
  ON friendList (friendListID);

CREATE INDEX friendList_userInfo_loginString_fk
  ON friendList (friendLoginString);

CREATE TABLE gift
(
  giftID    INT                    NOT NULL
    PRIMARY KEY,
  giftName  VARCHAR(32)            NULL,
  giftURL   VARCHAR(512)           NULL,
  purchased TINYINT(1) DEFAULT '0' NOT NULL,
  note      VARCHAR(1024)          NULL,
  imageID   INT                    NULL,
  CONSTRAINT gift_giftID_uindex
  UNIQUE (giftID)
)
  ENGINE = InnoDB;

CREATE TABLE giftList
(
  listID INT NOT NULL,
  giftID INT NULL,
  CONSTRAINT giftList_giftID_uindex
  UNIQUE (giftID),
  CONSTRAINT giftList_gift_giftID_fk
  FOREIGN KEY (giftID) REFERENCES gift (giftID)
)
  ENGINE = InnoDB;

CREATE TABLE image
(
  bitmapID  INT        NOT NULL
    PRIMARY KEY,
  byteArray MEDIUMBLOB NULL,
  CONSTRAINT image_bitmapID_uindex
  UNIQUE (bitmapID)
)
  ENGINE = InnoDB;

CREATE TABLE loginInfo
(
  loginString  VARCHAR(32) NOT NULL
    PRIMARY KEY,
  passwordHash VARCHAR(32) NOT NULL,
  CONSTRAINT loginInfo_loginString_uindex
  UNIQUE (loginString)
)
  ENGINE = InnoDB;

CREATE TABLE reminder
(
  userLoginString  VARCHAR(32) NULL,
  recipLoginString VARCHAR(32) NULL,
  triggerDate      TIMESTAMP   NOT NULL,
  refGiftID        INT         NULL
)
  ENGINE = InnoDB;

CREATE INDEX reminder_userInfo_loginString_fk
  ON reminder (userLoginString);

CREATE INDEX reminder_userInfo_loginString_fk_2
  ON reminder (recipLoginString);

CREATE TABLE userInfo
(
  loginString  VARCHAR(32) NOT NULL
    PRIMARY KEY,
  userName     VARCHAR(32) NOT NULL,
  userEmail    VARCHAR(32) NOT NULL,
  friendListID INT         NOT NULL,
  CONSTRAINT userInfo_loginString_uindex
  UNIQUE (loginString),
  CONSTRAINT userInfo_friendListID_uindex
  UNIQUE (friendListID),
  CONSTRAINT userInfo_loginInfo_loginString_fk
  FOREIGN KEY (loginString) REFERENCES loginInfo (loginString)
)
  ENGINE = InnoDB;

ALTER TABLE friendList
  ADD CONSTRAINT friendList_userInfo_friendListID_fk
FOREIGN KEY (friendListID) REFERENCES userInfo (friendListID);

ALTER TABLE friendList
  ADD CONSTRAINT friendList_userInfo_loginString_fk
FOREIGN KEY (friendLoginString) REFERENCES userInfo (loginString);

ALTER TABLE reminder
  ADD CONSTRAINT reminder_userInfo_loginString_fk
FOREIGN KEY (userLoginString) REFERENCES userInfo (loginString);

ALTER TABLE reminder
  ADD CONSTRAINT reminder_userInfo_loginString_fk_2
FOREIGN KEY (recipLoginString) REFERENCES userInfo (loginString);

CREATE TABLE userListPair
(
  userLoginString  VARCHAR(32) NULL,
  recipLoginString VARCHAR(32) NULL,
  giftListID       INT         NULL,
  CONSTRAINT userListPair_userInfo_loginString_fk
  FOREIGN KEY (userLoginString) REFERENCES userInfo (loginString),
  CONSTRAINT userListPair_userInfo_loginString_fk_2
  FOREIGN KEY (recipLoginString) REFERENCES userInfo (loginString)
)
  ENGINE = InnoDB;

CREATE INDEX userListPair_userInfo_loginString_fk
  ON userListPair (userLoginString);

CREATE INDEX userListPair_userInfo_loginString_fk_2
  ON userListPair (recipLoginString);

