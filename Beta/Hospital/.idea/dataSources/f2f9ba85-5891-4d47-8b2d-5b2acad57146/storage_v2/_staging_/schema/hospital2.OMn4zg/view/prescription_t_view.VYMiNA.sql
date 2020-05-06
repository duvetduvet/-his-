create view prescription_t_view as
select `p`.`listid`                 AS `prescriptionid`,
       `p`.`medicineid`             AS `medid`,
       `p`.`medname`                AS `name`,
       `medcure_view`.`unitname`    AS `unitname`,
       `medcure_view`.`outprice`    AS `outprice`,
       `medcure_view`.`inprice`     AS `inprice`,
       `p`.`number`                 AS `number`,
       `medcure_view`.`feetypename` AS `feetypename`,
       `medcure_view`.`type`        AS `type`,
       medcure_view.itemCode as itemCode,
       medcure_view.itemGrade as itemGrade
from (`hospital2`.`prescription` `p`
       join `hospital2`.`medcure_view`)
where (`p`.`medicineid` = `medcure_view`.`id`);

