create view profit_view as
select `medicine_view`.`id`          AS `id`,
       `medicine_view`.`name`        AS `name`,
       `medicine_view`.`unitname`    AS `unitname`,
       `medicine_view`.`inprice`     AS `inprice`,
       `medicine_view`.`feetypename` AS `feetypename`,
       `medicine_view`.`factoryname` AS `factoryname`,
       `medicine_view`.`stock`       AS `stock`,
       0                             AS `truestock`,
       0                             AS `number`,
       0                             AS `profit`
from `hospital2`.`medicine_view`
where (not ((`medicine_view`.`type` like '诊疗费')) and useful =1);

