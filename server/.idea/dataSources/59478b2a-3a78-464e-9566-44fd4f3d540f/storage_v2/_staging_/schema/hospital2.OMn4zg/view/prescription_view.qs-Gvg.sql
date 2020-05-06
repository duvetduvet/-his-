create view prescription_view as
select `p`.`listid`      AS `prescriptionid`,
       `p`.`medicineid`  AS `medid`,
       `p`.`medname`     AS `name`,
       medname2 as name2,
       `p`.`unitName`    AS `unitname`,
       `p`.`outprice`    AS `outprice`,
       `p`.`inprice`     AS `inprice`,
       `p`.`number`      AS `number`,
       `p`.`feetypeName` AS `feetypename`,
       `p`.`type`        AS `type`,
       `p`.`itemCode`    AS `itemCode`,
       `p`.`itemGrade`   AS `itemGrade`
from `hospital2`.`prescription` `p`;

