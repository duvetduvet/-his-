create view all_register_view as
select `hospital2`.`register`.`id`        AS `id`,
       `hospital2`.`register`.`patientid` AS `patientid`,
       `hospital2`.`patient`.`name`       AS `patientname`,
       `hospital2`.`patient`.`sex`        AS `sex`,
       `hospital2`.`patient`.`birth`      AS `birth`,
       `hospital2`.`patient`.`phone`      AS `phone`,
       `hospital2`.`patient`.`identity`   AS `identity`,
       `hospital2`.`department`.`name`    AS `departname`,
       `hospital2`.`doctor`.`name`        AS `doctorname`,
       `hospital2`.`regtype`.`name`       AS `regtypename`,
       `hospital2`.`register`.`fee`       AS `fee`,
       `hospital2`.`register`.`first`     AS `first`,
       `hospital2`.`position`.`name`      AS `positionname`,
       `hospital2`.`register`.`date`      AS `date`,
       `hospital2`.`register`.`useful`    AS `useful`,
       insurance,
       securityId,
       cardId,
       areaId,
       clinicNumber
from (((((`hospital2`.`patient` join `hospital2`.`doctor`) join `hospital2`.`register`) join `hospital2`.`department`) join `hospital2`.`regtype`)
       join `hospital2`.`position`)
where ((`hospital2`.`register`.`patientid` = `hospital2`.`patient`.`id`) and
       (`hospital2`.`regtype`.`id` = `hospital2`.`register`.`regtypeid`) and
       (`hospital2`.`register`.`departmentId` = `hospital2`.`department`.`id`) and
       (`hospital2`.`register`.`doctorId` = `hospital2`.`doctor`.`id`) and
       (`hospital2`.`register`.`positionId` = `hospital2`.`position`.`id`));

