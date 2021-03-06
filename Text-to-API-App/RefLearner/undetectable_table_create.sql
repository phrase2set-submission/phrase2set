SELECT refactoringId, reasonCREATE TABLE `undetectable` (
  `project_name` varchar(200) NOT NULL,
  `commit_id` varchar(200) NOT NULL,
  `commit_parent_id` varchar(200) NOT NULL,
  `commit_desc` varchar(15000) NOT NULL,
  `reason` varchar(15000) NOT NULL,
  `refactoringId` int(11) NOT NULL,
  UNIQUE KEY `refactoringId_UNIQUE` (`refactoringId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
 FROM `danilofs-refactoring`.undetectable;