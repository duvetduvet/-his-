create view medcure_view as
SELECT
       `medicine`.`id` AS `id`,
       `medicine`.`name` AS `name`,
       name2,
       `medicine`.`itemCode` AS `itemCode`,
       `medicine`.`itemGrade` AS `itemGrade`,
       `unit`.`name` AS `unitname`,
       IFNULL(`medicine`.`inprice`, 0) AS `inprice`,
       `medicine`.`outprice` AS `outprice`,
       IFNULL(`medicine`.`stock`, 0) AS `stock`,
       `feetype`.`name` AS `feetypename`,
       `medicine`.`spell` AS `spell`,
       `medicine`.`type` AS `type`,
       `medicine`.`useful` AS `useful`,
       `medicine`.`isInclude` AS `isInclude`,

FROM
       ( (`medicine` JOIN `unit`) JOIN `feetype`) left join factory)
WHERE
       ((`medicine`.`unitId` = `unit`.`id`)
              AND (`medicine`.`feetypeId` = `feetype`.`id`)
         and medicine.factoryId = factory.id
         )




