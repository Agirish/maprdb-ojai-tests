ALTER SESSION SET `PLANNER.ENABLE_STREAMAGG` = FALSE;
SELECT P_MFGR AS MFGR, COUNT(*) AS MYCOUNT 
  FROM part 
	GROUP BY P_MFGR 
	HAVING P_MFGR = 'MANUFACTURER#3';
ALTER SESSION SET `PLANNER.ENABLE_STREAMAGG` = TRUE;
