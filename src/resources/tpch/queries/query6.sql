SELECT
  SUM(L_EXTENDEDPRICE * L_DISCOUNT) AS REVENUE
FROM
  lineitem
WHERE
  L_SHIPDATE >= DATE '1997-01-01'
  AND L_SHIPDATE < DATE '1997-01-01' + INTERVAL '1' YEAR
  AND
  L_DISCOUNT BETWEEN 0.03 - 0.01 AND 0.03 + 0.01
  AND L_QUANTITY < 24;
