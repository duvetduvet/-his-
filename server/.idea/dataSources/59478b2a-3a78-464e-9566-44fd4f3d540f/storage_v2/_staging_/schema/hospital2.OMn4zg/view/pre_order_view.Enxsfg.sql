create view pre_order_view as
select `p`.`id`                                    AS `pid`,
       `r`.`id`                                    AS `rgid`,
       `r`.`patientname`                           AS `name`,
       `r`.`patientid`                             AS `patientid`,
       `r`.`sex`                                   AS `sex`,
       timestampdiff(YEAR, `r`.`birth`, curdate()) AS `age`,
       `r`.`fee`                                   AS `fee`,
       `r`.`departname`                            AS `departname`,
       `r`.`doctorname`                            AS `doctorname`,
       `r`.`date`                                  AS `regdate`,
       r.securityId                                as securityId,
       r.cardId                                    as cardId,
       r.areaId                                    as areaId,
       r.clinicNumber                              as clinicNumber,
       `p`.`dose`                                  AS `dose`,
       `p`.`medfee`                                AS `medfee`,
       `p`.`curefee`                               AS `curefee`,
       `o`.`id`                                    AS `orderid`,

       `o`.`medicinediscount`                      AS `medicinediscount`,
       `o`.`medcurediscount`                       AS `medcurediscount`,
       `o`.`card`                                  AS `card`,
       `o`.`given`                                 AS `given`,
       `o`.`cash`                                  AS `cash`,
       `o`.`other`                                 AS `other`,
       `o`.insurance                               as insurance,
       `o`.`date`                                  AS `date`,
       o.sum,
       o.insuranceNumber                           as insuranceNumber,
       `p`.`state`                                 AS `state`

from ((`prescriptionlist` `p` left join `order` `o` on ((`p`.`orderid` = `o`.`id`)))
       join `all_register_view` `r` on ((`p`.`rgid` = `r`.`id`)));

