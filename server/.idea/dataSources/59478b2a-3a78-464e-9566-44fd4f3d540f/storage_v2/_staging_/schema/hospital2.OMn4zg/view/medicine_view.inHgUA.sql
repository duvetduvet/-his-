create view medicine_view as
SELECT
       `medicine`.`id` AS `id`,
       `medicine`.`name` AS `name`,
       `unit`.`name` AS `unitname`,
       name2,
       `medicine`.`inprice` AS `inprice`,
       `medicine`.`outprice` AS `outprice`,
       `feetype`.`name` AS `feetypename`,
       `factory`.`name` AS `factoryname`,
       `medicine`.`stock` AS `stock`,
       `medicine`.`warn` AS `warn`,
       `medicine`.`spell` AS `spell`,
       `medicine`.`type` AS `type`,
       `medicine`.`useful` AS `useful`,
       `medicine`.`itemCode` AS `itemCode`,
       `medicine`.`isInclude` AS `isInclude`,
       `medicine`.`itemGrade` AS `itemGrade`
FROM
       (((`medicine`
              JOIN `unit`)
              JOIN `feetype`)
              JOIN `factory`)
WHERE
       ((`unit`.`id` = `medicine`.`unitId`)
              AND (`medicine`.`feetypeId` = `feetype`.`id`)
              AND (`medicine`.`factoryId` = `factory`.`id`))
