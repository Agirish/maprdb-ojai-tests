select t.v.key as vote , count(t.v.key) as ct from (select flatten(kvgen(votes)) as v from review) as t where t.v.`value` > 5.0  group by t.v.key having t.v.key=max(t.v.key) order by vote, ct limit 10;