ALTER SESSION SET `PLANNER.ENABLE_MERGEJOIN` = FALSE;
SELECT O.O_TOTALPRICE
    FROM customer C, orders O
    WHERE C.C_CUSTKEY = O.O_CUSTKEY;
ALTER SESSION SET `PLANNER.ENABLE_MERGEJOIN` = TRUE;
